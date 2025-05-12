package com.davidparry.scout.common;

import org.gradle.tooling.BuildException;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;

public class GradleTasksImpl implements GradleTasks {


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
            var build = connection.newBuild().forTasks("clean", "build")              // or any other task(s)
                    .setStandardOutput(stdOut)      // wire stdout
                    .setStandardError(stdErr);      // wire stderr

            try {
                build.run();                   // actually run it
                output = stdOut.toString(StandardCharsets.UTF_8);
                errorOutput = stdErr.toString(StandardCharsets.UTF_8);
                error = false;
            } catch (BuildException e) {
                errorOutput = stdErr.toString(StandardCharsets.UTF_8);
            }

        } catch (Exception io) {
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
