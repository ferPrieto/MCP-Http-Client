<p align="center">
  <img src="mcp-http.png" alt="MCP HTTP Client" width="200"/>
</p>

# MCP HTTP Client Server

A Model Context Protocol (MCP) server for making HTTP requests, GraphQL queries, and raw TCP/Telnet connections from AI assistants.

## Quick Start

### Prerequisites

- Java 11 or higher
- Gradle 7.0 or higher

### Build

```bash
./gradlew clean build
```

This generates `build/libs/mcp-http-client-all.jar`.

### Configuration

Add to your MCP client configuration:

**Cursor** (`~/.cursor/mcp.json`):
```json
{
  "mcpServers": {
    "http-client": {
      "command": "java",
      "args": ["-jar", "/absolute/path/to/mcp-http-client-all.jar"]
    }
  }
}
```

**Claude Desktop** (`~/Library/Application Support/Claude/claude_desktop_config.json`):
```json
{
  "mcpServers": {
    "http-client": {
      "command": "java",
      "args": ["-jar", "/absolute/path/to/mcp-http-client-all.jar"]
    }
  }
}
```

## Available Functionalities

### 1. HTTP/HTTPS Requests

Make HTTP requests with any method (GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS).

**Examples:**

Simple GET request:
```
"Get data from https://api.example.com/posts/1"
```

GET with query parameters:
```
"GET https://api.github.com/search/repositories with params q=kotlin and sort=stars"
```

POST with JSON body:
```
"POST to https://api.example.com/users with body {name: 'John', email: 'john@example.com'}"
```

POST with headers and body:
```
"Send POST request to https://api.example.com/users with header Authorization: Bearer token123 and JSON body {name: 'Alice'}"
```

PUT request:
```
"Update user at https://api.example.com/users/42 with PUT method and body {status: 'active'}"
```

DELETE request:
```
"Delete resource at https://api.example.com/users/42 with Authorization header Bearer token123"
```

### 2. GraphQL Queries

Execute GraphQL queries with variables and custom headers.

**Examples:**

Simple GraphQL query:
```
"Query GraphQL at https://api.example.com/graphql: { user(id: 1) { name email } }"
```

GraphQL with variables:
```
"GraphQL query to https://api.example.com/graphql with query: { user(id: $userId) { name } } and variable userId=123"
```

GraphQL with operation name:
```
"Execute GraphQL query at https://api.example.com/graphql with query: { users { name } } and operationName=GetUsers"
```

GraphQL with authentication:
```
"Query GraphQL at https://api.example.com/graphql with header Authorization: Bearer token and query: { me { name email } }"
```

### 3. TCP/Telnet Connections

Establish raw TCP socket connections for network testing.

**Examples:**

Basic TCP connection:
```
"Connect via telnet to 192.168.1.1 port 8080"
```

TCP with custom timeout:
```
"Test TCP connection to localhost:3000 with timeout 10 seconds"
```

TCP with message:
```
"Connect to example.com port 80 and send message: GET / HTTP/1.1"
```

TCP connection test:
```
"Check if port 8080 is open on localhost using telnet"
```

## Performance Caching

Implements intelligent LRU (Least Recently Used) cache with TTL expiration that automatically caches GET requests, delivering **13.7x speedup** (92% faster) on repeated requests. Inspired by Bruno API client, the cache is thread-safe, limits memory usage to 100 entries, and expires data after 5 minutes to balance performance with data freshness.

**Benchmark Results** (10 identical GET requests):
- Without Cache: 1537ms (153ms per request)
- With Cache: 112ms (11ms per request)
- Speedup: 13.7x faster with 9/10 requests served from cache

## License

MIT License
