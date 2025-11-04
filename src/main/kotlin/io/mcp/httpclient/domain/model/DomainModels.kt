package io.mcp.httpclient.domain.model

/**
 * Domain models representing the core business entities
 */

data class HttpRequestDomain(
    val url: Url,
    val method: HttpMethod,
    val headers: Map<String, String> = emptyMap(),
    val params: Map<String, String> = emptyMap(),
    val body: String? = null
) {
    val cacheKey: CacheKey
        get() = CacheKey.from(url, method, params)
    
    val isCacheable: Boolean
        get() = method == HttpMethod.GET
}

data class HttpResponseDomain(
    val status: HttpStatusCode,
    val headers: Map<String, List<String>>,
    val body: String
) {
    val isSuccess: Boolean get() = status.isSuccess
    val contentType: String? get() = headers["content-type"]?.firstOrNull()
}

data class GraphQLRequestDomain(
    val url: Url,
    val query: GraphQLQuery,
    val variables: Map<String, String> = emptyMap(),
    val operationName: String? = null,
    val headers: Map<String, String> = emptyMap()
)

data class TcpRequestDomain(
    val host: Host,
    val port: Port,
    val message: String? = null,
    val timeout: Timeout = Timeout(5)
)

sealed interface TcpResponseDomain {
    data class Success(val response: String) : TcpResponseDomain
    data class Failure(val error: String) : TcpResponseDomain
}

sealed interface RequestResult<out T> {
    data class Success<T>(val data: T) : RequestResult<T>
    data class Failure(val error: DomainException) : RequestResult<Nothing>
    data object Loading : RequestResult<Nothing>
}

sealed class DomainException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class NetworkException(message: String, cause: Throwable? = null) : DomainException(message, cause)
    class TimeoutException(message: String) : DomainException(message)
    class ValidationException(message: String) : DomainException(message)
    class UnknownException(message: String, cause: Throwable? = null) : DomainException(message, cause)
}

