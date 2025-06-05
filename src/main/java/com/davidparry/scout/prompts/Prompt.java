package com.davidparry.scout.prompts;

import com.davidparry.scout.spec.PromptListItem;
import com.davidparry.scout.spec.PromptMessage;

import java.util.Map;
import java.util.Set;

public interface Prompt {

    Set<PromptMessage> build(Map<String,String> arguments);

    PromptListItem item();

    String name();

    String description();
}
