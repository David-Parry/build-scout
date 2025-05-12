package com.davidparry.scout.tools;

import com.davidparry.scout.annotation.Schema;
import com.davidparry.scout.common.ArgumentUtils;
import com.davidparry.scout.common.BuildOutput;
import com.davidparry.scout.common.GradleTasks;
import com.davidparry.scout.common.GradleTasksImpl;
import com.davidparry.scout.io.ApplicationLogger;
import com.davidparry.scout.io.Logger;
import com.davidparry.scout.spec.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Schema(name = "build_gradle_project", description = "Given the fully qualified path of the root directory of the gradle project, this tool will invoke the build and return the result with both success and errors if isError is true.")
public class BuildGradleProject extends BuildTool implements Tool<ToolOutputResponse> {
    private static final Logger logger = ApplicationLogger.getInstance();
    private final GradleTasks service;

    public BuildGradleProject(GradleTasks service) {
        this.service = service;
    }

    public BuildGradleProject() {
        this(new GradleTasksImpl());
    }

    @Override
    public ToolOutputResponse action(JsonRpcRequest args) {
        List<Content> results = new ArrayList<>();
        boolean error;
        try {
            String projectRoot = ArgumentUtils.getArgument(args,"projectRoot");
            if (projectRoot == null || projectRoot.isEmpty()) {
                return createErrorResult("Missing project root path");
            }
            File projectDir = new File(projectRoot);
            if (!projectDir.exists() || !projectDir.isDirectory()) {
                return createErrorResult("Project root directory does not exist or is not a directory");
            }
            BuildOutput output = service.buildGradleProject(projectDir);
            results.add(new Content(service.formatOutput(output)));
            error = output.failed();
        } catch (Exception e) {
            logger.log("Error finding class usage", e);
            return createErrorResult("Error finding class usage: " + e.getMessage());
        }
        return new ToolOutputResponse(results, error);
    }


    @Override
    public InputSchema schema() {
        logger.log("BuildGradleProject schema Schema being created and returned");
        addProperty("projectRoot", new InputProperty("string", "The fully qualified path of the root directory of the project."), true);
        return new InputSchema("object", getProperties(), getRequired());
    }


}
