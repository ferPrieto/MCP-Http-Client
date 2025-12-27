package ferprieto.mcp.httpclient.data.contenttype

import io.github.oshai.kotlinlogging.KLogger
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Handles different content types for HTTP requests
 * Supports form-data, URL-encoded, JSON, etc.
 */
class ContentTypeHandler(
    private val logger: KLogger
) {
    
    /**
     * Convert form data map to URL-encoded string
     */
    fun encodeUrlFormData(data: Map<String, String>): String {
        return data.entries.joinToString("&") { (key, value) ->
            "${urlEncode(key)}=${urlEncode(value)}"
        }
    }
    
    /**
     * Convert form data map to multipart/form-data boundary format
     */
    fun encodeMultipartFormData(data: Map<String, String>, boundary: String = generateBoundary()): Pair<String, String> {
        val body = buildString {
            data.forEach { (key, value) ->
                append("--$boundary\r\n")
                append("Content-Disposition: form-data; name=\"$key\"\r\n")
                append("\r\n")
                append("$value\r\n")
            }
            append("--$boundary--\r\n")
        }
        
        val contentType = "multipart/form-data; boundary=$boundary"
        return body to contentType
    }
    
    /**
     * Parse body type and apply appropriate encoding
     */
    fun processBody(
        body: String?,
        bodyType: String?,
        headers: MutableMap<String, String>
    ): String? {
        if (body == null) return null
        
        return when (bodyType?.uppercase()) {
            "FORM_DATA", "MULTIPART" -> {
                // Parse body as key=value pairs or JSON object
                val data = parseFormData(body)
                val (encoded, contentType) = encodeMultipartFormData(data)
                headers["Content-Type"] = contentType
                encoded
            }
            
            "X_WWW_FORM_URLENCODED", "URLENCODED" -> {
                val data = parseFormData(body)
                val encoded = encodeUrlFormData(data)
                headers["Content-Type"] = "application/x-www-form-urlencoded"
                encoded
            }
            
            "JSON" -> {
                if (!headers.containsKey("Content-Type")) {
                    headers["Content-Type"] = "application/json"
                }
                body
            }
            
            "RAW", null -> {
                // Leave as-is
                body
            }
            
            else -> body
        }
    }
    
    /**
     * Parse form data from various input formats
     * Supports:
     * - JSON object: {"key": "value"}
     * - Key-value pairs: key1=value1&key2=value2
     * - Line-separated: key1=value1\nkey2=value2
     */
    private fun parseFormData(input: String): Map<String, String> {
        return try {
            // Try parsing as JSON first
            val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
            val jsonElement = json.parseToJsonElement(input)
            if (jsonElement is kotlinx.serialization.json.JsonObject) {
                jsonElement.entries.associate { (key, value) ->
                    key to (value as? kotlinx.serialization.json.JsonPrimitive)?.content.orEmpty()
                }
            } else {
                parseKeyValuePairs(input)
            }
        } catch (e: Exception) {
            // Not JSON, try key-value pairs
            parseKeyValuePairs(input)
        }
    }
    
    private fun parseKeyValuePairs(input: String): Map<String, String> {
        val separator = when {
            input.contains('&') -> '&'
            input.contains('\n') -> '\n'
            else -> '&'
        }
        
        return input.split(separator)
            .mapNotNull { pair ->
                val parts = pair.split('=', limit = 2)
                if (parts.size == 2) {
                    parts[0].trim() to parts[1].trim()
                } else null
            }
            .toMap()
    }
    
    private fun urlEncode(value: String): String {
        return URLEncoder.encode(value, StandardCharsets.UTF_8.toString())
    }
    
    private fun generateBoundary(): String {
        return "----WebKitFormBoundary${java.util.UUID.randomUUID().toString().replace("-", "")}"
    }
}




