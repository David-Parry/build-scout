package com.davidparry.scout.tools;

import com.davidparry.scout.spec.Content;
import com.davidparry.scout.spec.InputProperty;
import com.davidparry.scout.spec.ToolOutputResponse;

import java.util.*;

public abstract class BuildTool  {
    private final List<String> required = new ArrayList<>();
    private final Map<String, InputProperty> properties = new HashMap<>();

    /**
     * Constructor initializes the required list with default values
     * and adds the session_id property
     */
    public BuildTool() {
        required.add("session_id");
        InputProperty sessionId = new InputProperty("string", "The unique identifier for the current session, used by the tools to keep a session between multiple calls from the client within when chat conversation.");
        properties.put("session_id", sessionId);
    }

    /**
     * Returns an unmodifiable view of the required parameters list.
     *
     * @return List of required parameter names
     */
    protected List<String> getRequired() {
        return Collections.unmodifiableList(required);
    }

    /**
     * Adds a parameter name to the required list.
     *
     * @param paramName The name of the parameter to add to the required list
     * @return true if the parameter was added, false if it was already in the list
     */
    public boolean addRequired(String paramName) {
        if (paramName != null && !required.contains(paramName)) {
            return required.add(paramName);
        }
        return false;
    }


    public BuildTool addProperty(String key, InputProperty property, boolean isRequired) {
        properties.put(key, property);
        if (isRequired) {
            addRequired(key);
        }
        return this;
    }

    /**
     * Returns an unmodifiable view of the properties map.
     *
     * @return Map of property names to property definitions
     */
    public Map<String, InputProperty> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    public ToolOutputResponse createErrorResult(String errorMessage) {
        List<Content> results = new ArrayList<>();
        results.add(new Content("Error: " + errorMessage));
        return new ToolOutputResponse(results, true);
    }



}
