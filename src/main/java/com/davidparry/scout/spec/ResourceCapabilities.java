package com.davidparry.scout.spec;

/**
 * Represents resource capabilities of the server.
 */
public class ResourceCapabilities {
    private boolean subscribe;

    private boolean listChanged;

    // Default constructor
    public ResourceCapabilities() {
    }

    // Constructor with all fields
    public ResourceCapabilities(boolean subscribe, boolean listChanged) {
        this.subscribe = subscribe;
        this.listChanged = listChanged;
    }

    // Getters and Setters
    public boolean isSubscribe() {
        return subscribe;
    }

    public void setSubscribe(boolean subscribe) {
        this.subscribe = subscribe;
    }

    public boolean isListChanged() {
        return listChanged;
    }

    public void setListChanged(boolean listChanged) {
        this.listChanged = listChanged;
    }

    @Override
    public String toString() {
        return "ResourceCapabilities[" +
                "subscribe=" + subscribe +
                ", listChanged=" + listChanged +
                "]";
    }
}
