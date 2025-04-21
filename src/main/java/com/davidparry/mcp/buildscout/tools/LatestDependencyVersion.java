package com.davidparry.mcp.buildscout.tools;

import com.davidparry.mcp.buildscout.common.DependencyFetch;
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LatestDependencyVersion implements Tool {
    private static final Logger logger = LoggerFactory.getLogger(LatestDependencyVersion.class);
    private final DependencyFetch dependencyFetch;

    public LatestDependencyVersion(DependencyFetch dependencyFetch) {
        this.dependencyFetch = dependencyFetch;
    }

    @Override
    public McpSchema.CallToolResult handle(Object args) {
        return handleVersionLookUp(args);
    }

    @Override
    public String name() {
        return "latest_dependency_version";
    }

    @Override
    public String description() {
        return "Given the groupId and artifactId, this tool will return the latest version of this maven dependency.";
    }

    @Override
    public String schema() {
        return "{\"type\":\"object\",\"properties\":{\"groupId\":{\"type\": \"string\",\"description\": \"the maven group id used in maven dependency repository.\"},\"artifactId\":{\"type\": \"string\",\"description\":\"The maven artifact Id used in the maven dependency repository.\"}},\"required\":[\"groupId\",\"artifactId\"]}";
    }

    private McpSchema.CallToolResult handleVersionLookUp(Object args) {
        List<McpSchema.Content> results = new ArrayList<>();
        boolean error = true;
        String groupId = null;
        String artifactId = null;
        try {
            Map<String, String> request = (Map<String, String>) args;
            if (request != null) {
                groupId = request.get("groupId");
                artifactId = request.get("artifactId");
                String version = dependencyFetch.lookupLatestVersion(groupId, artifactId);

                if (groupId == null || groupId.isEmpty()) {
                    results.add(new McpSchema.TextContent("GroupId is missing!"));
                } else if (artifactId == null || artifactId.isEmpty()) {
                    results.add(new McpSchema.TextContent("ArtifactID is missing!"));
                } else {
                    error = false;
                    results.add(new McpSchema.TextContent(version));
                }
            }
        } catch (Exception e) {
            logger.error("Failed to process maven repo dependencies version ", e);
            results.add(new McpSchema.TextContent("Failed to lookup latest version for groupID " + groupId + " artifactID" + artifactId + " error message " + e.getMessage()));
        }
        return new McpSchema.CallToolResult(results, error);
    }
}
