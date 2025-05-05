package com.davidparry.mcp.buildscout.common;

import org.gradle.tooling.BuildException;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;

public class GradleTasksImpl implements GradleTasks {
    private static final Logger logger = LoggerFactory.getLogger(GradleTasksImpl.class);


    public BuildOutput buildGradleProject(File projectDir) {
        GradleConnector connector = GradleConnector.newConnector().forProjectDirectory(projectDir);
        boolean error = true;
        String output = "";
        String errorOutput = "";
        try (ProjectConnection connection = connector.connect()) {

            // 3. Prepare to capture output
            ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
            ByteArrayOutputStream stdErr = new ByteArrayOutputStream();

            // 4. Configure and launch the build
            var build = connection.newBuild().forTasks("clean","build")              // or any other task(s)
                    .setStandardOutput(stdOut)      // wire stdout
                    .setStandardError(stdErr);      // wire stderr

            try {
                build.run();                   // actually run it
                logger.info("Build succeeded!");
                output = stdOut.toString(StandardCharsets.UTF_8);
                errorOutput = stdErr.toString(StandardCharsets.UTF_8);
                error = false;
            } catch (BuildException e) {
                errorOutput = stdErr.toString(StandardCharsets.UTF_8);
                logger.error("Build failed ", e);
            }

        } catch (Exception io) {
            logger.error("Build failed ", io);
            errorOutput = io.getMessage();
        }
        logger.debug(">>> output:\n {}", output);
        logger.debug(">>> captured stderr:\n {}", errorOutput);
        return new BuildOutput(output, errorOutput, error);
    }

    public String formatOutput(BuildOutput buildOutput) {
        StringBuilder result = new StringBuilder();

        // Add output if present
        if (buildOutput.output() != null && !buildOutput.output().isEmpty()) {
            result.append("Output:\n").append(buildOutput.output());
        }

        // Add error if present
        if (buildOutput.error() != null && !buildOutput.error().isEmpty()) {
            // Add a separator if we already have content
            if (!result.isEmpty()) {
                result.append("\n\n");
            }
            result.append("Error:\n").append(buildOutput.error());
        }

        return result.toString();
    }


}
