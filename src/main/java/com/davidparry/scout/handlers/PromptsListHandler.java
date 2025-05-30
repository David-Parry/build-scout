package com.davidparry.scout.handlers;

import com.davidparry.scout.spec.JsonRpcRequest;
import com.davidparry.scout.spec.Prompt;
import com.davidparry.scout.spec.PromptArguments;
import com.davidparry.scout.spec.PromptsListResponse;

import java.util.Set;

public class PromptsListHandler implements Handler<PromptsListResponse> {

    public PromptsListHandler() {

    }

    @Override
    public PromptsListResponse handle(JsonRpcRequest request) {
        PromptArguments nameArg = new PromptArguments("dynamic_value", "A filler of the prompt", true);
        Prompt prompt = new Prompt("fantastic_prompt", "Describe a fantastic prompt that will help you achieve your goals", Set.of(nameArg));
        return new PromptsListResponse(Set.of(prompt), "nextdigiti");
    }
}
