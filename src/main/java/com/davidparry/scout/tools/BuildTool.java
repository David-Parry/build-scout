package com.davidparry.scout.tools;

import com.davidparry.scout.ApplicationState;
import com.davidparry.scout.common.ArgumentUtils;
import com.davidparry.scout.spec.Content;
import com.davidparry.scout.spec.InputProperty;
import com.davidparry.scout.spec.JsonRpcRequest;
import com.davidparry.scout.spec.ToolOutputResponse;

import java.io.File;
import java.net.URI;
import java.util.*;

import static com.davidparry.scout.ApplicationState.instance;

public abstract class BuildTool {
    private final List<String> required = new ArrayList<>();
    private final Map<String, InputProperty> properties = new HashMap<>();
    protected final String PROJECT_ROOT = "projectRoot";
    /**
     * Constructor initializes the required list with default values
     * and adds the session_id property
     */
    public BuildTool() {
        addProperty(new InputProperty("session_id", "string", "The unique identifier for the current session, used by the tools to keep a session between multiple calls from the client within when chat conversation.", true));
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
     */
    public void addRequired(String paramName) {
        if (paramName != null && !required.contains(paramName)) {
            required.add(paramName);
        }
    }


    public BuildTool addProperty(InputProperty property) {
        properties.put(property.key(), property);
        if (property.isRequired()) {
            addRequired(property.key());
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

    /**
     * Retrieves the project roots based on the provided arguments.
     * If no project root is specified, it retrieves all roots from the application state.
     *
     * @param args The JSON RPC request containing the project root argument
     * @return A set of project root directories as File objects
     */
    public Set<File> getProjectRoots(JsonRpcRequest args) {
        String projectRoot = ArgumentUtils.getArgument(args, PROJECT_ROOT);
        Set<File> projectRoots = new HashSet<>();
        if (projectRoot == null || projectRoot.isEmpty()) {
            Map<String, URI> paths = ApplicationState.instance().roots();
            for (URI uri : paths.values()) {
                File file = new File(uri);
                projectRoots.add(file);
            }
        } else {
             File projectDir = new File(projectRoot);
            projectRoots.add(projectDir);
        }
           return projectRoots;

    }

}
