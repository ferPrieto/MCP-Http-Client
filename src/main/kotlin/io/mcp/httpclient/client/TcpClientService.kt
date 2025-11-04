package io.mcp.httpclient.client

import io.github.oshai.kotlinlogging.KotlinLogging
import io.mcp.httpclient.models.TcpRequest
import io.mcp.httpclient.models.TcpResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

private val logger = KotlinLogging.logger {}

/**
 * Service for making raw TCP/Telnet connections
 */
class TcpClientService {
    
    /**
     * Makes a TCP connection to the specified host and port
     */
    suspend fun connect(tcpRequest: TcpRequest): TcpResponse = withContext(Dispatchers.IO) {
        logger.trace { "Starting TCP connection to ${tcpRequest.host}:${tcpRequest.port}" }
        
        try {
            logger.debug { "Creating socket connection to ${tcpRequest.host}:${tcpRequest.port}" }
            
            withTimeout((tcpRequest.timeout * 1000).toLong()) {
                Socket(tcpRequest.host, tcpRequest.port).use { socket ->
                    logger.trace { "Socket connected successfully" }
                    
                    val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                    val writer = PrintWriter(socket.getOutputStream(), true)
                    
                    val responseBuilder = StringBuilder()
                    
                    // Send message if provided
                    if (!tcpRequest.message.isNullOrBlank()) {
                        logger.trace { "Sending message: ${tcpRequest.message}" }
                        writer.println(tcpRequest.message)
                    }
                    
                    // Read response
                    logger.trace { "Reading response from socket" }
                    socket.soTimeout = tcpRequest.timeout * 1000
                    
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        responseBuilder.appendLine(line)
                        logger.trace { "Received line: $line" }
                        
                        // Break if no more data is immediately available
                        if (!reader.ready()) break
                    }
                    
                    val response = responseBuilder.toString().ifEmpty { 
                        "Connection established. No data received." 
                    }
                    
                    logger.info { "TCP connection successful to ${tcpRequest.host}:${tcpRequest.port}" }
                    
                    TcpResponse(
                        success = true,
                        response = response
                    )
                }
            }
        } catch (e: java.net.ConnectException) {
            logger.error(e) { "Connection refused: ${tcpRequest.host}:${tcpRequest.port}" }
            TcpResponse(
                success = false,
                response = "",
                error = "Connection refused: ${e.message}"
            )
        } catch (e: java.net.SocketTimeoutException) {
            logger.error(e) { "Connection timeout: ${tcpRequest.host}:${tcpRequest.port}" }
            TcpResponse(
                success = false,
                response = "",
                error = "Connection timeout after ${tcpRequest.timeout} seconds"
            )
        } catch (e: java.net.UnknownHostException) {
            logger.error(e) { "Unknown host: ${tcpRequest.host}" }
            TcpResponse(
                success = false,
                response = "",
                error = "Unknown host: ${tcpRequest.host}"
            )
        } catch (e: Exception) {
            logger.error(e) { "TCP connection error: ${e.message}" }
            TcpResponse(
                success = false,
                response = "",
                error = "Connection error: ${e.message}"
            )
        }
    }
}


