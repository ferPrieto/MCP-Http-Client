package ferprieto.mcp.httpclient.di

import ferprieto.mcp.httpclient.client.HttpClientService
import ferprieto.mcp.httpclient.client.TcpClientService
import ferprieto.mcp.httpclient.data.cache.InMemoryCache
import ferprieto.mcp.httpclient.data.contenttype.ContentTypeHandler
import ferprieto.mcp.httpclient.data.formatter.ResponseFormatter
import ferprieto.mcp.httpclient.data.repository.HttpRepositoryImpl
import ferprieto.mcp.httpclient.data.repository.TcpRepositoryImpl
import ferprieto.mcp.httpclient.domain.repository.CacheRepository
import ferprieto.mcp.httpclient.domain.repository.HttpRepository
import ferprieto.mcp.httpclient.domain.repository.TcpRepository
import ferprieto.mcp.httpclient.domain.usecase.InvalidateCacheUseCase
import ferprieto.mcp.httpclient.domain.usecase.MakeGraphQLRequestUseCase
import ferprieto.mcp.httpclient.domain.usecase.MakeHttpRequestUseCase
import ferprieto.mcp.httpclient.domain.usecase.MakeTcpConnectionUseCase
import ferprieto.mcp.httpclient.presentation.McpServerPresentation
import ferprieto.mcp.httpclient.server.McpServer
import org.koin.core.qualifier.named
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
    single { HttpClientService(get(named("HttpClientService"))) }
    single { TcpClientService(get(named("TcpClientService"))) }
    
    // Cache
    single<CacheRepository> { 
        InMemoryCache(
            maxSize = 100,
            logger = get(named("InMemoryCache"))
        ) 
    }
    
    // Repositories
    single<HttpRepository> { 
        HttpRepositoryImpl(
            httpClientService = get(),
            logger = get(named("RepositoryImpl"))
        ) 
    }
    single<TcpRepository> { 
        TcpRepositoryImpl(
            tcpClientService = get(),
            logger = get(named("RepositoryImpl"))
        ) 
    }
    
    // Utilities for better UX
    single { ResponseFormatter(get(named("ResponseFormatter"))) }
    single { ContentTypeHandler(get(named("ContentTypeHandler"))) }
}

/**
 * Domain layer module
 * Provides use cases (business logic)
 */
val domainModule = module {
    // Core HTTP Use cases
    single { 
        MakeHttpRequestUseCase(
            httpRepository = get(),
            cacheRepository = get(),
            logger = get(named("UseCases"))
        ) 
    }
    single { 
        MakeGraphQLRequestUseCase(
            httpRepository = get(),
            logger = get(named("UseCases"))
        ) 
    }
    single { 
        MakeTcpConnectionUseCase(
            tcpRepository = get(),
            logger = get(named("UseCases"))
        ) 
    }
    single { 
        InvalidateCacheUseCase(
            cacheRepository = get(),
            logger = get(named("UseCases"))
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
            invalidateCacheUseCase = get(),
            responseFormatter = get(),
            contentTypeHandler = get(),
            logger = get(named("McpServerPresentation"))
        ) 
    }
    
    // MCP Server
    single { 
        McpServer(
            presentation = get(),
            logger = get(named("McpServer"))
        ) 
    }
}

/**
 * All application modules
 * Import this list in Main.kt
 */
val appModules = listOf(
    loggerModule,
    dataModule,
    domainModule,
    presentationModule
)

