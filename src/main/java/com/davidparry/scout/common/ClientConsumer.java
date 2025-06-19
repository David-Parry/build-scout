package com.davidparry.scout.common;

import com.davidparry.scout.State;
import com.davidparry.scout.handlers.Handler;
import com.davidparry.scout.handlers.HandlerResponse;
import com.davidparry.scout.io.ApplicationLogger;
import com.davidparry.scout.io.LogFileWriter;
import com.davidparry.scout.io.Logger;
import com.davidparry.scout.spec.JsonRpcRequest;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class ClientConsumer implements Consumer, Handler {
    private final Logger logger = new ApplicationLogger().getLogger(LogFileWriter.getInstance(new LogFactory()));

    private final State state;

    public ClientConsumer(State state) {
        this.state = state;
    }

    public void consume(JsonRpcRequest request) {
        logger.log("Received request: " + request.result());
        // rigt now only this to consume is the paths
        // more messages will create a dispatcher
        List roots = (List) request.result().get("roots");
        if (roots != null && roots.size() > 0) {
            state.clearRoots();
            for (Object root : roots) {
                logger.log("Root " + root);
                Map<String, String> rootMap = (Map) root;
                logger.log("Root being saved is" + rootMap);
                String uriString = rootMap.get("uri");
                String name = rootMap.get("name");
                if (name == null) {
                    name = "root_" + Instant.now().getEpochSecond();
                }
                state.setRoot(name, URI.create(uriString));
            }
        }
    }

    @Override
    public HandlerResponse handle(JsonRpcRequest request) {
        consume(request);
        return new HandlerResponse();
    }
}
