package com.davidparry.scout.annotation;

import com.davidparry.scout.State;
import com.davidparry.scout.handlers.*;
import com.davidparry.scout.io.ApplicationLogger;
import com.davidparry.scout.io.IOHandler;
import com.davidparry.scout.io.Logger;
import com.davidparry.scout.tools.*;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Initializer for Schema annotations that uses class loading hooks to
 * automatically process annotated classes.
 */
public class SchemaInitializer {
    private static final Set<Class<?>> PROCESSED_CLASSES = ConcurrentHashMap.newKeySet();
    private static final Logger logger = ApplicationLogger.getInstance();
    private static final boolean IS_NATIVE_IMAGE = isNativeImage();

    /**
     * Determines if the code is running in a GraalVM native image
     * @return true if running as a native image, false otherwise
     */
    private static boolean isNativeImage() {
        return System.getProperty("org.graalvm.nativeimage.imagecode") != null;
    }

    /**
     * Initialize the Schema annotation processing system.
     * This should be called early in the application startup.
     */
    public static void initialize() {
        logger.log("Initializing Schema Processing...");
        // Register a shutdown hook to ensure all classes are processed
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Process any remaining classes that might have been loaded but not processed
            processLoadedClasses();
        }));
        //registerCoreClasses();

        // Process already loaded classes
        processLoadedClasses();
    }

    public static void registerCoreClasses(IOHandler ioHandler, State state) {
        SchemaRegistry.getInstance().registerHandler("initialize", new InitializeHandler());
        SchemaRegistry.getInstance().registerHandler("notifications", new NotificationHandler());
        SchemaRegistry.getInstance().registerHandler("notifications/roots/list_changed", new NotificationRootsHandler(ioHandler,state));
        SchemaRegistry.getInstance().registerHandler("notifications/initialized", new NotificationInitializedHandler(ioHandler,state));
        SchemaRegistry.getInstance().registerHandler("tools/list", new ToolsListHandler(SchemaRegistry.getInstance()));
        SchemaRegistry.getInstance().registerHandler("tools/call", new ToolDispatcherHandler());
        SchemaRegistry.getInstance().registerHandler("prompts/list", new PromptsListHandler());
        SchemaRegistry.getInstance().registerHandler("prompts/get", new PromptDispatchHandler());
        SchemaRegistry.getInstance().registerHandler("completion/complete", new CompletionComplete());
    }

    /**
     * Process all currently loaded classes to find and register those with @Schema annotations.
     */
    private static void processLoadedClasses() {
        try {

            // Get all loaded classes using reflection
            Class<?>[] loadedClasses = getLoadedClasses();
            logger.log("Loaded " + loadedClasses.length + " classes");
            if (loadedClasses != null) {
                for (Class<?> clazz : loadedClasses) {
                    processClassIfAnnotated(clazz);
                }
            }
        } catch (Exception e) {
            logger.error("Error processing loaded classes: ",  e);
        }
    }

    /**
     * Process a class if it has the @Schema annotation and hasn't been processed yet.
     *
     * @param clazz The class to check and process
     */
    public static void processClassIfAnnotated(Class<?> clazz) {
        if (clazz != null && !PROCESSED_CLASSES.contains(clazz) && clazz.isAnnotationPresent(Schema.class)) {
            PROCESSED_CLASSES.add(clazz);
            SchemaProcessor.processAnnotation(clazz);
        }
    }

    /**
     * Get all currently loaded classes using the ClassScanner utility.
     *
     * @return Array of loaded classes or an empty array if they couldn't be retrieved
     */
    private static Class<?>[] getLoadedClasses() {
        try {
            return findAllLoadedClasses();
        } catch (Exception e) {
            logger.log("Error processing loaded classes: ", e);
            // Return empty array instead of null
            return new Class<?>[0];
        }
    }

    /**
     * Find all loaded classes using the ClassScanner utility.
     *
     * @return Array of classes or an empty array if scanning failed
     */
    private static Class<?>[] findAllLoadedClasses() {
        try {
            // If running as a native image, use the explicit registry instead of dynamic scanning
            if (IS_NATIVE_IMAGE) {
                logger.log("Running as native image, using explicit class registry");
                List<Class<?>> classes = getAnnotatedClasses();
                return classes.toArray(new Class<?>[0]);
            }

            // Otherwise use dynamic class scanning (JVM mode)
            List<Class<?>> classes = ClassScanner.scanAllClasses();
            return classes.toArray(new Class<?>[0]);
        } catch (ClassNotFoundException | IOException e) {
            // Log the error
            logger.error("Error finding loaded classes: ", e);
            // Return an empty array as fallback
            return new Class<?>[0];
        }
    }
    /**
     * Get a list of all classes annotated with @Schema.
     * This method is used in native image mode instead of dynamic class scanning.
     *
     * @return List of all classes with the @Schema annotation
     */
    public static List<Class<?>> getAnnotatedClasses() {
        List<Class<?>> annotatedClasses = new ArrayList<>();

        // Add all classes with @Schema annotation here
        annotatedClasses.add(BuildGradleProject.class);
        annotatedClasses.add(BuildSystemFilePaths.class);
        annotatedClasses.add(BuildTool.class);
        annotatedClasses.add(DownloadCurrentLatestSource.class);
        annotatedClasses.add(FindBuildSystem.class);
        annotatedClasses.add(FindClassUsage.class);
        annotatedClasses.add(GetFileInfo.class);
        annotatedClasses.add(GetResourceInfo.class);
        annotatedClasses.add(JarDiffReporter.class);
        annotatedClasses.add(LatestDependencyVersion.class);
        annotatedClasses.add(ListDependencies.class);
        annotatedClasses.add(ReplaceSourceCodeComplete.class);
        annotatedClasses.add(UnitTestGradleProject.class);
        annotatedClasses.add(UpdateDependencyVersion.class);
        logger.log("Found " + annotatedClasses.size() + " explicitly registered @Schema classes");
        return annotatedClasses;
    }
}
