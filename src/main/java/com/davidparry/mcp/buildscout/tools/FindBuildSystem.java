package com.davidparry.mcp.buildscout.tools;

import com.davidparry.mcp.buildscout.common.BuildFile;
import com.davidparry.mcp.buildscout.common.BuildSystem;
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class FindBuildSystem implements Tool {
    private static final Logger logger = LoggerFactory.getLogger(FindBuildSystem.class);
    private final BuildSystem buildSystem;

    public FindBuildSystem(BuildSystem buildSystem) {
        this.buildSystem = buildSystem;
    }

    @Override
    public McpSchema.CallToolResult handle(Object args) {
        return handleGetCodeContext(args);
    }

    @Override
    public String name() {
        return "find_build_system";
    }

    @Override
    public String description() {
        return "This tool can find the build system given the root directory of the project. if you have more than one directory add it to the paths variable.";
    }

    @Override
    public McpSchema.JsonSchema schema() {
        Map<String, Object> properties = new HashMap<>();
        List<String> required = List.of("paths");
        properties.put("paths", createProperty("array", "Paths to the root directories for the project."));

        return new McpSchema.JsonSchema("object", properties, required, null);

    }

    private McpSchema.CallToolResult handleGetCodeContext(Object args) {
        Set<BuildFile> builds = new HashSet<>();
        try {
            Map<String, List<String>> request = (Map<String, List<String>>) args;
            if (request != null && request.containsKey("paths")) {
                builds = buildSystem.identifyBuildFiles(request.get("paths"));
            }
        } catch (Exception e) {
            logger.error("Failed to process paths", e);
        }
        logger.debug("Args class: {}", args != null ? args.getClass().getName() : "null");
        return buildSystem.createTypeResults(builds);
    }


}
