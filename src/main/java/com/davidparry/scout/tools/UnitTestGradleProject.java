package com.davidparry.scout.tools;

import com.davidparry.scout.common.BuildOutput;
import com.davidparry.scout.common.GradleProcessExecutor;
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
import java.util.List;
import java.util.Set;

public class UnitTestGradleProject extends BuildTool implements Handler {
    private final Logger logger = new ApplicationLogger().getLogger(LogFileWriter.getInstance(new LogFactory()));
    private final GradleProcessExecutor service;
    private final Tool tool;

    public UnitTestGradleProject(GradleProcessExecutor service) {
        this.service = service;
        this.tool = new Tool("gradle_tester", "Executes Gradle build and check tasks for any gradle project.", schema());
    }

    public ToolOutputResponse action(JsonRpcRequest args) {
        List<Content> results = new ArrayList<>();
        boolean error = false;
        try {
            Set<File> files = getProjectRoots(args);
            logger.log("Root directories " + files.size() + " projects in " + files);
            for (File file : files) {
                BuildOutput output = service.buildProject(file, true);
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
        return new InputSchema("object", getProperties(), getRequired());
    }

    @Override
    public HandlerResponse handle(JsonRpcRequest request) {
        return new HandlerResponse(action(request));
    }

    public Tool tool() {
        return tool;
    }

}
