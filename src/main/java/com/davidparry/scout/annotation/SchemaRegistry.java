package com.davidparry.scout.annotation;

import com.davidparry.scout.handlers.Handler;
import com.davidparry.scout.tools.Tool;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton registry that stores classes annotated with @Schema along with their metadata.
 * This registry is populated automatically when annotated classes are loaded.
 * It also maintains mappings between JSON-RPC methods and their handlers.
 */
public class SchemaRegistry {
    private static final SchemaRegistry INSTANCE = new SchemaRegistry();
    
    // Store class and its metadata
    private final Map<String, SchemaMetadata> registry = new ConcurrentHashMap<>();
    
    // Store method handlers
    private final Map<String, Handler> methodHandlers = new ConcurrentHashMap<>();
    
    // Private constructor to enforce singleton pattern
    private SchemaRegistry() {}
    
    /**
     * Get the singleton instance of the SchemaRegistry.
     *
     * @return The SchemaRegistry instance
     */
    public static SchemaRegistry getInstance() {
        return INSTANCE;
    }
    

    public void register(Class<?> clazz, String name, String description, Tool handler) {
        String key = name.isEmpty() ? clazz.getSimpleName() : name;
        registry.put(key, new SchemaMetadata(clazz, description, handler));
    }
    
    /**
     * Get a registered schema metadata by name.
     *
     * @param name The name of the schema to retrieve
     * @return The SchemaMetadata if found, null otherwise
     */
    public SchemaMetadata getSchema(String name) {
        return registry.get(name);
    }
    
    /**
     * Get all registered schemas.
     *
     * @return An unmodifiable map of all registered schemas
     */
    public Map<String, SchemaMetadata> getAllSchemas() {
        return Collections.unmodifiableMap(registry);
    }
    
    /**
     * Check if a schema with the given name exists in the registry.
     *
     * @param name The name to check
     * @return true if the schema exists, false otherwise
     */
    public boolean hasSchema(String name) {
        return registry.containsKey(name);
    }
    
    /**
     * Registers a handler for a specific method.
     *
     * @param method The JSON-RPC method name
     * @param handler The handler object that will process requests for this method
     */
    public void registerHandler(String method, Handler handler) {
        methodHandlers.put(method, handler);
    }

    /**
     * Gets the handler for a specific method.
     *
     * @param method The JSON-RPC method name
     * @return The handler object, or null if no handler is registered for the method
     */
    public Handler getHandlerForMethod(String method) {
        return methodHandlers.get(method);
    }

    /**
     * Checks if a handler is registered for a specific method.
     *
     * @param method The JSON-RPC method name
     * @return true if a handler is registered, false otherwise
     */
    public boolean hasHandlerForMethod(String method) {
        return methodHandlers.containsKey(method);
    }

    /**
     * Removes a handler for a specific method.
     *
     * @param method The JSON-RPC method name
     */
    public void unregisterHandler(String method) {
        methodHandlers.remove(method);
    }

    /**
     * Clears all registered handlers.
     */
    public void clearHandlers() {
        methodHandlers.clear();
    }
    
    /**
     * Gets all registered method handlers.
     *
     * @return An unmodifiable map of all registered method handlers
     */
    public Map<String, Object> getAllHandlers() {
        return Collections.unmodifiableMap(methodHandlers);
    }

}
