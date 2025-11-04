package io.mcp.httpclient.di

import io.mcp.httpclient.client.HttpClientService
import io.mcp.httpclient.client.TcpClientService
import io.mcp.httpclient.data.cache.InMemoryCache
import io.mcp.httpclient.data.repository.HttpRepositoryImpl
import io.mcp.httpclient.data.repository.TcpRepositoryImpl
import io.mcp.httpclient.domain.repository.CacheRepository
import io.mcp.httpclient.domain.repository.HttpRepository
import io.mcp.httpclient.domain.repository.TcpRepository
import io.mcp.httpclient.domain.usecase.*
import io.mcp.httpclient.presentation.McpServerPresentation
import io.mcp.httpclient.server.McpServer
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Koin modules for dependency injection
 * Organized by architectural layers
 */

/**
 * Data layer module
 * Provides data sources, repositories, and cache implementations
 */
val dataModule = module {
    // Data sources
    single { HttpClientService() }
    single { TcpClientService() }
    
    // Cache
    single<CacheRepository> { 
        InMemoryCache(maxSize = 100) 
    }
    
    // Repositories
    single<HttpRepository> { 
        HttpRepositoryImpl(get()) 
    }
    single<TcpRepository> { 
        TcpRepositoryImpl(get()) 
    }
}

/**
 * Domain layer module
 * Provides use cases (business logic)
 */
val domainModule = module {
    // Use cases
    single { 
        MakeHttpRequestUseCase(
            httpRepository = get(),
            cacheRepository = get()
        ) 
    }
    single { 
        MakeGraphQLRequestUseCase(
            httpRepository = get()
        ) 
    }
    single { 
        MakeTcpConnectionUseCase(
            tcpRepository = get()
        ) 
    }
    single { 
        InvalidateCacheUseCase(
            cacheRepository = get()
        ) 
    }
}

/**
 * Presentation layer module
 * Provides MCP server and presentation logic
 */
val presentationModule = module {
    // Presentation logic
    single { 
        McpServerPresentation(
            makeHttpRequestUseCase = get(),
            makeGraphQLRequestUseCase = get(),
            makeTcpConnectionUseCase = get(),
            invalidateCacheUseCase = get()
        ) 
    }
    
    // MCP Server
    single { 
        McpServer(
            makeHttpRequestUseCase = get(),
            makeGraphQLRequestUseCase = get(),
            makeTcpConnectionUseCase = get(),
            invalidateCacheUseCase = get()
        ) 
    }
}

/**
 * All application modules
 * Import this list in Main.kt
 */
val appModules = listOf(
    dataModule,
    domainModule,
    presentationModule
)

