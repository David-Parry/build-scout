package com.davidparry.scout.common;

import com.davidparry.scout.io.ApplicationLogger;
import com.davidparry.scout.io.LogFileWriter;
import com.davidparry.scout.io.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class DependencyFetch {
    private final BuildSystem buildSystem;
    private final Logger logger = new ApplicationLogger().getLogger(LogFileWriter.getInstance(new LogFactory()));
    private final GradleProcessExecutor gradleProcessExecutor;

    public DependencyFetch(BuildSystem buildSystem, GradleProcessExecutor gradleProcessExecutor) {
        this.buildSystem = buildSystem;
        this.gradleProcessExecutor = gradleProcessExecutor;
    }


    public String lookupLatestVersion(String groupId, String artifactId) {
        try {
            // Convert groupId to path format
            String groupPath = groupId.replace('.', '/');

            // Maven central repository URL
            String metadataUrl = String.format("https://repo1.maven.org/maven2/%s/%s/maven-metadata.xml", groupPath, artifactId);

            // Create temporary file to store the metadata
            Path tempFile = Files.createTempFile("maven-metadata", ".xml");
            File metadataFile = tempFile.toFile();

            // Download the metadata file
            java.net.URL url = new java.net.URL(metadataUrl);
            try (java.io.InputStream in = url.openStream(); java.io.FileOutputStream out = new java.io.FileOutputStream(metadataFile)) {

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }

            // Parse the XML to get the latest version
            javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
            org.w3c.dom.Document doc = builder.parse(metadataFile);

            // Clean up the temporary file
            Files.deleteIfExists(tempFile);

            // Extract the latest version
            org.w3c.dom.NodeList versionNodes = doc.getElementsByTagName("latest");
            if (versionNodes.getLength() > 0) {
                return versionNodes.item(0).getTextContent();
            }

            // If no "latest" tag, try to get the "release" tag
            versionNodes = doc.getElementsByTagName("release");
            if (versionNodes.getLength() > 0) {
                return versionNodes.item(0).getTextContent();
            }

            // If neither "latest" nor "release" tags exist, get the last version from "versions"
            versionNodes = doc.getElementsByTagName("version");
            if (versionNodes.getLength() > 0) {
                return versionNodes.item(versionNodes.getLength() - 1).getTextContent();
            }

            return null;
        } catch (Exception e) {
            logger.error("Error resolving latest version", e);
            return null;
        }
    }

    /**
     * Downloads the sources jar for version of the given Maven artifact into a temp directory.
     * Returns the path to the temp directory containing the downloaded  file.
     *
     * @param groupId    the Maven groupId
     * @param artifactId the Maven artifactId
     * @param version    the version to download source for
     * @return Path to the temp directory containing all downloaded sources jars, or null if failed.
     */
    public Path downloadSourceJar(String groupId, String artifactId, String version, String marker) {
        try {
            // Convert groupId to path format
            String groupPath = groupId.replace('.', '/');

            String sourcesJarUrl = String.format("https://repo1.maven.org/maven2/%s/%s/%s/%s-%s-sources.jar", groupPath, artifactId, version, artifactId, version);
            Path pathSourcesJarFile = Files.createTempFile(marker + ":" + artifactId + ":" + version + "-", "-sources.jar");
            File sourcesJarFile = pathSourcesJarFile.toFile();

            try (java.io.InputStream in = new java.net.URL(sourcesJarUrl).openStream(); java.io.FileOutputStream out = new java.io.FileOutputStream(sourcesJarFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            } catch (Exception e) {
                return null;
            }
            return sourcesJarFile.toPath();
        } catch (Exception e) {
            logger.error("Error resolving latest version", e);
            return null;
        }
    }

    public List<String> resolveDependencies(String path) {
        try {
            Path pathObj = Paths.get(path);
            File projectDir = (Files.isRegularFile(pathObj) ? pathObj.getParent() : pathObj).toFile();
            String type = buildSystem.identifyBuildFile(path);

            if (BuildSystem.GRADLE_GROOVY.equalsIgnoreCase(type) || BuildSystem.GRADLE_KOTLIN.equalsIgnoreCase(type)) {
                return resolveGradleDependencies(projectDir);
            } else if (BuildSystem.MAVEN.equalsIgnoreCase(type)) {
                return resolveMavenDependencies(projectDir);
            } else {
                throw new RuntimeException("No supported build file found");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error resolving dependencies", e);
        }
    }

    private List<String> resolveGradleDependencies(File projectDir) throws IOException, InterruptedException {
        // Use the ProcessBuilder approach instead of Gradle Tooling API
        List<DependencyInfo> deps = gradleProcessExecutor.resolveDependencies(projectDir);

        return deps.stream().map(Record::toString).collect(Collectors.toList());
    }

    private List<String> resolveMavenDependencies(File projectDir) throws IOException, InterruptedException {
        // Similar approach for Maven
        ProcessBuilder pb = new ProcessBuilder("mvn", "dependency:list", "-DoutputAbsoluteArtifactFilename=false", "-DincludeScope=compile");

        pb.directory(projectDir);
        // ... parse output similar to Gradle

        return List.of(); // Implement Maven parsing
    }


}
