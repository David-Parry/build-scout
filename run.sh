#!/bin/bash

## MCP path -jar /Users/davidparry/code/github/mcp-servers/build-scout/target/build-scout-1.0.0-SNAPSHOT.jar

# Build the project
mvn clean package

# Run the application
java -jar target/build-scout-1.0.0-SNAPSHOT.jar "$@"
