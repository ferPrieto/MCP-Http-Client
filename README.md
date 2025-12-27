<p align="center">
  <img src="mcp-http.png" alt="MCP HTTP Client" title="MCP-HTTP-CLIENT" align="right" width="64"/>
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

## Comparison with Postman

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


## Advanced Features

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


## Roadmap

Future features planned for upcoming releases:

- **Collections**: Save and organize API requests (like Postman collections)
- **Environments**: Manage variables across different environments (Dev, Staging, Production)
- **Authentication Helpers**: Built-in support for Basic Auth, Bearer Token, API Key, OAuth2
- **Request Chaining**: Use response values in subsequent requests with variable substitution
- **Postman Import**: Import existing Postman collections for easy migration

## License

MIT License

---

Made with ❤️ for the AI-native development workflow. Simpler than [Postman](https://www.postman.com), faster than manual curl commands, perfect for AI assistants!
