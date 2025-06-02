package com.davidparry.scout.annotation;

import com.davidparry.scout.State;
import com.davidparry.scout.handlers.*;
import com.davidparry.scout.io.ApplicationLogger;
import com.davidparry.scout.io.IOHandler;
import com.davidparry.scout.io.Logger;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
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

    /**
     * Initialize the Schema annotation processing system.
     * This should be called early in the application startup.
     */
    public static void initialize() {
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
            if (loadedClasses != null) {
                for (Class<?> clazz : loadedClasses) {
                    processClassIfAnnotated(clazz);
                }
            }
        } catch (Exception e) {
            logger.log("Error processing loaded classes: ",  e);
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
            // Try to get the instrumentation instance if available
            Class<?> instrumentationClass = Class.forName("java.lang.instrument.Instrumentation");
            Method getAllLoadedClassesMethod = instrumentationClass.getMethod("getAllLoadedClasses");

            // This will only work if the application was started with a Java agent
            Object instrumentation = getInstrumentationInstance();
            if (instrumentation != null) {
                return (Class<?>[]) getAllLoadedClassesMethod.invoke(instrumentation);
            }

            // Use ClassScanner utility instead of the non-existent getLoadedClasses method
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
            List<Class<?>> classes = ClassScanner.scanAllClasses();
            return classes.toArray(new Class<?>[0]);
        } catch (ClassNotFoundException | IOException e) {
            // Log the error

            // Return an empty array as fallback
            return new Class<?>[0];
        }
    }

    /**
     * Try to get the Instrumentation instance if available.
     * This will only work if the application was started with a Java agent.
     *
     * @return The Instrumentation instance or null if not available
     */
    private static Instrumentation getInstrumentationInstance() {
        try {
            Class<?> agentClass = Class.forName("com.davidparry.scout.agent.Agent");
            Method getInstrumentationMethod = agentClass.getMethod("getInstrumentation");
            return (Instrumentation) getInstrumentationMethod.invoke(null);
        } catch (Exception e) {
            // Silently fail - this is an optimization, not a critical feature
            return null;
        }
    }
}
