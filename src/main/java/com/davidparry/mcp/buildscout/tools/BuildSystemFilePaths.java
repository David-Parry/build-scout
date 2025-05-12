package com.davidparry.mcp.buildscout.tools;

import com.davidparry.mcp.buildscout.common.BuildFile;
import com.davidparry.mcp.buildscout.common.BuildSystem;
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BuildSystemFilePaths extends BuildTool {
    private static final Logger logger = LoggerFactory.getLogger(BuildSystemFilePaths.class);
    private final BuildSystem buildSystem;

    public BuildSystemFilePaths(BuildSystem buildSystem) {
        this.buildSystem = buildSystem;
    }

    @Override
    public McpSchema.CallToolResult handle(Object args) {
        return handleGetCodeContext(args);
    }

    @Override
    public String name() {
        return "build_system_file_paths";
    }

    @Override
    public String description() {
        return "This tool will return a single absolute path to the build system file that is present in the project, if there are multiple build system files it will return multiple text responses with the absolute file paths, given the root directory of the project.";
    }

    @Override
    public McpSchema.JsonSchema schema() {
        addProperty("paths", "string", "Paths to the root directories for the project.", true);
        return new McpSchema.JsonSchema("object", getProperties(), getRequired(), null);
    }

    private McpSchema.CallToolResult handleGetCodeContext(Object args) {
        Set<BuildFile> builds = new HashSet<>(); // Using Set to avoid duplicates
        try {
            Map<String, List<String>> request = (Map<String, List<String>>) args;
            if (request != null && request.containsKey("paths")) {
                builds = buildSystem.identifyBuildFiles(request.get("paths"));
            }
        } catch (Exception e) {
            logger.error("Failed to process paths", e);
        }

        logger.debug("Args class: {}", args != null ? args.getClass().getName() : "null");
        return buildSystem.createPathResults(builds);
    }


}
