package com.davidparry.scout.spec;

import java.util.Set;

public record Prompt(String name, String description, Set<PromptArguments> arguments) {

    public Prompt(String name, String description) {
        this(name, description, null);
    }
}
