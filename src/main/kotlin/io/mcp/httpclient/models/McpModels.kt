package io.mcp.httpclient.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/**
 * MCP Protocol models for JSON-RPC communication
 */

@Serializable
data class McpRequest(
    val jsonrpc: String = "2.0",
    val id: JsonElement,
    val method: String,
    val params: JsonObject? = null
)

@Serializable
data class McpResponse(
    val jsonrpc: String = "2.0",
    val id: JsonElement,
    val result: JsonElement? = null,
    val error: McpError? = null
)

@Serializable
data class McpError(
    val code: Int,
    val message: String,
    val data: JsonElement? = null
)

@Serializable
data class McpToolDefinition(
    val name: String,
    val description: String,
    val inputSchema: JsonObject
)

@Serializable
data class McpListToolsResult(
    val tools: List<McpToolDefinition>
)

@Serializable
data class McpInitializeResult(
    val protocolVersion: String,
    val serverInfo: ServerInfo,
    val capabilities: Capabilities
)

@Serializable
data class ServerInfo(
    val name: String,
    val version: String
)

@Serializable
data class Capabilities(
    val tools: ToolsCapability? = null
)

@Serializable
data class ToolsCapability(
    val listChanged: Boolean? = false
)








