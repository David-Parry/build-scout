package com.davidparry.mcp.buildscout.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Content;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A tool for listing and providing information about resources in the project.
 */
public class GetResourceInfo implements Tool {
    private static final Logger logger = LoggerFactory.getLogger(GetResourceInfo.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public CallToolResult handle(Object args) {
        logger.debug("GetResourceInfo tool called with args: {}", args);
        return getResourceInfo(args);
    }

    @Override
    public String name() {
        return "get_resource_info";
    }

    @Override
    public String description() {
        return "Lists available resources and provides information about them";
    }

    @Override
    public String schema() {
        return "{\"type\":\"object\",\"properties\":{\"path\":{\"type\":\"string\",\"description\":\"Optional path to list resources from\"},\"recursive\":{\"type\":\"boolean\",\"description\":\"Whether to list resources recursively\"}}}";
    }

    private CallToolResult getResourceInfo(Object args) {
        List<Content> results = new ArrayList<>();
        
        try {
            // Extract parameters from args
            JsonNode argsNode = objectMapper.valueToTree(args);
            String path = argsNode.has("path") ? argsNode.get("path").asText() : ".";
            boolean recursive = argsNode.has("recursive") && argsNode.get("recursive").asBoolean();
            
            // Get the resources
            List<Path> resources = listResources(path, recursive);
            
            // Create a JSON response
            ObjectNode responseJson = objectMapper.createObjectNode();
            ArrayNode resourcesArray = responseJson.putArray("resources");
            
            for (Path resource : resources) {
                ObjectNode resourceNode = resourcesArray.addObject();
                resourceNode.put("path", resource.toString());
                resourceNode.put("name", resource.getFileName().toString());
                resourceNode.put("isDirectory", Files.isDirectory(resource));
                if (!Files.isDirectory(resource)) {
                    resourceNode.put("size", Files.size(resource));
                    resourceNode.put("lastModified", Files.getLastModifiedTime(resource).toMillis());
                }
            }
            
            results.add(new TextContent(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(responseJson)));
            
        } catch (Exception e) {
            logger.error("Error in GetResourceInfo tool", e);
            results.add(new TextContent("Error processing request: " + e.getMessage()));
        }
        
        return new CallToolResult(results, true);
    }
    
    private List<Path> listResources(String pathStr, boolean recursive) throws Exception {
        Path path = Paths.get(pathStr).toAbsolutePath().normalize();
        
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("Path does not exist: " + path);
        }
        
        if (Files.isDirectory(path)) {
            try (Stream<Path> stream = recursive ? Files.walk(path) : Files.list(path)) {
                return stream.collect(Collectors.toList());
            }
        } else {
            return List.of(path);
        }
    }
}