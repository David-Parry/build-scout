package com.davidparry.mcp.buildscout.tools;

import com.davidparry.mcp.buildscout.common.SourceClassUsageService;
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class FindClassUsage implements Tool {
    private static final Logger logger = LoggerFactory.getLogger(FindClassUsage.class);
    private SourceClassUsageService service;

    public FindClassUsage(SourceClassUsageService service) {
        this.service = service;
    }

    @Override
    public McpSchema.CallToolResult handle(Object args) {
        return handleFindClassUsage(args);
    }

    @Override
    public String name() {
        return "find_class_usage";
    }

    @Override
    public String description() {
        return "Given the fully qualified path of the root directory of the project and the fully qualified class name this tool will search the projects source code for the classes usage and return file path that uses this class and line numbers where it is used.";
    }

    @Override
    public String schema() {
        return "{\"type\":\"object\",\"properties\":{\"fullyQualifiedClassName\":{\"type\": \"string\",\"description\": \"the fully qualified class name of the class.\"},\"projectRoot\":{\"type\": \"string\",\"description\":\"The fully qualified path of the root directory of the project.\"}},\"required\":[\"fullyQualifiedClassName\",\"rootDirectoryPath\"]}";
    }

    private McpSchema.CallToolResult handleFindClassUsage(Object args) {
        List<McpSchema.Content> results = new ArrayList<>();
        boolean error = false;

        try {
            if (!(args instanceof Map)) {
                return createErrorResult("Invalid arguments format");
            }

            Map<String, Object> argsMap = (Map<String, Object>) args;
            String fullyQualifiedClassName = (String) argsMap.get("fullyQualifiedClassName");
            String projectRoot = (String) argsMap.get("projectRoot");

            if (fullyQualifiedClassName == null || fullyQualifiedClassName.isEmpty()) {
                return createErrorResult("Missing fully qualified class name");
            }

            if (projectRoot == null || projectRoot.isEmpty()) {
                return createErrorResult("Missing project root path");
            }

            // Find source directories
            List<Path> sourceDirs = service.findSourceDirectories(projectRoot);
            if (sourceDirs.isEmpty()) {
                return createErrorResult("No source directories found in the project");
            }

            Map<String, Set<Integer>> usageMap = service.searchClassUsage(sourceDirs, fullyQualifiedClassName);

            if (usageMap.isEmpty()) {
                results.add(new McpSchema.TextContent("No usages found for class: " + fullyQualifiedClassName));
            } else {
                StringBuilder resultBuilder = new StringBuilder();
                resultBuilder.append("Found usages of ").append(fullyQualifiedClassName).append(":\n\n");

                for (Map.Entry<String, Set<Integer>> entry : usageMap.entrySet()) {
                    resultBuilder.append("File: ").append(entry.getKey()).append("\n");
                    resultBuilder.append("Lines: ").append(entry.getValue().stream().map(String::valueOf).collect(Collectors.joining(", "))).append("\n\n");
                }

                results.add(new McpSchema.TextContent(resultBuilder.toString()));
            }
        } catch (Exception e) {
            logger.error("Error finding class usage", e);
            return createErrorResult("Error finding class usage: " + e.getMessage());
        }

        return new McpSchema.CallToolResult(results, error);
    }

    private McpSchema.CallToolResult createErrorResult(String errorMessage) {
        List<McpSchema.Content> results = new ArrayList<>();
        results.add(new McpSchema.TextContent("Error: " + errorMessage));
        return new McpSchema.CallToolResult(results, true);
    }


}
