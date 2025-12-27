package ferprieto.mcp.httpclient.data.formatter

import kotlinx.serialization.json.Json
import io.github.oshai.kotlinlogging.KLogger

/**
 * Formats HTTP responses with pretty-printing for JSON
 */
class ResponseFormatter(
    private val logger: KLogger
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    
    /**
     * Format response body with pretty-printing if JSON
     */
    fun formatBody(body: String, contentType: String?): String {
        return when {
            contentType?.contains("application/json", ignoreCase = true) == true -> {
                try {
                    val jsonElement = json.parseToJsonElement(body)
                    json.encodeToString(
                        kotlinx.serialization.json.JsonElement.serializer(),
                        jsonElement
                    )
                } catch (e: Exception) {
                    logger.trace(e) { "Not valid JSON, returning as-is" }
                    body
                }
            }
            contentType?.contains("application/xml", ignoreCase = true) == true ||
            contentType?.contains("text/xml", ignoreCase = true) == true -> {
                // Could add XML formatting here
                body
            }
            else -> body
        }
    }
    
    /**
     * Format complete HTTP response with metadata
     */
    fun formatResponse(
        status: Int,
        headers: Map<String, List<String>>,
        body: String,
        duration: Long? = null
    ): String = buildString {
        appendLine("╔═══════════════════════════════════════════════════════════╗")
        appendLine("║                     HTTP RESPONSE                         ║")
        appendLine("╚═══════════════════════════════════════════════════════════╝")
        appendLine()
        
        // Status line
        val statusEmoji = when (status) {
            in 200..299 -> "✅"
            in 300..399 -> "↪️"
            in 400..499 -> "⚠️"
            in 500..599 -> "❌"
            else -> "❓"
        }
        appendLine("$statusEmoji Status: $status ${getStatusText(status)}")
        
        // Performance
        if (duration != null) {
            appendLine("⏱️  Duration: ${duration}ms")
        }
        
        // Content info
        val contentType = headers.entries
            .firstOrNull { it.key.equals("content-type", ignoreCase = true) }
            ?.value?.firstOrNull()
        val contentLength = headers.entries
            .firstOrNull { it.key.equals("content-length", ignoreCase = true) }
            ?.value?.firstOrNull()
        
        if (contentType != null) {
            appendLine("📄 Content-Type: $contentType")
        }
        if (contentLength != null) {
            appendLine("📏 Content-Length: $contentLength bytes")
        }
        
        appendLine()
        appendLine("─────────────────── HEADERS ───────────────────")
        headers.forEach { (key, values) ->
            values.forEach { value ->
                appendLine("  $key: $value")
            }
        }
        
        appendLine()
        appendLine("─────────────────── BODY ──────────────────────")
        
        val formattedBody = formatBody(body, contentType)
        if (formattedBody.length > 10000) {
            appendLine(formattedBody.take(10000))
            appendLine()
            appendLine("... (truncated, ${formattedBody.length - 10000} more characters)")
        } else {
            append(formattedBody)
        }
    }
    
    private fun getStatusText(status: Int): String = when (status) {
        200 -> "OK"
        201 -> "Created"
        204 -> "No Content"
        301 -> "Moved Permanently"
        302 -> "Found"
        304 -> "Not Modified"
        400 -> "Bad Request"
        401 -> "Unauthorized"
        403 -> "Forbidden"
        404 -> "Not Found"
        405 -> "Method Not Allowed"
        429 -> "Too Many Requests"
        500 -> "Internal Server Error"
        502 -> "Bad Gateway"
        503 -> "Service Unavailable"
        504 -> "Gateway Timeout"
        else -> ""
    }
}




