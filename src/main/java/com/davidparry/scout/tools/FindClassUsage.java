package com.davidparry.scout.tools;

import com.davidparry.scout.annotation.Schema;
import com.davidparry.scout.common.ArgumentUtils;
import com.davidparry.scout.common.SourceClassUsageService;
import com.davidparry.scout.io.ApplicationLogger;
import com.davidparry.scout.io.Logger;
import com.davidparry.scout.spec.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Schema(name = "find_class_usage", description = "Given the fully qualified path of the root directory of the project and the fully qualified class name this tool will search the projects source code for the classes usage and return file path that uses this class and line numbers where it is used.")
public class FindClassUsage extends BuildTool implements Tool<ToolOutputResponse> {
    private static final Logger logger = ApplicationLogger.getInstance();
    private final SourceClassUsageService service;

    public FindClassUsage(SourceClassUsageService service) {
        this.service = service;
    }

    public FindClassUsage() {
        this(new SourceClassUsageService());
    }

    @Override
    public InputSchema schema() {
        logger.log("FindClassUsage schema Schema being created and returned");
        addProperty("fullyQualifiedClassName", new InputProperty("string", "The fully qualified class name of the class."), true);
        addProperty("projectRoot", new InputProperty("string", "The fully qualified path of the root directory of the project."), true);
        return new InputSchema("object", getProperties(), getRequired());

    }

    @Override
    public ToolOutputResponse action(JsonRpcRequest request) {
        List<Content> results = new ArrayList<>();
        try {
            if (request == null) {
                return createErrorResult("Empty request you must provide a valid request");
            }

            String fullyQualifiedClassName = ArgumentUtils.getArgument(request, "fullyQualifiedClassName");
            String projectRoot = ArgumentUtils.getArgument(request, "projectRoot");

            if (fullyQualifiedClassName == null || fullyQualifiedClassName.isEmpty()) {
                return createErrorResult("Missing fully qualified class name");
            }

            if (projectRoot == null || projectRoot.isEmpty()) {
                return createErrorResult("Missing project root path");
            }

            // Find source directories
            List<Path> sourceDirs = service.findSourceDirectories(projectRoot);
            if (sourceDirs.isEmpty()) {
                return createErrorResult("No source directories found in the project");
            }

            Map<String, Set<Integer>> usageMap = service.searchClassUsage(sourceDirs, fullyQualifiedClassName);

            if (usageMap.isEmpty()) {
                return createErrorResult("No usages found for class: " + fullyQualifiedClassName);
            } else {
                StringBuilder resultBuilder = new StringBuilder();
                resultBuilder.append("Found usages of ").append(fullyQualifiedClassName).append(":\n\n");

                for (Map.Entry<String, Set<Integer>> entry : usageMap.entrySet()) {
                    resultBuilder.append("File: ").append(entry.getKey()).append("\n");
                    resultBuilder.append("Lines: ").append(entry.getValue().stream().map(String::valueOf).collect(Collectors.joining(", "))).append("\n\n");
                }
                results.add(new Content(resultBuilder.toString()));
            }
        } catch (Exception e) {
            logger.log("Error finding class usage", e);
            return createErrorResult("Error finding class usage: " + e.getMessage());
        }
        return new ToolOutputResponse(results, false);
    }
}
