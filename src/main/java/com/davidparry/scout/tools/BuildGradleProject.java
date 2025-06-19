package com.davidparry.scout.tools;

import com.davidparry.scout.common.*;
import com.davidparry.scout.handlers.Handler;
import com.davidparry.scout.handlers.HandlerResponse;
import com.davidparry.scout.io.ApplicationLogger;
import com.davidparry.scout.io.LogFileWriter;
import com.davidparry.scout.io.Logger;
import com.davidparry.scout.spec.*;
import com.davidparry.scout.spec.Tool;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BuildGradleProject extends BuildTool implements Handler {
    private static final Logger logger = new ApplicationLogger().getLogger(LogFileWriter.getInstance(new LogFactory()));
    private final GradleProcessExecutor service;
    private final Tool tool;

    public BuildGradleProject(GradleProcessExecutor gradleProcessExecutor) {
        this.service = gradleProcessExecutor;
        tool = new Tool("build_gradle_project", "if you want to also run a complete build with all checks pass the check = true this tool will invoke the build and return the result with both success and errors if isError is true.", schema());
    }


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
                BuildOutput output = service.buildProject(file, check);
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

    public InputSchema schema() {
        logger.log("BuildGradleProject schema Schema being created and returned");
        addProperty(new InputProperty(PROJECT_ROOT, "string", "The fully qualified path of the root directory of the project.", rootProjectMandatory()));
        addProperty(new InputProperty("check", "boolean", "If this flag is passed and is true then the check part of the gradle build will be also done.", false));
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
