package com.davidparry.mcp.buildscout.tools;

import com.davidparry.mcp.buildscout.common.DependencyFetch;
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DownloadCurrentLatestSource implements Tool {
    private static final Logger logger = LoggerFactory.getLogger(DownloadCurrentLatestSource.class);
    private final DependencyFetch dependencyFetch;

    public DownloadCurrentLatestSource(DependencyFetch dependencyFetch) {
        this.dependencyFetch = dependencyFetch;
    }

    @Override
    public McpSchema.CallToolResult handle(Object args) {
        return handleDownloadSource(args);
    }

    @Override
    public String name() {
        return "download_source_dependencies";
    }

    @Override
    public String description() {
        return "Given the groupId, artifactId and version will download the source of the version given and will lookup and download the latest version of that dependency if it is present. It will return the current: and latest: prefixed file names when they are downloaded.";
    }

    @Override
    public McpSchema.JsonSchema schema() {
        Map<String, Object> properties = new HashMap<>();
        List<String> required = List.of("groupId", "artifactId", "version");
        properties.put("groupId", createProperty("string", "the maven group id used in maven dependency repository."));
        properties.put("artifactId", createProperty("string", "The maven artifact Id used in the maven dependency repository."));
        properties.put("version", createProperty("string", "The current version of the dependency to download its source."));
        return new McpSchema.JsonSchema("object", properties, required, null);
    }

    private McpSchema.CallToolResult handleDownloadSource(Object args) {
        List<McpSchema.Content> results = new ArrayList<>();
        boolean error = true;
        String groupId = null;
        String artifactId = null;
        String latestVersion;
        String version = null;
        try {
            Map<String, String> request = (Map<String, String>) args;
            if (request != null) {
                groupId = request.get("groupId");
                artifactId = request.get("artifactId");
                latestVersion = dependencyFetch.lookupLatestVersion(groupId, artifactId);
                version = request.get("version");

                if (groupId == null || groupId.isEmpty()) {
                    results.add(new McpSchema.TextContent("GroupId is missing!"));
                } else if (artifactId == null || artifactId.isEmpty()) {
                    results.add(new McpSchema.TextContent("ArtifactID is missing!"));
                } else if (version == null || version.isEmpty()) {
                    results.add(new McpSchema.TextContent("Version is missing!"));
                }
                Path currentVersion = dependencyFetch.downloadSourceJar(groupId, artifactId, version, "current");
                if (currentVersion != null) {
                    error = false;
                    results.add(new McpSchema.TextContent(currentVersion.toAbsolutePath().toString()));
                    if (version != null && latestVersion != null && !latestVersion.equals(version)) {
                        Path latestPah = dependencyFetch.downloadSourceJar(groupId, artifactId, latestVersion, "latest");
                        results.add(new McpSchema.TextContent(latestPah.toAbsolutePath().toString()));
                    }
                } else {
                    results.add(new McpSchema.TextContent("Failed to download source jar! for groupId: " + groupId + ", artifactId: " + artifactId + ", version: " + version));
                }
            }
        } catch (Exception e) {
            logger.error("Failed to process maven repo dependencies version ", e);
            results.add(new McpSchema.TextContent("Failed to lookup version for groupID " + groupId + " artifactID" + artifactId + " version" + version + " error message " + e.getMessage()));
        }
        return new McpSchema.CallToolResult(results, error);
    }
}
