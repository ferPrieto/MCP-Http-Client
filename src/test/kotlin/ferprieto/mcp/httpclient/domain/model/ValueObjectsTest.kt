package ferprieto.mcp.httpclient.domain.model

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ValueObjectsTest {
    
    @Test
    fun `Url should accept valid http url`() {
        val url = Url("https://api.example.com/users")
        assertEquals("https://api.example.com/users", url.value)
    }
    
    @Test
    fun `Url should reject blank url`() {
        assertThrows<IllegalArgumentException> {
            Url("")
        }
    }
    
    @Test
    fun `Url should reject invalid protocol`() {
        assertThrows<IllegalArgumentException> {
            Url("ftp://example.com")
        }
    }
    
    @Test
    fun `HttpMethod should accept valid methods`() {
        val methods = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS")
        methods.forEach { method ->
            val httpMethod = HttpMethod(method)
            assertEquals(method.uppercase(), httpMethod.value)
        }
    }
    
    @Test
    fun `HttpMethod should reject invalid method`() {
        assertThrows<IllegalArgumentException> {
            HttpMethod("INVALID")
        }
    }
    
    @Test
    fun `HttpStatusCode should detect success status`() {
        val status = HttpStatusCode(200)
        assertTrue(status.isSuccess)
    }
    
    @Test
    fun `HttpStatusCode should detect client error`() {
        val status = HttpStatusCode(404)
        assertTrue(status.isClientError)
    }
    
    @Test
    fun `Port should accept valid port`() {
        val port = Port(8080)
        assertEquals(8080, port.value)
    }
    
    @Test
    fun `Port should reject invalid port`() {
        assertThrows<IllegalArgumentException> {
            Port(70000)
        }
    }
    
    @Test
    fun `Timeout should convert to milliseconds`() {
        val timeout = Timeout(5)
        assertEquals(5000L, timeout.milliseconds)
    }
    
    @Test
    fun `CacheKey should be generated from request params`() {
        val url = Url("https://api.example.com/users")
        val method = HttpMethod.GET
        val params = mapOf("page" to "1", "limit" to "10")
        
        val cacheKey = CacheKey.from(url, method, params)
        assertTrue(cacheKey.value.contains("GET"))
        assertTrue(cacheKey.value.contains(url.value))
    }
}

