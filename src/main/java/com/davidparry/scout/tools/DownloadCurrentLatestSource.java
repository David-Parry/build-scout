package com.davidparry.scout.tools;

import com.davidparry.scout.annotation.Schema;
import com.davidparry.scout.common.BuildSystemImpl;
import com.davidparry.scout.common.DependencyFetch;
import com.davidparry.scout.io.ApplicationLogger;
import com.davidparry.scout.io.Logger;
import com.davidparry.scout.spec.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Schema(name = "download_source_dependencies", description = "Given the groupId, artifactId and version will download the source of the version given and will lookup and download the latest version of that dependency if it is present. It will return the current: and latest: prefixed file names when they are downloaded.")
public class DownloadCurrentLatestSource extends BuildTool implements Tool<ToolOutputResponse> {
    private static final Logger logger = ApplicationLogger.getInstance();
    private final DependencyFetch dependencyFetch;

    public DownloadCurrentLatestSource(DependencyFetch dependencyFetch) {
        this.dependencyFetch = dependencyFetch;
    }

    public DownloadCurrentLatestSource() {
        this(new DependencyFetch(new BuildSystemImpl()));
    }

    @Override
    public InputSchema schema() {
        logger.log("DownloadCurrentLatestSource schema Schema being created and returned");
        addProperty("groupId", new InputProperty("string", "The maven group id used in maven dependency repository."), true);
        addProperty("artifactId", new InputProperty("string", "The maven artifact Id used in the maven dependency repository."), true);
        addProperty("version", new InputProperty("string", "The current version of the dependency to download its source."), true);
        return new InputSchema("object", getProperties(), getRequired());

    }

    @Override
    public ToolOutputResponse action(JsonRpcRequest request) {
        List<Content> results = new ArrayList<>();
        boolean error = true;
        String groupId = null;
        String artifactId = null;
        String latestVersion;
        String version = null;
        try {
            if (request != null) {
                groupId = (String) request.params().arguments().get("groupId");
                artifactId = (String) request.params().arguments().get("artifactId");
                latestVersion = dependencyFetch.lookupLatestVersion(groupId, artifactId);
                version = (String) request.params().arguments().get("version");

                if (groupId == null || groupId.isEmpty()) {
                    return createErrorResult("GroupId is missing!");
                } else if (artifactId == null || artifactId.isEmpty()) {
                    return createErrorResult("ArtifactID is missing!");
                } else if (version == null || version.isEmpty()) {
                    return createErrorResult("Version is missing!");
                }
                Path currentVersion = dependencyFetch.downloadSourceJar(groupId, artifactId, version, "current");
                if (currentVersion != null) {
                    error = false;
                    results.add(new Content(currentVersion.toAbsolutePath().toString()));
                    if (version != null && latestVersion != null && !latestVersion.equals(version)) {
                        Path latestPah = dependencyFetch.downloadSourceJar(groupId, artifactId, latestVersion, "latest");
                        results.add(new Content(latestPah.toAbsolutePath().toString()));
                    }
                } else {
                    results.add(new Content("Failed to download source jar! for groupId: " + groupId + ", artifactId: " + artifactId + ", version: " + version));
                }
            }
        } catch (Exception e) {
            logger.log("Failed to process maven repo dependencies version ", e);
            return createErrorResult("Failed to lookup version for groupID " + groupId + " artifactID" + artifactId + " version" + version + " error message " + e.getMessage());
        }
        return new ToolOutputResponse(results, error);
    }
}
