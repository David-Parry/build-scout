package com.davidparry.mcp.buildscout.tools;

import java.util.*;

public abstract class BuildTool implements Tool {
    private final List<String> required = new ArrayList<>();
    private final Map<String, Object> properties = new HashMap<>();

    /**
     * Constructor initializes the required list with default values
     * and adds the session_id property
     */
    public BuildTool() {
        required.add("session_id");
        properties.put("session_id", createProperty("string", "The unique identifier for the current session, used by the tools to keep a session between multiple calls from the client within when chat conversation."));
    }

    /**
     * Returns an unmodifiable view of the required parameters list.
     *
     * @return List of required parameter names
     */
    public List<String> getRequired() {
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

    /**
     * Helper method to create a property map for JSON schema.
     *
     * @param type        the type of the property (e.g., "string")
     * @param description the description of the property
     * @return a LinkedHashMap representing the property
     */
    public Map<String, Object> createProperty(String type, Object description) {
        Map<String, Object> property = new LinkedHashMap<>();
        property.put("type", type);
        property.put("description", description);
        return property;
    }
    /**
     * Adds a property with the specified key, type, and description to the properties map.
     * If the property key should be required, it will also be added to the required list.
     *
     * @param key         the property key
     * @param type        the type of the property (e.g., "string", "integer", "boolean")
     * @param description the description of the property
     * @param isRequired  whether the property is required
     * @return the BuildTool instance for method chaining
     */
    public BuildTool addProperty(String key, String type, String description, boolean isRequired) {
        properties.put(key, createProperty(type, description));
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
    public Map<String, Object> getProperties() {
        return Collections.unmodifiableMap(properties);
    }
}
