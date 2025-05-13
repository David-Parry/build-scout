package com.davidparry.scout.spec;

/**
 * Represents prompt capabilities of the server.
 */
public class PromptCapabilities {
    private boolean listChanged;

    // Default constructor
    public PromptCapabilities() {
    }

    // Constructor with all fields
    public PromptCapabilities(boolean listChanged) {
        this.listChanged = listChanged;
    }

    // Getters and Setters
    public boolean isListChanged() {
        return listChanged;
    }

    public void setListChanged(boolean listChanged) {
        this.listChanged = listChanged;
    }

    @Override
    public String toString() {
        return "PromptCapabilities[" +
                "listChanged=" + listChanged +
                "]";
    }
}
