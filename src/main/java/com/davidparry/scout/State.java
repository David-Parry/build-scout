package com.davidparry.scout;

import com.davidparry.scout.prompts.Prompt;
import com.davidparry.scout.spec.IdType;
import com.davidparry.scout.spec.RequestParams;
import com.google.gson.JsonElement;

import java.net.URI;
import java.util.Map;

public interface State {

    RequestParams clientInformation();
    void setRoot(String key, URI uri);
    URI getRoot(String key);

    void jsonrpc(String jsonrpc);

    String jsonrpc();

    void clientInformation(RequestParams clientInformation);

    void clearRoots();

    Map<String, URI> roots();

    Map<String, Prompt> prompts();

    void setPrompt(String key, Prompt prompt);

    Prompt getPrompt(String key);

    boolean hasRootCapability();

    void setVersion(String version);

    String getVersion();

    // ID policy methods
    void observeIdIfUnset(JsonElement id);
    JsonElement nextId();
    IdType getIdType();

}
