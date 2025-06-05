package com.davidparry.scout.spec;


/**
 * Represents the capabilities of the server.
 */
public class ServerCapabilities {

    private ToolCapabilities tools;
    private PromptsCapabilities prompts;
    // Default constructor
    public ServerCapabilities() {
    }

    // Constructor with all fields
    public ServerCapabilities(ToolCapabilities tools, PromptsCapabilities prompts) {
        this.tools = tools;
        this.prompts = prompts;
    }


    public ToolCapabilities getTools() {
        return tools;
    }

    public void setTools(ToolCapabilities tools) {
        this.tools = tools;
    }

}