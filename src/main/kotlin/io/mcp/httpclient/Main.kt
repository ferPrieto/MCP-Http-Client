package io.mcp.httpclient

import io.github.oshai.kotlinlogging.KotlinLogging
import io.mcp.httpclient.di.appModules
import io.mcp.httpclient.server.McpServer
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.java.KoinJavaComponent.inject

private val logger = KotlinLogging.logger {}

/**
 * Main entry point for the MCP HTTP Client server
 * Uses Koin for dependency injection
 */
fun main() {
    logger.info { "Starting MCP HTTP Client Server" }
    logger.trace { "Initializing Koin DI container" }
    
    try {
        // Initialize Koin
        startKoin {
            modules(appModules)
        }
        
        logger.trace { "Koin initialization complete" }
        
        // Get MCP Server from Koin
        val mcpServer: McpServer by inject(McpServer::class.java)
        
        logger.trace { "Starting MCP server" }
        mcpServer.start()
        
        // Cleanup on shutdown
        Runtime.getRuntime().addShutdownHook(Thread {
            logger.info { "Shutting down MCP HTTP Client Server" }
            stopKoin()
        })
    } catch (e: Exception) {
        logger.error(e) { "Fatal error starting server" }
        stopKoin()
        System.exit(1)
    }
}







