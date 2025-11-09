package ferprieto.mcp.httpclient.domain.usecase

import io.github.oshai.kotlinlogging.KotlinLogging
import ferprieto.mcp.httpclient.domain.model.*
import ferprieto.mcp.httpclient.domain.repository.CacheRepository
import ferprieto.mcp.httpclient.domain.repository.HttpRepository
import ferprieto.mcp.httpclient.domain.repository.TcpRepository

private val logger = KotlinLogging.logger {}

/**
 * Use cases encapsulating business logic
 * Following Single Responsibility Principle
 */

class MakeHttpRequestUseCase(
    private val httpRepository: HttpRepository,
    private val cacheRepository: CacheRepository
) {
    suspend operator fun invoke(request: HttpRequestDomain): RequestResult<HttpResponseDomain> {
        logger.trace { "Executing MakeHttpRequestUseCase for ${request.method.value} ${request.url.value}" }
        
        // Check cache for GET requests
        if (request.isCacheable) {
            logger.trace { "Checking cache for key: ${request.cacheKey.value}" }
            cacheRepository.get(request.cacheKey)?.let { cachedResponse ->
                logger.debug { "Cache hit for ${request.url.value}" }
                return RequestResult.Success(cachedResponse)
            }
            logger.trace { "Cache miss for ${request.url.value}" }
        }
        
        // Make actual request
        val result = httpRepository.makeRequest(request)
        
        // Cache successful GET responses
        if (result is RequestResult.Success && request.isCacheable) {
            logger.trace { "Caching response for ${request.url.value}" }
            cacheRepository.put(request.cacheKey, result.data)
        }
        
        return result
    }
}

class MakeGraphQLRequestUseCase(
    private val httpRepository: HttpRepository
) {
    suspend operator fun invoke(request: GraphQLRequestDomain): RequestResult<HttpResponseDomain> {
        logger.trace { "Executing MakeGraphQLRequestUseCase for ${request.url.value}" }
        return httpRepository.makeGraphQLRequest(request)
    }
}

class MakeTcpConnectionUseCase(
    private val tcpRepository: TcpRepository
) {
    suspend operator fun invoke(request: TcpRequestDomain): RequestResult<TcpResponseDomain> {
        logger.trace { "Executing MakeTcpConnectionUseCase for ${request.host.value}:${request.port.value}" }
        return tcpRepository.connect(request)
    }
}

class InvalidateCacheUseCase(
    private val cacheRepository: CacheRepository
) {
    suspend operator fun invoke(key: CacheKey) {
        logger.trace { "Invalidating cache for key: ${key.value}" }
        cacheRepository.invalidate(key)
    }
    
    suspend fun clear() {
        logger.trace { "Clearing entire cache" }
        cacheRepository.clear()
    }
}

