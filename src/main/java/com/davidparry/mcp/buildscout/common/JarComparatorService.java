package com.davidparry.mcp.buildscout.common;

import com.davidparry.mcp.buildscout.common.JarDownloader;
import japicmp.cmp.JApiCmpArchive;
import japicmp.cmp.JarArchiveComparator;
import japicmp.cmp.JarArchiveComparatorOptions;
import japicmp.model.AccessModifier;
import japicmp.model.JApiChangeStatus;
import japicmp.model.JApiClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service for comparing JAR files and identifying changes between versions.
 */
public class JarComparatorService {
    private static final Logger logger = LoggerFactory.getLogger(JarComparatorService.class);
    private final JarDownloader jarDownloader;

    public JarComparatorService(JarDownloader jarDownloader) {
        this.jarDownloader = jarDownloader;
    }

    /**
     * Compares two versions of a JAR file and returns a list of changed classes.
     *
     * @param params Map containing groupId, artifactId, currentVersion, and latestVersion
     * @return List of strings describing the changes between versions
     * @throws IOException If there's an error downloading the JAR files
     */
    public List<DiffData> compareJars(Map<String, String> params) throws IOException {
        List<DiffData> classesChanged = new ArrayList<>();
        
        String groupId = params.get("groupId");
        String artifactId = params.get("artifactId");
        String currentVersion = params.get("currentVersion");
        String latestVersion = params.get("latestVersion");

        logger.info("Comparing JARs - groupId: {}, artifactId: {}, currentVersion: {}, latestVersion: {}", 
                groupId, artifactId, currentVersion, latestVersion);

        File oldJar = jarDownloader.downloadJar(groupId, artifactId, currentVersion).toFile();
        File newJar = jarDownloader.downloadJar(groupId, artifactId, latestVersion).toFile();

        // Set up comparator options
        JarArchiveComparatorOptions options = new JarArchiveComparatorOptions();
        options.setIncludeSynthetic(false);
        options.setAccessModifier(AccessModifier.PUBLIC);

        // Create comparator
        JarArchiveComparator comparator = new JarArchiveComparator(options);

        JApiCmpArchive oldArchive = new JApiCmpArchive(oldJar, currentVersion);
        JApiCmpArchive newArchive = new JApiCmpArchive(newJar, latestVersion);
        List<JApiClass> jApiClasses = comparator.compare(oldArchive, newArchive);

        for (JApiClass cls : jApiClasses) {
            if (cls.getChangeStatus().equals(JApiChangeStatus.MODIFIED) || 
                cls.getChangeStatus().equals(JApiChangeStatus.NEW) || 
                cls.getChangeStatus().equals(JApiChangeStatus.REMOVED)) {
                classesChanged.add(new DiffData(cls.getFullyQualifiedName(), cls.getChangeStatus().name()));
            }
        }
        return classesChanged;
    }
}