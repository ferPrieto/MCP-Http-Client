package ferprieto.mcp.httpclient.di

import ferprieto.mcp.httpclient.domain.repository.CacheRepository
import ferprieto.mcp.httpclient.domain.repository.HttpRepository
import ferprieto.mcp.httpclient.domain.repository.TcpRepository
import ferprieto.mcp.httpclient.domain.usecase.InvalidateCacheUseCase
import ferprieto.mcp.httpclient.domain.usecase.MakeGraphQLRequestUseCase
import ferprieto.mcp.httpclient.domain.usecase.MakeHttpRequestUseCase
import ferprieto.mcp.httpclient.domain.usecase.MakeTcpConnectionUseCase
import io.mockk.mockk
import org.koin.dsl.module

/**
 * Test modules for Koin with mocked dependencies
 */

val testRepositoryModule = module {
    single<HttpRepository> { mockk(relaxed = true) }
    single<TcpRepository> { mockk(relaxed = true) }
    single<CacheRepository> { mockk(relaxed = true) }
}

val testUseCaseModule = module {
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

val testModules = listOf(
    testRepositoryModule,
    testUseCaseModule
)

