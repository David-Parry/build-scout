package com.davidparry.mcp.buildscout.tools;

import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReplaceSourceCodeComplete extends BuildTool {
    private static final Logger logger = LoggerFactory.getLogger(ReplaceSourceCodeComplete.class);

    public ReplaceSourceCodeComplete() {
    }

    @Override
    public McpSchema.CallToolResult handle(Object args) {
        return handleFindClassUsage(args);
    }

    @Override
    public String name() {
        return "replace_source_file_contents";
    }

    @Override
    public String description() {
        return "Given the fully qualified path of the source file and the entire sourceCode of the file you want to replace the existing source code with this tool will do this task for you.";
    }

    @Override
    public McpSchema.JsonSchema schema() {
        addProperty("fullyQualifiedSourceFilePath", "string", "The fully qualified path of the source file you want its contents replaced.", true);
        addProperty("sourceCode", "string", "The fully qualified path of the sourceCode file that you want to replace it contents with.", true);
        return new McpSchema.JsonSchema("object", getProperties(), getRequired(), null);
    }

    private McpSchema.CallToolResult handleFindClassUsage(Object args) {
        List<McpSchema.Content> results = new ArrayList<>();
        boolean error = false;

        try {
            if (!(args instanceof Map)) {
                return createErrorResult("Invalid arguments format");
            }

            Map<String, Object> argsMap = (Map<String, Object>) args;
            String fullyQualifiedSourceFilePath = (String) argsMap.get("fullyQualifiedSourceFilePath");
            String sourceCode = (String) argsMap.get("sourceCode");

            if (fullyQualifiedSourceFilePath == null || fullyQualifiedSourceFilePath.isEmpty()) {
                return createErrorResult("Missing fully qualified path of the source file");
            }

            if (sourceCode == null || sourceCode.isEmpty()) {
                return createErrorResult("Missing sourceCode to replace the contents");
            }

            // Implement file replacement logic
            Path filePath = Paths.get(fullyQualifiedSourceFilePath);

            // Check if the file exists
            if (!Files.exists(filePath)) {
                return createErrorResult("Source file does not exist: " + fullyQualifiedSourceFilePath);
            }

            // Check if the file is writable
            if (!Files.isWritable(filePath)) {
                return createErrorResult("Source file is not writable: " + fullyQualifiedSourceFilePath);
            }

            try {
                // Backup the original file
                Path backupPath = Paths.get(fullyQualifiedSourceFilePath + ".backup");
                Files.copy(filePath, backupPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                // Write the new content to the file
                Files.writeString(filePath, sourceCode, StandardOpenOption.TRUNCATE_EXISTING);

                // Build the result

                results.add(new McpSchema.TextContent("Successfully replaced the contents of file: " + fullyQualifiedSourceFilePath + "\n" + "Backup created at: " + backupPath));

            } catch (IOException e) {
                logger.error("Error replacing file contents", e);
                return createErrorResult("Error replacing file contents: " + e.getMessage());
            }
        } catch (Exception e) {
            logger.error("Error finding class usage", e);
            return createErrorResult("Error finding class usage: " + e.getMessage());
        }

        return new McpSchema.CallToolResult(results, error);
    }

    private McpSchema.CallToolResult createErrorResult(String errorMessage) {
        List<McpSchema.Content> results = new ArrayList<>();
        results.add(new McpSchema.TextContent("Error: " + errorMessage));
        return new McpSchema.CallToolResult(results, true);
    }


}
