package com.davidparry.scout.tools;

import com.davidparry.scout.common.BuildSystem;
import com.davidparry.scout.common.LogFactory;
import com.davidparry.scout.handlers.Handler;
import com.davidparry.scout.handlers.HandlerResponse;
import com.davidparry.scout.io.ApplicationLogger;
import com.davidparry.scout.io.LogFileWriter;
import com.davidparry.scout.io.Logger;
import com.davidparry.scout.spec.*;
import com.davidparry.scout.spec.Tool;

import java.util.ArrayList;
import java.util.List;

public class ListDependencies extends BuildTool implements Handler {
    private final Logger logger = new ApplicationLogger().getLogger(LogFileWriter.getInstance(new LogFactory()));
    //private final DependencyFetch dependencyFetch;
     private final com.davidparry.scout.spec.Tool tool;



//    public ListDependencies(DependencyFetch dependencyFetch, BuildSystem buildSystem) {
//        logger.log("Inside ListDependencies constructor");
//        this.dependencyFetch = dependencyFetch;
//        this.buildSystem = buildSystem;
//        this.tool = new com.davidparry.scout.spec.Tool("dependencies_list", "Given the build file path, this tool will list all the top level dependencies of the project. With the fully qualified name of the dependency, it will return the version of the dependency. Example for Gradle: 'org.springframework.boot:spring-boot-starter-jdbc:3.4.4' ", schema());
//    }

    public ListDependencies() {
        logger.log("Inside ListDependencies constructor");
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
//                String path = ArgumentUtils.getArgument(request, "path");
//                if (path == null || path.isEmpty()) {
//                    Set<File> files = getProjectRoots(request);
//                    Set<BuildFile> buildFiles = this.buildSystem.onlyBuildFilesFilter(files);
//                    if (buildFiles.isEmpty()) {
//                        results.add(new Content("Absolute Path to the build file is missing!"));
//                    } else {
//                        logger.log("Root directories " + files.size() + " projects in " + files);
//                        for (BuildFile buildFile : buildFiles) {
//                            List<String> dependencies = dependencyFetch.resolveDependencies(buildFile.path());
//                            error = false;
//                            dependencies.forEach(d -> results.add(new Content(d)));
//                            logger.log("Path not supplied root directories " + dependencies.size() + " projects in " + dependencies);
//                        }
//                    }
//                } else {
//                    List<String> dependencies = dependencyFetch.resolveDependencies(path);
//                    error = false;
//                    dependencies.forEach(d -> results.add(new Content(d)));
//                    logger.log(path + " Root directories " + dependencies.size() + " projects in " + dependencies);
//                }
            }
        } catch (Exception e) {
            logger.error("Failed to process paths", e);
        }
        results.add(new Content("aaaaI am a list and the classes are the bad actors"));

        return new ToolOutputResponse(results, error);
    }

    @Override
    public HandlerResponse handle(JsonRpcRequest request) {
        return new HandlerResponse(action(request));
    }
}
