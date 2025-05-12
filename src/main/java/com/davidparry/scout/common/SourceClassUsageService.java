package com.davidparry.scout.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class SourceClassUsageService {
    private static final List<String> SOURCE_FILE_EXTENSIONS = List.of(".java", ".kt");

    private static final List<String> SOURCE_DIRS = List.of("src/main/java", "src/test/java", "src/main/kotlin", "src/test/kotlin");

    public List<Path> findSourceDirectories(String projectRoot) {
        List<Path> sourceDirs = new ArrayList<>();
        Path rootPath = Paths.get(projectRoot);

        // Check standard source directories
        for (String sourceDir : SOURCE_DIRS) {
            Path path = rootPath.resolve(sourceDir);
            if (Files.exists(path) && Files.isDirectory(path)) {
                sourceDirs.add(path);
            }
        }

        // If no standard directories found, try to find any directories that might contain source files
        if (sourceDirs.isEmpty()) {
            try {
                try (Stream<Path> paths = Files.walk(rootPath, 5)) {
                    paths.filter(Files::isDirectory).filter(path -> path.toString().contains("src") || path.toString().contains("java") || path.toString().contains("kotlin")).forEach(sourceDirs::add);
                }
            } catch (IOException e) {
            }
        }

        return sourceDirs;
    }


    public Map<String, Set<Integer>> searchClassUsage(List<Path> sourceDirs, String fullyQualifiedClassName) {
        Map<String, Set<Integer>> usageMap = new HashMap<>();
        String simpleClassName = extractSimpleClassName(fullyQualifiedClassName);
        // Create patterns for different ways the class might be used
        Pattern importPattern = Pattern.compile("import\\s+" + Pattern.quote(fullyQualifiedClassName) + "\\s*;");
        Pattern staticImportPattern = Pattern.compile("import\\s+static\\s+" + Pattern.quote(fullyQualifiedClassName) + "\\.");
        Pattern fullyQualifiedPattern = Pattern.compile("\\b" + Pattern.quote(fullyQualifiedClassName) + "\\b");
        Pattern simpleClassPattern = Pattern.compile("\\b" + Pattern.quote(simpleClassName) + "\\b");

        for (Path sourceDir : sourceDirs) {
            try {
                try (Stream<Path> paths = Files.walk(sourceDir)) {
                    paths.filter(Files::isRegularFile).filter(path -> SOURCE_FILE_EXTENSIONS.stream().anyMatch(ext -> path.toString().endsWith(ext))).forEach(filePath -> {
                        try {
                            List<String> lines = Files.readAllLines(filePath);
                            boolean hasImport = false;

                            // First pass: check for imports
                            for (int i = 0; i < lines.size(); i++) {
                                String line = lines.get(i);
                                if (importPattern.matcher(line).find() || staticImportPattern.matcher(line).find()) {
                                    hasImport = true;
                                    addUsage(usageMap, filePath.toString(), i + 1);
                                }
                            }

                            // Second pass: check for usage
                            for (int i = 0; i < lines.size(); i++) {
                                String line = lines.get(i);

                                // Always check for fully qualified usage
                                if (fullyQualifiedPattern.matcher(line).find()) {
                                    addUsage(usageMap, filePath.toString(), i + 1);
                                }
                                // Only check for simple class name if we found an import
                                else if (hasImport && simpleClassPattern.matcher(line).find()) {
                                    addUsage(usageMap, filePath.toString(), i + 1);
                                }
                            }
                        } catch (IOException e) {
                        }
                    });
                }
            } catch (IOException e) {
            }
        }

        return usageMap;
    }

    private String extractSimpleClassName(String fullyQualifiedClassName) {
        int lastDotIndex = fullyQualifiedClassName.lastIndexOf('.');
        return lastDotIndex > 0 ? fullyQualifiedClassName.substring(lastDotIndex + 1) : fullyQualifiedClassName;
    }


    private void addUsage(Map<String, Set<Integer>> usageMap, String filePath, int lineNumber) {
        usageMap.computeIfAbsent(filePath, k -> new HashSet<>()).add(lineNumber);
    }

}
