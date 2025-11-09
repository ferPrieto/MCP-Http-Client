class McpHttpClient < Formula
  desc "MCP server for HTTP/HTTPS, GraphQL, and TCP/Telnet connections"
  homepage "https://github.com/ferPrieto/MCP-Http-Client"
  url "https://github.com/ferPrieto/MCP-Http-Client/releases/download/v1.0.0/mcp-http-client-1.0.0.tar.gz"
  sha256 "REPLACE_WITH_ACTUAL_SHA256"
  license "MIT"

  depends_on "openjdk@11"

  def install
    libexec.install "build/libs/mcp-http-client-all.jar"
    
    # Create wrapper script
    (bin/"mcp-http-client").write <<~EOS
      #!/bin/bash
      exec "#{Formula["openjdk@11"].opt_bin}/java" -jar "#{libexec}/mcp-http-client-all.jar" "$@"
    EOS
  end

  test do
    # Test that the binary exists and Java can load it
    output = shell_output("#{bin}/mcp-http-client 2>&1", 0)
    assert_match "MCP HTTP Client Server", output
  end
end

