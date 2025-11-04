package io.mcp.httpclient.domain.model

/**
 * Value classes for type-safe domain modeling
 * Using @JvmInline for zero-cost abstractions
 */

@JvmInline
value class Url(val value: String) {
    init {
        require(value.isNotBlank()) { "URL cannot be blank" }
        require(value.startsWith("http://") || value.startsWith("https://") || value.startsWith("tcp://")) {
            "URL must start with http://, https://, or tcp://"
        }
    }
}

@JvmInline
value class HttpMethod(val value: String) {
    init {
        require(value.uppercase() in VALID_METHODS) { "Invalid HTTP method: $value" }
    }
    
    companion object {
        private val VALID_METHODS = setOf("GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS")
        
        val GET = HttpMethod("GET")
        val POST = HttpMethod("POST")
        val PUT = HttpMethod("PUT")
        val DELETE = HttpMethod("DELETE")
        val PATCH = HttpMethod("PATCH")
        val HEAD = HttpMethod("HEAD")
        val OPTIONS = HttpMethod("OPTIONS")
    }
}

@JvmInline
value class HttpStatusCode(val value: Int) {
    init {
        require(value in 0..599) { "HTTP status code must be between 0 and 599" }
    }
    
    val isSuccess: Boolean get() = value in 200..299
    val isRedirect: Boolean get() = value in 300..399
    val isClientError: Boolean get() = value in 400..499
    val isServerError: Boolean get() = value in 500..599
}

@JvmInline
value class Port(val value: Int) {
    init {
        require(value in 1..65535) { "Port must be between 1 and 65535" }
    }
}

@JvmInline
value class Host(val value: String) {
    init {
        require(value.isNotBlank()) { "Host cannot be blank" }
    }
}

@JvmInline
value class GraphQLQuery(val value: String) {
    init {
        require(value.isNotBlank()) { "GraphQL query cannot be blank" }
    }
}

@JvmInline
value class Timeout(val seconds: Int) {
    init {
        require(seconds > 0) { "Timeout must be positive" }
    }
    
    val milliseconds: Long get() = seconds * 1000L
}

@JvmInline
value class CacheKey(val value: String) {
    companion object {
        fun from(url: Url, method: HttpMethod, params: Map<String, String>): CacheKey {
            val paramsString = params.entries
                .sortedBy { it.key }
                .joinToString("&") { "${it.key}=${it.value}" }
            return CacheKey("${method.value}:${url.value}?$paramsString")
        }
    }
}

