package com.davidparry.scout.tools;

import com.davidparry.scout.annotation.Schema;
import com.davidparry.scout.common.ArgumentUtils;
import com.davidparry.scout.common.FileInfoRecord;
import com.davidparry.scout.io.ApplicationLogger;
import com.davidparry.scout.io.Logger;
import com.davidparry.scout.spec.*;

import java.util.ArrayList;
import java.util.List;

@Schema(name = "get_file_info", description = "Get information about a file, just simply give a path and it will return the file information for sure.")
public class GetFileInfo extends BuildTool implements Tool<ToolOutputResponse> {
    private static final Logger logger = ApplicationLogger.getInstance();


    @Override
    public InputSchema schema() {
        logger.log("GetFileInfo schema Schema being created and returned");
        addProperty(new InputProperty("path", "string", "Path to the file.", true));
        return new InputSchema("object", getProperties(), getRequired());
    }

    @Override
    public ToolOutputResponse action(JsonRpcRequest args) {
        List<Content> results = new ArrayList<>();
        try {
            String path = ArgumentUtils.getArgument(args, "path");
            java.io.File file = new java.io.File(path);
            FileInfoRecord fileInfoRecord = FileInfoRecord.fromFile(file);
            results.add(new Content(fileInfoRecord.toJson()));
        } catch (Exception e) {
            logger.log("Error handling get-file-info tool call", e);
            return createErrorResult("Error handling get-file-info tool call " + e.getMessage());
        }
        return new ToolOutputResponse(results, false);
    }
}
