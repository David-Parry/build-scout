package com.davidparry.mcp.buildscout.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class GetFileInfo extends BuildTool {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(GetFileInfo.class);

    // Static method that delegates to the instance method
    public McpSchema.CallToolResult handle(Object args) {
        return handleGetFileInfo(args);
    }

    @Override
    public McpSchema.JsonSchema schema() {
        addProperty("path", "string", "Path to the file.", true);
        return new McpSchema.JsonSchema("object", getProperties(), getRequired(), null);
    }

    @Override
    public String name() {
        return "get_file_info";
    }

    @Override
    public String description() {
        return "Get information about a file, just simply give a path and it will return the file information for sure.";
    }

    private McpSchema.CallToolResult handleGetFileInfo(Object args) {
        List<McpSchema.Content> results = new ArrayList<>();
        boolean failed = false;
        try {
            ObjectNode result = objectMapper.createObjectNode();

            if (args instanceof ObjectNode argsNode) {
                String path = argsNode.path("path").asText("");

                if (!path.isEmpty()) {
                    java.io.File file = new java.io.File(path);

                    if (file.exists()) {
                        result.put("exists", true);
                        result.put("isDirectory", file.isDirectory());
                        result.put("size", file.length());
                        result.put("lastModified", file.lastModified());
                        result.put("canRead", file.canRead());
                        result.put("canWrite", file.canWrite());
                        result.put("absolutePath", file.getAbsolutePath());
                    } else {
                        result.put("exists", false);
                        result.put("error", "File does not exist");
                    }
                } else {
                    result.put("error", "Path is empty");
                }
            } else if (args instanceof java.util.Map) {
                // Handle Map arguments (which could come from JSON deserialization)
                @SuppressWarnings("unchecked") java.util.Map<String, Object> argsMap = (java.util.Map<String, Object>) args;

                if (argsMap.containsKey("path")) {
                    String path = String.valueOf(argsMap.get("path"));

                    if (path != null && !path.isEmpty()) {
                        java.io.File file = new java.io.File(path);

                        if (file.exists()) {
                            result.put("exists", true);
                            result.put("isDirectory", file.isDirectory());
                            result.put("size", file.length());
                            result.put("lastModified", file.lastModified());
                            result.put("canRead", file.canRead());
                            result.put("canWrite", file.canWrite());
                            result.put("absolutePath", file.getAbsolutePath());
                        } else {
                            result.put("exists", false);
                            result.put("error", "File does not exist");
                        }
                    } else {
                        result.put("error", "Path is empty");
                    }
                } else {
                    result.put("error", "Missing 'path' parameter");
                }
            } else {
                if (args != null) {
                    result.put("error", "DP 1 Invalid arguments type: " + args.getClass().getName());
                } else {
                    result.put("error", "DP 2 Invalid arguments: they are null");
                }
            }

            McpSchema.Content content = new McpSchema.TextContent(objectMapper.writeValueAsString(result));
            results.add(content);

        } catch (Exception e) {
            logger.error("Error handling get-file-info tool call", e);
            McpSchema.Content content = new McpSchema.TextContent("Failed " + e.getMessage());
            failed = true;
            results.add(content);
        }
        return new McpSchema.CallToolResult(results, failed);
    }

}
