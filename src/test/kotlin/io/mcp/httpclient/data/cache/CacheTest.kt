package io.mcp.httpclient.data.cache

import io.mcp.httpclient.domain.model.CacheKey
import io.mcp.httpclient.domain.model.HttpResponseDomain
import io.mcp.httpclient.domain.model.HttpStatusCode
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.milliseconds

class InMemoryCacheTest {
    
    private lateinit var cache: InMemoryCache
    
    @BeforeEach
    fun setup() {
        cache = InMemoryCache(maxSize = 3, defaultTtl = 50.milliseconds)
    }
    
    @Test
    fun `should store and retrieve value`() = runTest {
        // Given
        val key = CacheKey("test-key")
        val response = HttpResponseDomain(
            status = HttpStatusCode(200),
            headers = emptyMap(),
            body = "test"
        )
        
        // When
        cache.put(key, response)
        val retrieved = cache.get(key)
        
        // Then
        assertEquals(response, retrieved)
    }
    
    @Test
    fun `should return null for non-existent key`() = runTest {
        // Given
        val key = CacheKey("non-existent")
        
        // When
        val result = cache.get(key)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `should expire entries after TTL`() = runTest {
        // Given
        val key = CacheKey("test-key")
        val response = HttpResponseDomain(
            status = HttpStatusCode(200),
            headers = emptyMap(),
            body = "test"
        )
        
        // When
        cache.put(key, response)
        Thread.sleep(100) // Wait for TTL to expire
        val retrieved = cache.get(key)
        
        // Then
        assertNull(retrieved)
    }
    
    @Test
    fun `should evict oldest entry when cache is full`() = runTest {
        // Given
        val key1 = CacheKey("key-1")
        val key2 = CacheKey("key-2")
        val key3 = CacheKey("key-3")
        val key4 = CacheKey("key-4")
        
        val response = HttpResponseDomain(
            status = HttpStatusCode(200),
            headers = emptyMap(),
            body = "test"
        )
        
        // When
        cache.put(key1, response)
        cache.put(key2, response)
        cache.put(key3, response)
        cache.put(key4, response) // Should evict key1
        
        // Then
        assertNull(cache.get(key1))
        assertEquals(response, cache.get(key2))
        assertEquals(response, cache.get(key3))
        assertEquals(response, cache.get(key4))
    }
    
    @Test
    fun `should update LRU order on access`() = runTest {
        // Given
        val key1 = CacheKey("key-1")
        val key2 = CacheKey("key-2")
        val key3 = CacheKey("key-3")
        val key4 = CacheKey("key-4")
        
        val response = HttpResponseDomain(
            status = HttpStatusCode(200),
            headers = emptyMap(),
            body = "test"
        )
        
        // When
        cache.put(key1, response)
        cache.put(key2, response)
        cache.put(key3, response)
        cache.get(key1) // Access key1 to update LRU
        cache.put(key4, response) // Should evict key2, not key1
        
        // Then
        assertEquals(response, cache.get(key1))
        assertNull(cache.get(key2))
        assertEquals(response, cache.get(key3))
        assertEquals(response, cache.get(key4))
    }
    
    @Test
    fun `should invalidate specific key`() = runTest {
        // Given
        val key1 = CacheKey("key-1")
        val key2 = CacheKey("key-2")
        val response = HttpResponseDomain(
            status = HttpStatusCode(200),
            headers = emptyMap(),
            body = "test"
        )
        
        // When
        cache.put(key1, response)
        cache.put(key2, response)
        cache.invalidate(key1)
        
        // Then
        assertNull(cache.get(key1))
        assertEquals(response, cache.get(key2))
    }
    
    @Test
    fun `should clear all entries`() = runTest {
        // Given
        val key1 = CacheKey("key-1")
        val key2 = CacheKey("key-2")
        val response = HttpResponseDomain(
            status = HttpStatusCode(200),
            headers = emptyMap(),
            body = "test"
        )
        
        // When
        cache.put(key1, response)
        cache.put(key2, response)
        cache.clear()
        
        // Then
        assertNull(cache.get(key1))
        assertNull(cache.get(key2))
        assertEquals(0, cache.getStats().size)
    }
    
    @Test
    fun `should provide cache statistics`() = runTest {
        // Given
        val key1 = CacheKey("key-1")
        val key2 = CacheKey("key-2")
        val response = HttpResponseDomain(
            status = HttpStatusCode(200),
            headers = emptyMap(),
            body = "test"
        )
        
        // When
        cache.put(key1, response)
        cache.put(key2, response)
        val stats = cache.getStats()
        
        // Then
        assertEquals(2, stats.size)
        assertEquals(3, stats.maxSize)
        assertEquals(2, stats.entries.size)
    }
}

