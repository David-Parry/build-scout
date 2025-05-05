package com.davidparry.mcp.buildscout.tools;

import com.davidparry.mcp.buildscout.common.DiffData;
import com.davidparry.mcp.buildscout.common.JarComparatorService;
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JarDiffReporter extends BuildTool {
    private static final Logger logger = LoggerFactory.getLogger(JarDiffReporter.class);
    private final JarComparatorService jarComparatorService;

    public JarDiffReporter(JarComparatorService jarComparatorService) {
        this.jarComparatorService = jarComparatorService;
    }

    @Override
    public McpSchema.CallToolResult handle(Object args) {
        return handleDiffTool(args);
    }

    @Override
    public String name() {
        return "version_change_analyzer";
    }

    @Override
    public String description() {
        return "Given the groupId, artifactId current version and the latest version of the artifact will return the changes in the public code between the two versions. This allows for a other agents to then check if the code will need to be updated based on the changes.";
    }

    @Override
    public McpSchema.JsonSchema schema() {
        addProperty("groupId", "string", "The maven group id used in maven dependency repository.", true);
        addProperty("artifactId", "string", "The maven artifact Id used in the maven dependency repository.", true);
        addProperty("latestVersion", "string", "Current version of the artifact.", true);
        addProperty("currentVersion", "string", "The latest version of the artifact.", true);
        return new McpSchema.JsonSchema("object", getProperties(), getRequired(), null);
    }

    /**
     * Validates and extracts request parameters from the input arguments.
     *
     * @param args The input arguments containing request parameters
     * @return A Map containing the validated parameters or null if validation fails
     */
    private Map<String, String> validateAndExtractParameters(Object args) {
        if (args == null) {
            logger.warn("Request arguments are null");
            return null;
        }

        try {
            Map<String, String> request = (Map<String, String>) args;
            if (request == null) {
                logger.warn("Request map is null");
                return null;
            }

            Map<String, String> validatedParams = new HashMap<>();
            String[] requiredParams = {"groupId", "artifactId", "currentVersion", "latestVersion"};

            for (String param : requiredParams) {
                if (!request.containsKey(param)) {
                    logger.warn("Missing required parameter: {}", param);
                    return null;
                }

                String value = request.get(param);
                if (value == null || value.trim().isEmpty()) {
                    logger.warn("Parameter {} has null or empty value", param);
                    return null;
                }

                validatedParams.put(param, value);
            }

            return validatedParams;
        } catch (ClassCastException e) {
            logger.error("Failed to cast args to Map<String, String>", e);
            return null;
        }
    }


    private McpSchema.CallToolResult handleDiffTool(Object args) {
        logger.debug("Args class: {}", args != null ? args.getClass().getName() : "null");
        List<McpSchema.Content> results = new ArrayList<>();
        boolean error = true;

        try {
            Map<String, String> validatedParams = validateAndExtractParameters(args);
            if (validatedParams == null) {
                results.add(new McpSchema.TextContent("Invalid or missing parameters in request"));
                return new McpSchema.CallToolResult(results, true);
            }

            List<DiffData> changedClasses = jarComparatorService.compareJars(validatedParams);

            for (DiffData change : changedClasses) {
                results.add(new McpSchema.TextContent("class:" + change.clazz() + " changeStatus:" + change.changeStatus()));
            }
            error = false;
        } catch (Exception e) {
            results.add(new McpSchema.TextContent("Error finding diffs " + e.getMessage()));
            logger.error("Failed to find diffs on files ", e);
        }
        return new McpSchema.CallToolResult(results, error);

    }

}
