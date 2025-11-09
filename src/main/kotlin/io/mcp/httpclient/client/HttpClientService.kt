package ferprieto.mcp.httpclient.client

import io.github.oshai.kotlinlogging.KotlinLogging
import ferprieto.mcp.httpclient.models.HttpRequest
import ferprieto.mcp.httpclient.models.HttpResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

private val logger = KotlinLogging.logger {}

/**
 * Service for executing HTTP requests using OkHttp
 */
class HttpClientService {
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()
    
    /**
     * Makes an HTTP request and returns the response
     */
    suspend fun makeRequest(httpRequest: HttpRequest): HttpResponse = withContext(Dispatchers.IO) {
        try {
            logger.info { "Making HTTP request: ${httpRequest.method} ${httpRequest.url}" }
            logger.trace { "Request details - Headers: ${httpRequest.headers}, Params: ${httpRequest.params}, Body: ${httpRequest.body?.take(100)}" }
            
            // Build the URL with query parameters
            val urlWithParams = buildUrlWithParams(httpRequest.url, httpRequest.params)
            logger.trace { "Built URL with params: $urlWithParams" }
            
            // Build OkHttp request
            val requestBuilder = Request.Builder().url(urlWithParams)
            
            // Add headers
            httpRequest.headers?.forEach { (key, value) ->
                logger.trace { "Adding header: $key = $value" }
                requestBuilder.addHeader(key, value)
            }
            
            // Set method and body
            logger.trace { "Setting HTTP method: ${httpRequest.method.uppercase()}" }
            when (httpRequest.method.uppercase()) {
                "GET" -> requestBuilder.get()
                "POST" -> {
                    val body = httpRequest.body?.toRequestBody(getContentType(httpRequest))
                        ?: "".toRequestBody()
                    requestBuilder.post(body)
                }
                "PUT" -> {
                    val body = httpRequest.body?.toRequestBody(getContentType(httpRequest))
                        ?: "".toRequestBody()
                    requestBuilder.put(body)
                }
                "DELETE" -> {
                    if (httpRequest.body != null) {
                        val body = httpRequest.body.toRequestBody(getContentType(httpRequest))
                        requestBuilder.delete(body)
                    } else {
                        requestBuilder.delete()
                    }
                }
                "PATCH" -> {
                    val body = httpRequest.body?.toRequestBody(getContentType(httpRequest))
                        ?: "".toRequestBody()
                    requestBuilder.patch(body)
                }
                "HEAD" -> requestBuilder.head()
                "OPTIONS" -> requestBuilder.method("OPTIONS", null)
                else -> throw IllegalArgumentException("Unsupported HTTP method: ${httpRequest.method}")
            }
            
            val request = requestBuilder.build()
            logger.trace { "Executing HTTP request" }
            
            // Execute request
            client.newCall(request).execute().use { response ->
                logger.trace { "Response code: ${response.code}" }
                val responseBody = response.body?.string() ?: ""
                logger.trace { "Response body length: ${responseBody.length} chars" }
                val responseHeaders = response.headers.toMultimap()
                logger.trace { "Response headers: ${responseHeaders.keys}" }
                
                logger.info { "HTTP response received: ${response.code}" }
                
                HttpResponse(
                    status = response.code,
                    headers = responseHeaders,
                    body = responseBody
                )
            }
        } catch (e: IOException) {
            logger.error(e) { "Network error during HTTP request" }
            HttpResponse(
                status = 0,
                headers = emptyMap(),
                body = """{"error": "Network error", "message": "${e.message}"}"""
            )
        } catch (e: IllegalArgumentException) {
            logger.error(e) { "Invalid request parameters" }
            HttpResponse(
                status = 0,
                headers = emptyMap(),
                body = """{"error": "Invalid request", "message": "${e.message}"}"""
            )
        } catch (e: Exception) {
            logger.error(e) { "Unexpected error during HTTP request" }
            HttpResponse(
                status = 0,
                headers = emptyMap(),
                body = """{"error": "Unexpected error", "message": "${e.message}"}"""
            )
        }
    }
    
    /**
     * Makes a GraphQL request
     */
    suspend fun makeGraphQLRequest(graphqlRequest: io.mcp.httpclient.models.GraphQLRequest): HttpResponse = withContext(Dispatchers.IO) {
        logger.info { "Making GraphQL request to ${graphqlRequest.url}" }
        logger.trace { "GraphQL query: ${graphqlRequest.query.take(200)}" }
        logger.trace { "GraphQL variables: ${graphqlRequest.variables}" }
        
        // Build the GraphQL request body
        val bodyJson = buildString {
            append("{\"query\":\"")
            append(graphqlRequest.query.replace("\"", "\\\"").replace("\n", "\\n"))
            append("\"")
            
            if (!graphqlRequest.variables.isNullOrEmpty()) {
                append(",\"variables\":{")
                append(graphqlRequest.variables.entries.joinToString(",") { (key, value) ->
                    "\"$key\":\"$value\""
                })
                append("}")
            }
            
            if (graphqlRequest.operationName != null) {
                append(",\"operationName\":\"${graphqlRequest.operationName}\"")
            }
            
            append("}")
        }
        
        logger.trace { "GraphQL request body: $bodyJson" }
        
        // Create HTTP request with GraphQL body
        val headers = mutableMapOf("Content-Type" to "application/json")
        graphqlRequest.headers?.let { headers.putAll(it) }
        
        val httpRequest = HttpRequest(
            url = graphqlRequest.url,
            method = "POST",
            headers = headers,
            body = bodyJson
        )
        
        makeRequest(httpRequest)
    }
    
    /**
     * Builds a URL with query parameters
     */
    private fun buildUrlWithParams(url: String, params: Map<String, String>?): String {
        if (params.isNullOrEmpty()) return url
        
        val separator = if (url.contains("?")) "&" else "?"
        val queryString = params.entries.joinToString("&") { (key, value) ->
            "${encodeURIComponent(key)}=${encodeURIComponent(value)}"
        }
        
        return "$url$separator$queryString"
    }
    
    /**
     * Gets the content type from headers or defaults to JSON
     */
    private fun getContentType(httpRequest: HttpRequest): okhttp3.MediaType {
        val contentType = httpRequest.headers?.entries?.find { 
            it.key.equals("Content-Type", ignoreCase = true) 
        }?.value
        
        return (contentType ?: "application/json").toMediaType()
    }
    
    /**
     * Simple URL encoding
     */
    private fun encodeURIComponent(value: String): String {
        return java.net.URLEncoder.encode(value, "UTF-8")
            .replace("+", "%20")
            .replace("%21", "!")
            .replace("%27", "'")
            .replace("%28", "(")
            .replace("%29", ")")
            .replace("%7E", "~")
    }
}







