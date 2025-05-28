package com.davidparry.scout;

import com.davidparry.scout.annotation.SchemaRegistry;
import com.davidparry.scout.common.Consumer;
import com.davidparry.scout.io.ApplicationLogger;
import com.davidparry.scout.io.IOHandler;
import com.davidparry.scout.io.Logger;
import com.davidparry.scout.io.RequestProcessor;
import com.davidparry.scout.spec.JSONResponse;
import com.davidparry.scout.spec.JsonRpcRequest;
import com.davidparry.scout.spec.RpcError;
import com.google.gson.Gson;

/**
 * Controller class responsible for processing incoming JSON-RPC requests,
 * routing them to the appropriate tool handler, and returning responses.
 */
public class RequestController {
    private final Logger logger;
    private final IOHandler io;
    private final SchemaRegistry registry;
    private final Gson gson;
    private final Consumer consumer;

    /**
     * Constructs a new RequestController with the necessary dependencies.
     *
     * @param io       The IOHandler for writing responses
     * @param registry The SchemaRegistry for looking up tool handlers
     */
    public RequestController(IOHandler io, SchemaRegistry registry, Consumer consumer) {
        this.io = io;
        this.registry = registry;
        this.logger = ApplicationLogger.getInstance();
        this.gson = new Gson();
        this.consumer = consumer;
    }


    /**
     * Processes an incoming line containing a JSON-RPC request.
     * Creates a RequestProcessor and runs it in a virtual thread.
     *
     * @param line The raw JSON-RPC request string
     */
    public void processRequest(String line) {
        if (line.isEmpty()) {
            return;
        }

        logger.log("RAW Received request: " + line);

        try {
            JsonRpcRequest request = JsonRpcRequest.fromJson(line);
            if (request.error() != null) {
                logger.log("Error processing request: " + request.error());
            } else if (request.result() != null) {
                consumer.consume(request);
            } else {
                // Create a RequestProcessor and run it in a virtual thread
                RequestProcessor processor = new RequestProcessor(request, io, registry);
                Thread.startVirtualThread(processor);
            }
        } catch (Exception e) {
            // This should only happen if there's an error creating the thread
            logger.log("Error creating request processor thread", e);
            JSONResponse<?> jsonResponse = new JSONResponse<>("2.0", 0, new RpcError(-32603, "Error creating request processor: " + e.getMessage()));
            io.writeLine(gson.toJson(jsonResponse));
        }
    }


}
