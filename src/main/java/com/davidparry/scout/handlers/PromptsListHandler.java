package com.davidparry.scout.handlers;

import com.davidparry.scout.ApplicationState;
import com.davidparry.scout.common.LogFactory;
import com.davidparry.scout.io.ApplicationLogger;
import com.davidparry.scout.io.LogFileWriter;
import com.davidparry.scout.io.Logger;
import com.davidparry.scout.prompts.BuildSystemDependencies;
import com.davidparry.scout.spec.JsonRpcRequest;
import com.davidparry.scout.spec.PromptsListResponse;

import java.util.Set;

public class PromptsListHandler implements Handler {
    private final Logger logger = new ApplicationLogger().getLogger(LogFileWriter.getInstance(new LogFactory()));


    @Override
    public HandlerResponse handle(JsonRpcRequest request) {
        BuildSystemDependencies buildSystemDependencies = new BuildSystemDependencies();
        logger.log("Setting the prompt for "+ buildSystemDependencies.name() + " class stored is " + buildSystemDependencies.getClass().getName());
        ApplicationState.instance().setPrompt(buildSystemDependencies.name(), buildSystemDependencies);
        return new HandlerResponse(new PromptsListResponse(Set.of(buildSystemDependencies.item()), "listCursor"));
    }
}
