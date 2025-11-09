package ferprieto.mcp.httpclient.data.cache

import io.github.oshai.kotlinlogging.KotlinLogging
import ferprieto.mcp.httpclient.domain.model.CacheKey
import ferprieto.mcp.httpclient.domain.model.HttpResponseDomain
import ferprieto.mcp.httpclient.domain.repository.CacheRepository
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

private val logger = KotlinLogging.logger {}

/**
 * In-memory LRU cache implementation inspired by Bruno API client
 * Features:
 * - Time-based expiration (TTL)
 * - Size-based eviction (LRU)
 * - Thread-safe concurrent access
 */

data class CacheEntry(
    val response: HttpResponseDomain,
    val timestamp: Long = System.currentTimeMillis(),
    val ttl: Duration = 5.minutes
) {
    fun isExpired(): Boolean = System.currentTimeMillis() - timestamp > ttl.inWholeMilliseconds
}

class InMemoryCache(
    private val maxSize: Int = 100,
    private val defaultTtl: Duration = 5.minutes
) : CacheRepository {
    
    private val cache = ConcurrentHashMap<String, CacheEntry>()
    private val accessOrder = mutableListOf<String>()
    
    override suspend fun get(key: CacheKey): HttpResponseDomain? {
        logger.trace { "Cache GET: ${key.value}" }
        
        val entry = cache[key.value] ?: run {
            logger.trace { "Cache MISS: ${key.value}" }
            return null
        }
        
        if (entry.isExpired()) {
            logger.trace { "Cache EXPIRED: ${key.value}" }
            invalidate(key)
            return null
        }
        
        // Update access order for LRU
        synchronized(accessOrder) {
            accessOrder.remove(key.value)
            accessOrder.add(key.value)
        }
        
        logger.debug { "Cache HIT: ${key.value}" }
        return entry.response
    }
    
    override suspend fun put(key: CacheKey, response: HttpResponseDomain) {
        logger.trace { "Cache PUT: ${key.value}" }
        
        // Evict oldest entry if cache is full
        if (cache.size >= maxSize) {
            evictOldest()
        }
        
        val entry = CacheEntry(
            response = response,
            ttl = defaultTtl
        )
        
        cache[key.value] = entry
        
        synchronized(accessOrder) {
            accessOrder.remove(key.value)
            accessOrder.add(key.value)
        }
        
        logger.debug { "Cache size: ${cache.size}" }
    }
    
    override suspend fun invalidate(key: CacheKey) {
        logger.trace { "Cache INVALIDATE: ${key.value}" }
        cache.remove(key.value)
        synchronized(accessOrder) {
            accessOrder.remove(key.value)
        }
    }
    
    override suspend fun clear() {
        logger.info { "Cache CLEAR: removing ${cache.size} entries" }
        cache.clear()
        synchronized(accessOrder) {
            accessOrder.clear()
        }
    }
    
    private fun evictOldest() {
        val oldestKey = synchronized(accessOrder) {
            accessOrder.firstOrNull()
        }
        
        oldestKey?.let { key ->
            logger.trace { "Cache EVICT (LRU): $key" }
            cache.remove(key)
            synchronized(accessOrder) {
                accessOrder.remove(key)
            }
        }
    }
    
    fun getStats(): CacheStats = CacheStats(
        size = cache.size,
        maxSize = maxSize,
        entries = cache.keys.toList()
    )
}

data class CacheStats(
    val size: Int,
    val maxSize: Int,
    val entries: List<String>
)

