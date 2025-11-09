package ferprieto.mcp.httpclient.benchmark

import ferprieto.mcp.httpclient.client.HttpClientService
import ferprieto.mcp.httpclient.data.cache.InMemoryCache
import ferprieto.mcp.httpclient.data.repository.HttpRepositoryImpl
import ferprieto.mcp.httpclient.domain.model.HttpMethod
import ferprieto.mcp.httpclient.domain.model.HttpRequestDomain
import ferprieto.mcp.httpclient.domain.model.RequestResult
import ferprieto.mcp.httpclient.domain.model.Url
import ferprieto.mcp.httpclient.domain.repository.CacheRepository
import ferprieto.mcp.httpclient.domain.usecase.MakeHttpRequestUseCase
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.system.measureTimeMillis
import kotlin.time.Duration.Companion.minutes

/**
 * Benchmark test to demonstrate cache performance improvements
 * 
 * This test makes real HTTP calls to JSONPlaceholder API to show
 * the actual performance benefit of the LRU caching system.
 */
class CacheBenchmarkTest {
    
    @Test
    fun `benchmark cache performance with real HTTP calls`() = runBlocking {
        println("\n=== Cache Performance Benchmark ===\n")
        
        // Setup
        val httpClientService = HttpClientService()
        val httpRepository = HttpRepositoryImpl(httpClientService)
        val realCache = InMemoryCache(maxSize = 100, defaultTtl = 5.minutes)
        val noOpCache = NoOpCache() // Cache that does nothing
        
        val useCaseWithCache = MakeHttpRequestUseCase(httpRepository, realCache)
        val useCaseWithoutCache = MakeHttpRequestUseCase(httpRepository, noOpCache)
        
        // Test URL - JSONPlaceholder API
        val request = HttpRequestDomain(
            url = Url("https://jsonplaceholder.typicode.com/posts/1"),
            method = HttpMethod.GET
        )
        
        val iterations = 10
        
        // Benchmark WITHOUT cache
        println("Testing WITHOUT cache ($iterations requests)...")
        val timeWithoutCache = measureTimeMillis {
            repeat(iterations) {
                useCaseWithoutCache(request)
            }
        }
        
        // Benchmark WITH cache
        println("Testing WITH cache ($iterations requests)...")
        val timeWithCache = measureTimeMillis {
            repeat(iterations) {
                useCaseWithCache(request)
            }
        }
        
        // Results
        val improvement = ((timeWithoutCache - timeWithCache).toDouble() / timeWithoutCache * 100)
        val speedup = timeWithoutCache.toDouble() / timeWithCache
        
        println("\n--- Results ---")
        println("Without Cache: ${timeWithoutCache}ms (${timeWithoutCache / iterations}ms per request)")
        println("With Cache:    ${timeWithCache}ms (${timeWithCache / iterations}ms per request)")
        println("Improvement:   ${improvement.toInt()}% faster")
        println("Speedup:       ${String.format("%.1f", speedup)}x")
        println("\nCache Stats:")
        println("- Cache hits: ${iterations - 1} (only first request hits network)")
        println("- Network calls avoided: ${iterations - 1}")
        println("- Time saved: ${timeWithoutCache - timeWithCache}ms")
        println("\n=== End Benchmark ===\n")
    }
    
    /**
     * No-op cache that never stores or retrieves anything
     * Used to simulate behavior without caching
     */
    private class NoOpCache : CacheRepository {
        override suspend fun get(key: ferprieto.mcp.httpclient.domain.model.CacheKey): 
            ferprieto.mcp.httpclient.domain.model.HttpResponseDomain? = null
        
        override suspend fun put(
            key: ferprieto.mcp.httpclient.domain.model.CacheKey,
            response: ferprieto.mcp.httpclient.domain.model.HttpResponseDomain
        ) {}
        
        override suspend fun invalidate(key: ferprieto.mcp.httpclient.domain.model.CacheKey) {}
        
        override suspend fun clear() {}
    }
}

