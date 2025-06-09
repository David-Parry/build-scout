package com.davidparry.scout.annotation;

import com.davidparry.scout.tools.Tool;

/**
 * Class to hold metadata about a registered schema.
 */
public class SchemaMetadata {
    private final Class<?> clazz;
    private final String description;
    private final Tool handler;

    public SchemaMetadata(Class<?> clazz, String description, Tool handler) {
        this.clazz = clazz;
        this.description = description;
        this.handler = handler;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public String getDescription() {
        return description;
    }

    public Tool getTool() {
        return handler;
    }
}
