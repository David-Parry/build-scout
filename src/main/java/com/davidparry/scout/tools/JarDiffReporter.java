package com.davidparry.scout.tools;

import com.davidparry.scout.annotation.Schema;
import com.davidparry.scout.common.ArgumentUtils;
import com.davidparry.scout.common.DiffData;
import com.davidparry.scout.common.JarComparatorService;
import com.davidparry.scout.common.JarDownloader;
import com.davidparry.scout.io.ApplicationLogger;
import com.davidparry.scout.io.Logger;
import com.davidparry.scout.spec.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Schema(name = "version_change_analyzer", description = "Given the groupId, artifactId current version and the latest version of the artifact will return the changes in the public code between the two versions. This allows for a other agents to then check if the code will need to be updated based on the changes.")
public class JarDiffReporter extends BuildTool implements Tool<ToolOutputResponse> {
    private static final Logger logger = ApplicationLogger.getInstance();
    private final JarComparatorService jarComparatorService;

    public JarDiffReporter(JarComparatorService jarComparatorService) {
        this.jarComparatorService = jarComparatorService;
    }

    public JarDiffReporter() {
        this(new JarComparatorService(new JarDownloader()));
    }

    @Override
    public InputSchema schema() {
        logger.log("JarDiffReporter schema being created and returned");
        addProperty("groupId", new InputProperty("string", "The maven group id used in maven dependency repository."), true);
        addProperty("artifactId", new InputProperty("string", "The maven artifact Id used in the maven dependency repository."), true);
        addProperty("latestVersion", new InputProperty("string", "Current version of the artifact."), true);
        addProperty("currentVersion", new InputProperty("string", "The latest version of the artifact."), true);
        return new InputSchema("object", getProperties(), getRequired());
    }

    @Override
    public ToolOutputResponse action(JsonRpcRequest args) {
        List<Content> results = new ArrayList<>();
        boolean error = true;

        try {
            Map<String, String> validatedParams = validateAndExtractParameters(args);
            if (validatedParams == null) {
                return createErrorResult("Invalid or missing parameters in request");
            }

            List<DiffData> changedClasses = jarComparatorService.compareJars(validatedParams);

            for (DiffData change : changedClasses) {
                results.add(new Content("class:" + change.clazz() + " changeStatus:" + change.changeStatus()));
            }
            error = false;
        } catch (Exception e) {
            logger.log("Failed to find diffs on files ", e);
            return createErrorResult("Failed to find diffs on files " + e.getMessage());
        }
        return new ToolOutputResponse(results, error);
    }

    private Map<String, String> validateAndExtractParameters(JsonRpcRequest request) {
        try {
            if (request == null) {
                logger.log("Request map is null");
                return null;
            }
            Map<String, String> validatedParams = new HashMap<>();
            String[] requiredParams = {"groupId", "artifactId", "currentVersion", "latestVersion"};
            for (String param : requiredParams) {
                String parameter = ArgumentUtils.getArgument(request, param);
                if (parameter == null) {
                    logger.log("Missing required parameter: {} " + param);
                    return null;
                }
                validatedParams.put(param, parameter);
            }
            return validatedParams;
        } catch (ClassCastException e) {
            logger.log("Failed to gather parameters from request", e);
            return null;
        }
    }

}
