package com.davidparry.scout.spec;


/**
 * Represents tool capabilities of the server.
 */
public class PromptsCapabilities {
    private boolean listChanged;

    // Default constructor
    public PromptsCapabilities() {
        this(true);
    }

    // Constructor with all fields
    public PromptsCapabilities(boolean listChanged) {
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
        return "PromptsCapabilities[" +
                "listChanged=" + listChanged +
                "]";
    }
}