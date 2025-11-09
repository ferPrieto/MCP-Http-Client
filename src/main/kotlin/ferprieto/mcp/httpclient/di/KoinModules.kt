package ferprieto.mcp.httpclient.di

import ferprieto.mcp.httpclient.client.HttpClientService
import ferprieto.mcp.httpclient.client.TcpClientService
import ferprieto.mcp.httpclient.data.cache.InMemoryCache
import ferprieto.mcp.httpclient.data.repository.HttpRepositoryImpl
import ferprieto.mcp.httpclient.data.repository.TcpRepositoryImpl
import ferprieto.mcp.httpclient.domain.repository.CacheRepository
import ferprieto.mcp.httpclient.domain.repository.HttpRepository
import ferprieto.mcp.httpclient.domain.repository.TcpRepository
import ferprieto.mcp.httpclient.domain.usecase.*
import ferprieto.mcp.httpclient.presentation.McpServerPresentation
import ferprieto.mcp.httpclient.server.McpServer
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

