package com.davidparry.scout.io;

import com.davidparry.scout.annotation.SchemaRegistry;
import com.davidparry.scout.handlers.Handler;
import com.davidparry.scout.spec.JSONResponse;
import com.davidparry.scout.spec.JsonRpcRequest;
import com.davidparry.scout.spec.RpcError;
import com.google.gson.Gson;

/**
 * Processor class that handles a single JSON-RPC request in a virtual thread.
 * Each instance processes one request independently.
 */
public class RequestProcessor implements Runnable {
    private final JsonRpcRequest request;
    private final IOHandler io;
    private final SchemaRegistry registry;
    private final Logger logger;
    private final Gson gson;

    /**
     * Constructs a new RequestProcessor with the necessary dependencies.
     *
     * @param request  The  JSON-RPC request
     * @param io       The IOHandler for writing responses
     * @param registry The SchemaRegistry for looking up tool handlers
     */
    public RequestProcessor(JsonRpcRequest request, IOHandler io, SchemaRegistry registry) {
        // Create a copy of the request line to ensure thread safety
        this.request = request;
        this.io = io;
        this.registry = registry;
        this.logger = ApplicationLogger.getInstance();
        this.gson = new Gson();
    }

    /**
     * Processes the request in a virtual thread.
     */
    @Override
    public void run() {
        String jsonRpc = "2.0";
        int id = 0;

        try {
            // Parse the incoming JSON-RPC request

            logger.log("Thread processing request: " + request.toJson());

            String method = request.method();
            jsonRpc = request.jsonrpc();
            id = request.id();

            // Handle the request based on the method
            Object result = handleMethod(method, request);

            if (result != null) {
                JSONResponse<?> jsonResponse = new JSONResponse<>(jsonRpc, id, result);
                String outString = gson.toJson(jsonResponse);
                io.writeLine(outString);

                // Log the response
                logger.log("Thread sending response: " + outString);
            } else {
                logger.log("Thread notification no response for: " + request);
            }
        } catch (Exception e) {
            logger.log("Error in request processor", e);
            JSONResponse<?> jsonResponse = new JSONResponse<>(jsonRpc, id, new RpcError(-32603, "Error processing request: " + e.getMessage()));
            io.writeLine(gson.toJson(jsonResponse));
        }
    }

    /**
     * Handles the request based on the specified method by delegating to the appropriate tool.
     * If the method contains a forward slash (/), only the part before the slash is used
     * to look up the handler.
     *
     * @param method  The method name from the JSON-RPC request
     * @param request The full JSON-RPC request
     * @return The result object to be included in the response
     */
    private Object handleMethod(String method, JsonRpcRequest request) {
        try {
            // Extract the base method name if it contains a forward slash
            String baseMethod = method;
            if (method.contains("/")) {
                String base = method.substring(0, method.indexOf('/'));
                if ("notifications".equals(base)) {
                    baseMethod = method.substring(0, method.indexOf('/'));
                    logger.log("Using base method: " + baseMethod + " from full method: " + method);
                }
            }
            Handler handler = registry.getHandlerForMethod(baseMethod);
            logger.log("Handler: " + handler);
            if (handler != null) {
                // Get the handler for the base method
                return handler.handle(request);
            } else {
                logger.log("No Handler for the following method and call : " + method + " request: " + request);
                // method not found
                return new RpcError(-32601, "This capability is not supported. ");
            }
        } catch (Exception e) {
            logger.log("Error invoking handler for method: " + method, e);
            // method not found
            return new RpcError(-32603, "Error processing request: " + e.getMessage());
        }
    }
}