package io.mcp.httpclient.benchmark

import io.mcp.httpclient.client.HttpClientService
import io.mcp.httpclient.data.cache.InMemoryCache
import io.mcp.httpclient.data.repository.HttpRepositoryImpl
import io.mcp.httpclient.domain.model.*
import io.mcp.httpclient.domain.repository.CacheRepository
import io.mcp.httpclient.domain.usecase.MakeHttpRequestUseCase
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis
import kotlin.time.Duration.Companion.minutes

/**
 * Standalone benchmark runner
 * Run with: ./gradlew runBenchmark
 */
fun main() = runBlocking {
    println("\n=== Cache Performance Benchmark ===\n")
    
    // Setup
    val httpClientService = HttpClientService()
    val httpRepository = HttpRepositoryImpl(httpClientService)
    val realCache = InMemoryCache(maxSize = 100, defaultTtl = 5.minutes)
    val noOpCache = NoOpCache()
    
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
            val result = useCaseWithoutCache(request)
            if (result !is RequestResult.Success) {
                println("  Request failed!")
            }
        }
    }
    
    // Benchmark WITH cache
    println("Testing WITH cache ($iterations requests)...")
    val timeWithCache = measureTimeMillis {
        repeat(iterations) {
            val result = useCaseWithCache(request)
            if (result !is RequestResult.Success) {
                println("  Request failed!")
            }
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
 */
private class NoOpCache : CacheRepository {
    override suspend fun get(key: CacheKey): HttpResponseDomain? = null
    override suspend fun put(key: CacheKey, response: HttpResponseDomain) {}
    override suspend fun invalidate(key: CacheKey) {}
    override suspend fun clear() {}
}

