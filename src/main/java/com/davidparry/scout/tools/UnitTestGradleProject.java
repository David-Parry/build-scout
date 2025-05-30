package com.davidparry.scout.tools;

import com.davidparry.scout.annotation.Schema;
import com.davidparry.scout.common.BuildOutput;
import com.davidparry.scout.common.GradleTasks;
import com.davidparry.scout.common.GradleTasksImpl;
import com.davidparry.scout.io.ApplicationLogger;
import com.davidparry.scout.io.Logger;
import com.davidparry.scout.spec.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Schema(name = "gradle_builder", description = "Executes Gradle build tasks for any gradle project. Runs the standard build process and optionally performs comprehensive testing when quality assurance is required.")
public class UnitTestGradleProject extends BuildTool implements Tool<ToolOutputResponse> {
    private static final Logger logger = ApplicationLogger.getInstance();
    private final GradleTasks service;

    public UnitTestGradleProject(GradleTasks service) {
        this.service = service;
    }

    public UnitTestGradleProject() {
        this(new GradleTasksImpl());
    }

    @Override
    public ToolOutputResponse action(JsonRpcRequest args) {
        List<Content> results = new ArrayList<>();
        boolean error = false;
        try {
            Set<File> files = getProjectRoots(args);
            logger.log("Root directories " + files.size() + " projects in " + files);
            for (File file : files) {
                BuildOutput output = service.buildGradleProject(file, true);
                results.add(new Content(service.formatOutput(output)));
                if (!error) {
                    error = output.failed();
                }
            }
        } catch (Exception e) {
            logger.log("Error finding class usage", e);
            return createErrorResult("Error finding class usage: " + e.getMessage());
        }
        return new ToolOutputResponse(results, error);
    }


    @Override
    public InputSchema schema() {
        logger.log("BuildGradleProject schema Schema being created and returned");
        addProperty(new InputProperty("projectRoot", "string", "The fully qualified path of the root directory of the project.", false));
        return new InputSchema("object", getProperties(), getRequired());
    }


}
