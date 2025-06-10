# Build-Scout MCP Server

A comprehensive Model Context Protocol (MCP) server that enables LLM clients to interact with various build systems including Gradle, Maven, NPM/Yarn, Cargo, Python, Makefile, and CMake.

## Overview

Build-scout provides a standardized interface for build tool operations through the Model Context Protocol, allowing AI assistants to understand, analyze, and manipulate software projects across multiple build technologies.

## Supported Build Systems

- **Gradle** (Groovy and Kotlin DSL) - `build.gradle`, `build.gradle.kts`
- **Maven** - `pom.xml`
- **NPM/Yarn** - `package.json`
- **Cargo** (Rust) - `Cargo.toml`
- **Python** - `requirements.txt`, `setup.py`
- **Makefile** - `Makefile`
- **CMake** - `CMakeLists.txt`

## Available Tools

### Build System Discovery
- **`find_build_system`** - Discovers build systems in project directories
- **`build_system_file_paths`** - Returns paths to build system files

### Gradle Operations
- **`build_gradle_project`** - Executes Gradle builds with optional checks
- **`gradle_tester`** - Runs Gradle build and test tasks

### Dependency Management
- **`dependencies_list`** - Lists all top-level dependencies with versions
- **`update_dependency_version`** - Updates dependency versions in build files
- **`latest_dependency_version`** - Fetches latest versions from repositories

### Code Analysis
- **`find_class_usage`** - Finds class usage across the codebase
- **`jar_diff_reporter`** - Compares JAR files for differences

### File Operations
- **`get_file_info`** - Retrieves file metadata and information
- **`get_resource_info`** - Gets resource information
- **`replace_source_code_complete`** - Performs source code replacements

### Source Management
- **`download_current_latest_source`** - Downloads latest source versions

## Building the Server

### JAR Build (Recommended)
This will increment the version number and build the JAR:

```bash
./gradlew clean prebuild updateConfigJson
```

### Native Image Build (Experimental)
This will increment the version number and build the native image:

```bash
./gradlew clean prebuild nativeCompile 
```

## Installation & Configuration

### Claude Desktop
Add to your `claude_desktop_config.json`:

```json
{
  "mcpServers": {
    "scout-server": {
      "command": "java",
      "args": ["-jar", "/path/to/scout-1.0.{VERSION}.jar"]
    }
  }
}
```

### GitHub Copilot
Add to your `~/.config/github-copilot/intellij/mcp.json`:

```json
{
  "servers": {
    "scout-server": {
      "command": "java",
      "args": ["-jar", "/path/to/scout-1.0.{VERSION}.jar"]
    }
  }
}
```

### Custom MCP Client
The server can be integrated with any MCP-compatible client by configuring the appropriate JSON configuration file.

## Usage Examples

### Discovering Build Systems
```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "tools/call",
  "params": {
    "name": "find_build_system",
    "arguments": {
      "project_root": "/path/to/your/project"
    }
  }
}
```

### Building a Gradle Project
```json
{
  "jsonrpc": "2.0",
  "id": 2,
  "method": "tools/call",
  "params": {
    "name": "build_gradle_project",
    "arguments": {
      "project_root": "/path/to/gradle/project",
      "check": true
    }
  }
}
```

### Listing Dependencies
```json
{
  "jsonrpc": "2.0",
  "id": 3,
  "method": "tools/call",
  "params": {
    "name": "dependencies_list",
    "arguments": {
      "path": "/path/to/build.gradle"
    }
  }
}
```

### Updating Dependency Version
```json
{
  "jsonrpc": "2.0",
  "id": 4,
  "method": "tools/call",
  "params": {
    "name": "update_dependency_version",
    "arguments": {
      "groupId": "org.springframework.boot",
      "artifactId": "spring-boot-starter-web",
      "version": "3.2.0",
      "path": "/path/to/build.gradle"
    }
  }
}
```

## Architecture

### Core Components
- **Main.java** - Entry point and server initialization
- **RequestController** - Routes JSON-RPC requests to appropriate handlers
- **SchemaInitializer** - Registers tools and handlers using annotations
- **IOHandler** - Manages input/output communication
- **BuildSystem** - Core interface for build system operations

### Key Features
- Annotation-based tool registration using `@Schema` annotations
- Virtual thread processing for concurrent request handling
- Gradle Tooling API integration for direct Gradle operations
- Native image support with GraalVM configuration
- Automatic version management and JAR updates

## Development

### Requirements
- Java 21+
- Gradle 8.14+

### Project Structure
```
src/
├── main/
│   ├── java/
│   │   └── com/davidparry/scout/
│   │       ├── Main.java
│   │       ├── RequestController.java
│   │       ├── tools/           # MCP tools implementation
│   │       ├── common/          # Build system implementations
│   │       ├── spec/            # MCP protocol specifications
│   │       └── annotation/      # Schema annotation system
│   └── resources/
│       └── META-INF/native-image/  # GraalVM configuration
└── test/
    └── java/                    # Unit tests
```

### Adding New Tools
1. Create a new class implementing the `Tool<T>` interface
2. Add the `@Schema` annotation with name and description
3. Register the class in `SchemaInitializer.getAnnotatedClasses()`
4. Implement the `schema()` and `action()` methods

### Testing
Run the test suite:
```bash
./gradlew test
```

## Version Management

The build system automatically:
- Increments patch version numbers
- Updates JAR references in MCP client configurations
- Generates version metadata for runtime access

## Troubleshooting

### Common Issues
1. **Build failures** - Check Java version compatibility (requires Java 21+)
2. **Native image issues** - Ensure GraalVM is properly configured
3. **Tool registration** - Verify `@Schema` annotations are present and classes are registered

### Logging
The server includes comprehensive logging at different levels. Check the console output for detailed information about tool execution and errors.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Add tests for new functionality
4. Ensure all tests pass
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.