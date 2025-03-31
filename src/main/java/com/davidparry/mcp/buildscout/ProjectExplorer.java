package com.davidparry.mcp.buildscout;

import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.LoggingMessageNotification;
import io.modelcontextprotocol.spec.McpSchema.LoggingLevel;
import io.modelcontextprotocol.spec.McpServerTransportProvider;
import io.modelcontextprotocol.util.Assert;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ProjectExplorer provides functionality to explore and interact with projects
 * using the Model Context Protocol.
 */
public class ProjectExplorer implements AutoCloseable {
    
    private final McpSyncServer server;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    
    /**
     * Creates a new ProjectExplorer with the specified transport provider.
     *
     * @param transportProvider the MCP transport provider
     * @throws IllegalArgumentException if transportProvider is null
     */
    public ProjectExplorer(McpServerTransportProvider transportProvider) {
        Assert.notNull(transportProvider, "Transport provider must not be null");
        this.server = McpServer.sync(transportProvider)
                .serverInfo("mcp-explorer-server", "0.1.10")
                .capabilities(ServerCapabilities.builder()
                        .tools(true)
                        .resources(false, false)
                        .prompts(false)
                        .logging()
                        .build())
                .build();
        this.isRunning.set(true);
    }
    
    /**
     * Adds a tool to the server.
     *
     * @param toolName the name of the tool
     * @param description the description of the tool
     * @param jsonSchema the JSON schema for the tool's parameters
     * @param handler the handler for tool invocations
     * @return this ProjectExplorer instance for method chaining
     */
    public ProjectExplorer addTool(String toolName, String description, String jsonSchema, 
                                  ToolHandler handler) {
        Assert.notNull(toolName, "Tool name must not be null");
        Assert.notNull(description, "Tool description must not be null");
        Assert.notNull(jsonSchema, "JSON schema must not be null");
        Assert.notNull(handler, "Tool handler must not be null");
        
        Tool tool = new McpSchema.Tool(toolName, description, jsonSchema);
        server.addTool(new McpServerFeatures.SyncToolSpecification(
            tool,
            (exchange, args) -> handler.handleToolCall(args)
        ));
        
        return this;
    }
    
    /**
     * Removes a tool from the server.
     *
     * @param toolName the name of the tool to remove
     * @return this ProjectExplorer instance for method chaining
     */
    public ProjectExplorer removeTool(String toolName) {
        Assert.notNull(toolName, "Tool name must not be null");
        server.removeTool(toolName);
        return this;
    }
    
    /**
     * Notifies clients that the tools list has changed.
     *
     * @return this ProjectExplorer instance for method chaining
     */
    public ProjectExplorer notifyToolsListChanged() {
        server.notifyToolsListChanged();
        return this;
    }
    
    /**
     * Adds a resource to the server.
     *
     * @param resourceUri the URI of the resource
     * @param resourceHandler the handler for the resource
     * @return this ProjectExplorer instance for method chaining
     */
    public ProjectExplorer addResource(String resourceUri, ResourceHandler resourceHandler) {
        Assert.notNull(resourceUri, "Resource URI must not be null");
        Assert.notNull(resourceHandler, "Resource handler must not be null");
        
        // Convert the ResourceHandler to a SyncResourceSpecification that the server can accept
        McpServerFeatures.SyncResourceSpecification resourceSpec = 
            resourceHandler.createResourceSpecification(resourceUri);
        
        // Add the resource specification to the server
        server.addResource(resourceSpec);
        
        return this;
    }
    
    /**
     * Removes a resource from the server.
     *
     * @param resourceUri the URI of the resource to remove
     * @return this ProjectExplorer instance for method chaining
     */
    public ProjectExplorer removeResource(String resourceUri) {
        Assert.notNull(resourceUri, "Resource URI must not be null");
        server.removeResource(resourceUri);
        return this;
    }
    
    /**
     * Notifies clients that the resources list has changed.
     *
     * @return this ProjectExplorer instance for method chaining
     */
    public ProjectExplorer notifyResourcesListChanged() {
        server.notifyResourcesListChanged();
        return this;
    }
    

    
    /**
     * Logs a message with the specified level.
     *
     * @param level the logging level
     * @param message the message to log
     * @return this ProjectExplorer instance for method chaining
     */
    public ProjectExplorer log(LoggingLevel level, String message) {
        Assert.notNull(level, "Logging level must not be null");
        Assert.notNull(message, "Message must not be null");
        
        LoggingMessageNotification notification = LoggingMessageNotification.builder()
                .level(level)
                .logger("mcp-explorer-server") // Using the server name as logger
                .data(message)              // The message is passed as the data parameter
                .build();
        server.loggingNotification(notification);
        return this;
    }
    
    /**
     * Gets the underlying McpSyncServer.
     *
     * @return the McpSyncServer
     */
    public McpSyncServer getServer() {
        return server;
    }
    
    /**
     * Checks if the server is running.
     *
     * @return true if the server is running, false otherwise
     */
    public boolean isRunning() {
        return isRunning.get();
    }
    
    /**
     * Closes the server gracefully.
     */
    @Override
    public void close() {
        if (isRunning.getAndSet(false)) {
            server.closeGracefully();
        }
    }
    
    /**
     * Closes the server immediately.
     */
    public void closeImmediately() {
        if (isRunning.getAndSet(false)) {
            server.close();
        }
    }
    
    /**
     * Interface for handling tool calls.
     */
    public interface ToolHandler {
        CallToolResult handleToolCall(Object args);
    }
    
    /**
     * Interface for handling resources.
     */
    public interface ResourceHandler {
        McpServerFeatures.SyncResourceSpecification createResourceSpecification(String resourceUri);
    }
    
    /**
     * Interface for handling prompts.
     */
    public interface PromptHandler {
        McpSchema.Prompt createPromptSpecification(String promptName);
    }
}
