package com.davidparry.mcp.buildscout;

import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.*;
import io.modelcontextprotocol.spec.McpServerTransportProvider;
import io.modelcontextprotocol.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * ProjectExplorer provides functionality to explore and interact with projects
 * using the Model Context Protocol.
 */
public class ProjectExplorer implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(ProjectExplorer.class);

    private final McpSyncServer server;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private McpSyncServerExchange currentExchange;

    /**
     * Creates a new ProjectExplorer with the specified transport provider.
     *
     * @param transportProvider the MCP transport provider
     * @throws IllegalArgumentException if transportProvider is null
     */
    public ProjectExplorer(McpServerTransportProvider transportProvider, String name, String version) {
        Assert.notNull(transportProvider, "Transport provider must not be null");
        // Configure the server with proper capabilities
        this.server = McpServer.sync(transportProvider)
                .serverInfo(name, version)
                .capabilities(ServerCapabilities.builder()
                        .tools(false)
                        .resources(false, false)
                        .prompts(false)
                        .logging()
                        .experimental(new HashMap<>())
                        .build())
                .build();
        this.isRunning.set(true);
    }


    public ProjectExplorer addTool(String toolName, String description, McpSchema.JsonSchema jsonSchema, ToolHandler handler) {
        Assert.notNull(toolName, "Tool name must not be null");
        Assert.notNull(description, "Tool description must not be null");
        Assert.notNull(jsonSchema, "JSON schema must not be null");
        Assert.notNull(handler, "Tool handler must not be null");

        Tool tool = new McpSchema.Tool(toolName, description, jsonSchema);
        server.addTool(new McpServerFeatures.SyncToolSpecification(tool, (exchange, args) -> {
            this.currentExchange = exchange;
            try {
                return handler.handleToolCall(args);
            } catch (Exception e) {
                // Log the exception and return an error result
                log(LoggingLevel.ERROR, "Error handling tool call: " + e.getMessage());
                return CallToolResult.builder().addTextContent("Error processing tool call: " + e.getMessage()).build();

            }
        }));

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
     * @param resourceUri     the URI of the resource
     * @param resourceHandler the handler for the resource
     * @return this ProjectExplorer instance for method chaining
     */
    public ProjectExplorer addResource(String resourceUri, ResourceHandler resourceHandler) {
        Assert.notNull(resourceUri, "Resource URI must not be null");
        Assert.notNull(resourceHandler, "Resource handler must not be null");

        // Convert the ResourceHandler to a SyncResourceSpecification that the server can accept
        McpServerFeatures.SyncResourceSpecification resourceSpec = resourceHandler.createResourceSpecification(resourceUri);

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
     * @param level   the logging level
     * @param message the message to log
     * @return this ProjectExplorer instance for method chaining
     */
    public ProjectExplorer log(LoggingLevel level, String message) {
        Assert.notNull(level, "Logging level must not be null");
        Assert.notNull(message, "Message must not be null");

        LoggingMessageNotification notification = LoggingMessageNotification.builder().level(level).logger("buildscout") // Using the server name as logger
                .data(message)        // The message is passed as the data parameter
                .build();

        if (currentExchange != null) {
            try {
                // Use the exchange-specific method instead of the deprecated server-wide method
                currentExchange.loggingNotification(notification);
            } catch (Exception e) {
                logger.debug("Error logging notification: ", e);
            }
        }
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
            try {
                server.closeGracefully();
            } catch (Exception e) {
                logger.error("Error closing server gracefully", e);
                server.close();
            }
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
