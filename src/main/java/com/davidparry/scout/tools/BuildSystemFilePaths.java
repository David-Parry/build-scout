package com.davidparry.scout.tools;

import com.davidparry.scout.common.BuildFile;
import com.davidparry.scout.common.BuildSystem;
import com.davidparry.scout.common.LogFactory;
import com.davidparry.scout.handlers.Handler;
import com.davidparry.scout.handlers.HandlerResponse;
import com.davidparry.scout.io.ApplicationLogger;
import com.davidparry.scout.io.LogFileWriter;
import com.davidparry.scout.io.Logger;
import com.davidparry.scout.spec.InputProperty;
import com.davidparry.scout.spec.InputSchema;
import com.davidparry.scout.spec.JsonRpcRequest;
import com.davidparry.scout.spec.Tool;
import com.davidparry.scout.spec.ToolOutputResponse;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class BuildSystemFilePaths extends BuildTool implements Handler {
    private static final Logger logger = new ApplicationLogger().getLogger(LogFileWriter.getInstance(new LogFactory()));
    private final BuildSystem buildSystem;
    private final com.davidparry.scout.spec.Tool tool;


    public BuildSystemFilePaths(BuildSystem buildSystem) {
        this.buildSystem = buildSystem;
        tool = new com.davidparry.scout.spec.Tool("build_system_file_paths", "Given the root directory of a project, this tool identifies all build system files present (e.g., Gradle, Maven, CMake, etc.) and returns their absolute file paths. If multiple build system files are detected, the tool returns multiple path entries. Returns An array of strings, where each string is an absolute file path to a detected build system file.", schema());

    }

    public InputSchema schema() {
        logger.log("BuildSystemFilePaths schema Schema being created and returned");
        addProperty(new InputProperty(PROJECT_ROOT, "string", "The fully qualified path of the root directory of the project.", rootProjectMandatory()));
        return new InputSchema("object", getProperties(), getRequired());
    }

    public ToolOutputResponse action(JsonRpcRequest args) {

        Set<BuildFile> builds; // Using Set to avoid duplicates
        try {
            Set<File> files = getProjectRoots(args);
            logger.log("Root directories " + files.size() + " projects in " + files);
            Set<String> absolutePaths = new HashSet<>();
            for (File file : files) {
                buildSystem.processRootFolder(file.getAbsolutePath(), absolutePaths);
            }
            builds = buildSystem.identifyBuildFiles(absolutePaths);
        } catch (Exception e) {
            logger.log("Failed to process paths", e);
            return createErrorResult("Failed to find root build paths for the project." + e.getMessage());
        }
        return buildSystem.createPathResults(builds);

    }

    @Override
    public HandlerResponse handle(JsonRpcRequest request) {
        return new HandlerResponse(action(request));
    }
    public Tool tool() {
        return this.tool;
    }
}
