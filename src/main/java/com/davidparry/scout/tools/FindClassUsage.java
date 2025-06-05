package com.davidparry.scout.tools;

import com.davidparry.scout.annotation.Schema;
import com.davidparry.scout.common.ArgumentUtils;
import com.davidparry.scout.common.SourceClassUsageService;
import com.davidparry.scout.io.ApplicationLogger;
import com.davidparry.scout.io.Logger;
import com.davidparry.scout.spec.*;

import java.io.File;
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
        addProperty(new InputProperty(PROJECT_ROOT, "string", "The fully qualified path of the root directory of the project.", rootProjectMandatory()));
        addProperty(new InputProperty("fullyQualifiedClassName", "string", "The fully qualified class name of the class.", true));
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
            if (fullyQualifiedClassName == null || fullyQualifiedClassName.isEmpty()) {
                return createErrorResult("Missing fully qualified class name");
            }

            List<Path> totalSourceDirs = new ArrayList<>();
            Set<File> files = getProjectRoots(request);
            for (File file : files) {
                logger.log("Root directories " + files.size() + " projects in " + files);
                totalSourceDirs.addAll(service.findSourceDirectories(file.getAbsolutePath()));
            }
            // Find source directories
            if (totalSourceDirs.isEmpty()) {
                return createErrorResult("No source directories found in the project");
            }

            Map<String, Set<Integer>> usageMap = service.searchClassUsage(totalSourceDirs, fullyQualifiedClassName);

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
