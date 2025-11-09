package ferprieto.mcp.httpclient.data.repository

import io.github.oshai.kotlinlogging.KotlinLogging
import ferprieto.mcp.httpclient.client.HttpClientService
import ferprieto.mcp.httpclient.client.TcpClientService
import ferprieto.mcp.httpclient.data.mapper.*
import ferprieto.mcp.httpclient.domain.model.*
import ferprieto.mcp.httpclient.domain.repository.HttpRepository
import ferprieto.mcp.httpclient.domain.repository.TcpRepository

private val logger = KotlinLogging.logger {}

/**
 * Repository implementations
 * Bridge between domain and data sources
 */

class HttpRepositoryImpl(
    private val httpClientService: HttpClientService
) : HttpRepository {
    
    override suspend fun makeRequest(request: HttpRequestDomain): RequestResult<HttpResponseDomain> {
        logger.trace { "HttpRepository: makeRequest ${request.method.value} ${request.url.value}" }
        
        return try {
            val dataRequest = request.toData()
            val response = httpClientService.makeRequest(dataRequest)
            
            if (response.status == 0) {
                // OkHttp returns status 0 for errors
                RequestResult.Failure(
                    DomainException.NetworkException("Request failed with status 0")
                )
            } else {
                RequestResult.Success(response.toDomain())
            }
        } catch (e: Exception) {
            logger.error(e) { "Error in makeRequest" }
            RequestResult.Failure(e.toDomainException())
        }
    }
    
    override suspend fun makeGraphQLRequest(request: GraphQLRequestDomain): RequestResult<HttpResponseDomain> {
        logger.trace { "HttpRepository: makeGraphQLRequest ${request.url.value}" }
        
        return try {
            // Convert GraphQL request to HTTP POST request
            val httpRequest = request.toHttpRequest()
            makeRequest(httpRequest)
        } catch (e: Exception) {
            logger.error(e) { "Error in makeGraphQLRequest" }
            RequestResult.Failure(e.toDomainException())
        }
    }
}

class TcpRepositoryImpl(
    private val tcpClientService: TcpClientService
) : TcpRepository {
    
    override suspend fun connect(request: TcpRequestDomain): RequestResult<TcpResponseDomain> {
        logger.trace { "TcpRepository: connect ${request.host.value}:${request.port.value}" }
        
        return try {
            val dataRequest = request.toTcpRequest()
            val response = tcpClientService.connect(dataRequest)
            RequestResult.Success(response.toDomain())
        } catch (e: Exception) {
            logger.error(e) { "Error in connect" }
            RequestResult.Failure(e.toDomainException())
        }
    }
}

