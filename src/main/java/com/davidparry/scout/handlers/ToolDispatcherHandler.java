package com.davidparry.scout.handlers;

import com.davidparry.scout.annotation.SchemaRegistry;
import com.davidparry.scout.io.ApplicationLogger;
import com.davidparry.scout.io.Logger;
import com.davidparry.scout.spec.JsonRpcRequest;
import com.davidparry.scout.spec.ToolOutputResponse;
import com.davidparry.scout.tools.Tool;

public class ToolDispatcherHandler implements Handler<ToolOutputResponse> {
    private static final Logger logger = ApplicationLogger.getInstance();

    @Override
    public ToolOutputResponse handle(JsonRpcRequest request) {
        logger.log("Handling request: " + request);
        SchemaRegistry.SchemaMetadata metadata = SchemaRegistry.getInstance().getSchema(request.params().name());
        Tool tool = metadata.getTool();
        return (ToolOutputResponse) tool.action(request);
    }
}
