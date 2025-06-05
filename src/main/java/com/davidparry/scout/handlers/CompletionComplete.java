package com.davidparry.scout.handlers;

import com.davidparry.scout.io.ApplicationLogger;
import com.davidparry.scout.io.Logger;
import com.davidparry.scout.spec.JsonRpcRequest;

public class CompletionComplete implements Handler<String> {
    private Logger logger;
    public CompletionComplete() {
        this.logger = ApplicationLogger.getInstance();
    }

    @Override
    public String handle(JsonRpcRequest request) {
        logger.log("Completion complete called with "+ request);
        return "COMPLETION";
    }
}
