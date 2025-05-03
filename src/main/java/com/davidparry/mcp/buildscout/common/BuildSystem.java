package com.davidparry.mcp.buildscout.common;

import io.modelcontextprotocol.spec.McpSchema;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public interface BuildSystem {
    // String constants for build system names
    String MAVEN = "Maven";
    String GRADLE_GROOVY = "Gradle/Groovy";
    String GRADLE_KOTLIN = "Gradle/Kotlin";
    String NPM_YARN = "NPM/Yarn";
    String CARGO = "Cargo";
    String PYTHON = "Python";
    String MAKEFILE = "Makefile";
    String CMAKE = "CMake";

    DiscoveredPath discover(String path);

    void walkDirectory(Path directory, Set<BuildFile> builds);

    void checkBuildFile(String path, Set<BuildFile> builds);

    McpSchema.CallToolResult createTypeResults(Set<BuildFile> builds);

    int processRootFolder(String rootPath, Set<String> absolutePaths);

    Set<BuildFile> identifyBuildFiles(List<String> potentialPaths);

    McpSchema.CallToolResult createPathResults(Set<BuildFile> builds);

    String updateDependencyVersion(String groupId, String artifactId, String version, String path);

    String identifyBuildFile(String path);
}
