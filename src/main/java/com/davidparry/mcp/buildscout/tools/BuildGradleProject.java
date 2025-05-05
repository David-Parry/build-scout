package com.davidparry.mcp.buildscout.tools;

import com.davidparry.mcp.buildscout.common.BuildOutput;
import com.davidparry.mcp.buildscout.common.GradleTasks;
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BuildGradleProject extends BuildTool {
    private static final Logger logger = LoggerFactory.getLogger(BuildGradleProject.class);
    private final GradleTasks service;

    public BuildGradleProject(GradleTasks service) {
        this.service = service;
    }

    @Override
    public McpSchema.CallToolResult handle(Object args) {
        return handleFindClassUsage(args);
    }

    @Override
    public String name() {
        return "build_gradle_project";
    }

    @Override
    public String description() {
        return "Given the fully qualified path of the root directory of the gradle project, this tool will invoke the build and return the result with both success and errors if isError is true.";
    }

    @Override
    public McpSchema.JsonSchema schema() {
        addProperty("projectRoot", "string", "The fully qualified path of the root directory of the project.", true);
        return new McpSchema.JsonSchema("object", getProperties(), getRequired(), null);
    }

    private McpSchema.CallToolResult handleFindClassUsage(Object args) {
        boolean error = true;
        List<McpSchema.Content> results = new ArrayList<>();
        try {
            if (!(args instanceof Map)) {
                return createErrorResult("Invalid arguments format");
            }
            Map<String, Object> argsMap = (Map<String, Object>) args;
            String projectRoot = (String) argsMap.get("projectRoot");

            if (projectRoot == null || projectRoot.isEmpty()) {
                return createErrorResult("Missing project root path");
            }
            File projectDir = new File(projectRoot);
            if (!projectDir.exists() || !projectDir.isDirectory()) {
                return createErrorResult("Project root directory does not exist or is not a directory");
            }
            BuildOutput output = service.buildGradleProject(projectDir);
            results.add(new McpSchema.TextContent(service.formatOutput(output)));
            error = output.failed();
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
