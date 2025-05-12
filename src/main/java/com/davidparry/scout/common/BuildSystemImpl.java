package com.davidparry.scout.common;

import com.davidparry.scout.spec.Content;
import com.davidparry.scout.spec.JsonRpcTextResponse;
import com.davidparry.scout.spec.ToolOutputResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class BuildSystemImpl implements BuildSystem {

    @Override
    public DiscoveredPath discover(String pathStr) {
        PathType type = PathType.NA;
        Optional<Path> discoverPath = Optional.empty();
        try {
            Path path = Paths.get(pathStr);
            discoverPath = Optional.of(path);
            if (Files.exists(path)) {
                if (Files.isDirectory(path)) {
                    type = PathType.DIRECTORY;
                } else {
                    type = PathType.FILE;
                }
            }
        } catch (Exception e) {
        }
        return new DiscoveredPath(type, discoverPath);
    }

    /**
     * Recursively walks a directory to find build files, skipping hidden directories
     *
     * @param directory The directory to walk
     * @param builds    The set to add build systems to
     */
    public void walkDirectory(Path directory, Set<BuildFile> builds) {
        try {
            Files.walk(directory).filter(path -> !isHiddenDirectory(path)).filter(Files::isRegularFile).forEach(file -> checkBuildFile(file.toString(), builds));
        } catch (Exception e) {
        }
    }

    /**
     * Checks if a path is a hidden directory (starts with a dot)
     *
     * @param path The path to check
     * @return true if the path is a hidden directory, false otherwise
     */
    private boolean isHiddenDirectory(Path path) {
        // Skip the initial check for the root directory being processed
        if (Files.isDirectory(path)) {
            String fileName = path.getFileName().toString();
            return fileName.startsWith(".");
        }
        // For files, check if any parent directory (except the root) is hidden
        Path parent = path.getParent();
        while (parent != null && !parent.equals(path.getRoot())) {
            String dirName = parent.getFileName().toString();
            if (dirName.startsWith(".")) {
                return true;
            }
            parent = parent.getParent();
        }
        return false;
    }

    /**
     * Checks if a file is a build file and adds the corresponding build system to the set
     *
     * @param path   The file path to check
     * @param builds The set to add build systems to
     */
    public void checkBuildFile(String path, Set<BuildFile> builds) {
        String type = identifyBuildFile(path);
        if (type != null) {
            builds.add(new BuildFile(path, type));
        }
    }

    public String identifyBuildFile(String path) {
        if (path.endsWith("pom.xml")) {
            return MAVEN;
        } else if (path.endsWith("build.gradle")) {
            return GRADLE_GROOVY;
        } else if (path.endsWith("build.gradle.kts")) {
            return GRADLE_KOTLIN;
        } else if (path.endsWith("package.json")) {
            return NPM_YARN;
        } else if (path.endsWith("Cargo.toml")) {
            return CARGO;
        } else if (path.endsWith("requirements.txt") || path.endsWith("setup.py")) {
            return PYTHON;
        } else if (path.endsWith("Makefile")) {
            return MAKEFILE;
        } else if (path.endsWith("CMakeLists.txt")) {
            return CMAKE;
        } else {
            return null;
        }
    }


    /**
     * Creates a CallToolResult containing information about detected build systems.
     *
     * @param builds A set of build system types that were detected
     * @return A CallToolResult containing the build system information
     * with error flag set to true if no build systems were found
     */
    public ToolOutputResponse createTypeResults(Set<BuildFile> builds) {
        List<Content> results = new ArrayList<>();
        boolean error = true;
        if (builds.isEmpty()) {
            results.add(new Content("No build system found"));
        } else {
            error = false;
            for (BuildFile file : builds) {
                results.add(new Content(file.type()));
            }
        }
        return new ToolOutputResponse(results, error);
    }

    public ToolOutputResponse createPathResults(Set<BuildFile> builds) {
        List<Content> results = new ArrayList<>();
        boolean error = true;
        if (builds.isEmpty()) {
            results.add(new Content("No build system found"));
        } else {
            error = false;
            for (BuildFile file : builds) {
                results.add(new Content(file.path()));
            }
        }
        return new ToolOutputResponse(results, error);
    }

    /**
     * Processes a root folder path to find build files and adds their absolute paths to the provided set.
     * If the path is a directory, it walks through it to find build files, skipping hidden directories.
     * If the path is a file, it checks if it's a build file directly.
     *
     * @param rootPath      The string path to the root folder to process
     * @param absolutePaths The set to collect absolute paths of found build files
     * @return The number of build files found
     */
    public int processRootFolder(String rootPath, Set<String> absolutePaths) {
        int initialSize = absolutePaths.size();

        DiscoveredPath discoveredPath = discover(rootPath);
        if (discoveredPath.type() == PathType.DIRECTORY && discoveredPath.path().isPresent()) {
            Path directoryPath = discoveredPath.path().get();
            // Create a wrapper set that adds the absolute path to the provided set
            // when a build file is found via checkBuildFile
            Set<BuildFile> buildSystemTypes = new HashSet<>();

            // Custom implementation of walkDirectory that tracks absolute paths
            try {
                Files.walk(directoryPath).filter(path -> !isHiddenDirectory(path)).filter(Files::isRegularFile).forEach(file -> {
                    String filePath = file.toString();
                    // Track the original size of the build systems set
                    int beforeSize = buildSystemTypes.size();
                    checkBuildFile(filePath, buildSystemTypes);
                    // If a build file was found (buildSystemTypes size increased), add the path
                    if (buildSystemTypes.size() > beforeSize) {
                        absolutePaths.add(filePath);
                    }
                });
            } catch (Exception e) {
            }
        } else if (discoveredPath.type() == PathType.FILE && discoveredPath.path().isPresent()) {
            // If it's a single file, check if it's a build file
            String filePath = discoveredPath.path().get().toString();
            Set<BuildFile> buildSystemTypes = new HashSet<>();
            checkBuildFile(filePath, buildSystemTypes);

            // If it's a build file, add its path to the set
            if (!buildSystemTypes.isEmpty()) {
                absolutePaths.add(filePath);
            }
        } else {
        }

        return absolutePaths.size() - initialSize;
    }


    public Set<BuildFile> identifyBuildFiles(List<String> potentialPaths) {
        Set<BuildFile> builds = new HashSet<>();

        for (String pathStr : potentialPaths) {
            DiscoveredPath discoveredPath = discover(pathStr);
            switch (discoveredPath.type()) {
                case DIRECTORY:
                    discoveredPath.path().ifPresent(path -> walkDirectory(path, builds));
                    break;
                case FILE:
                    discoveredPath.path().ifPresent(path -> checkBuildFile(path.toString(), builds));
                    break;
                case NA:
                    break;
                default:
                    break;
            }
        }
        return builds;
    }

    public String updateDependencyVersion(String groupId, String artifactId, String version, String path) {

        Path buildFilePath = Paths.get(path);
        String type = identifyBuildFile(path);

        if (GRADLE_GROOVY.equals(type)) {
            try {
                List<String> lines = Files.readAllLines(buildFilePath);
                //String groupArtifact = groupId + ":" + artifactId;
                boolean updated = false;
                List<String> updatedLines = new ArrayList<>();

                for (String line : lines) {
                    String trimmed = line.trim();
                    // Handles both single and double quotes, and optional whitespace
                    if (trimmed.matches(".*['\"]" + groupId + ":" + artifactId + ":[^'\"]+['\"].*")) {
                        // Replace the version part
                        String updatedLine = line.replaceAll("(['\"]" + groupId + ":" + artifactId + "):[^'\"]+(['\"])", "$1:" + version + "$2");
                        updatedLines.add(updatedLine);
                        updated = true;
                    } else if (trimmed.matches("^id\\s+['\"]" + groupId + "['\"]\\s+version\\s+['\"][^'\"]+['\"].*")) {
                        // Handle Gradle plugin declarations: id 'org.graalvm.buildtools.native' version '0.10.4'
                        String updatedLine = line.replaceAll("(id\\s+['\"]" + groupId + "['\"]\\s+version\\s+['\"])[^'\"]+(['\"])", "$1" + version + "$2");
                        updatedLines.add(updatedLine);
                        updated = true;
                    } else {
                        updatedLines.add(line);
                    }
                }

                if (updated) {
                    Files.write(buildFilePath, updatedLines);
                    return "Dependency " + groupId + ":" + artifactId + " updated to version " + version + " in " + path;
                } else {
                    return "Dependency " + groupId + ":" + artifactId + " not found in " + path;
                }
            } catch (IOException e) {
                return "Failed to update Gradle build file: " + e.getMessage();
            }
        } else {
            return "Unsupported build file type for: " + path;
        }
    }


}
