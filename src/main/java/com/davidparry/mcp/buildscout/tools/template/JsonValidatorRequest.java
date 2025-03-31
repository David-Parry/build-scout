package com.davidparry.mcp.buildscout.tools.template;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request class for the JSON validator tool.
 */
public class JsonValidatorRequest {
    @JsonProperty(required = true)
    private String jsonString;
    
    private boolean verbose = false;

    public String getJsonString() {
        return jsonString;
    }

    public void setJsonString(String jsonString) {
        this.jsonString = jsonString;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
}