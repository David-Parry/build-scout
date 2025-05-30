package com.davidparry.scout.common;

import com.davidparry.scout.spec.ToolOutputResponse;

import java.io.File;
import java.nio.file.Path;
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

    ToolOutputResponse createTypeResults(Set<BuildFile> builds);

    int processRootFolder(String rootPath, Set<String> absolutePaths);

    Set<BuildFile> identifyBuildFiles(Set<String> potentialPaths);

    ToolOutputResponse createPathResults(Set<BuildFile> builds);

    String updateDependencyVersion(String groupId, String artifactId, String version, String path);

    String identifyBuildFile(String path);

    Set<BuildFile> onlyBuildFilesFilter(Set<File> files);
}
