package com.davidparry.scout.handlers;

import com.davidparry.scout.ApplicationState;
import com.davidparry.scout.State;
import com.davidparry.scout.io.ApplicationLogger;
import com.davidparry.scout.io.Logger;
import com.davidparry.scout.prompts.Prompt;
import com.davidparry.scout.spec.JsonRpcRequest;
import com.davidparry.scout.spec.PromptMessage;
import com.davidparry.scout.spec.PromptResponse;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class PromptDispatchHandler implements Handler<PromptResponse> {
    private final Logger logger;
    private final State state;

    public PromptDispatchHandler() {
        this.logger = ApplicationLogger.getInstance();
        state = ApplicationState.instance();
    }

    @Override
    public PromptResponse handle(JsonRpcRequest request) {
        if (request == null || request.params() == null || request.params().arguments() == null) {
            String errorMsg;
            if (request == null) {
                logger.log("Error with request from client invocation " + request);
                errorMsg = "Invalid request";
                return new PromptResponse(errorMsg, Collections.emptySet());
            }
        }
        Object argsObj = request.params().arguments();
        Map<String, String> arguments = (argsObj instanceof Map<?, ?> map) ? map.entrySet().stream().filter(e -> e.getKey() instanceof String && e.getValue() != null).collect(java.util.stream.Collectors.toMap(e -> (String) e.getKey(), e -> e.getValue() instanceof String ? (String) e.getValue() : String.valueOf(e.getValue()))) : Collections.emptyMap();

        String name = request.params().name();
        logger.log("PromptDispatchHandler name is : " + name);
        Prompt prompt = state.getPrompt(name);
        if (prompt == null) {
            logger.log("Prompt is null for name " + name);
            return new PromptResponse("No prompt for the following named prompt " + name, Collections.emptySet());
        } else {
            Set<PromptMessage> messages = prompt.build(arguments);
            return new PromptResponse(prompt.description(), messages);
        }
    }
}
