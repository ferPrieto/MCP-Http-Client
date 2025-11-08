#!/usr/bin/env node

const { execSync } = require('child_process');

/**
 * Post-install script for npm package
 * Verifies Java installation and version
 */

console.log('Checking Java installation...');

try {
  const javaVersion = execSync('java -version 2>&1', { encoding: 'utf8' });
  
  // Extract version number
  const versionMatch = javaVersion.match(/version "(\d+)/);
  if (versionMatch) {
    const majorVersion = parseInt(versionMatch[1]);
    
    if (majorVersion >= 11) {
      console.log(`✓ Java ${majorVersion} detected - compatible`);
      console.log('');
      console.log('MCP HTTP Client installed successfully!');
      console.log('');
      console.log('To use with Cursor or Claude Desktop, add to your MCP config:');
      console.log('');
      console.log('{');
      console.log('  "mcpServers": {');
      console.log('    "http-client": {');
      console.log('      "command": "npx",');
      console.log('      "args": ["@mcp/http-client"]');
      console.log('    }');
      console.log('  }');
      console.log('}');
    } else {
      console.warn(`⚠ Warning: Java ${majorVersion} detected, but Java 11+ is recommended`);
    }
  }
} catch (error) {
  console.error('');
  console.error('❌ Java not found!');
  console.error('');
  console.error('MCP HTTP Client requires Java 11 or higher.');
  console.error('');
  console.error('Install Java:');
  console.error('  macOS:   brew install openjdk@11');
  console.error('  Linux:   sudo apt install openjdk-11-jdk');
  console.error('  Windows: Download from https://adoptium.net/');
  console.error('');
  process.exit(1);
}

