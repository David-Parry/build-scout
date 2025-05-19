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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Schema(name = "build_system_file_paths", description = "Given the root directory of a project, this tool identifies all build system files present (e.g., Gradle, Maven, CMake, etc.) and returns their absolute file paths. If multiple build system files are detected, the tool returns multiple path entries. Returns An array of strings, where each string is an absolute file path to a detected build system file.")
public class BuildSystemFilePaths extends BuildTool implements Tool<ToolOutputResponse> {
    private static final Logger logger = ApplicationLogger.getInstance();
    private final BuildSystem buildSystem;

    public BuildSystemFilePaths(BuildSystem buildSystem) {
        this.buildSystem = buildSystem;
    }

    public BuildSystemFilePaths() {
        this(new BuildSystemImpl());
    }


    @Override
    public InputSchema schema() {
        logger.log("BuildSystemFilePaths schema Schema being created and returned");
        addProperty(new InputProperty("projectRoot", "string", "The fully qualified path of the root directory of the project.", true));
        return new InputSchema("object", getProperties(), getRequired());
    }

    @Override
    public ToolOutputResponse action(JsonRpcRequest args) {

        Set<BuildFile> builds = new HashSet<>(); // Using Set to avoid duplicates
        try {
            String projectRoot = (String) args.params().arguments().get("projectRoot");
            if (projectRoot == null || projectRoot.isEmpty()) {
                return createErrorResult("Missing project root path");
            }
            File projectDir = new File(projectRoot);
            if (!projectDir.exists() || !projectDir.isDirectory()) {
                return createErrorResult("Project root directory does not exist or is not a directory");
            }
            Set<String> absolutePaths = new HashSet<>();
            buildSystem.processRootFolder(projectRoot, absolutePaths);
            List<String> absolutePathsList = new ArrayList<>(absolutePaths);
            builds = buildSystem.identifyBuildFiles(absolutePathsList);
        } catch (Exception e) {
            logger.log("Failed to process paths", e);
            return createErrorResult("Failed to find root build paths for the project." + e.getMessage());
        }
        return buildSystem.createPathResults(builds);

    }
}
