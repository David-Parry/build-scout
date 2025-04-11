package com.davidparry.mcp.buildscout.common;

import com.davidparry.mcp.buildscout.tools.ListDependencies;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.model.idea.IdeaProject;
import org.gradle.tooling.model.idea.IdeaModule;
import org.gradle.tooling.model.idea.IdeaDependency;
import org.gradle.tooling.model.idea.IdeaSingleEntryLibraryDependency;
import org.gradle.tooling.model.GradleModuleVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class DependencyResolver {
    private static final Logger logger = LoggerFactory.getLogger(DependencyResolver.class);

    public List<String> resolveDependencies(String fileContents, String buildType) {
        return resolveGradleGroovyDependencies(fileContents);
    }

    private List<String> resolveGradleGroovyDependencies(String fileContents) {
        List<String> dependencies = new ArrayList<>();

        try {
            Path projectPath = Files.createTempDirectory("buildscout-gradle-");
            projectPath.toFile().deleteOnExit();
            File projectDir = projectPath.toAbsolutePath().toFile();
            projectDir.deleteOnExit();

            // Write the build file to the temporary directory
            File buildFile = new File(projectDir, "build.gradle");

            Files.write(buildFile.toPath(), fileContents.getBytes());

            // Create an empty settings.gradle file
            File settingsFile = new File(projectDir, "settings.gradle");
            Files.write(settingsFile.toPath(), "".getBytes());

            // Connect to the Gradle project using the Tooling API
            GradleConnector connector = GradleConnector.newConnector();
            connector.forProjectDirectory(projectDir);

            try (ProjectConnection connection = connector.connect()) {
                // Request the IDEA model which contains dependency information
                IdeaProject ideaProject = connection.getModel(IdeaProject.class);

                for (IdeaModule module : ideaProject.getModules()) {
                    System.out.println("Module: " + module.getName());
                    for (IdeaDependency dependency : module.getDependencies()) {
                        if (dependency instanceof IdeaSingleEntryLibraryDependency) {
                            IdeaSingleEntryLibraryDependency libraryDependency = (IdeaSingleEntryLibraryDependency) dependency;
                            GradleModuleVersion moduleVersion = libraryDependency.getGradleModuleVersion();

                            if (moduleVersion != null) {
                                String group = moduleVersion.getGroup();
                                String name = moduleVersion.getName();
                                String version = moduleVersion.getVersion();

                                String groupName = group + ":" + name;
                                if (fileContents.contains(groupName)) {
                                    String dependencyNotation = groupName + ":" + version;
                                    dependencies.add(dependencyNotation);
                                    logger.debug("Found dependency: {}", dependencyNotation);
                                }

                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            logger.error("Failed to resolve Gradle dependencies: for gradle file {} ", fileContents, e);
        }

        return dependencies;
    }

}
