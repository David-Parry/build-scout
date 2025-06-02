package com.davidparry.scout.prompts;

import com.davidparry.scout.spec.Content;
import com.davidparry.scout.spec.PromptArguments;
import com.davidparry.scout.spec.PromptListItem;
import com.davidparry.scout.spec.PromptMessage;

import java.util.Map;
import java.util.Set;

public class BuildSystemDependencies implements Prompt {
    private static final String NAME = "build_system_dependencies";
    private static final String BUILD_SYSTEM_TYPE = "build_system_type";
    private static final String DESCRIPTION = "A prompt to invoke the dependencies_list tool to have all the dependencies for a specific build system";
    @Override
    public Set<PromptMessage> build(Map<String, String> arguments) {
        String type = arguments.get(BUILD_SYSTEM_TYPE);
        return Set.of( new PromptMessage("user",new Content("Return all the dependencies using the tool dependencies_list for the build system of type : " + type + ", " +
               "this should include all dependencies you must invoke the build tool dependencies_list to retrieve this list.")));
    }



    @Override
    public PromptListItem item() {
        PromptArguments nameArg = new PromptArguments(BUILD_SYSTEM_TYPE, "The build type for the project, this comes from invoking the tool find_build_system that will return the build system type.", true);
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


