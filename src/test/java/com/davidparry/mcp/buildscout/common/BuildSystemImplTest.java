package com.davidparry.mcp.buildscout.common;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class BuildSystemImplTest {
    // Successfully updates a dependency version in a Gradle/Groovy build file
    @Test
    public void test_update_dependency_version_success() throws IOException {
        // Setup
        BuildSystemImpl buildSystem = new BuildSystemImpl();
        Path tempFile = Files.createTempFile("build", "build.gradle");
        List<String> buildFileContent = Arrays.asList(
                "dependencies {",
                "    implementation 'com.example:library:1.0.0'",
                "    testImplementation \"org.junit:junit:4.12\"",
                "}"
        );
        Files.write(tempFile, buildFileContent);

        // Execute
        String result = buildSystem.updateDependencyVersion("com.example", "library", "2.0.0", tempFile.toString());

        // Verify
        List<String> updatedContent = Files.readAllLines(tempFile);
        assertTrue(result.contains("updated to version 2.0.0"));
        assertEquals("    implementation 'com.example:library:2.0.0'", updatedContent.get(1));
        assertEquals("    testImplementation \"org.junit:junit:4.12\"", updatedContent.get(2));

        // Cleanup
        Files.deleteIfExists(tempFile);
    }
}
