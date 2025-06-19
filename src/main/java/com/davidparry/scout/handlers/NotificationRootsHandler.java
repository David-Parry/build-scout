package com.davidparry.scout.handlers;

import com.davidparry.scout.State;
import com.davidparry.scout.common.LogFactory;
import com.davidparry.scout.io.ApplicationLogger;
import com.davidparry.scout.io.IOHandler;
import com.davidparry.scout.io.LogFileWriter;
import com.davidparry.scout.io.Logger;
import com.davidparry.scout.spec.JsonRpcRequest;

public class NotificationRootsHandler implements Handler {
    private final IOHandler ioHandler;
    private final State state;
    private final Logger logger = new ApplicationLogger().getLogger(LogFileWriter.getInstance(new LogFactory()));

    public NotificationRootsHandler(IOHandler ioHandler, State state) {
        this.ioHandler = ioHandler;
        this.state = state;
    }

    @Override
    public HandlerResponse handle(JsonRpcRequest request) {
        logger.log("Received notification request of updated  " + request);
        JsonRpcRequest responseMethod = new JsonRpcRequest(state.jsonrpc(), "roots/list", request.id() + 1);
        String message = responseMethod.toJson();
        ioHandler.writeLine(message);
        this.logger.log("sent message to client from update: " + message);
        return new HandlerResponse();
    }
}
