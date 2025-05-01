package com.davidparry.mcp.buildscout.tools;

import com.davidparry.mcp.buildscout.common.DependencyFetch;
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListDependencies implements Tool {
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
        return "Given the contents of a build file and build type, this tool will list all the top level dependencies of the project. With the fully qualified name of the dependency, it will return the version of the dependency. Example for Gradle: 'org.springframework.boot:spring-boot-starter-jdbc:3.4.4' ";
    }

    @Override
    public McpSchema.JsonSchema schema() {
        Map<String, Object> properties = new HashMap<>();
        List<String> required = List.of("contents", "build");
        properties.put("contents", createProperty("string", "The file contents of a build file that can be read and used to return the dependencies."));
        properties.put("build", createProperty("string", "The type of build system (e.g., Maven, Gradle, NPM, etc...)."));

        return new McpSchema.JsonSchema("object", properties, required, null);
    }

    private McpSchema.CallToolResult handleListDependencies(Object args) {
        List<McpSchema.Content> results = new ArrayList<>();
        boolean error = true;
        try {
            Map<String, String> request = (Map<String, String>) args;
            if (request != null) {
                String contents = request.get("contents");
                String buildType = request.get("build");

                List<String> dependencies = dependencyFetch.resolveDependencies(contents, buildType);

                if (contents == null || contents.isEmpty()) {
                    results.add(new McpSchema.TextContent("Contents of build file is missing!"));
                } else if (buildType == null || buildType.isEmpty()) {
                    results.add(new McpSchema.TextContent("Build type of file is missing!"));
                } else {
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
