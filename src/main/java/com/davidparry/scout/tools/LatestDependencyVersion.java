package com.davidparry.scout.tools;

import com.davidparry.scout.common.ArgumentUtils;
import com.davidparry.scout.common.DependencyFetch;
import com.davidparry.scout.common.LogFactory;
import com.davidparry.scout.handlers.Handler;
import com.davidparry.scout.handlers.HandlerResponse;
import com.davidparry.scout.io.ApplicationLogger;
import com.davidparry.scout.io.LogFileWriter;
import com.davidparry.scout.io.Logger;
import com.davidparry.scout.spec.*;
import com.davidparry.scout.spec.Tool;

import java.util.ArrayList;
import java.util.List;

public class LatestDependencyVersion extends BuildTool implements Handler {
    private final Logger logger = new ApplicationLogger().getLogger(LogFileWriter.getInstance(new LogFactory()));
    private final DependencyFetch dependencyFetch;
    private final Tool tool;

    public LatestDependencyVersion(DependencyFetch dependencyFetch) {
        this.dependencyFetch = dependencyFetch;
        this.tool = new Tool("latest_dependency_version", "Given the groupId and artifactId, this tool will return the latest version of this maven dependency.", schema());
    }

    public InputSchema schema() {
        logger.log("LatestDependencyVersion schema Schema being created and returned");
        addProperty(new InputProperty("groupId", "string", "The maven group id used in maven dependency repository.", true));
        addProperty(new InputProperty("artifactId", "string", "The maven artifact Id used in the maven dependency repository.", true));
        return new InputSchema("object", getProperties(), getRequired());
    }

    public ToolOutputResponse action(JsonRpcRequest request) {
        List<Content> results = new ArrayList<>();
        boolean error = true;
        String groupId = null;
        String artifactId = null;
        String version = null;
        try {
            if (request != null) {
                groupId = ArgumentUtils.getArgument(request, "groupId");
                artifactId = ArgumentUtils.getArgument(request, "artifactId");
                if (groupId == null || groupId.isEmpty()) {
                    results.add(new Content("GroupId is missing!"));
                } else if (artifactId == null || artifactId.isEmpty()) {
                    results.add(new Content("ArtifactID is missing!"));
                } else {
                    version = dependencyFetch.lookupLatestVersion(groupId, artifactId);
                }
                if (version != null && !version.isEmpty()) {
                    error = false;
                    results.add(new Content(version));
                } else {
                    results.add(new Content("Cannot retrieve the latest version of this maven dependency!"));
                }
            }
        } catch (Exception e) {
            logger.log("Failed to process maven repo dependencies version ", e);
            return createErrorResult("Failed to lookup latest version for groupID " + groupId + " artifactID" + artifactId + " error message " + e.getMessage());
        }
        return new ToolOutputResponse(results, error);
    }

    @Override
    public HandlerResponse handle(JsonRpcRequest request) {
        return new HandlerResponse(action(request));
    }

    public Tool tool() {
        return this.tool;
    }
}
