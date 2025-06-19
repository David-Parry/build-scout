package com.davidparry.scout.tools;

import com.davidparry.scout.common.BuildFile;
import com.davidparry.scout.common.BuildSystem;
import com.davidparry.scout.common.LogFactory;
import com.davidparry.scout.handlers.Handler;
import com.davidparry.scout.handlers.HandlerResponse;
import com.davidparry.scout.io.ApplicationLogger;
import com.davidparry.scout.io.LogFileWriter;
import com.davidparry.scout.io.Logger;
import com.davidparry.scout.spec.*;
import com.davidparry.scout.spec.Tool;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FindBuildSystem extends BuildTool implements Handler {
    private static final Logger logger = new ApplicationLogger().getLogger(LogFileWriter.getInstance(new LogFactory()));
    private final Tool tool;
    private final BuildSystem buildSystem;

    public FindBuildSystem(BuildSystem buildSystem) {
        this.buildSystem = buildSystem;
        this.tool = new Tool("find_build_system", "This tool can find the build system given the root directory of the project.", schema());
    }

    public Tool getTool() {
        return tool;
    }

    public ToolOutputResponse action(JsonRpcRequest args) {
        Set<BuildFile> builds = new HashSet<>();
        logger.log("Finding build system using " + args);
        try {
            Set<File> files = getProjectRoots(args);
            logger.log("FindBuildSystem Root directories " + files.size() + " projects in " + files);
            builds = this.buildSystem.onlyBuildFilesFilter(files);
        } catch (Exception e) {
            logger.log("Error finding the build path", e);
            return createErrorResult("Error finding the build path; " + e.getMessage());
        }
        return createTypeResults(builds);
    }

    public ToolOutputResponse createTypeResults(Set<BuildFile> builds) {
        List<Content> results = new ArrayList<>();
        boolean error = true;
        if (builds.isEmpty()) {
            results.add(new Content("No build system found"));
        } else {
            error = false;
            for (BuildFile file : builds) {
                results.add(new Content(file.type()));
            }
        }
        return new ToolOutputResponse(results, error);
    }

    public InputSchema schema() {
        logger.log("FindBuildSystem schema Schema being created and returned");
        addProperty(new InputProperty(PROJECT_ROOT, "string", "Path to the root directories for the project.", rootProjectMandatory()));
        return new InputSchema("object", getProperties(), getRequired());
    }

    @Override
    public HandlerResponse handle(JsonRpcRequest request) {
        return new HandlerResponse(action(request));
    }

    public Tool tool() {
        return this.tool;
    }
}
