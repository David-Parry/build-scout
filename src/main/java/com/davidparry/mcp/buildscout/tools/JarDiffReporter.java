package com.davidparry.mcp.buildscout.tools;

import com.davidparry.mcp.buildscout.common.JarDownloader;
import io.modelcontextprotocol.spec.McpSchema;
import japicmp.cmp.JApiCmpArchive;
import japicmp.cmp.JarArchiveComparator;
import japicmp.cmp.JarArchiveComparatorOptions;
import japicmp.model.AccessModifier;
import japicmp.model.JApiChangeStatus;
import japicmp.model.JApiClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

public class JarDiffReporter implements Tool {
    private static final Logger logger = LoggerFactory.getLogger(JarDiffReporter.class);
    private final JarDownloader jarDownloader;

    public JarDiffReporter(JarDownloader jarDownloader) {
        this.jarDownloader = jarDownloader;
    }

    @Override
    public McpSchema.CallToolResult handle(Object args) {
        return handleDiffTool(args);
    }

    @Override
    public String name() {
        return "version_change_analyzer";
    }

    @Override
    public String description() {
        return "Given the groupId, artifactId current version and the latest version of the artifact will return the changes in the public code between the two versions. This allows for a other agents to then check if the code will need to be updated based on the changes.";
    }

    @Override
    public String schema() {
        return "{\"type\":\"object\",\"properties\":{\"groupId\":{\"type\": \"string\",\"description\": \"the maven group id used in maven dependency repository.\"},\"artifactId\":{\"type\": \"string\",\"description\":\"The maven artifact Id used in the maven dependency repository.\"},\"currentVersion\":{\"type\": \"string\",\"description\":\"Current version of the artifact.\"},\"latestVersion\":{\"type\": \"string\",\"description\":\"The latest version of the artifact.\"}},\"required\":[\"groupId\",\"artifactId\",\"latestVersion\",\"currentVersion\"]}";
    }

    /**
     * Validates and extracts request parameters from the input arguments.
     * 
     * @param args The input arguments containing request parameters
     * @return A Map containing the validated parameters or null if validation fails
     */
    private Map<String, String> validateAndExtractParameters(Object args) {
        if (args == null) {
            logger.warn("Request arguments are null");
            return null;
        }
        
        try {
            Map<String, String> request = (Map<String, String>) args;
            if (request == null) {
                logger.warn("Request map is null");
                return null;
            }
            
            Map<String, String> validatedParams = new HashMap<>();
            String[] requiredParams = {"groupId", "artifactId", "currentVersion", "latestVersion"};
            
            for (String param : requiredParams) {
                if (!request.containsKey(param)) {
                    logger.warn("Missing required parameter: {}", param);
                    return null;
                }
                
                String value = request.get(param);
                if (value == null || value.trim().isEmpty()) {
                    logger.warn("Parameter {} has null or empty value", param);
                    return null;
                }
                
                validatedParams.put(param, value);
            }
            
            return validatedParams;
        } catch (ClassCastException e) {
            logger.error("Failed to cast args to Map<String, String>", e);
            return null;
        }
    }


    private McpSchema.CallToolResult handleDiffTool(Object args) {
        Set<String> classesChanged = new HashSet<>();
        logger.debug("Args class: {}", args != null ? args.getClass().getName() : "null");
        List<McpSchema.Content> results = new ArrayList<>();
        boolean error = true;
        
        try {
            Map<String, String> validatedParams = validateAndExtractParameters(args);
            if (validatedParams == null) {
                results.add(new McpSchema.TextContent("Invalid or missing parameters in request"));
                return new McpSchema.CallToolResult(results, true);
            }
            
            String groupId = validatedParams.get("groupId");
            String artifactId = validatedParams.get("artifactId");
            String currentVersion = validatedParams.get("currentVersion");
            String latestVersion = validatedParams.get("latestVersion");

            logger.info("groupId: {}, artifactId: {}, currentVersion: {}, latestVersion: {}", 
                    groupId, artifactId, currentVersion, latestVersion);


                File oldJar = jarDownloader.downloadJar(groupId, artifactId, currentVersion).toFile();
                File newJar = jarDownloader.downloadJar(groupId, artifactId, latestVersion).toFile();

                // Set up comparator options
                JarArchiveComparatorOptions options = new JarArchiveComparatorOptions();
                // Configure options as needed
                options.setIncludeSynthetic(false);
                options.setAccessModifier(AccessModifier.PUBLIC);

                // Create comparator
                JarArchiveComparator comparator = new JarArchiveComparator(options);

                JApiCmpArchive oldArchive = new JApiCmpArchive(oldJar, currentVersion);
                JApiCmpArchive newArchive = new JApiCmpArchive(newJar, latestVersion);
                List<JApiClass> jApiClasses = comparator.compare(oldArchive, newArchive);

                for (JApiClass cls : jApiClasses) {
                    if (cls.getChangeStatus().equals(JApiChangeStatus.MODIFIED) || cls.getChangeStatus().equals(JApiChangeStatus.NEW) || cls.getChangeStatus().equals(JApiChangeStatus.REMOVED)) {
                        classesChanged.add("Class: " + cls.getFullyQualifiedName() + " [" + cls.getChangeStatus() + "]");
                    }
                }
                if (classesChanged.isEmpty()) {
                    results.add(new McpSchema.TextContent("No changes found for " + artifactId));
                    error = false;
                } else {
                    error = false;
                    for (String clazz : classesChanged) {
                        results.add(new McpSchema.TextContent(clazz));
                    }
                }
        } catch (Exception e) {
            results.add(new McpSchema.TextContent("Error finding diffs " + e.getMessage()));
            logger.error("Failed to find diffs on files ", e);
        }
        return new McpSchema.CallToolResult(results, error);

    }

}
