package com.davidparry.scout.tools;

import com.davidparry.scout.common.ArgumentUtils;
import com.davidparry.scout.common.FileInfoRecord;
import com.davidparry.scout.common.LogFactory;
import com.davidparry.scout.handlers.Handler;
import com.davidparry.scout.handlers.HandlerResponse;
import com.davidparry.scout.io.ApplicationLogger;
import com.davidparry.scout.io.LogFileWriter;
import com.davidparry.scout.io.Logger;
import com.davidparry.scout.spec.*;
import com.davidparry.scout.spec.Tool;

import java.util.ArrayList;
import java.util.List;

public class GetFileInfo extends BuildTool implements Handler {
    private final Logger logger = new ApplicationLogger().getLogger(LogFileWriter.getInstance(new LogFactory()));
    private final Tool tool;

    // Add explicit no-argument constructor
    public GetFileInfo() {
        this.tool = new Tool("get_file_info", "Get information about a file, just simply give an absolute path and it will return the file information for sure.", schema());
    }

    public InputSchema schema() {
        logger.log("GetFileInfo schema Schema being created and returned");
        addProperty(new InputProperty("path", "string", "The Absolute Path to the file.", true));
        return new InputSchema("object", getProperties(), getRequired());
    }

    public ToolOutputResponse action(JsonRpcRequest args) {
        List<Content> results = new ArrayList<>();
        try {
            String path = ArgumentUtils.getArgument(args, "path");
            java.io.File file = new java.io.File(path);
            FileInfoRecord fileInfoRecord = FileInfoRecord.fromFile(file);
            results.add(new Content(fileInfoRecord.toJson()));
        } catch (Exception e) {
            logger.error("Error handling get-file-info tool call", e);
            return createErrorResult("Error handling get-file-info tool call " + e.getMessage());
        }
        return new ToolOutputResponse(results, false);
    }

    @Override
    public HandlerResponse handle(JsonRpcRequest request) {
        return new HandlerResponse(action(request));
    }

    public Tool tool() {
        return tool;
    }
}
