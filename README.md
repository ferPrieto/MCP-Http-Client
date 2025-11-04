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

## Features

- **Intelligent Caching**: GET requests are automatically cached with LRU eviction (5-minute TTL)
- **Type Safety**: Built with Kotlin value classes for compile-time validation
- **Clean Architecture**: Organized with domain, data, and presentation layers
- **Dependency Injection**: Uses Koin for maintainable dependency management
- **Async Processing**: Kotlin coroutines for efficient concurrent operations

## License

MIT License
