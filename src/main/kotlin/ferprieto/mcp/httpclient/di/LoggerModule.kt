package ferprieto.mcp.httpclient.di

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.KLogger
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Logger module for dependency injection
 * Provides named loggers for each component
 */
val loggerModule = module {
    // Main
    single(named("Main")) { KotlinLogging.logger("ferprieto.mcp.httpclient.Main") }
    
    // Server layer
    single(named("McpServer")) { KotlinLogging.logger("ferprieto.mcp.httpclient.server.McpServer") }
    
    // Presentation layer
    single(named("McpServerPresentation")) { KotlinLogging.logger("ferprieto.mcp.httpclient.presentation.McpServerPresentation") }
    
    // Domain layer - Use cases
    single(named("UseCases")) { KotlinLogging.logger("ferprieto.mcp.httpclient.domain.usecase.UseCases") }
    
    // Data layer - Repositories
    single(named("RepositoryImpl")) { KotlinLogging.logger("ferprieto.mcp.httpclient.data.repository.RepositoryImpl") }
    
    // Data layer - Cache
    single(named("InMemoryCache")) { KotlinLogging.logger("ferprieto.mcp.httpclient.data.cache.InMemoryCache") }
    
    // Data layer - Utilities
    single(named("ResponseFormatter")) { KotlinLogging.logger("ferprieto.mcp.httpclient.data.formatter.ResponseFormatter") }
    single(named("ContentTypeHandler")) { KotlinLogging.logger("ferprieto.mcp.httpclient.data.contenttype.ContentTypeHandler") }
    
    // Client layer
    single(named("HttpClientService")) { KotlinLogging.logger("ferprieto.mcp.httpclient.client.HttpClientService") }
    single(named("TcpClientService")) { KotlinLogging.logger("ferprieto.mcp.httpclient.client.TcpClientService") }
}

