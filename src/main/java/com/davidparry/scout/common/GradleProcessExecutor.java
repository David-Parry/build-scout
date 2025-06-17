package com.davidparry.scout.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GradleProcessExecutor {

    public static class DependencyInfo {
        public final String group;
        public final String name;
        public final String version;

        public DependencyInfo(String group, String name, String version) {
            this.group = group;
            this.name = name;
            this.version = version;
        }

        @Override
        public String toString() {
            return group + ":" + name + ":" + version;
        }
    }

    public static List<DependencyInfo> resolveDependencies(File projectDir) throws IOException, InterruptedException {
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

    private static String findGradleExecutable(File projectDir) {
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

    private static DependencyInfo parseDependencyLine(String line) {
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