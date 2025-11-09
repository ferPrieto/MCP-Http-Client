package ferprieto.mcp.httpclient.domain.repository

import ferprieto.mcp.httpclient.domain.model.*

/**
 * Repository interfaces defining data operations
 * Implementations in the data layer
 */

interface HttpRepository {
    suspend fun makeRequest(request: HttpRequestDomain): RequestResult<HttpResponseDomain>
    suspend fun makeGraphQLRequest(request: GraphQLRequestDomain): RequestResult<HttpResponseDomain>
}

interface TcpRepository {
    suspend fun connect(request: TcpRequestDomain): RequestResult<TcpResponseDomain>
}

interface CacheRepository {
    suspend fun get(key: CacheKey): HttpResponseDomain?
    suspend fun put(key: CacheKey, response: HttpResponseDomain)
    suspend fun invalidate(key: CacheKey)
    suspend fun clear()
}

