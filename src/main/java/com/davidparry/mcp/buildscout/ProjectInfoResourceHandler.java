package com.davidparry.mcp.buildscout;

import io.modelcontextprotocol.server.McpServerFeatures;

/**
 * ResourceHandler implementation for providing project information.
 * This handler responds to requests for the "project://info" resource.
 */
public class ProjectInfoResourceHandler implements ProjectExplorer.ResourceHandler {

    private final String projectName;
    private final String projectVersion;
    private final String projectDescription;

    /**
     * Creates a new ProjectInfoResourceHandler with the specified project details.
     *
     * @param projectName the name of the project
     * @param projectVersion the version of the project
     * @param projectDescription the description of the project
     */
    public ProjectInfoResourceHandler(String projectName, String projectVersion, String projectDescription) {
        this.projectName = projectName;
        this.projectVersion = projectVersion;
        this.projectDescription = projectDescription;
    }

    @Override
    public McpServerFeatures.SyncResourceSpecification createResourceSpecification(String resourceUri) {
       return null;
    }


}
