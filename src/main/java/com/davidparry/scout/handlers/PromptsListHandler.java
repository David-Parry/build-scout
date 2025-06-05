package com.davidparry.scout.handlers;

import com.davidparry.scout.ApplicationState;
import com.davidparry.scout.io.ApplicationLogger;
import com.davidparry.scout.io.Logger;
import com.davidparry.scout.prompts.BuildSystemDependencies;
import com.davidparry.scout.spec.JsonRpcRequest;
import com.davidparry.scout.spec.PromptsListResponse;

import java.util.Set;

public class PromptsListHandler implements Handler<PromptsListResponse> {
    private static final Logger logger = ApplicationLogger.getInstance();

    public PromptsListHandler() {

    }

    @Override
    public PromptsListResponse handle(JsonRpcRequest request) {
        BuildSystemDependencies buildSystemDependencies = new BuildSystemDependencies();
        logger.log("Setting the prompt for "+ buildSystemDependencies.name() + " class stored is " + buildSystemDependencies.getClass().getName());
        ApplicationState.instance().setPrompt(buildSystemDependencies.name(), buildSystemDependencies);
        return new PromptsListResponse(Set.of(buildSystemDependencies.item()), "listCursor");
    }
}
