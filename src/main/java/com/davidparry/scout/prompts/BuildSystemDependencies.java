package com.davidparry.scout.prompts;

import com.davidparry.scout.spec.Content;
import com.davidparry.scout.spec.PromptArguments;
import com.davidparry.scout.spec.PromptListItem;
import com.davidparry.scout.spec.PromptMessage;

import java.util.Map;
import java.util.Set;

public class BuildSystemDependencies implements Prompt {
    private static final String NAME = "BuildSystemDependencies";
    private static final String BUILD_SYSTEM_LOCATION = "BuildSystemLocation";
    private static final String DESCRIPTION = "A prompt to invoke the dependencies_list tool to have all the dependencies for a specific build system";

    @Override
    public Set<PromptMessage> build(Map<String, String> arguments) {
        String location = arguments.get(BUILD_SYSTEM_LOCATION);
        String prompt = "Return all the dependencies using the tool dependencies_list for the build system this must include all dependencies returned from calling build tool dependencies_list to retrieve this list.";
        if (location != null && !location.isBlank()) {
            prompt = "Return all the dependencies using the tool dependencies_list for the build system located in the following absolute path " + location + ", this list must include all dependencies returned from calling build tool dependencies_list to retrieve this list.";
        }
        return Set.of(new PromptMessage("user", new Content(prompt)));
    }


    @Override
    public PromptListItem item() {
        PromptArguments nameArg = new PromptArguments(BUILD_SYSTEM_LOCATION, "The absolute path to where the build system file is located.", false);
        return new PromptListItem(NAME, DESCRIPTION, Set.of(nameArg));
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public String description() {
        return DESCRIPTION;
    }
}


