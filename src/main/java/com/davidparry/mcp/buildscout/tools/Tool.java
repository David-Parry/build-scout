package com.davidparry.mcp.buildscout.tools;

import io.modelcontextprotocol.spec.McpSchema;

import java.util.LinkedHashMap;
import java.util.Map;
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
   * Get the JSON Schema for this tool.
   *
   * @return The JSON schema
   */
  default McpSchema.JsonSchema schema() {
    return null;
  }


  /**
   * Helper method to create a property map for JSON schema.
   *
   * @param type        the type of the property (e.g., "string")
   * @param description the description of the property
   * @return a LinkedHashMap representing the property
   */
  default Map<String, Object> createProperty(String type, Object description) {
    Map<String, Object> property = new LinkedHashMap<>();
    property.put("type", type);
    property.put("description", description);
    return property;
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
