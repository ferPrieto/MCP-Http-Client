package ferprieto.mcp.httpclient.models

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

@OptIn(ExperimentalSerializationApi::class)
class McpResponseSerializationTest {
    
    private val json = Json {
        encodeDefaults = true
        explicitNulls = false
        prettyPrint = false
    }
    
    @Test
    fun `successful response should not include error field`() {
        // Given
        val response = McpResponse(
            id = JsonPrimitive(1),
            result = buildJsonObject {
                put("status", "success")
            }
        )
        
        // When
        val jsonString = json.encodeToString(McpResponse.serializer(), response)
        
        // Then
        assertFalse(jsonString.contains("\"error\""), 
            "Successful response should not contain 'error' field. Got: $jsonString")
        assertTrue(jsonString.contains("\"result\""),
            "Successful response should contain 'result' field. Got: $jsonString")
        assertTrue(jsonString.contains("\"jsonrpc\":\"2.0\""),
            "Response should contain jsonrpc field. Got: $jsonString")
        assertTrue(jsonString.contains("\"id\":1"),
            "Response should contain id field. Got: $jsonString")
    }
    
    @Test
    fun `error response should not include result field`() {
        // Given
        val response = McpResponse(
            id = JsonPrimitive(2),
            error = McpError(
                code = -32600,
                message = "Invalid request"
            )
        )
        
        // When
        val jsonString = json.encodeToString(McpResponse.serializer(), response)
        
        // Then
        assertFalse(jsonString.contains("\"result\""),
            "Error response should not contain 'result' field. Got: $jsonString")
        assertTrue(jsonString.contains("\"error\""),
            "Error response should contain 'error' field. Got: $jsonString")
        assertTrue(jsonString.contains("\"code\":-32600"),
            "Error should contain code field. Got: $jsonString")
        assertTrue(jsonString.contains("\"message\":\"Invalid request\""),
            "Error should contain message field. Got: $jsonString")
    }
    
    @Test
    fun `successful response with complex result`() {
        // Given
        val response = McpResponse(
            id = JsonPrimitive(3),
            result = buildJsonObject {
                put("protocolVersion", "2024-11-05")
                putJsonObject("serverInfo") {
                    put("name", "mcp-http-client")
                    put("version", "1.0.0")
                }
            }
        )
        
        // When
        val jsonString = json.encodeToString(McpResponse.serializer(), response)
        
        // Then
        assertFalse(jsonString.contains("\"error\""),
            "Successful response should not contain 'error' field. Got: $jsonString")
        assertTrue(jsonString.contains("\"result\""),
            "Successful response should contain 'result' field. Got: $jsonString")
        assertTrue(jsonString.contains("\"protocolVersion\""),
            "Result should contain nested data. Got: $jsonString")
    }
}

