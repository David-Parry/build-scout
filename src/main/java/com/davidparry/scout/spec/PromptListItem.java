package com.davidparry.scout.spec;

import java.util.Set;

public record PromptListItem(String name, String description, Set<PromptArguments> arguments) {

    public PromptListItem(String name, String description) {
        this(name, description, null);
    }
}
