package com.davidparry.scout.tools;

import com.davidparry.scout.annotation.Schema;
import com.davidparry.scout.common.BuildFile;
import com.davidparry.scout.common.BuildSystem;
import com.davidparry.scout.common.BuildSystemImpl;
import com.davidparry.scout.io.ApplicationLogger;
import com.davidparry.scout.io.Logger;
import com.davidparry.scout.spec.InputProperty;
import com.davidparry.scout.spec.InputSchema;
import com.davidparry.scout.spec.JsonRpcRequest;
import com.davidparry.scout.spec.ToolOutputResponse;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

@Schema(name = "find_build_system", description = "This tool can find the build system given the root directory of the project.")
public class FindBuildSystem extends BuildTool implements Tool<ToolOutputResponse> {
    private static final Logger logger = ApplicationLogger.getInstance();
    private final BuildSystem buildSystem;

    public FindBuildSystem(BuildSystem buildSystem) {
        this.buildSystem = buildSystem;
    }

    public FindBuildSystem() {
        this(new BuildSystemImpl());
    }


    @Override
    public ToolOutputResponse action(JsonRpcRequest args) {
        Set<BuildFile> builds = new HashSet<>();
        try {
            Set<File> files = getProjectRoots(args);
            logger.log("Root directories " + files.size() + " projects in " + files);
            builds = buildSystem.onlyBuildFilesFilter(files);
        } catch (Exception e) {
            logger.log("Error finding the build path", e);
            return createErrorResult("Error finding the build path; " + e.getMessage());
        }
        return buildSystem.createTypeResults(builds);
    }


    @Override
    public InputSchema schema() {
        logger.log("FindBuildSystem schema Schema being created and returned");
        addProperty(new InputProperty(PROJECT_ROOT, "string", "Path to the root directories for the project.", false));
        return new InputSchema("object", getProperties(), getRequired());
    }


}
