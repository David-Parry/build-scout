package com.davidparry.scout.annotation;

import com.davidparry.scout.io.ApplicationLogger;
import com.davidparry.scout.io.LogFileWriter;
import com.davidparry.scout.io.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Utility class for scanning and loading classes from the classpath.
 */
public class ClassScanner {
    private static final Logger logger = ApplicationLogger.getLogger(LogFileWriter.getInstance());

    /**
     * Scans all classes accessible from the context class loader which belong to the given package.
     *
     * @param packageName The base package to scan
     * @return The classes found
     * @throws ClassNotFoundException If a class cannot be loaded
     * @throws IOException            If there is an error reading from the file system or JAR
     */
    public static List<Class<?>> scanClasses(String packageName) throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<>();

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }

        List<Class<?>> classes = new ArrayList<>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }

        // Also scan JAR files
        classes.addAll(findClassesInJars(packageName));

        return classes;
    }

    /**
     * Scans all classes in the com.davidparry.scout.tools package.
     *
     * @return The classes found in the com.davidparry.scout.tools package
     * @throws ClassNotFoundException If a class cannot be loaded
     * @throws IOException            If there is an error reading from the file system or JAR
     */
    public static List<Class<?>> scanAllClasses() throws ClassNotFoundException, IOException {
        // Only scan classes in the com.davidparry.scout.tools package
        return scanClasses("com.davidparry.scout.tools");
    }

    /**
     * Recursive method used to find all classes in a given directory and subdirectories.
     *
     * @param directory   The base directory
     * @param packageName The package name for classes found inside the base directory
     * @return The classes found
     * @throws ClassNotFoundException If a class cannot be loaded
     */
    private static List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        if (!directory.exists()) {
            return classes;
        }

        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    classes.addAll(findClasses(file, packageName + "." + file.getName()));
                } else if (file.getName().endsWith(".class")) {
                    String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                    try {
                        classes.add(Class.forName(className));
                    } catch (NoClassDefFoundError | ClassNotFoundException e) {
                        // Skip classes that can't be loaded
                    }
                }
            }
        }
        return classes;
    }

    /**
     * Finds classes in JAR files on the classpath.
     *
     * @param packageName The package name to search for
     * @return The classes found
     */
    private static List<Class<?>> findClassesInJars(String packageName) {
        List<Class<?>> classes = new ArrayList<>();
        String classpath = System.getProperty("java.class.path");
        String[] classpathEntries = classpath.split(File.pathSeparator);

        String packagePath = packageName.replace('.', '/');
        boolean scanAllPackages = packageName.isEmpty();

        for (String classpathEntry : classpathEntries) {
            if (classpathEntry.endsWith(".jar")) {
                try (JarFile jarFile = new JarFile(classpathEntry)) {
                    Enumeration<JarEntry> entries = jarFile.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        String entryName = entry.getName();

                        if (entryName.endsWith(".class") && (scanAllPackages || entryName.startsWith(packagePath))) {
                            String className = entryName.replace('/', '.').substring(0, entryName.length() - 6);
                            try {
                                classes.add(Class.forName(className));
                            } catch (NoClassDefFoundError | ClassNotFoundException e) {
                                // Skip classes that can't be loaded
                            }
                        }
                    }
                } catch (IOException e) {
                    // Skip JAR files that can't be read
                }
            }
        }

        return classes;
    }
}
