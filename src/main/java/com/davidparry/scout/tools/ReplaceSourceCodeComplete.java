package com.davidparry.scout.tools;

import com.davidparry.scout.annotation.Schema;
import com.davidparry.scout.common.ArgumentUtils;
import com.davidparry.scout.common.LogFactory;
import com.davidparry.scout.handlers.Handler;
import com.davidparry.scout.handlers.HandlerResponse;
import com.davidparry.scout.io.ApplicationLogger;
import com.davidparry.scout.io.LogFileWriter;
import com.davidparry.scout.io.Logger;
import com.davidparry.scout.spec.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

@Schema(name = "replace_source_file_contents", description = "Given the fully qualified path of the source file and the entire sourceCode of the file you want to replace the existing source code with this tool will do this task for you.")
public class ReplaceSourceCodeComplete extends BuildTool implements Tool, Handler {
    private static final Logger logger = new ApplicationLogger().getLogger(LogFileWriter.getInstance(new LogFactory()));

    @Override
    public InputSchema schema() {
        logger.log("ReplaceSourceCodeComplete schema Schema being created and returned");
        addProperty(new InputProperty("fullyQualifiedSourceFilePath", "string", "The fully qualified path of the source file you want its contents replaced.", true));
        addProperty(new InputProperty("sourceCode", "string", "The fully qualified path of the sourceCode file that you want to replace it contents with.", true));
        return new InputSchema("object", getProperties(), getRequired());
    }

    @Override
    public ToolOutputResponse action(JsonRpcRequest request) {
        List<Content> results = new ArrayList<>();
        boolean error = false;

        try {
            String fullyQualifiedSourceFilePath = ArgumentUtils.getArgument(request, "fullyQualifiedSourceFilePath");
            String sourceCode = ArgumentUtils.getArgument(request, "sourceCode");

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
                //Path backupPath = Paths.get(fullyQualifiedSourceFilePath + ".backup");
                //Files.copy(filePath, backupPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                // Write the new content to the file
                Files.writeString(filePath, sourceCode, StandardOpenOption.TRUNCATE_EXISTING);

                // Build the result

                results.add(new Content("Successfully replaced the contents of file: " + fullyQualifiedSourceFilePath));

            } catch (IOException e) {
                logger.log("Error replacing file contents", e);
                return createErrorResult("Error replacing file contents: " + e.getMessage());
            }
        } catch (Exception e) {
            logger.log("Error finding class usage", e);
            return createErrorResult("Error finding class usage: " + e.getMessage());
        }

        return new ToolOutputResponse(results, error);
    }

    @Override
    public HandlerResponse handle(JsonRpcRequest request) {
        return new HandlerResponse(action(request));
    }
}
