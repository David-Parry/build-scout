package com.davidparry.scout.tools;

import com.davidparry.scout.common.*;
import com.davidparry.scout.handlers.Handler;
import com.davidparry.scout.handlers.HandlerResponse;
import com.davidparry.scout.io.ApplicationLogger;
import com.davidparry.scout.io.LogFileWriter;
import com.davidparry.scout.io.Logger;
import com.davidparry.scout.spec.*;
import com.davidparry.scout.spec.Tool;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ListDependencies extends BuildTool implements Handler {
    private final Logger logger = new ApplicationLogger().getLogger(LogFileWriter.getInstance(new LogFactory()));
    private final DependencyFetch dependencyFetch;
    private final BuildSystem buildSystem;
    private final com.davidparry.scout.spec.Tool tool;



//    public ListDependencies(DependencyFetch dependencyFetch, BuildSystem buildSystem) {
//        logger.log("Inside ListDependencies constructor");
//        this.dependencyFetch = dependencyFetch;
//        this.buildSystem = buildSystem;
//        this.tool = new com.davidparry.scout.spec.Tool("dependencies_list", "Given the build file path, this tool will list all the top level dependencies of the project. With the fully qualified name of the dependency, it will return the version of the dependency. Example for Gradle: 'org.springframework.boot:spring-boot-starter-jdbc:3.4.4' ", schema());
//    }

    public ListDependencies(DependencyFetch dependencyFetch, BuildSystem buildSystem) {
        logger.log("Inside ListDependencies constructor");
        this.buildSystem = buildSystem;
        this.dependencyFetch = dependencyFetch;
        this.tool = new com.davidparry.scout.spec.Tool("dependencies_list", "Given the build file path, this tool will list all the top level dependencies of the project. With the fully qualified name of the dependency, it will return the version of the dependency. Example for Gradle: 'org.springframework.boot:spring-boot-starter-jdbc:3.4.4' ", schema());
    }

    public Tool getTool() {
        return tool;
    }

    public InputSchema schema() {
        logger.log("ListDependencies schema Schema being created and returned");
        addProperty(new InputProperty("path", "string", "The absolute path to the build file.", false));
        return new InputSchema("object", getProperties(), getRequired());
    }

    public ToolOutputResponse action(JsonRpcRequest request) {
        logger.log("ListDependencies action method received request: " + request);
        List<Content> results = new ArrayList<>();
        boolean error = true;
        try {
            if (request != null) {
                logger.log("Request received: " + request);
                String path = ArgumentUtils.getArgument(request, "path");
                if (path == null || path.isEmpty()) {
                    logger.log("Path is empty "+path);
                    Set<File> files = getProjectRoots(request);
                    Set<BuildFile> buildFiles = this.buildSystem.onlyBuildFilesFilter(files);
                    if (buildFiles.isEmpty()) {
                        logger.log("build files is empty "+buildFiles);
                        results.add(new Content("Absolute Path to the build file is missing!"));
                    } else {
                        logger.log("Root directories " + files.size() + " projects in " + files);
                        for (BuildFile buildFile : buildFiles) {
                            List<String> dependencies = dependencyFetch.resolveDependencies(buildFile.path());
                            error = false;
                            dependencies.forEach(d -> results.add(new Content(d)));
                            logger.log("Path not supplied root directories " + dependencies.size() + " projects in " + dependencies);
                        }
                    }
                } else {
                    logger.log("Path: " + path);
                    List<String> dependencies = dependencyFetch.resolveDependencies(path);
                    error = false;
                    dependencies.forEach(d -> results.add(new Content(d)));
                    logger.log(path + " Root directories " + dependencies.size() + " projects in " + dependencies);
               }
            }
        } catch (Exception e) {
            logger.error("Failed to process paths", e);
        }
        return new ToolOutputResponse(results, error);
    }

    @Override
    public HandlerResponse handle(JsonRpcRequest request) {
        return new HandlerResponse(action(request));
    }


    public List<String> resolveDependencies(File projectDir) {
        try {
            // Detect build system
            if (new File(projectDir, "build.gradle").exists() ||
                    new File(projectDir, "build.gradle.kts").exists()) {
                return resolveGradleDependencies(projectDir);
            } else if (new File(projectDir, "pom.xml").exists()) {
                return resolveMavenDependencies(projectDir);
            } else {
                throw new RuntimeException("No supported build file found");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error resolving dependencies", e);
        }
    }

    private List<String> resolveGradleDependencies(File projectDir) throws IOException, InterruptedException {
        // Use the ProcessBuilder approach instead of Gradle Tooling API
        List<GradleProcessExecutor.DependencyInfo> deps =
                GradleProcessExecutor.resolveDependencies(projectDir);

        return deps.stream()
                .map(dep -> dep.toString())
                .collect(Collectors.toList());
    }

    private List<String> resolveMavenDependencies(File projectDir) throws IOException, InterruptedException {
        // Similar approach for Maven
        ProcessBuilder pb = new ProcessBuilder(
                "mvn",
                "dependency:list",
                "-DoutputAbsoluteArtifactFilename=false",
                "-DincludeScope=compile"
        );

        pb.directory(projectDir);
        // ... parse output similar to Gradle

        return List.of(); // Implement Maven parsing
    }

    // Keep this method for compatibility but mark as deprecated
    @Deprecated
    public void resolveGradleGroovyDependencies(File projectDir) {
        // This method was using Gradle Tooling API which doesn't work well in native image
        // Redirect to the new implementation
        resolveDependencies(projectDir);
    }

}
