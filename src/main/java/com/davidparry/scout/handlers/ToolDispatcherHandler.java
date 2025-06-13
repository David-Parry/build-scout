package com.davidparry.scout.handlers;

import com.davidparry.scout.io.ApplicationLogger;
import com.davidparry.scout.io.LogFileWriter;
import com.davidparry.scout.io.Logger;
import com.davidparry.scout.spec.JsonRpcRequest;

public class ToolDispatcherHandler implements Handler {
    private static final Logger logger = ApplicationLogger.getLogger(LogFileWriter.getInstance());

    @Override
    public HandlerResponse handle(JsonRpcRequest request) {
        logger.info("ToolDispatcherHandler Received request");
        logger.log("ToolDispatcherHandler Handling request: " + request);
//        SchemaMetadata metadata = SchemaRegistry.getInstance().getSchema(request.params().name());
//
//        Tool tool = metadata.getTool();
        return new HandlerResponse();
    }
}
