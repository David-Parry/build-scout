package com.davidparry.scout.annotation;

import com.davidparry.scout.State;
import com.davidparry.scout.handlers.*;
import com.davidparry.scout.io.ApplicationLogger;
import com.davidparry.scout.io.IOHandler;
import com.davidparry.scout.io.LogFileWriter;
import com.davidparry.scout.io.Logger;
import com.davidparry.scout.tools.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Initializer for Schema annotations that uses class loading hooks to
 * automatically process annotated classes.
 */
public class SchemaInitializer {
    private  final Set<Class<?>> PROCESSED_CLASSES = ConcurrentHashMap.newKeySet();
    private  final Logger logger = ApplicationLogger.getLogger(LogFileWriter.getInstance());
    public final SchemaRegistry schemaRegistry;

    public SchemaInitializer(SchemaRegistry schemaRegistry) {
        this.schemaRegistry = schemaRegistry;
    }

    /**
     * Initialize the Schema annotation processing system.
     * This should be called early in the application startup.
     */
    public  void initialize() {
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


    /**
     * Process all currently loaded classes to find and register those with @Schema annotations.
     */
    private  void processLoadedClasses() {
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
            logger.error("Error processing loaded classes: ", e);
        }
    }

    /**
     * Process a class if it has the @Schema annotation and hasn't been processed yet.
     *
     * @param clazz The class to check and process
     */
    public  void processClassIfAnnotated(Class<?> clazz) {
        if (clazz != null && !PROCESSED_CLASSES.contains(clazz) && clazz.isAnnotationPresent(Schema.class)) {
            PROCESSED_CLASSES.add(clazz);
            processAnnotation(clazz);
        }
    }

    /**
     * Get all currently loaded classes using the ClassScanner utility.
     *
     * @return Array of loaded classes or an empty array if they couldn't be retrieved
     */
    private  Class<?>[] getLoadedClasses() {
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
    private  Class<?>[] findAllLoadedClasses() {
        try {
            logger.log("Finding all loaded classes...");
            List<Class<?>> classes = getAnnotatedClasses();
            if (classes.isEmpty()) {
                logger.log("No classes found with @Schema annotation, calling JVM class scanner");
                classes = ClassScanner.scanAllClasses();
            }
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
    public  List<Class<?>> getAnnotatedClasses() {
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

    /**
     * Creates a type-safe instance of the given class if it implements Tool interface.
     *
     * @param clazz The class to instantiate
     * @param <T> The type parameter extending Tool
     * @return An instance of the class cast to Tool type
     * @throws Exception if instantiation fails or class doesn't implement Tool
     */
    @SuppressWarnings("unchecked")
    private <T extends Tool> T createToolInstance(Class<?> clazz) throws Exception {
        // First check if the class implements Tool interface
        if (!Tool.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException("Class " + clazz.getName() + " does not implement Tool interface");
        }

        // Cast the class to the bounded type and create instance
        Class<T> toolClass = (Class<T>) clazz;
        return toolClass.getDeclaredConstructor().newInstance();
    }

    /**
     * Alternative approach using Class.asSubclass() for even better type safety
     */
    private Tool createToolInstanceSafe(Class<?> clazz) throws Exception {
        // This will throw ClassCastException at compile time if clazz doesn't extend Tool
        Class<? extends Tool> toolClass = clazz.asSubclass(Tool.class);
        return toolClass.getDeclaredConstructor().newInstance();
    }

    public void processAnnotation(Class<?> annotatedClass) {
        logger.log("Ready to process this annotatedClass: " + annotatedClass);
        Schema annotation = annotatedClass.getAnnotation(Schema.class);
        logger.log("Schema class found: " + annotation);
        if (annotation == null) {
            return;
        }

        String name = annotation.name().isEmpty() ? annotatedClass.getSimpleName() : annotation.name();
        String description = annotation.description();

        try {
            // Use the type-safe instance creation method
            Tool instance = createToolInstanceSafe(annotatedClass);

            if (instance instanceof Handler) {
                // Create another instance for handler registration to avoid potential issues
                Handler handlerInstance = (Handler) createToolInstanceSafe(annotatedClass);
                schemaRegistry.registerHandler(name, handlerInstance);
            }

            // Now you can work with the instance
            logger.log("Successfully created an instance of: " + annotatedClass.getName());
            // Register the class with the SchemaRegistry
            schemaRegistry.register(annotatedClass, name, description, instance);
        } catch (Exception e) {
            logger.error("Error: " + annotatedClass.getName() + " must have a no-argument constructor and implement Tool interface", e);
        }
    }
}