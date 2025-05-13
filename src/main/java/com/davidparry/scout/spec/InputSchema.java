package com.davidparry.scout.spec;

import java.util.List;
import java.util.Map;

public record InputSchema(String type, Map<String, InputProperty> properties, List<String> required) {
    
    /**
     * Compact constructor that ensures type is "object" if null is provided
     */
    public InputSchema {
        if (type == null) {
            type = "object";
        }
    }
    
    /**
     * Static factory method to create an InputSchema with default type "object"
     * 
     * @param properties The properties map
     * @param required The list of required property names
     * @return A new InputSchema with type "object"
     */
    public static InputSchema createDefault(Map<String, InputProperty> properties, List<String> required) {
        return new InputSchema("object", properties, required);
    }
}
