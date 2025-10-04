package com.davidparry.scout;

import com.davidparry.scout.common.LogFactory;
import com.davidparry.scout.io.ApplicationLogger;
import com.davidparry.scout.io.LogFileWriter;
import com.davidparry.scout.io.Logger;
import com.davidparry.scout.prompts.Prompt;
import com.davidparry.scout.spec.IdType;
import com.davidparry.scout.spec.RequestParams;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Singleton class to manage application state.
 */
public class ApplicationState implements State {

    // Singleton instance
    private static ApplicationState INSTANCE;
    private final Logger logger = new ApplicationLogger().getLogger(LogFileWriter.getInstance(new LogFactory()));
    private final Map<String, Prompt> prompts = new HashMap<>();
    // State roots
    private final Map<String, URI> roots = new HashMap<>();
    private final AtomicLong idCounter = new AtomicLong(0);
    private String version = "1.0.0";
    private RequestParams clientInformation = new RequestParams("", null, null, "", null);
    private String jsonrpc = "2.0";
    // ID policy fields
    private IdType idType = IdType.UNKNOWN;

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

    public boolean hasRootCapability() {
        if (clientInformation == null || clientInformation.capabilities() == null || clientInformation
                .capabilities()
                .roots() == null) {
            logger.log("NO capabilities provided");
            return false;
        } else {
            logger.log("HAS capabilities provided");
            return true;
        }

    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public void setVersion(String version) {
        this.version = version;
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

    public Map<String, URI> roots() {
        return Map.copyOf(roots);
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

    public Map<String, Prompt> prompts() {
        return Map.copyOf(prompts);
    }

    public Prompt getPrompt(String key) {
        return prompts.get(key);
    }

    public void setPrompt(String key, Prompt prompt) {
        prompts.put(key, prompt);
    }

    // ID policy methods
    @Override
    public synchronized void observeIdIfUnset(JsonElement id) {
        if (idType != IdType.UNKNOWN || id == null || !id.isJsonPrimitive()) {
            return;
        }

        var prim = id.getAsJsonPrimitive();
        if (prim.isNumber()) {
            idType = IdType.NUMERIC;
            logger.log("ID policy set to NUMERIC based on first message " + idType);
        } else if (prim.isString()) {
            idType = IdType.STRING;
            logger.log("ID policy set to STRING based on first message " + idType);
        }
    }

    @Override
    public synchronized JsonElement nextId() {
        long next = idCounter.incrementAndGet();
        if (idType == IdType.NUMERIC) {
            return new JsonPrimitive(next);
        }
        // Default to STRING for UNKNOWN and STRING
        return new JsonPrimitive(String.valueOf(next));
    }

    @Override
    public synchronized IdType getIdType() {
        return idType;
    }
}
