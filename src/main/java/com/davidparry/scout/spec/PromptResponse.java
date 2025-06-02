package com.davidparry.scout.spec;

import java.util.Set;

public record PromptResponse(String description, Set<PromptMessage> messages) {
}
