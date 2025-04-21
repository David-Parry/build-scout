package com.davidparry.mcp.buildscout.common;

import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.model.GradleModuleVersion;
import org.gradle.tooling.model.idea.IdeaDependency;
import org.gradle.tooling.model.idea.IdeaModule;
import org.gradle.tooling.model.idea.IdeaProject;
import org.gradle.tooling.model.idea.IdeaSingleEntryLibraryDependency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DependencyFetch {
    private static final Logger logger = LoggerFactory.getLogger(DependencyFetch.class);

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
                        if (dependency instanceof IdeaSingleEntryLibraryDependency libraryDependency) {
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

            logger.warn("Could not determine latest version for {}:{}", groupId, artifactId);
            return null;
        } catch (Exception e) {
            logger.error("Error looking up latest version for {}:{}: {}", groupId, artifactId, e.getMessage());
            return null;
        }
    }

    /**
     * Downloads the sources jar for each available version of the given Maven artifact into a temp directory.
     * Returns the path to the temp directory containing the downloaded sources.
     *
     * @param groupId        the Maven groupId
     * @param artifactId     the Maven artifactId
     * @param currentVersion the current version (not used for filtering, but included for signature consistency)
     * @return Path to the temp directory containing all downloaded sources jars, or null if failed.
     */
    public Path downloadAllSourcesForArtifact(String groupId, String artifactId, String currentVersion) {
        try {
            // Convert groupId to path format
            String groupPath = groupId.replace('.', '/');

            List<JarData> versions = fetchCurrentLatestMavenArtifactVersions(groupId, artifactId, currentVersion);

            // Create a temp directory for sources
            Path sourcesDir = Files.createTempDirectory("maven-sources-" + artifactId + "-");
            sourcesDir.toFile().deleteOnExit();

            // Download sources jar for each version
            for (JarData version : versions) {
                String sourcesJarUrl = String.format("https://repo1.maven.org/maven2/%s/%s/%s/%s-%s-sources.jar", groupPath, artifactId, version.version(), artifactId, version.version());
                File sourcesJarFile = new File(sourcesDir.toFile(), artifactId + "-" + version.classifier() + "-sources.jar");
                try (java.io.InputStream in = new java.net.URL(sourcesJarUrl).openStream(); java.io.FileOutputStream out = new java.io.FileOutputStream(sourcesJarFile)) {

                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                    logger.info("Downloaded sources for {}:{}:{} to {}", groupId, artifactId, version, sourcesJarFile.getAbsolutePath());
                } catch (Exception e) {
                    logger.warn("Could not download sources for {}:{}:{} - {}", groupId, artifactId, version, e.getMessage());
                    // Continue with next version
                }
            }

            return sourcesDir;
        } catch (Exception e) {
            logger.error("Error downloading sources for {}:{}: {}", groupId, artifactId, e.getMessage());
            return null;
        }
    }

    private List<JarData> fetchCurrentLatestMavenArtifactVersions(String groupId, String artifactId, String currentVersion) {
        List<JarData> versions = new ArrayList<>();
        Path tempMetadataFile = null;
        try {
            // Convert groupId to path format
            String groupPath = groupId.replace('.', '/');

            // Maven central repository URL for metadata
            String metadataUrl = String.format("https://repo1.maven.org/maven2/%s/%s/maven-metadata.xml", groupPath, artifactId);

            // Download the metadata file
            tempMetadataFile = Files.createTempFile("maven-metadata", ".xml");
            File metadataFile = tempMetadataFile.toFile();

            java.net.URL url = new java.net.URL(metadataUrl);
            try (java.io.InputStream in = url.openStream(); java.io.FileOutputStream out = new java.io.FileOutputStream(metadataFile)) {

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }

            // Parse the XML to get all versions
            javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
            org.w3c.dom.Document doc = builder.parse(metadataFile);

            org.w3c.dom.NodeList versionNodes = doc.getElementsByTagName("latest");
            if (versionNodes.getLength() > 0) {
                versions.add(new JarData(versionNodes.item(0).getTextContent(), groupId, artifactId, "latest"));
            }

            // If no "latest" tag, try to get the "release" tag
            versionNodes = doc.getElementsByTagName("release");
            if (versionNodes.getLength() > 0) {
                versions.add(new JarData(versionNodes.item(0).getTextContent(), groupId, artifactId, "release"));
            }

            versionNodes = doc.getElementsByTagName("version");
            for (int i = 0; i < versionNodes.getLength(); i++) {
                String version = versionNodes.item(i).getTextContent();
                if (version.equals(currentVersion)) {
                    versions.add(new JarData(version, groupId, artifactId, "current"));
                }
            }
        } catch (Exception e) {
            logger.error("Error fetching versions for {}:{}: {}", groupId, artifactId, e.getMessage());
        } finally {
            if (tempMetadataFile != null) {
                try {
                    Files.deleteIfExists(tempMetadataFile);
                } catch (Exception ignore) {
                }
            }
        }
        return versions;
    }
}
