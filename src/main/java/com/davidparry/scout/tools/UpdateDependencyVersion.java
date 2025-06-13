package com.davidparry.scout.tools;

import com.davidparry.scout.annotation.Schema;
import com.davidparry.scout.common.ArgumentUtils;
import com.davidparry.scout.common.BuildSystem;
import com.davidparry.scout.common.BuildSystemImpl;
import com.davidparry.scout.handlers.Handler;
import com.davidparry.scout.handlers.HandlerResponse;
import com.davidparry.scout.io.ApplicationLogger;
import com.davidparry.scout.io.LogFileWriter;
import com.davidparry.scout.io.Logger;
import com.davidparry.scout.spec.*;

import java.util.ArrayList;
import java.util.List;

@Schema(name = "update_dependency_version", description = "Given the groupId, artifactId, version and file path to build system main file, this tool will update this dependency in the build system file and save the file.")
public class UpdateDependencyVersion extends BuildTool implements Tool, Handler {
    private static final Logger logger =ApplicationLogger.getLogger(LogFileWriter.getInstance());
    private final BuildSystem buildSystem;

    public UpdateDependencyVersion(BuildSystem buildSystem) {
        this.buildSystem = buildSystem;
    }

    public UpdateDependencyVersion() {
        this(new BuildSystemImpl());
    }

    @Override
    public InputSchema schema() {
        logger.log("UpdateDependencyVersion schema Schema being created and returned");
        addProperty(new InputProperty("groupId", "string", "The maven group id used in maven dependency repository.", true));
        addProperty(new InputProperty("artifactId", "string", "The maven artifact Id used in the maven dependency repository.", true));
        addProperty(new InputProperty("version", "string", "The version that you want this tool to update the dependency too.", true));
        addProperty(new InputProperty("path", "string", "The absolute path to the build file.", true));
        return new InputSchema("object", getProperties(), getRequired());
    }

    @Override
    public ToolOutputResponse action(JsonRpcRequest request) {
        List<Content> results = new ArrayList<>();
        boolean error = true;
        String groupId = null;
        String artifactId = null;
        String path = null;
        String version = null;
        try {
            if (request != null) {
                groupId = ArgumentUtils.getArgument(request, "groupId");
                artifactId = ArgumentUtils.getArgument(request, "artifactId");
                version = ArgumentUtils.getArgument(request, "version");
                path = ArgumentUtils.getArgument(request, "path");

                if (groupId == null || groupId.isEmpty()) {
                    results.add(new Content("GroupId is missing!"));
                } else if (artifactId == null || artifactId.isEmpty()) {
                    results.add(new Content("ArtifactID is missing!"));
                } else if (path == null || path.isEmpty()) {
                    results.add(new Content("path is missing!"));
                } else if (version == null || version.isEmpty()) {
                    results.add(new Content("version is missing!"));
                } else {
                    error = false;
                    results.add(new Content(buildSystem.updateDependencyVersion(groupId, artifactId, version, path)));
                }
            }
        } catch (Exception e) {
            logger.log("Failed to process maven repo dependencies version ", e);
            return createErrorResult("Failed to lookup latest version for groupID " + groupId + " artifactID " + artifactId + " version " + version + " path " + path + " error message " + e.getMessage());
        }
        return new ToolOutputResponse(results, error);
    }

    @Override
    public HandlerResponse handle(JsonRpcRequest request) {
        return new HandlerResponse(action(request));
    }
}
