package com.davidparry.scout.tools;

import com.davidparry.scout.annotation.Schema;
import com.davidparry.scout.common.*;
import com.davidparry.scout.handlers.Handler;
import com.davidparry.scout.handlers.HandlerResponse;
import com.davidparry.scout.io.ApplicationLogger;
import com.davidparry.scout.io.LogFileWriter;
import com.davidparry.scout.io.Logger;
import com.davidparry.scout.spec.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Schema(name = "find_build_system", description = "This tool can find the build system given the root directory of the project.")
public class FindBuildSystem extends BuildTool implements Tool, Handler {
   private final Logger logger = ApplicationLogger.getLogger(LogFileWriter.getInstance());


    public FindBuildSystem() {
        logger.log("111111 FindBuildSystem started CREATED !!!!!!!!!!!!!!!!!!!!!!!!!!");
    }


    @Override
    public ToolOutputResponse action(JsonRpcRequest args) {
        Set<BuildFile> builds = new HashSet<>();
        logger.log("Finding build system using " + args);
        try {
            Set<File> files = getProjectRoots(args);
            logger.log("FindBuildSystem Root directories " + files.size() + " projects in " + files);
            builds = onlyBuildFilesFilter(files);
        } catch (Exception e) {
            logger.log("Error finding the build path", e);
            return createErrorResult("Error finding the build path; " + e.getMessage());
        }
        return createTypeResults(builds);
    }
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

    @Override
    public InputSchema schema() {
        logger.log("FindBuildSystem schema Schema being created and returned");
        addProperty(new InputProperty(PROJECT_ROOT, "string", "Path to the root directories for the project.", rootProjectMandatory()));
        return new InputSchema("object", getProperties(), getRequired());
    }

    @Override
    public HandlerResponse handle(JsonRpcRequest request) {
        return new HandlerResponse(action(request));
    }



    public Set<BuildFile> onlyBuildFilesFilter(Set<File> files) {
        Set<String> absolutePaths = files.stream().map(File::getAbsolutePath).collect(Collectors.toSet());
        return identifyBuildFiles(absolutePaths);
    }

    public Set<BuildFile> identifyBuildFiles(Set<String> potentialPaths) {
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
    public void checkBuildFile(String path, Set<BuildFile> builds) {
        String type = identifyBuildFile(path);
        if (type != null) {
            builds.add(new BuildFile(path, type));
        }
    }

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
    public void walkDirectory(Path directory, Set<BuildFile> builds) {
        try {
            Files.walk(directory).filter(path -> !isHiddenDirectory(path)).filter(Files::isRegularFile).forEach(file -> checkBuildFile(file.toString(), builds));
        } catch (Exception e) {
            logger.error("Error while trying to walk Scout directory " + directory, e);
        }
    }
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
            logger.error("Error while trying to discover Scout path " + pathStr, e);
        }
        return new DiscoveredPath(type, discoverPath);
    }

    public String identifyBuildFile(String path) {
        String MAVEN = "Maven";
        String GRADLE_GROOVY = "Gradle/Groovy";
        String GRADLE_KOTLIN = "Gradle/Kotlin";
        String NPM_YARN = "NPM/Yarn";
        String CARGO = "Cargo";
        String PYTHON = "Python";
        String MAKEFILE = "Makefile";
        String CMAKE = "CMake";

        logger.log("BuildSystemImpl Building Scout path " + path);
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

}
