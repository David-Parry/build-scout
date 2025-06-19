package com.davidparry.scout.handlers;

import com.davidparry.scout.common.LogFactory;
import com.davidparry.scout.io.ApplicationLogger;
import com.davidparry.scout.io.LogFileWriter;
import com.davidparry.scout.io.Logger;
import com.davidparry.scout.spec.JsonRpcRequest;

public class CompletionComplete implements Handler {
    private final Logger logger = new ApplicationLogger().getLogger(LogFileWriter.getInstance(new LogFactory()));

    @Override
    public HandlerResponse handle(JsonRpcRequest request) {
        logger.log("Completion complete called with "+ request);
        return new HandlerResponse("COMPLETION");
    }
}
