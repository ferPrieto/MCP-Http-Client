<p align="center">
  <img src="mcp-http.png" alt="MCP HTTP Client" width="200"/>
</p>

# MCP HTTP Client Server

A powerful Model Context Protocol (MCP) server for making HTTP requests, GraphQL queries, and TCP/Telnet connections from AI assistants. Inspired by [Postman](https://www.postman.com) but designed for AI-native workflows with enhanced response formatting, intelligent caching, and multiple content type support.

## Why Use This?

- Seamlessly integrates with AI assistants like Claude
- 13.7x faster with intelligent LRU caching
- Auto-formatted JSON, status emojis, performance metrics
- JSON, form-data, URL-encoded - all supported
- very request shows response time
- atural language commands, no complex setup

## Installation

Choose your preferred installation method:

### Option 1: npm (Recommended for easy setup)

```bash
npm install -g @mcp/http-client
```

### Option 2: Homebrew (macOS/Linux)

```bash
brew tap ferPrieto/mcp-http-client
brew install mcp-http-client
```

### Option 3: GitHub Packages (for Kotlin/JVM projects)

Add the repository and dependency to your `build.gradle.kts`:

```kotlin
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/ferPrieto/MCP-Http-Client")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation("ferprieto.mcp:httpclient:1.0.0")
}
```

**Note:** GitHub Packages requires authentication. Generate a [Personal Access Token](https://github.com/settings/tokens) with `read:packages` scope.

### Option 4: Build from Source

```bash
git clone https://github.com/ferPrieto/MCP-Http-Client.git
cd MCP-Http-Client
./gradlew clean build
```

This generates `build/libs/mcp-http-client-all.jar`.

## Configuration

Add to your MCP client configuration file:
- **Cursor**: `~/.cursor/mcp.json`
- **Claude Desktop**: `~/Library/Application Support/Claude/claude_desktop_config.json`

### If installed via npm:

```json
{
  "mcpServers": {
    "http-client": {
      "command": "npx",
      "args": ["@mcp/http-client"]
    }
  }
}
```

### If installed via Homebrew:

```json
{
  "mcpServers": {
    "http-client": {
      "command": "mcp-http-client"
    }
  }
}
```

### If using JAR directly:

```json
{
  "mcpServers": {
    "http-client": {
      "command": "java",
      "args": ["-jar", "/path/to/mcp-http-client-all.jar"]
    }
  }
}
```

##  Features & Examples

### 1. HTTP/HTTPS Requests

Make HTTP requests with any method (GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS).

**Simple GET request:**
```
"Get data from https://api.example.com/posts/1"
```

**GET with query parameters:**
```
"GET https://api.github.com/search/repositories with params q=kotlin and sort=stars"
```

**POST with JSON body:**
```
"POST to https://api.example.com/users with body {name: 'John', email: 'john@example.com'}"
```

**POST with custom headers:**
```
"Send POST request to https://api.example.com/users with header Authorization: Bearer token123 and JSON body {name: 'Alice'}"
```

**PUT request:**
```
"Update user at https://api.example.com/users/42 with PUT method and body {status: 'active'}"
```

**DELETE request:**
```
"Delete resource at https://api.example.com/users/42 with Authorization header Bearer token123"
```

### 2. Multiple Content Types

Support for different body formats makes it easy to work with various APIs.

**Form Data (multipart/form-data):**
```
"POST to https://api.example.com/upload with bodyType FORM_DATA and body {
  username: 'admin',
  password: 'secret123',
  remember: 'true'
}"
```

**URL-Encoded (application/x-www-form-urlencoded):**
```
"POST to https://api.example.com/login with bodyType X_WWW_FORM_URLENCODED and body username=admin&password=secret"
```

**JSON (application/json):**
```
"POST to https://api.example.com/users with bodyType JSON and body {name: 'John', email: 'john@example.com'}"
```

### 3. GraphQL Queries

Execute GraphQL queries with variables and custom headers.

**Simple GraphQL query:**
```
"Query GraphQL at https://api.example.com/graphql: { user(id: 1) { name email } }"
```

**GraphQL with variables:**
```
"GraphQL query to https://api.example.com/graphql with query: { user(id: $userId) { name } } and variable userId=123"
```

**GraphQL with operation name:**
```
"Execute GraphQL query at https://api.example.com/graphql with query: { users { name } } and operationName=GetUsers"
```

**GraphQL with authentication:**
```
"Query GraphQL at https://api.example.com/graphql with header Authorization: Bearer token and query: { me { name email } }"
```

### 4. TCP/Telnet Connections

Establish raw TCP socket connections for network testing.

**Basic TCP connection:**
```
"Connect via telnet to 192.168.1.1 port 8080"
```

**TCP with custom timeout:**
```
"Test TCP connection to localhost:3000 with timeout 10 seconds"
```

**TCP with message:**
```
"Connect to example.com port 80 and send message: GET / HTTP/1.1"
```

**TCP connection test:**
```
"Check if port 8080 is open on localhost using telnet"
```

### 5. Cache Management

The intelligent cache automatically speeds up repeated GET requests by 13.7x. You can manually clear it when needed.

**Clear the cache:**
```
"Clear the HTTP cache"
```

**When to clear:**
- Testing API changes
- Need fresh data immediately
- After server updates
- Debugging cache-related issues

The cache automatically:
- Only caches GET requests
- Expires entries after 5 minutes
- Limits to 100 entries (memory efficient)
- Thread-safe for concurrent requests

## 🎨 Enhanced Response Formatting

Every response is beautifully formatted with:

```
╔═══════════════════════════════════════════════════════════╗
║                     HTTP RESPONSE                         ║
╚═══════════════════════════════════════════════════════════╝

✅ Status: 200 OK
⏱️  Duration: 145ms
📄 Content-Type: application/json
📏 Content-Length: 1234 bytes

─────────────────── HEADERS ───────────────────
  content-type: application/json
  cache-control: max-age=3600

─────────────────── BODY ──────────────────────
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com"
}
```

**Features:**
- Status codes with emoji indicators (✅ 2xx, ↪️ 3xx, ⚠️ 4xx, ❌ 5xx)
- Response time in milliseconds
- Content-Type detection
- Automatic JSON pretty-printing
- Content-Length display

## Performance Caching

Implements intelligent LRU (Least Recently Used) cache with TTL expiration that automatically caches GET requests, delivering **13.7x speedup** (92% faster) on repeated requests.

**Benchmark Results** (10 identical GET requests):
- **Without Cache**: 1537ms (153ms per request)
- **With Cache**: 112ms (11ms per request)
- **Speedup**: 13.7x faster with 9/10 requests served from cache

Inspired by Bruno API client, the cache is thread-safe, limits memory usage to 100 entries, and expires data after 5 minutes to balance performance with data freshness.

## 💡 Comparison with Postman

| Feature | MCP HTTP Client | Postman |
|---------|----------------|---------|
| **Interface** | 🤖 Natural Language | 🖱️ GUI |
| **Setup Time** | ⚡ 1 minute | ⏱️ 5+ minutes |
| **HTTP Methods** | ✅ All | ✅ All |
| **GraphQL** | ✅ Native | ✅ Yes |
| **TCP/Telnet** | ✅ Yes | ❌ No |
| **Content Types** | ✅ JSON, Form, URL-encoded | ✅ Many |
| **Response Formatting** | ✅ Auto pretty-print | ✅ Yes |
| **Performance Cache** | ✅ 13.7x faster | ❌ No |
| **Response Time** | ✅ Auto-tracked | ✅ Yes |
| **Cost** | 🆓 Free & Open Source | 💰 Free/Paid |
| **AI Integration** | ✅ Native | ❌ Manual |

## 🎯 Real-World Examples

### Testing a REST API

```
"GET https://jsonplaceholder.typicode.com/posts/1"

"POST to https://jsonplaceholder.typicode.com/posts with JSON body {
  title: 'My Post',
  body: 'This is the content',
  userId: 1
}"

"PUT to https://jsonplaceholder.typicode.com/posts/1 with body {
  title: 'Updated Title'
}"

"DELETE https://jsonplaceholder.typicode.com/posts/1"
```

### Authentication Flow

```
"POST to https://api.example.com/auth/login with body {
  email: 'user@example.com',
  password: 'secret123'
}"

"GET https://api.example.com/profile with header Authorization: Bearer <token-from-previous-response>"
```

### Form Submission

```
"POST to https://api.example.com/contact with bodyType FORM_DATA and body {
  name: 'John Doe',
  email: 'john@example.com',
  message: 'Hello World'
}"
```

### GraphQL API

```
"Query GraphQL at https://api.github.com/graphql with header Authorization: Bearer YOUR_TOKEN and query:
{
  viewer {
    login
    name
    repositories(first: 5) {
      nodes {
        name
        stargazerCount
      }
    }
  }
}"
```

### Network Debugging

```
"Test TCP connection to localhost:3000 with timeout 5 seconds"

"Connect via telnet to api.example.com port 443"
```

## 🛠️ Advanced Features

### Content-Type Auto-Detection

The server automatically:
- Detects JSON responses and pretty-prints them
- Sets appropriate `Content-Type` headers based on `bodyType`
- Handles form-data with proper multipart boundaries
- URL-encodes form parameters automatically

### Performance Metrics

Every request automatically tracks:
- Total request duration
- Response size
- Status codes
- Timing information

## 🧪 Testing

Run the test suite:

```bash
./gradlew test
```

Run performance benchmarks:

```bash
./gradlew runBenchmark
```

## 📝 Roadmap

Future features planned for upcoming releases:

- **Collections**: Save and organize API requests (like Postman collections)
- **Environments**: Manage variables across different environments (Dev, Staging, Production)
- **Authentication Helpers**: Built-in support for Basic Auth, Bearer Token, API Key, OAuth2
- **Request Chaining**: Use response values in subsequent requests with variable substitution
- **Postman Import**: Import existing Postman collections for easy migration

## 📄 License

MIT License

---

Made with ❤️ for the AI-native development workflow. Simpler than [Postman](https://www.postman.com), faster than manual curl commands, perfect for AI assistants!
