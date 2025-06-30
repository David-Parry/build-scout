package com.davidparry.scout.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class GradleProcessExecutor {

    public BuildOutput build(File projectDir, List<String> commands) throws IOException, InterruptedException{
        StringBuilder output = new StringBuilder();
        StringBuilder errorOutput = new StringBuilder();
        boolean hasError = false;

        // Determine the gradle executable
        String gradleExecutable = findGradleExecutable(projectDir);
        commands.addFirst(gradleExecutable);

        // Build the command
        ProcessBuilder pb = new ProcessBuilder(commands);
        pb.directory(projectDir);

        Process process = pb.start();

        // Read output stream
        Thread outputThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            } catch (IOException e) {
                // Don't append to errorOutput here as it's not a build error
                output.append("Error reading output: ").append(e.getMessage()).append("\n");
            }
        });

        // Read error stream - but collect warnings separately
        StringBuilder warnings = new StringBuilder();
        Thread errorThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Check if it's just a warning or note
                    if (line.contains("Note:") || line.contains("warning:") || line.contains("VM warning:")) {
                        warnings.append(line).append("\n");
                    } else {
                        errorOutput.append(line).append("\n");
                    }
                }
            } catch (IOException e) {
                errorOutput.append("Error reading error stream: ").append(e.getMessage()).append("\n");
            }
        });

        outputThread.start();
        errorThread.start();

        boolean finished = process.waitFor(300, TimeUnit.SECONDS); // 5 minutes timeout for builds

        outputThread.join(5000); // Wait max 5 seconds for threads to finish
        errorThread.join(5000);

        if (!finished) {
            process.destroyForcibly();
            errorOutput.append("Gradle build process timed out after 5 minutes\n");
            hasError = true;
        } else if (process.exitValue() != 0) {
            hasError = true;
            if (errorOutput.isEmpty()) {
                errorOutput.append("Gradle build failed with exit code: ").append(process.exitValue()).append("\n");
            }
        }

        // Append warnings to output (not error) if build succeeded
        if (!hasError && !warnings.isEmpty()) {
            if (!output.isEmpty()) {
                output.append("\n");
            }
            output.append("Build Warnings:\n").append(warnings);
        } else if (hasError && !warnings.isEmpty()) {
            // If build failed, include warnings in error output
            errorOutput.append("\nBuild Warnings:\n").append(warnings);
        }

        return new BuildOutput(output.toString(), errorOutput.toString(), hasError);
    }


    public BuildOutput buildProject(File projectDir, boolean check) throws IOException, InterruptedException {
        // Determine the gradle executable
        String gradleExecutable = findGradleExecutable(projectDir);

        // Build the task list
        List<String> command = new ArrayList<>();
        command.add(gradleExecutable);
        command.add("clean");
        command.add("build");
        if (check) {
            command.add("check");
        }

        command.add("--no-daemon");
        command.add("-Dorg.gradle.daemon=false");
        command.add("-Dorg.gradle.parallel=false");
        command.add("-Dorg.gradle.workers.max=1");

        return  build(projectDir, command);
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

    public List<DependencyInfo> resolveDependencies(File projectDir) throws IOException, InterruptedException {
        List<DependencyInfo> dependencies = new ArrayList<>();

        // Determine the gradle executable
        String gradleExecutable = findGradleExecutable(projectDir);

        // Build the command
        ProcessBuilder pb = new ProcessBuilder(
                gradleExecutable,
                "dependencies",
                "--configuration", "compileClasspath",
                "--no-daemon" // Important for native image
        );

        pb.directory(projectDir);
        pb.redirectErrorStream(true);

        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            boolean inDependencySection = false;

            while ((line = reader.readLine()) != null) {
                // Skip empty lines and headers
                if (line.trim().isEmpty() || line.startsWith("----")) {
                    continue;
                }

                // Check if we're in the dependency section
                if (line.contains("compileClasspath")) {
                    inDependencySection = true;
                    continue;
                }

                if (inDependencySection && line.contains(":")) {
                    // Parse dependency line
                    DependencyInfo dep = parseDependencyLine(line);
                    if (dep != null) {
                        dependencies.add(dep);
                    }
                }
            }
        }

        boolean finished = process.waitFor(60, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            throw new RuntimeException("Gradle process timed out");
        }

        if (process.exitValue() != 0) {
            throw new RuntimeException("Gradle process failed with exit code: " + process.exitValue());
        }

        return dependencies;
    }

    private String findGradleExecutable(File projectDir) {
        // First try gradlew in the project directory
        File gradlewUnix = new File(projectDir, "gradlew");
        File gradlewWindows = new File(projectDir, "gradlew.bat");

        String os = System.getProperty("os.name").toLowerCase();
        boolean isWindows = os.contains("win");

        if (isWindows && gradlewWindows.exists()) {
            return gradlewWindows.getAbsolutePath();
        } else if (!isWindows && gradlewUnix.exists()) {
            // Make sure it's executable
            gradlewUnix.setExecutable(true);
            return gradlewUnix.getAbsolutePath();
        }

        // Fall back to system gradle
        return "gradle";
    }

    private DependencyInfo parseDependencyLine(String line) {
        // Remove tree characters and trim
        String cleaned = line.replaceAll("[\\|\\\\+\\-\\s]+", " ").trim();

        // Skip lines that don't look like dependencies
        if (!cleaned.contains(":") || cleaned.startsWith("(")) {
            return null;
        }

        // Extract the dependency part (before any arrows)
        String depPart = cleaned.split("->")[0].trim();

        // Parse group:name:version
        String[] parts = depPart.split(":");
        if (parts.length >= 3) {
            return new DependencyInfo(parts[0], parts[1], parts[2]);
        }

        return null;
    }
}