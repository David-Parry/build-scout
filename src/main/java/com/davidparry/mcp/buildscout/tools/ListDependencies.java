package com.davidparry.mcp.buildscout.tools;

import com.davidparry.mcp.buildscout.common.DependencyFetch;
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ListDependencies extends BuildTool {
    private static final Logger logger = LoggerFactory.getLogger(ListDependencies.class);
    private final DependencyFetch dependencyFetch;

    public ListDependencies(DependencyFetch dependencyFetch) {
        this.dependencyFetch = dependencyFetch;
    }

    @Override
    public McpSchema.CallToolResult handle(Object args) {
        return handleListDependencies(args);
    }

    @Override
    public String name() {
        return "dependencies_list";
    }

    @Override
    public String description() {
        return "Given the build file path, this tool will list all the top level dependencies of the project. With the fully qualified name of the dependency, it will return the version of the dependency. Example for Gradle: 'org.springframework.boot:spring-boot-starter-jdbc:3.4.4' ";
    }

    @Override
    public McpSchema.JsonSchema schema() {
        addProperty("path", "string", "The absolute path to the build file.", true);
        return new McpSchema.JsonSchema("object", getProperties(), getRequired(), null);
    }

    private McpSchema.CallToolResult handleListDependencies(Object args) {
        List<McpSchema.Content> results = new ArrayList<>();
        boolean error = true;
        try {
            Map<String, String> request = (Map<String, String>) args;
            if (request != null) {
                String path = request.get("path");
                String sessionId = request.get("session_id");
                logger.debug("THE SESSION ID is -:{}::--------", sessionId);
                if (path == null || path.isEmpty()) {
                    results.add(new McpSchema.TextContent("Absolute Path to the build file is missing!"));
                } else {
                    List<String> dependencies = dependencyFetch.resolveDependencies(path);
                    error = false;
                    dependencies.forEach(d -> results.add(new McpSchema.TextContent(d)));
                }
            }
        } catch (Exception e) {
            logger.error("Failed to process paths", e);
        }
        return new McpSchema.CallToolResult(results, error);
    }
}
