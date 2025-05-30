package com.davidparry.scout.tools;

import com.davidparry.scout.annotation.Schema;
import com.davidparry.scout.common.*;
import com.davidparry.scout.io.ApplicationLogger;
import com.davidparry.scout.io.Logger;
import com.davidparry.scout.spec.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Schema(name = "dependencies_list", description = "Given the build file path, this tool will list all the top level dependencies of the project. With the fully qualified name of the dependency, it will return the version of the dependency. Example for Gradle: 'org.springframework.boot:spring-boot-starter-jdbc:3.4.4' ")
public class ListDependencies extends BuildTool implements Tool<ToolOutputResponse> {
    private static final Logger logger = ApplicationLogger.getInstance();
    private final DependencyFetch dependencyFetch;
    private final BuildSystem buildSystem;

    public ListDependencies(DependencyFetch dependencyFetch, BuildSystem buildSystem) {
        this.dependencyFetch = dependencyFetch;
        this.buildSystem = buildSystem;
    }

    public ListDependencies() {
        this(new DependencyFetch(new BuildSystemImpl()), new BuildSystemImpl());
    }

    @Override
    public InputSchema schema() {
        logger.log("ListDependencies schema Schema being created and returned");
        addProperty(new InputProperty("path", "string", "The absolute path to the build file.", false));
        return new InputSchema("object", getProperties(), getRequired());
    }

    @Override
    public ToolOutputResponse action(JsonRpcRequest request) {
        List<Content> results = new ArrayList<>();
        boolean error = true;
        try {
            if (request != null) {
                String path = ArgumentUtils.getArgument(request, "path");
                if (path == null || path.isEmpty()) {
                    Set<File> files = getProjectRoots(request);
                    Set<BuildFile> buildFiles = this.buildSystem.onlyBuildFilesFilter(files);
                    if (buildFiles.isEmpty()) {
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

                    List<String> dependencies = dependencyFetch.resolveDependencies(path);
                    error = false;
                    dependencies.forEach(d -> results.add(new Content(d)));
                    logger.log(path + " Root directories " + dependencies.size() + " projects in " + dependencies);
                }
            }
        } catch (Exception e) {
            logger.log("Failed to process paths", e);
        }
        return new ToolOutputResponse(results, error);
    }
}
