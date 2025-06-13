package com.davidparry.scout.tools;

import com.davidparry.scout.annotation.Schema;
import com.davidparry.scout.common.ArgumentUtils;
import com.davidparry.scout.common.FileInfoRecord;
import com.davidparry.scout.handlers.Handler;
import com.davidparry.scout.handlers.HandlerResponse;
import com.davidparry.scout.io.ApplicationLogger;
import com.davidparry.scout.io.LogFileWriter;
import com.davidparry.scout.io.Logger;
import com.davidparry.scout.spec.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Schema(name = "get_project_file_resource_info", description = "Lists available resources/files of a project and provides information about them.")
public class GetResourceInfo extends BuildTool implements Tool, Handler {
    private static final Logger logger = ApplicationLogger.getLogger(LogFileWriter.getInstance());

    // Add explicit no-argument constructor
    public GetResourceInfo() {
        // Default constructor for schema processor
    }

    @Override
    public InputSchema schema() {
        logger.log("GetResourceInfo schema Schema being created and returned");
        addProperty(new InputProperty("path", "string", "Absolute Path to list resources at that directory.", true));
        addProperty(new InputProperty("recursive", "boolean", "Whether to list resources recursively.", true));
        return new InputSchema("object", getProperties(), getRequired());
    }

    @Override
    public ToolOutputResponse action(JsonRpcRequest args) {
        List<Content> results = new ArrayList<>();
        try {
            String pathValue = ArgumentUtils.getArgument(args, "path");
            String path = pathValue != null ? pathValue : ".";
            Boolean recursive = ArgumentUtils.getArgument(args, "recursive");
            // Get the resources
            List<Path> resources = listResources(path, recursive);

            for (Path resource : resources) {
                FileInfoRecord fileInfoRecord = FileInfoRecord.fromFile(resource.toFile());
                results.add(new Content(fileInfoRecord.toJson()));
            }
        } catch (Exception e) {
            logger.log("Error in GetResourceInfo tool", e);
            return createErrorResult("Error in GetResourceInfo tool" + e.getMessage());
        }
        return new ToolOutputResponse(results, false);
    }

    private List<Path> listResources(String pathStr, boolean recursive) throws Exception {
        Path path = Paths.get(pathStr).toAbsolutePath().normalize();

        if (!Files.exists(path)) {
            throw new IllegalArgumentException("Path does not exist: " + path);
        }

        if (Files.isDirectory(path)) {
            try (Stream<Path> stream = recursive ? Files.walk(path) : Files.list(path)) {
                return stream.collect(Collectors.toList());
            }
        } else {
            return List.of(path);
        }
    }
    @Override
    public HandlerResponse handle(JsonRpcRequest request) {
        return new HandlerResponse(action(request));
    }
}
