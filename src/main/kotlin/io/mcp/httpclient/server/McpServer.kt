package io.mcp.httpclient.server

import io.github.oshai.kotlinlogging.KotlinLogging
import io.mcp.httpclient.client.HttpClientService
import io.mcp.httpclient.models.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter

private val logger = KotlinLogging.logger {}

/**
 * MCP Server that handles JSON-RPC communication via stdio
 * Updated to use Clean Architecture with use cases
 */
class McpServer(
    private val makeHttpRequestUseCase: io.mcp.httpclient.domain.usecase.MakeHttpRequestUseCase,
    private val makeGraphQLRequestUseCase: io.mcp.httpclient.domain.usecase.MakeGraphQLRequestUseCase,
    private val makeTcpConnectionUseCase: io.mcp.httpclient.domain.usecase.MakeTcpConnectionUseCase,
    private val invalidateCacheUseCase: io.mcp.httpclient.domain.usecase.InvalidateCacheUseCase
) {
    private val presentation = io.mcp.httpclient.presentation.McpServerPresentation(
        makeHttpRequestUseCase = makeHttpRequestUseCase,
        makeGraphQLRequestUseCase = makeGraphQLRequestUseCase,
        makeTcpConnectionUseCase = makeTcpConnectionUseCase,
        invalidateCacheUseCase = invalidateCacheUseCase
    )
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = false
    }
    
    private val reader = BufferedReader(InputStreamReader(System.`in`))
    private val writer = PrintWriter(OutputStreamWriter(System.out), true)
    
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    /**
     * Start the MCP server and listen for requests
     */
    fun start() {
        logger.info { "MCP HTTP Client Server starting..." }
        logger.trace { "Server initialization complete, listening for requests" }
        
        runBlocking {
            try {
                while (true) {
                    logger.trace { "Waiting for next request" }
                    val line = withContext(Dispatchers.IO) {
                        reader.readLine()
                    } ?: break
                    
                    if (line.isBlank()) {
                        logger.trace { "Received blank line, skipping" }
                        continue
                    }
                    
                    logger.debug { "Received request: $line" }
                    
                    scope.launch {
                        try {
                            logger.trace { "Parsing JSON-RPC request" }
                            val request = json.decodeFromString<McpRequest>(line)
                            logger.trace { "Handling method: ${request.method}" }
                            val response = handleRequest(request)
                            val responseJson = json.encodeToString(McpResponse.serializer(), response)
                            
                            synchronized(writer) {
                                writer.println(responseJson)
                                writer.flush()
                            }
                            
                            logger.debug { "Sent response: $responseJson" }
                        } catch (e: Exception) {
                            logger.error(e) { "Error processing request: $line" }
                            val errorResponse = McpResponse(
                                id = JsonPrimitive("error"),
                                error = McpError(
                                    code = -32700,
                                    message = "Parse error: ${e.message}"
                                )
                            )
                            val responseJson = json.encodeToString(McpResponse.serializer(), errorResponse)
                            
                            synchronized(writer) {
                                writer.println(responseJson)
                                writer.flush()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                logger.error(e) { "Server error" }
            } finally {
                scope.cancel()
                logger.info { "MCP HTTP Client Server stopped" }
            }
        }
    }
    
    /**
     * Handle incoming MCP requests
     */
    private suspend fun handleRequest(request: McpRequest): McpResponse {
        return try {
            when (request.method) {
                "initialize" -> handleInitialize(request)
                "tools/list" -> handleListTools(request)
                "tools/call" -> handleToolCall(request)
                else -> McpResponse(
                    id = request.id,
                    error = McpError(
                        code = -32601,
                        message = "Method not found: ${request.method}"
                    )
                )
            }
        } catch (e: Exception) {
            logger.error(e) { "Error handling request: ${request.method}" }
            McpResponse(
                id = request.id,
                error = McpError(
                    code = -32603,
                    message = "Internal error: ${e.message}"
                )
            )
        }
    }
    
    /**
     * Handle initialize request
     */
    private fun handleInitialize(request: McpRequest): McpResponse {
        val result = McpInitializeResult(
            protocolVersion = "2024-11-05",
            serverInfo = ServerInfo(
                name = "mcp-http-client",
                version = "1.0.0"
            ),
            capabilities = Capabilities(
                tools = ToolsCapability(listChanged = false)
            )
        )
        
        return McpResponse(
            id = request.id,
            result = json.encodeToJsonElement(result)
        )
    }
    
    /**
     * Handle tools/list request
     */
    private fun handleListTools(request: McpRequest): McpResponse {
        logger.trace { "Building tools list" }
        val tools = listOf(
            McpToolDefinition(
                name = "make_request",
                description = "Makes an HTTP/HTTPS request with the specified parameters. Supports GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS methods. " +
                        "You can specify custom headers, query parameters, and request body. " +
                        "Returns the HTTP response with status code, headers, and body.",
                inputSchema = buildJsonObject {
                    put("type", "object")
                    putJsonObject("properties") {
                        putJsonObject("url") {
                            put("type", "string")
                            put("description", "The URL to send the request to (e.g., https://api.example.com/users)")
                        }
                        putJsonObject("method") {
                            put("type", "string")
                            put("description", "The HTTP method to use (GET, POST, PUT, DELETE, PATCH)")
                            put("enum", JsonArray(listOf("GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS").map { JsonPrimitive(it) }))
                        }
                        putJsonObject("headers") {
                            put("type", "object")
                            put("description", "Optional HTTP headers as key-value pairs (e.g., {\"Authorization\": \"Bearer token\"})")
                            putJsonObject("additionalProperties") {
                                put("type", "string")
                            }
                        }
                        putJsonObject("params") {
                            put("type", "object")
                            put("description", "Optional query parameters as key-value pairs (e.g., {\"page\": \"1\", \"limit\": \"10\"})")
                            putJsonObject("additionalProperties") {
                                put("type", "string")
                            }
                        }
                        putJsonObject("body") {
                            put("type", "string")
                            put("description", "Optional request body (usually JSON string for POST/PUT/PATCH requests)")
                        }
                    }
                    put("required", JsonArray(listOf(JsonPrimitive("url"), JsonPrimitive("method"))))
                }
            ),
            McpToolDefinition(
                name = "graphql_query",
                description = "Makes a GraphQL query to the specified endpoint. Automatically formats the query and variables. " +
                        "Supports custom headers and operation names. Returns the GraphQL response.",
                inputSchema = buildJsonObject {
                    put("type", "object")
                    putJsonObject("properties") {
                        putJsonObject("url") {
                            put("type", "string")
                            put("description", "The GraphQL endpoint URL (e.g., https://api.example.com/graphql)")
                        }
                        putJsonObject("query") {
                            put("type", "string")
                            put("description", "The GraphQL query string")
                        }
                        putJsonObject("variables") {
                            put("type", "object")
                            put("description", "Optional query variables as key-value pairs")
                            putJsonObject("additionalProperties") {
                                put("type", "string")
                            }
                        }
                        putJsonObject("operationName") {
                            put("type", "string")
                            put("description", "Optional operation name if multiple operations in query")
                        }
                        putJsonObject("headers") {
                            put("type", "object")
                            put("description", "Optional HTTP headers (e.g., {\"Authorization\": \"Bearer token\"})")
                            putJsonObject("additionalProperties") {
                                put("type", "string")
                            }
                        }
                    }
                    put("required", JsonArray(listOf(JsonPrimitive("url"), JsonPrimitive("query"))))
                }
            ),
            McpToolDefinition(
                name = "tcp_connect",
                description = "Makes a raw TCP/Telnet connection to a host and port. " +
                        "Optionally sends a message and reads the response. " +
                        "Useful for testing network connectivity and services.",
                inputSchema = buildJsonObject {
                    put("type", "object")
                    putJsonObject("properties") {
                        putJsonObject("host") {
                            put("type", "string")
                            put("description", "The hostname or IP address to connect to")
                        }
                        putJsonObject("port") {
                            put("type", "integer")
                            put("description", "The port number to connect to")
                        }
                        putJsonObject("message") {
                            put("type", "string")
                            put("description", "Optional message to send after connecting")
                        }
                        putJsonObject("timeout") {
                            put("type", "integer")
                            put("description", "Connection timeout in seconds (default: 5)")
                            put("default", 5)
                        }
                    }
                    put("required", JsonArray(listOf(JsonPrimitive("host"), JsonPrimitive("port"))))
                }
            )
        )
        
        logger.trace { "Returning ${tools.size} tools" }
        val result = McpListToolsResult(tools = tools)
        
        return McpResponse(
            id = request.id,
            result = json.encodeToJsonElement(result)
        )
    }
    
    /**
     * Handle tools/call request
     */
    private suspend fun handleToolCall(request: McpRequest): McpResponse {
        val params = request.params ?: return McpResponse(
            id = request.id,
            error = McpError(
                code = -32602,
                message = "Invalid params"
            )
        )
        
        val toolName = params["name"]?.jsonPrimitive?.content
        logger.trace { "Tool call: $toolName" }
        val arguments = params["arguments"]?.jsonObject
        
        return when (toolName) {
            "make_request" -> presentation.handleMakeRequest(request.id, arguments)
            "graphql_query" -> presentation.handleGraphQLQuery(request.id, arguments)
            "tcp_connect" -> presentation.handleTcpConnect(request.id, arguments)
            else -> {
                logger.warn { "Unknown tool requested: $toolName" }
                McpResponse(
                    id = request.id,
                    error = McpError(
                        code = -32602,
                        message = "Unknown tool: $toolName"
                    )
                )
            }
        }
    }
    
    /**
     * Parse HttpRequest from JSON arguments (legacy support)
     */
    private fun parseHttpRequest(arguments: JsonObject): HttpRequest {
        val url = arguments["url"]?.jsonPrimitive?.content
            ?: throw IllegalArgumentException("Missing 'url' parameter")
        
        val method = arguments["method"]?.jsonPrimitive?.content
            ?: throw IllegalArgumentException("Missing 'method' parameter")
        
        val headers = arguments["headers"]?.jsonObject?.mapValues { 
            it.value.jsonPrimitive.content 
        }
        
        val params = arguments["params"]?.jsonObject?.mapValues { 
            it.value.jsonPrimitive.content 
        }
        
        val body = arguments["body"]?.jsonPrimitive?.content
        
        return HttpRequest(
            url = url,
            method = method,
            headers = headers,
            params = params,
            body = body
        )
    }
    
    /**
     * Format HTTP response as a readable string
     */
    private fun formatHttpResponse(response: HttpResponse): String {
        val sb = StringBuilder()
        sb.appendLine("HTTP Response:")
        sb.appendLine("Status: ${response.status}")
        sb.appendLine()
        sb.appendLine("Headers:")
        response.headers.forEach { (key, values) ->
            values.forEach { value ->
                sb.appendLine("  $key: $value")
            }
        }
        sb.appendLine()
        sb.appendLine("Body:")
        sb.append(response.body)
        
        return sb.toString()
    }
}







