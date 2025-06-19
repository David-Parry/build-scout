package com.davidparry.scout.handlers;

import com.davidparry.scout.common.LogFactory;
import com.davidparry.scout.io.ApplicationLogger;
import com.davidparry.scout.io.LogFileWriter;
import com.davidparry.scout.io.Logger;
import com.davidparry.scout.spec.JsonRpcRequest;
import com.davidparry.scout.spec.Tool;
import com.davidparry.scout.spec.ToolsListResponse;

import java.util.List;

public class ToolsListHandler implements Handler {
    private final List<Tool> tools;
    private final Logger logger = new ApplicationLogger().getLogger(LogFileWriter.getInstance(new LogFactory()));

    public ToolsListHandler(List<Tool> tools) {
        this.tools = tools;
    }


    @Override
    public HandlerResponse handle(JsonRpcRequest request) {
        logger.log("Handling tools list request: " + request);
        return new HandlerResponse(new ToolsListResponse(tools));
    }


}
