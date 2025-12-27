package ferprieto.mcp.httpclient

import io.github.oshai.kotlinlogging.KLogger
import ferprieto.mcp.httpclient.di.appModules
import ferprieto.mcp.httpclient.server.McpServer
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.qualifier.named
import org.koin.java.KoinJavaComponent.inject

/**
 * Main entry point for the MCP HTTP Client server
 * Uses Koin for dependency injection
 */
fun main() {
    // Initialize Koin first
    startKoin {
        modules(appModules)
    }
    
    // Get logger after Koin is initialized
    val logger: KLogger by inject(KLogger::class.java, named("Main"))
    
    logger.info { "Starting MCP HTTP Client Server" }
    logger.trace { "Initializing Koin DI container" }
    
    try {
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







