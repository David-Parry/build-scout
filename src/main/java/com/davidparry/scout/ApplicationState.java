package com.davidparry.scout;

import com.davidparry.scout.spec.RequestParams;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton class to manage application state.
 */
public class ApplicationState implements State {

    // Singleton instance
    private static ApplicationState INSTANCE;

    // State roots
    private final Map<String, URI> roots = new HashMap<>();

    private RequestParams clientInformation = new RequestParams("", null, null, "", null);

    private String jsonrpc = "2.0";

    // Private constructor to prevent instantiation
    private ApplicationState() {

    }

    /**
     * Factory method to get the singleton instance.
     *
     * @return ApplicationState singleton instance
     */
    public static synchronized State instance() {
        if (INSTANCE == null) {
            INSTANCE = new ApplicationState();
        }
        return INSTANCE;
    }

    public String jsonrpc() {
        return jsonrpc;
    }

    public void jsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }

    public RequestParams clientInformation() {
        return clientInformation;
    }

    public void clientInformation(RequestParams clientInformation) {
        this.clientInformation = clientInformation;
    }

    /**
     * Set a root value.
     *
     * @param key The key for the root
     * @param uri The URI value
     */
    public void setRoot(String key, URI uri) {
        roots.put(key, uri);
    }

    /**
     * Get a root value.
     *
     * @param key The key for the root
     * @return The URI value, or null if not present
     */
    public URI getRoot(String key) {
        return roots.get(key);
    }

    public void clearRoots() {
        roots.clear();
    }

}
