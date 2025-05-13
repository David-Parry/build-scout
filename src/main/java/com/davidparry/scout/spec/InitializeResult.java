package com.davidparry.scout.spec;

/**
 * Represents the result of an initialization request to the server.
 */
public class InitializeResult {
    private String protocolVersion;

    private ServerCapabilities capabilities;

    private Implementation serverInfo;

    // Default constructor
    public InitializeResult() {
    }

    // Constructor with all fields
    public InitializeResult(String protocolVersion, ServerCapabilities capabilities, Implementation serverInfo) {
        this.protocolVersion = protocolVersion;
        this.capabilities = capabilities;
        this.serverInfo = serverInfo;
    }

    // Getters and Setters
    public String getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public ServerCapabilities getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(ServerCapabilities capabilities) {
        this.capabilities = capabilities;
    }

    public Implementation getServerInfo() {
        return serverInfo;
    }

    public void setServerInfo(Implementation serverInfo) {
        this.serverInfo = serverInfo;
    }


}