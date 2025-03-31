package com.davidparry.mcp.buildscout.tools;

import io.modelcontextprotocol.spec.McpSchema;
import java.util.function.Function;

/**
 * Interface for tools that can be used in the MCP Explorer.
 */
@FunctionalInterface
public interface Tool {
  /**
   * Handle a tool call with the given arguments.
   *
   * @param args The arguments for the tool call
   * @return The result of the tool call
   */
  McpSchema.CallToolResult handle(Object args);
  
  /**
   * Get the name of the tool.
   *
   * @return The tool name
   */
  default String name() {
    return getClass().getSimpleName().toLowerCase();
  }
  
  /**
   * Get the description of the tool.
   *
   * @return The tool description
   */
  default String description() {
    return "Tool for " + name();
  }
  
  /**
   * Get the JSON schema for the tool's arguments.
   *
   * @return The JSON schema
   */
  default String schema() {
    return "{}";
  }
  
  /**
   * Get the handler function for this tool.
   *
   * @return A function that handles tool calls
   */
  default Function<Object, McpSchema.CallToolResult> handleFunction() {
    return this::handle;
  }
}
