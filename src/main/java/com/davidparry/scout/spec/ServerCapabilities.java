package com.davidparry.scout.spec;


/**
 * Represents the capabilities of the server.
 */
public class ServerCapabilities {

    private ToolCapabilities tools;

    // Default constructor
    public ServerCapabilities() {
    }

    // Constructor with all fields
    public ServerCapabilities(ToolCapabilities tools) {
        this.tools = tools;
    }


    public ToolCapabilities getTools() {
        return tools;
    }

    public void setTools(ToolCapabilities tools) {
        this.tools = tools;
    }

}