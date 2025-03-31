package com.davidparry.mcp.buildscout.tools;

import com.davidparry.mcp.buildscout.common.BuildFile;
import com.davidparry.mcp.buildscout.common.DiscoveredPath;
import com.davidparry.mcp.buildscout.common.PathType;
import io.modelcontextprotocol.spec.McpSchema;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BuildSystemImpl implements BuildSystem {
  private static final Logger logger = LoggerFactory.getLogger(BuildSystemImpl.class);


  @Override
  public DiscoveredPath discover(String pathStr) {
    PathType type = PathType.NA;
    Optional<Path> discoverPath = Optional.empty();
    try {
      Path path = Paths.get(pathStr);
      discoverPath = Optional.of(path);
      if (Files.exists(path)) {
        if (Files.isDirectory(path)) {
          logger.debug("Processing directory: {}", path);
          type = PathType.DIRECTORY;
        } else {
          logger.debug("Processing file: {}", path);
          type = PathType.FILE;
        }
      }
    } catch (Exception e) {
      logger.error("Failed to process paths", e);
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
      Files.walk(directory)
          .filter(path -> !isHiddenDirectory(path))
          .filter(Files::isRegularFile)
          .forEach(file -> checkBuildFile(file.toString(), builds));
    } catch (Exception e) {
      logger.error("Error walking directory: {}", directory, e);
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
    if (path.endsWith("pom.xml")) {
      logger.info("HERE Checking build file: {}", path);
      builds.add(new BuildFile(path,"Maven"));
    } else if (path.endsWith("build.gradle") || path.endsWith("build.gradle.kts")) {
      builds.add(new BuildFile(path,"Gradle"));
    } else if (path.endsWith("package.json")) {
      builds.add(new BuildFile(path,"NPM/Yarn"));
    } else if (path.endsWith("Cargo.toml")) {
      builds.add(new BuildFile(path,"Cargo"));
   } else if (path.endsWith("requirements.txt") || path.endsWith("setup.py")) {
      builds.add(new BuildFile(path,"Python"));
   } else if (path.endsWith("Makefile")) {
      builds.add(new BuildFile(path,"Makefile"));
   } else if (path.endsWith("CMakeLists.txt")) {
      builds.add(new BuildFile(path,"CMake"));
    }
  }

  /**
   * Creates a CallToolResult containing information about detected build systems.
   *
   * @param builds A set of build system types that were detected
   * @return A CallToolResult containing the build system information
   * with error flag set to true if no build systems were found
   */
  public McpSchema.CallToolResult createTypeResults(Set<BuildFile> builds) {
    List<McpSchema.Content> results = new ArrayList<>();
    boolean error = true;
    if (builds.isEmpty()) {
      results.add(new McpSchema.TextContent("No build system found"));
    } else {
      error = false;
      for (BuildFile file : builds) {
        results.add(new McpSchema.TextContent(file.type()));
      }
    }
    return new McpSchema.CallToolResult(results, error);
  }

  public McpSchema.CallToolResult createPathResults(Set<BuildFile> builds) {
    List<McpSchema.Content> results = new ArrayList<>();
    boolean error = true;
    if (builds.isEmpty()) {
      results.add(new McpSchema.TextContent("No build system found"));
    } else {
      error = false;
      for (BuildFile file : builds) {
        results.add(new McpSchema.TextContent(file.path()));
      }
    }
    return new McpSchema.CallToolResult(results, error);
  }

  /**
   * Processes a root folder path to find build files and adds their absolute paths to the provided set.
   * If the path is a directory, it walks through it to find build files, skipping hidden directories.
   * If the path is a file, it checks if it's a build file directly.
   *
   * @param rootPath       The string path to the root folder to process
   * @param absolutePaths  The set to collect absolute paths of found build files
   * @return               The number of build files found
   */
  public int processRootFolder(String rootPath, Set<String> absolutePaths) {
    logger.debug("Processing root folder: {}", rootPath);
    int initialSize = absolutePaths.size();
    
    DiscoveredPath discoveredPath = discover(rootPath);
    if (discoveredPath.type() == PathType.DIRECTORY && discoveredPath.path().isPresent()) {
      Path directoryPath = discoveredPath.path().get();
      // Create a wrapper set that adds the absolute path to the provided set
      // when a build file is found via checkBuildFile
      Set<BuildFile> buildSystemTypes = new java.util.HashSet<>();
      
      // Custom implementation of walkDirectory that tracks absolute paths
      try {
        Files.walk(directoryPath)
             .filter(path -> !isHiddenDirectory(path))
             .filter(Files::isRegularFile)
             .forEach(file -> {
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
        logger.error("Error walking directory: {}", directoryPath, e);
      }
    } else if (discoveredPath.type() == PathType.FILE && discoveredPath.path().isPresent()) {
      // If it's a single file, check if it's a build file
      String filePath = discoveredPath.path().get().toString();
      Set<BuildFile> buildSystemTypes = new java.util.HashSet<>();
      checkBuildFile(filePath, buildSystemTypes);
      
      // If it's a build file, add its path to the set
      if (!buildSystemTypes.isEmpty()) {
        absolutePaths.add(filePath);
      }
    } else {
      logger.warn("Invalid path or path does not exist: {}", rootPath);
    }
    
    return absolutePaths.size() - initialSize;
  }


  public Set<BuildFile> identifyBuildFiles(List<String> potentialPaths) {
    Set<BuildFile> builds = new HashSet<>();

    for (String pathStr : potentialPaths) {
      logger.info("Processing path: {}", pathStr);
      DiscoveredPath discoveredPath = discover(pathStr);
      switch (discoveredPath.type()) {
        case DIRECTORY:
          discoveredPath.path().ifPresent(path -> walkDirectory(path, builds));
          break;
        case FILE:
          discoveredPath.path().ifPresent(path -> checkBuildFile(path.toString(), builds));
          break;
        case NA:
          logger.warn("Path does not exist: {}", pathStr);
          break;
        default:
          logger.warn("Unhandled path type: {} for path: {}", discoveredPath.type(), pathStr);
          break;
      }
    }
    return builds;
  }


}
