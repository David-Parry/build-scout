package com.davidparry.scout.tools;

import com.davidparry.scout.ApplicationState;
import com.davidparry.scout.annotation.Schema;
import com.davidparry.scout.common.*;
import com.davidparry.scout.handlers.Handler;
import com.davidparry.scout.handlers.HandlerResponse;
import com.davidparry.scout.io.ApplicationLogger;
import com.davidparry.scout.io.LogFileWriter;
import com.davidparry.scout.io.Logger;
import com.davidparry.scout.spec.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Schema(name = "build_gradle_project", description = "if you want to also run a complete build with all checks pass the check = true this tool will invoke the build and return the result with both success and errors if isError is true.")
public class BuildGradleProject extends BuildTool implements Tool, Handler {
    private static final Logger logger = new ApplicationLogger().getLogger(LogFileWriter.getInstance(new LogFactory()));    private final GradleTasks service;

    public BuildGradleProject(GradleTasks service) {
        this.service = service;
    }

    public BuildGradleProject() {
        this(new GradleTasksImpl());
    }

    @Override
    public ToolOutputResponse action(JsonRpcRequest args) {
        List<Content> results = new ArrayList<>();
        boolean error = false;
        try {
            Set<File> files = getProjectRoots(args);
            logger.log("Root directories " + files.size() + " projects in " + files);

            boolean check = Boolean.FALSE;
            Boolean checkParameter = ArgumentUtils.getArgument(args, "check");
            if (checkParameter != null) {
                check = checkParameter;
            }
            for (File file : files) {
                BuildOutput output = service.buildGradleProject(file, check);
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
        addProperty(new InputProperty(PROJECT_ROOT, "string", "The fully qualified path of the root directory of the project.", rootProjectMandatory()));
        addProperty(new InputProperty("check","boolean", "If this flag is passed and is true then the check part of the gradle build will be also done.",false));
        return new InputSchema("object", getProperties(), getRequired());
    }

    @Override
    public HandlerResponse handle(JsonRpcRequest request) {
        return new HandlerResponse(action(request));
    }
}
