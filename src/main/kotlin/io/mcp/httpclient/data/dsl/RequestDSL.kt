package io.mcp.httpclient.data.dsl

import io.mcp.httpclient.domain.model.*

/**
 * DSL for building HTTP requests in a fluent, type-safe manner
 * Example:
 *   val request = httpRequest {
 *       url = "https://api.example.com/users"
 *       method = HttpMethod.POST
 *       header("Authorization", "Bearer token")
 *       param("page", "1")
 *       body = """{"name": "John"}"""
 *   }
 */

@DslMarker
annotation class RequestDslMarker

@RequestDslMarker
class HttpRequestBuilder {
    private var _url: String = ""
    private var _method: HttpMethod = HttpMethod.GET
    private val _headers = mutableMapOf<String, String>()
    private val _params = mutableMapOf<String, String>()
    private var _body: String? = null
    
    var url: String
        get() = _url
        set(value) { _url = value }
    
    var method: HttpMethod
        get() = _method
        set(value) { _method = value }
    
    var body: String?
        get() = _body
        set(value) { _body = value }
    
    fun header(key: String, value: String) {
        _headers[key] = value
    }
    
    fun headers(block: MutableMap<String, String>.() -> Unit) {
        _headers.apply(block)
    }
    
    fun param(key: String, value: String) {
        _params[key] = value
    }
    
    fun params(block: MutableMap<String, String>.() -> Unit) {
        _params.apply(block)
    }
    
    fun build(): HttpRequestDomain = HttpRequestDomain(
        url = Url(_url),
        method = _method,
        headers = _headers,
        params = _params,
        body = _body
    )
}

fun httpRequest(block: HttpRequestBuilder.() -> Unit): HttpRequestDomain =
    HttpRequestBuilder().apply(block).build()

@RequestDslMarker
class GraphQLRequestBuilder {
    private var _url: String = ""
    private var _query: String = ""
    private val _variables = mutableMapOf<String, String>()
    private var _operationName: String? = null
    private val _headers = mutableMapOf<String, String>()
    
    var url: String
        get() = _url
        set(value) { _url = value }
    
    var query: String
        get() = _query
        set(value) { _query = value }
    
    var operationName: String?
        get() = _operationName
        set(value) { _operationName = value }
    
    fun variable(key: String, value: String) {
        _variables[key] = value
    }
    
    fun variables(block: MutableMap<String, String>.() -> Unit) {
        _variables.apply(block)
    }
    
    fun header(key: String, value: String) {
        _headers[key] = value
    }
    
    fun headers(block: MutableMap<String, String>.() -> Unit) {
        _headers.apply(block)
    }
    
    fun build(): GraphQLRequestDomain = GraphQLRequestDomain(
        url = Url(_url),
        query = GraphQLQuery(_query),
        variables = _variables,
        operationName = _operationName,
        headers = _headers
    )
}

fun graphqlRequest(block: GraphQLRequestBuilder.() -> Unit): GraphQLRequestDomain =
    GraphQLRequestBuilder().apply(block).build()

@RequestDslMarker
class TcpRequestBuilder {
    private var _host: String = ""
    private var _port: Int = 0
    private var _message: String? = null
    private var _timeout: Int = 5
    
    var host: String
        get() = _host
        set(value) { _host = value }
    
    var port: Int
        get() = _port
        set(value) { _port = value }
    
    var message: String?
        get() = _message
        set(value) { _message = value }
    
    var timeout: Int
        get() = _timeout
        set(value) { _timeout = value }
    
    fun build(): TcpRequestDomain = TcpRequestDomain(
        host = Host(_host),
        port = Port(_port),
        message = _message,
        timeout = Timeout(_timeout)
    )
}

fun tcpRequest(block: TcpRequestBuilder.() -> Unit): TcpRequestDomain =
    TcpRequestBuilder().apply(block).build()

