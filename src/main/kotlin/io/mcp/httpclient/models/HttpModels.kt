package ferprieto.mcp.httpclient.models

import kotlinx.serialization.Serializable

/**
 * Represents an HTTP request to be made by the MCP server
 */
@Serializable
data class HttpRequest(
    val url: String,
    val method: String,
    val headers: Map<String, String>? = null,
    val params: Map<String, String>? = null,
    val body: String? = null
)

/**
 * Represents the HTTP response returned by the server
 */
@Serializable
data class HttpResponse(
    val status: Int,
    val headers: Map<String, List<String>>,
    val body: String
)

/**
 * Represents a GraphQL request
 */
@Serializable
data class GraphQLRequest(
    val url: String,
    val query: String,
    val variables: Map<String, String>? = null,
    val operationName: String? = null,
    val headers: Map<String, String>? = null
)

/**
 * Represents a TCP/Telnet connection request
 */
@Serializable
data class TcpRequest(
    val host: String,
    val port: Int,
    val message: String? = null,
    val timeout: Int = 5
)

/**
 * Represents a TCP/Telnet connection response
 */
@Serializable
data class TcpResponse(
    val success: Boolean,
    val response: String,
    val error: String? = null
)







