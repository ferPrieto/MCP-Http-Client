package ferprieto.mcp.httpclient.domain.usecase

import ferprieto.mcp.httpclient.di.testModules
import ferprieto.mcp.httpclient.domain.model.*
import ferprieto.mcp.httpclient.domain.repository.CacheRepository
import ferprieto.mcp.httpclient.domain.repository.HttpRepository
import ferprieto.mcp.httpclient.domain.repository.TcpRepository
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.test.KoinTest
import org.koin.test.inject
import org.koin.test.junit5.KoinTestExtension
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MakeHttpRequestUseCaseTest : KoinTest {
    
    @JvmField
    @RegisterExtension
    val koinTestExtension = KoinTestExtension.create {
        modules(testModules)
    }
    
    private val httpRepository: HttpRepository by inject()
    private val cacheRepository: CacheRepository by inject()
    private val useCase: MakeHttpRequestUseCase by inject()
    
    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }
    
    @Test
    fun `should check cache for GET requests`() = runTest {
        // Given
        val request = HttpRequestDomain(
            url = Url("https://api.example.com/users"),
            method = HttpMethod.GET
        )
        val cachedResponse = HttpResponseDomain(
            status = HttpStatusCode(200),
            headers = emptyMap(),
            body = "cached"
        )
        
        coEvery { cacheRepository.get(any()) } returns cachedResponse
        
        // When
        val result = useCase(request)
        
        // Then
        assertTrue(result is RequestResult.Success)
        assertEquals("cached", result.data.body)
        coVerify { cacheRepository.get(request.cacheKey) }
        coVerify(exactly = 0) { httpRepository.makeRequest(any()) }
    }
    
    @Test
    fun `should make request on cache miss`() = runTest {
        // Given
        val request = HttpRequestDomain(
            url = Url("https://api.example.com/users"),
            method = HttpMethod.GET
        )
        val response = HttpResponseDomain(
            status = HttpStatusCode(200),
            headers = emptyMap(),
            body = "fresh"
        )
        
        coEvery { cacheRepository.get(any()) } returns null
        coEvery { httpRepository.makeRequest(any()) } returns RequestResult.Success(response)
        coEvery { cacheRepository.put(any(), any()) } just Runs
        
        // When
        val result = useCase(request)
        
        // Then
        assertTrue(result is RequestResult.Success)
        assertEquals("fresh", result.data.body)
        coVerify { cacheRepository.put(request.cacheKey, response) }
    }
    
    @Test
    fun `should not cache POST requests`() = runTest {
        // Given
        val request = HttpRequestDomain(
            url = Url("https://api.example.com/users"),
            method = HttpMethod.POST,
            body = "{\"name\":\"John\"}"
        )
        val response = HttpResponseDomain(
            status = HttpStatusCode(201),
            headers = emptyMap(),
            body = "created"
        )
        
        coEvery { httpRepository.makeRequest(any()) } returns RequestResult.Success(response)
        
        // When
        val result = useCase(request)
        
        // Then
        assertTrue(result is RequestResult.Success)
        coVerify(exactly = 0) { cacheRepository.get(any()) }
        coVerify(exactly = 0) { cacheRepository.put(any(), any()) }
    }
}

class MakeGraphQLRequestUseCaseTest : KoinTest {
    
    @JvmField
    @RegisterExtension
    val koinTestExtension = KoinTestExtension.create {
        modules(testModules)
    }
    
    private val httpRepository: HttpRepository by inject()
    private val useCase: MakeGraphQLRequestUseCase by inject()
    
    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }
    
    @Test
    fun `should execute GraphQL request`() = runTest {
        // Given
        val request = GraphQLRequestDomain(
            url = Url("https://api.example.com/graphql"),
            query = GraphQLQuery("{ user { name } }")
        )
        val response = HttpResponseDomain(
            status = HttpStatusCode(200),
            headers = emptyMap(),
            body = "{\"data\":{\"user\":{\"name\":\"John\"}}}"
        )
        
        coEvery { httpRepository.makeGraphQLRequest(any()) } returns RequestResult.Success(response)
        
        // When
        val result = useCase(request)
        
        // Then
        assertTrue(result is RequestResult.Success)
        assertEquals(200, result.data.status.value)
    }
}

class MakeTcpConnectionUseCaseTest : KoinTest {
    
    @JvmField
    @RegisterExtension
    val koinTestExtension = KoinTestExtension.create {
        modules(testModules)
    }
    
    private val tcpRepository: TcpRepository by inject()
    private val useCase: MakeTcpConnectionUseCase by inject()
    
    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }
    
    @Test
    fun `should execute TCP connection`() = runTest {
        // Given
        val request = TcpRequestDomain(
            host = Host("localhost"),
            port = Port(8080)
        )
        val response = TcpResponseDomain.Success("Connection established")
        
        coEvery { tcpRepository.connect(any()) } returns RequestResult.Success(response)
        
        // When
        val result = useCase(request)
        
        // Then
        assertTrue(result is RequestResult.Success)
        assertTrue(result.data is TcpResponseDomain.Success)
    }
}

class InvalidateCacheUseCaseTest : KoinTest {
    
    @JvmField
    @RegisterExtension
    val koinTestExtension = KoinTestExtension.create {
        modules(testModules)
    }
    
    private val cacheRepository: CacheRepository by inject()
    private val useCase: InvalidateCacheUseCase by inject()
    
    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }
    
    @Test
    fun `should invalidate single cache entry`() = runTest {
        // Given
        val cacheKey = CacheKey("test-key")
        coEvery { cacheRepository.invalidate(any()) } just Runs
        
        // When
        useCase(cacheKey)
        
        // Then
        coVerify { cacheRepository.invalidate(cacheKey) }
    }
    
    @Test
    fun `should clear entire cache`() = runTest {
        // Given
        coEvery { cacheRepository.clear() } just Runs
        
        // When
        useCase.clear()
        
        // Then
        coVerify { cacheRepository.clear() }
    }
}

