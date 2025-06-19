package com.davidparry.scout.common;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Utility class for downloading JAR files from Maven Central repository
 * using groupId, artifactId and version.
 */
public class JarDownloader {
    private final String MAVEN_CENTRAL_URL = "https://repo1.maven.org/maven2";

    /**
     * Downloads a file from a URL to a local file.
     *
     * @param fileUrl    The URL of the file to download
     * @param outputPath The path where the file will be saved
     * @throws IOException If an I/O error occurs
     */
    private void downloadFile(String fileUrl, String outputPath) throws IOException {
        URL url = new URL(fileUrl);
        try (ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream()); FileOutputStream fileOutputStream = new FileOutputStream(outputPath)) {
            fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
        }
    }

    /**
     * Downloads a JAR file from Maven Central repository.
     *
     * @param groupId    The group ID of the dependency
     * @param artifactId The artifact ID of the dependency
     * @param version    The version of the dependency
     * @return The path to the downloaded JAR file
     * @throws IOException If an I/O error occurs
     */
    public Path downloadJar(String groupId, String artifactId, String version) throws IOException {
        // Create output directory if it doesn't exist
        String jarFileName = artifactId + "-" + version;
        Path outputPath = Files.createTempFile(jarFileName, ".jar");

        // Construct the URL for the JAR file
        String groupPath = groupId.replace('.', '/');
        String jarFileNameExt = artifactId + "-" + version + ".jar";
        String jarUrl = String.format("%s/%s/%s/%s/%s", MAVEN_CENTRAL_URL, groupPath, artifactId, version, jarFileNameExt);

        // Download the JAR file

        downloadFile(jarUrl, outputPath.toString());
        return outputPath;
    }


}