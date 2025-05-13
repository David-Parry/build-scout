package com.davidparry.scout.spec;


/**
 * Represents tool capabilities of the server.
 */
public class ToolCapabilities {
    private boolean listChanged;
    
    // Default constructor
    public ToolCapabilities() {
        this(true);
    }
    
    // Constructor with all fields
    public ToolCapabilities(boolean listChanged) {
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
        return "ToolCapabilities[" +
                "listChanged=" + listChanged +
                "]";
    }
}