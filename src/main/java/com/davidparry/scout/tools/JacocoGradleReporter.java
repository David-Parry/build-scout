package com.davidparry.scout.tools;

import com.davidparry.scout.common.*;
import com.davidparry.scout.handlers.Handler;
import com.davidparry.scout.handlers.HandlerResponse;
import com.davidparry.scout.io.ApplicationLogger;
import com.davidparry.scout.io.LogFileWriter;
import com.davidparry.scout.io.Logger;
import com.davidparry.scout.spec.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.davidparry.scout.common.BuildSystem.GRADLE_GROOVY;

public class JacocoGradleReporter extends BuildTool implements Handler {
    private final Logger logger = new ApplicationLogger().getLogger(LogFileWriter.getInstance(new LogFactory()));
    private final GradleProcessExecutor service;
    private final Tool tool;
    private final BuildSystem buildSystem;

    public JacocoGradleReporter(GradleProcessExecutor service, BuildSystem buildSystem) {
        this.service = service;
        this.buildSystem = buildSystem;
        this.tool = new Tool("gradle_jacoco_coverage_reporter", "Executes Gradle jacocoTestReport tasks and returns back the results in an xml format, for you to consume and understand the unit test coverage.", schema());
    }

    public ToolOutputResponse action(JsonRpcRequest args) {
        List<Content> results = new ArrayList<>();
        boolean error = false;
        try {
            Set<File> files = getProjectRoots(args);

            // Convert Set<File> to Set<String> with absolute paths
            Set<String> absolutePaths = new HashSet<>();
            for (File file : files) {
                absolutePaths.add(file.getAbsolutePath());


                logger.log("Root directories " + files.size() + " projects in " + files);
                Set<BuildFile> buildFiles = buildSystem.identifyBuildFiles(absolutePaths);

                for (BuildFile buildFile : buildFiles) {
                    if (GRADLE_GROOVY.equalsIgnoreCase(buildFile.type())) {
                        JacocoGradleAnalyzer analyzer = new JacocoGradleAnalyzer();
                        // Analyze the file
                        JacocoConfig config = analyzer.analyzeGradleFile(buildFile.path());
                        String xmlPath = config.xmlOutputLocation();
                        if (!config.hasJacocoTestReport() || !config.xmlRequired()) {
                            logger.log("Updating build.gradle to ensure XML report is enabled...");
                            xmlPath = analyzer.updateGradleFile(buildFile.path());
                            logger.log("XML report will be generated at: " + xmlPath);
                        }

                        List<String> commands = new ArrayList<>();
                        commands.add("clean");
                        commands.add("build");
                        commands.add("jacocoTestReport");
                        commands.add("--no-daemon");
                        commands.add("-Dorg.gradle.daemon=false");
                        commands.add("-Dorg.gradle.parallel=false");
                        commands.add("-Dorg.gradle.workers.max=1");

                        BuildOutput output = service.build(file, commands);

                        if (!output.failed()) {
                            // Build succeeded, read the XML file
                            try {
                                // Construct the full path to the XML file
                                Path projectRoot = Paths.get(file.getAbsolutePath());
                                Path xmlFilePath = projectRoot.resolve(xmlPath);

                                if (Files.exists(xmlFilePath)) {
                                    String xmlContent = Files.readString(xmlFilePath);
                                    results.add(new Content("Jacoco XML Report from " + xmlFilePath + ":\n" + xmlContent));
                                    logger.log("Successfully read Jacoco XML report from: " + xmlFilePath);
                                } else {
                                    logger.log("Jacoco XML report not found at: " + xmlFilePath);
                                    error = true;
                                    results.add(new Content("Jacoco XML report not found at: " + xmlFilePath + "\nBuild output:\n" + service.formatOutput(output)));
                                }
                            } catch (IOException e) {
                                logger.log("Error reading Jacoco XML file: " + e.getMessage(), e);
                                results.add(new Content("Error reading Jacoco XML file: " + e.getMessage() + "\nBuild output:\n" + service.formatOutput(output)));
                            }
                        } else {
                            // Build failed
                            error = true;
                            logger.log("Build failed with error: " + output.error());
                            results.add(new Content(service.formatOutput(output)));
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.log("Error finding class usage", e);
            return createErrorResult("Error finding class usage: " + e.getMessage());
        }
        return new ToolOutputResponse(results, error);
    }

    public InputSchema schema() {
        logger.log("BuildGradleProject schema Schema being created and returned");
        addProperty(new InputProperty(PROJECT_ROOT, "string", "The fully qualified path to the project", rootProjectMandatory()));
        return new InputSchema("object", getProperties(), getRequired());
    }

    @Override
    public HandlerResponse handle(JsonRpcRequest request) {
        return new HandlerResponse(action(request));
    }

    public Tool tool() {
        return tool;
    }

}
