package io.mcp.httpclient.data.dsl

import io.mcp.httpclient.domain.model.HttpMethod
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class RequestDSLTest {
    
    @Test
    fun `should build HTTP request using DSL`() {
        // When
        val request = httpRequest {
            url = "https://api.example.com/users"
            method = HttpMethod.POST
            header("Authorization", "Bearer token")
            param("page", "1")
            body = "{\"name\":\"John\"}"
        }
        
        // Then
        assertEquals("https://api.example.com/users", request.url.value)
        assertEquals("POST", request.method.value)
        assertEquals("Bearer token", request.headers["Authorization"])
        assertEquals("1", request.params["page"])
        assertEquals("{\"name\":\"John\"}", request.body)
    }
    
    @Test
    fun `should build HTTP request with headers block`() {
        // When
        val request = httpRequest {
            url = "https://api.example.com/users"
            method = HttpMethod.GET
            headers {
                put("Authorization", "Bearer token")
                put("Accept", "application/json")
            }
        }
        
        // Then
        assertEquals(2, request.headers.size)
        assertEquals("Bearer token", request.headers["Authorization"])
        assertEquals("application/json", request.headers["Accept"])
    }
    
    @Test
    fun `should build GraphQL request using DSL`() {
        // When
        val request = graphqlRequest {
            url = "https://api.example.com/graphql"
            query = "{ user(id: 1) { name email } }"
            variable("userId", "123")
            operationName = "GetUser"
            header("Authorization", "Bearer token")
        }
        
        // Then
        assertEquals("https://api.example.com/graphql", request.url.value)
        assertEquals("{ user(id: 1) { name email } }", request.query.value)
        assertEquals("123", request.variables["userId"])
        assertEquals("GetUser", request.operationName)
        assertEquals("Bearer token", request.headers["Authorization"])
    }
    
    @Test
    fun `should build TCP request using DSL`() {
        // When
        val request = tcpRequest {
            host = "localhost"
            port = 8080
            message = "GET / HTTP/1.1"
            timeout = 10
        }
        
        // Then
        assertEquals("localhost", request.host.value)
        assertEquals(8080, request.port.value)
        assertEquals("GET / HTTP/1.1", request.message)
        assertEquals(10, request.timeout.seconds)
    }
    
    @Test
    fun `should build minimal HTTP request`() {
        // When
        val request = httpRequest {
            url = "https://api.example.com/users"
            method = HttpMethod.GET
        }
        
        // Then
        assertEquals("https://api.example.com/users", request.url.value)
        assertEquals("GET", request.method.value)
        assertEquals(0, request.headers.size)
        assertEquals(0, request.params.size)
        assertEquals(null, request.body)
    }
}

