package io.mcp.httpclient.data.mapper

import io.mcp.httpclient.domain.model.*
import io.mcp.httpclient.models.HttpRequest
import io.mcp.httpclient.models.HttpResponse

/**
 * Mappers for converting between data and domain models
 * Following the Mapper pattern for Clean Architecture
 */

fun HttpRequestDomain.toData(): HttpRequest = HttpRequest(
    url = url.value,
    method = method.value,
    headers = headers,
    params = params,
    body = body
)

fun HttpResponse.toDomain(): HttpResponseDomain = HttpResponseDomain(
    status = HttpStatusCode(status),
    headers = headers,
    body = body
)

fun GraphQLRequestDomain.toHttpRequest(): HttpRequestDomain {
    val bodyJson = buildString {
        append("{\"query\":\"")
        append(query.value.replace("\"", "\\\"").replace("\n", "\\n"))
        append("\"")
        
        if (variables.isNotEmpty()) {
            append(",\"variables\":{")
            append(variables.entries.joinToString(",") { (key, value) ->
                "\"$key\":\"$value\""
            })
            append("}")
        }
        
        operationName?.let {
            append(",\"operationName\":\"$it\"")
        }
        
        append("}")
    }
    
    val mergedHeaders = headers.toMutableMap().apply {
        putIfAbsent("Content-Type", "application/json")
    }
    
    return HttpRequestDomain(
        url = url,
        method = HttpMethod.POST,
        headers = mergedHeaders,
        body = bodyJson
    )
}

fun TcpRequestDomain.toTcpRequest(): io.mcp.httpclient.models.TcpRequest =
    io.mcp.httpclient.models.TcpRequest(
        host = host.value,
        port = port.value,
        message = message,
        timeout = timeout.seconds
    )

fun io.mcp.httpclient.models.TcpResponse.toDomain(): TcpResponseDomain =
    if (success) {
        TcpResponseDomain.Success(response)
    } else {
        TcpResponseDomain.Failure(error ?: "Unknown error")
    }

fun Throwable.toDomainException(): DomainException = when (this) {
    is java.net.SocketTimeoutException -> DomainException.TimeoutException(message ?: "Connection timeout")
    is java.net.ConnectException -> DomainException.NetworkException("Connection refused", this)
    is java.net.UnknownHostException -> DomainException.NetworkException("Unknown host: $message", this)
    is java.io.IOException -> DomainException.NetworkException(message ?: "Network error", this)
    is IllegalArgumentException -> DomainException.ValidationException(message ?: "Validation error")
    else -> DomainException.UnknownException(message ?: "Unknown error", this)
}

