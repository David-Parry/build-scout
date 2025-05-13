package com.davidparry.scout.handlers;

import com.davidparry.scout.io.ApplicationLogger;
import com.davidparry.scout.io.Logger;
import com.davidparry.scout.spec.JsonRpcRequest;

public class NotificationHandler implements Handler<Object> {
    private static final Logger logger = ApplicationLogger.getInstance();

    @Override
    public Object handle(JsonRpcRequest request) {
        logger.log("Received notification request " + request);
        return null;
    }
}
