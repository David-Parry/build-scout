package com.davidparry.scout;

import com.davidparry.scout.common.GsonProvider;
import com.davidparry.scout.common.LogFactory;
import com.davidparry.scout.handlers.Handler;
import com.davidparry.scout.handlers.HandlerResponse;
import com.davidparry.scout.io.ApplicationLogger;
import com.davidparry.scout.io.IOHandler;
import com.davidparry.scout.io.LogFileWriter;
import com.davidparry.scout.io.Logger;
import com.davidparry.scout.spec.JSONResponse;
import com.davidparry.scout.spec.JsonRpcRequest;
import com.google.gson.Gson;

import java.util.Map;

public class Router {
    private final IOHandler io;
    private final State state;
    private final Gson gson;
    private final Logger logger = new ApplicationLogger().getLogger(LogFileWriter.getInstance(new LogFactory()));
    private final Map<String, Handler> handlers;

    public Router(IOHandler io, State state, Map<String, Handler> handlers) {
        this.io = io;
        this.state = state;
        this.gson = GsonProvider.getInstance();
        this.handlers = handlers;

    }


    public void route(String message) {
        logger.log("Router received message: " + message);

        if (message == null || message.isEmpty()) {
            return;
        }

        JsonRpcRequest request = JsonRpcRequest.fromJson(message);
        try {
            // Observe the id to set session policy if not already set
            state.observeIdIfUnset(request.id());
            
            String jsonRpc = request.jsonrpc();
            HandlerResponse result = null;
            String key = extractKey(request);
            logger.log("Router extracted key is : " + key);
            Handler handler = handlers.get(key);
            if (handler != null) {
                result = handler.handle(request);
            }

            if (result != null && result.response() != null) {
                // Echo back the original id as-is to preserve type and value
                JSONResponse<?> jsonResponse = new JSONResponse<>(jsonRpc, request.id(), result.response());
                String outString = gson.toJson(jsonResponse);
                io.writeLine(outString);

                // Log the response
                logger.log("response from handler in json " + outString);
            } else {
                logger.log("No response from handler : " + handler + " for request:" + request);
            }
        } catch (Exception e) {
            logger.error("Router exception message : " + message, e);
        }
    }

    private String extractKey(JsonRpcRequest request) {
        String key = null;
        if (request.error() != null) {
            logger.log("Error processing request: " + request.error());
            key = "error";
        } else if (request.result() != null && !request.result().isEmpty()) {
            if (request.result().containsKey("roots")) {
                key = "roots";
            } else {
                // missed need to handle
                logger.error("Missed consumer call for request " + request);
                key = request.result().values().toString();
            }

        } else if (request.method() != null && !request.method().isEmpty()) {
            var method = request.method();
            if ("tools/call".equals(method)) {
                key = request.params().name();
            } else {
                key = request.method();
            }
        }
        return key;
    }


}
