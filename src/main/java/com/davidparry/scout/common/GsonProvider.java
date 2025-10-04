package com.davidparry.scout.common;

import com.google.gson.Gson;

/**
 * Centralized provider for Gson instances to avoid creating multiple instances throughout the application.
 */
public final class GsonProvider {
    
    private static final Gson INSTANCE = new Gson();
    
    private GsonProvider() {
        // Utility class - prevent instantiation
    }
    
    /**
     * Get the shared Gson instance.
     * 
     * @return The shared Gson instance
     */
    public static Gson getInstance() {
        return INSTANCE;
    }
}