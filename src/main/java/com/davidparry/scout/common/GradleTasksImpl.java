package com.davidparry.scout.common;

import com.davidparry.scout.io.ApplicationLogger;
import com.davidparry.scout.io.LogFileWriter;
import com.davidparry.scout.io.Logger;
import org.gradle.tooling.BuildException;
import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class GradleTasksImpl implements GradleTasks {
    private static final Logger logger = ApplicationLogger.getLogger(LogFileWriter.getInstance(new LogFactory()));


    public BuildOutput buildGradleProject(File projectDir, boolean check) {
        GradleConnector connector = GradleConnector.newConnector().forProjectDirectory(projectDir);
        boolean error = true;
        String output = "";
        String errorOutput = "";
        try (ProjectConnection connection = connector.connect()) {
            ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
            ByteArrayOutputStream stdErr = new ByteArrayOutputStream();

            List<String> taskList = new ArrayList<>();
            taskList.add("clean");
            taskList.add("build");
            if (check) {
                taskList.add("check");
            }

            BuildLauncher build = connection.newBuild().forTasks(taskList.toArray(new String[0])).setStandardOutput(stdOut).setStandardError(stdErr);

            try {
                build.run();                   // actually run it
                output = stdOut.toString(StandardCharsets.UTF_8);
                errorOutput = stdErr.toString(StandardCharsets.UTF_8);
                error = false;
            } catch (BuildException e) {
                errorOutput = stdErr.toString(StandardCharsets.UTF_8);
            }

        } catch (Exception io) {
            logger.error(io.getMessage(), io);
            errorOutput = io.getMessage();
        }
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
