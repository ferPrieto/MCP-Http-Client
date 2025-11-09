package ferprieto.mcp.httpclient.presentation

import io.github.oshai.kotlinlogging.KotlinLogging
import ferprieto.mcp.httpclient.domain.model.*
import ferprieto.mcp.httpclient.domain.usecase.*
import ferprieto.mcp.httpclient.models.McpRequest
import ferprieto.mcp.httpclient.models.McpResponse
import ferprieto.mcp.httpclient.models.McpError
import kotlinx.serialization.json.*

private val logger = KotlinLogging.logger {}

/**
 * Presentation layer logic for MCP Server
 * Handles parsing JSON-RPC requests and executing use cases
 */

class McpServerPresentation(
    private val makeHttpRequestUseCase: MakeHttpRequestUseCase,
    private val makeGraphQLRequestUseCase: MakeGraphQLRequestUseCase,
    private val makeTcpConnectionUseCase: MakeTcpConnectionUseCase,
    private val invalidateCacheUseCase: InvalidateCacheUseCase
) {
    
    suspend fun handleMakeRequest(requestId: JsonElement, arguments: JsonObject?): McpResponse {
        logger.trace { "Handling make_request" }
        
        if (arguments == null) {
            return createErrorResponse(requestId, -32602, "Missing arguments")
        }
        
        return try {
            val request = parseHttpRequest(arguments)
            logger.trace { "Executing HTTP request use case" }
            
            when (val result = makeHttpRequestUseCase(request)) {
                is RequestResult.Success -> {
                    createSuccessResponse(requestId, formatHttpResponse(result.data))
                }
                is RequestResult.Failure -> {
                    createErrorResponse(requestId, -32603, "Request failed: ${result.error.message}")
                }
                is RequestResult.Loading -> {
                    createErrorResponse(requestId, -32603, "Unexpected loading state")
                }
            }
        } catch (e: Exception) {
            logger.error(e) { "Error executing make_request" }
            createErrorResponse(requestId, -32603, "Error: ${e.message}")
        }
    }
    
    suspend fun handleGraphQLQuery(requestId: JsonElement, arguments: JsonObject?): McpResponse {
        logger.trace { "Handling graphql_query" }
        
        if (arguments == null) {
            return createErrorResponse(requestId, -32602, "Missing arguments")
        }
        
        return try {
            val request = parseGraphQLRequest(arguments)
            logger.trace { "Executing GraphQL request use case" }
            
            when (val result = makeGraphQLRequestUseCase(request)) {
                is RequestResult.Success -> {
                    createSuccessResponse(requestId, formatHttpResponse(result.data))
                }
                is RequestResult.Failure -> {
                    createErrorResponse(requestId, -32603, "GraphQL query failed: ${result.error.message}")
                }
                is RequestResult.Loading -> {
                    createErrorResponse(requestId, -32603, "Unexpected loading state")
                }
            }
        } catch (e: Exception) {
            logger.error(e) { "Error executing graphql_query" }
            createErrorResponse(requestId, -32603, "Error: ${e.message}")
        }
    }
    
    suspend fun handleTcpConnect(requestId: JsonElement, arguments: JsonObject?): McpResponse {
        logger.trace { "Handling tcp_connect" }
        
        if (arguments == null) {
            return createErrorResponse(requestId, -32602, "Missing arguments")
        }
        
        return try {
            val request = parseTcpRequest(arguments)
            logger.trace { "Executing TCP connection use case" }
            
            when (val result = makeTcpConnectionUseCase(request)) {
                is RequestResult.Success -> {
                    val responseText = when (val tcpResult = result.data) {
                        is TcpResponseDomain.Success -> formatTcpSuccess(request, tcpResult)
                        is TcpResponseDomain.Failure -> formatTcpFailure(request, tcpResult)
                    }
                    createSuccessResponse(requestId, responseText)
                }
                is RequestResult.Failure -> {
                    createErrorResponse(requestId, -32603, "TCP connection failed: ${result.error.message}")
                }
                is RequestResult.Loading -> {
                    createErrorResponse(requestId, -32603, "Unexpected loading state")
                }
            }
        } catch (e: Exception) {
            logger.error(e) { "Error executing tcp_connect" }
            createErrorResponse(requestId, -32603, "Error: ${e.message}")
        }
    }
    
    private fun parseHttpRequest(arguments: JsonObject): HttpRequestDomain {
        val url = arguments["url"]?.jsonPrimitive?.content
            ?: throw IllegalArgumentException("Missing 'url' parameter")
        
        val method = arguments["method"]?.jsonPrimitive?.content
            ?: throw IllegalArgumentException("Missing 'method' parameter")
        
        val headers = arguments["headers"]?.jsonObject?.mapValues { 
            it.value.jsonPrimitive.content 
        } ?: emptyMap()
        
        val params = arguments["params"]?.jsonObject?.mapValues { 
            it.value.jsonPrimitive.content 
        } ?: emptyMap()
        
        val body = arguments["body"]?.jsonPrimitive?.content
        
        return HttpRequestDomain(
            url = Url(url),
            method = HttpMethod(method),
            headers = headers,
            params = params,
            body = body
        )
    }
    
    private fun parseGraphQLRequest(arguments: JsonObject): GraphQLRequestDomain {
        val url = arguments["url"]?.jsonPrimitive?.content
            ?: throw IllegalArgumentException("Missing 'url' parameter")
        
        val query = arguments["query"]?.jsonPrimitive?.content
            ?: throw IllegalArgumentException("Missing 'query' parameter")
        
        val variables = arguments["variables"]?.jsonObject?.mapValues { 
            it.value.jsonPrimitive.content 
        } ?: emptyMap()
        
        val operationName = arguments["operationName"]?.jsonPrimitive?.content
        
        val headers = arguments["headers"]?.jsonObject?.mapValues { 
            it.value.jsonPrimitive.content 
        } ?: emptyMap()
        
        return GraphQLRequestDomain(
            url = Url(url),
            query = GraphQLQuery(query),
            variables = variables,
            operationName = operationName,
            headers = headers
        )
    }
    
    private fun parseTcpRequest(arguments: JsonObject): TcpRequestDomain {
        val host = arguments["host"]?.jsonPrimitive?.content
            ?: throw IllegalArgumentException("Missing 'host' parameter")
        
        val port = arguments["port"]?.jsonPrimitive?.intOrNull
            ?: throw IllegalArgumentException("Missing or invalid 'port' parameter")
        
        val message = arguments["message"]?.jsonPrimitive?.content
        val timeout = arguments["timeout"]?.jsonPrimitive?.intOrNull ?: 5
        
        return TcpRequestDomain(
            host = Host(host),
            port = Port(port),
            message = message,
            timeout = Timeout(timeout)
        )
    }
    
    private fun formatHttpResponse(response: HttpResponseDomain): String = buildString {
        appendLine("HTTP Response:")
        appendLine("Status: ${response.status.value}")
        appendLine()
        appendLine("Headers:")
        response.headers.forEach { (key, values) ->
            values.forEach { value ->
                appendLine("  $key: $value")
            }
        }
        appendLine()
        appendLine("Body:")
        append(response.body)
    }
    
    private fun formatTcpSuccess(request: TcpRequestDomain, response: TcpResponseDomain.Success): String = buildString {
        appendLine("TCP Connection to ${request.host.value}:${request.port.value}")
        appendLine("Status: SUCCESS")
        appendLine()
        appendLine("Response:")
        append(response.response)
    }
    
    private fun formatTcpFailure(request: TcpRequestDomain, response: TcpResponseDomain.Failure): String = buildString {
        appendLine("TCP Connection to ${request.host.value}:${request.port.value}")
        appendLine("Status: FAILED")
        appendLine()
        appendLine("Error: ${response.error}")
    }
    
    private fun createSuccessResponse(requestId: JsonElement, text: String): McpResponse {
        return McpResponse(
            id = requestId,
            result = kotlinx.serialization.json.buildJsonObject {
                putJsonArray("content") {
                    addJsonObject {
                        put("type", "text")
                        put("text", text)
                    }
                }
            }
        )
    }
    
    private fun createErrorResponse(requestId: JsonElement, code: Int, message: String): McpResponse {
        return McpResponse(
            id = requestId,
            error = McpError(code = code, message = message)
        )
    }
}

