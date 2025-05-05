package com.davidparry.mcp.buildscout.tools;

import com.davidparry.mcp.buildscout.common.BuildSystem;
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UpdateDependencyVersion extends BuildTool {
    private static final Logger logger = LoggerFactory.getLogger(UpdateDependencyVersion.class);
    private final BuildSystem buildSystem;

    public UpdateDependencyVersion(BuildSystem buildSystem) {
        this.buildSystem = buildSystem;
    }

    @Override
    public McpSchema.CallToolResult handle(Object args) {
        // looking for a session id
        logger.error("session_id"); // <-- this stores the session
        if (args instanceof Map) {
            Map<String, String> request = (Map<String, String>) args;
            request.keySet().forEach(key -> {
                logger.error(key + " : " + request.get(key));

            });
        }
        return handleVersionLookUp(args);
    }

    @Override
    public String name() {
        return "update_dependency_version";
    }

    @Override
    public String description() {
        return "Given the groupId, artifactId, version and file path to build system main file, this tool will update this dependency in the build system file and save the file.";
    }

    @Override
    public McpSchema.JsonSchema schema() {
        addProperty("groupId", "string", "The maven group id used in maven dependency repository.", true);
        addProperty("artifactId", "string", "The maven artifact Id used in the maven dependency repository.", true);
        addProperty("version", "string", "The version that you want this tool to update the dependency too.", true);
        addProperty("path", "string", "The absolute path to the build file.", true);
        return new McpSchema.JsonSchema("object", getProperties(), getRequired(), null);
    }


    private McpSchema.CallToolResult handleVersionLookUp(Object args) {
        List<McpSchema.Content> results = new ArrayList<>();
        boolean error = true;
        String groupId = null;
        String artifactId = null;
        String path = null;
        String version = null;
        try {
            Map<String, String> request = (Map<String, String>) args;
            if (request != null) {
                groupId = request.get("groupId");
                artifactId = request.get("artifactId");
                version = request.get("version");
                path = request.get("path");

                if (groupId == null || groupId.isEmpty()) {
                    results.add(new McpSchema.TextContent("GroupId is missing!"));
                } else if (artifactId == null || artifactId.isEmpty()) {
                    results.add(new McpSchema.TextContent("ArtifactID is missing!"));
                } else if (path == null || path.isEmpty()) {
                    results.add(new McpSchema.TextContent("path is missing!"));
                } else if (version == null || version.isEmpty()) {
                    results.add(new McpSchema.TextContent("version is missing!"));
                } else {
                    error = false;
                    results.add(new McpSchema.TextContent(buildSystem.updateDependencyVersion(groupId, artifactId, version, path)));
                }
            }
        } catch (Exception e) {
            logger.error("Failed to process maven repo dependencies version ", e);
            results.add(new McpSchema.TextContent("Failed to lookup latest version for groupID " + groupId + " artifactID " + artifactId + " version " + version + " path " + path + " error message " + e.getMessage()));
        }
        return new McpSchema.CallToolResult(results, error);
    }


}
