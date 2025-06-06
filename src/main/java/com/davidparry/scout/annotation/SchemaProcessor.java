package com.davidparry.scout.annotation;

import com.davidparry.scout.io.ApplicationLogger;
import com.davidparry.scout.io.Logger;
import com.davidparry.scout.tools.Tool;

/**
 * Processor for the Schema annotation that registers annotated classes
 * with the SchemaRegistry when they are loaded.
 */
public class SchemaProcessor {
    private static final Logger logger = ApplicationLogger.getInstance();

    /**
     * Process a class annotated with @Schema and register it with the SchemaRegistry.
     * This method should be called when a class with the @Schema annotation is loaded.
     *
     * @param annotatedClass The class annotated with @Schema
     */
    public static void processAnnotation(Class<?> annotatedClass) {
        logger.log("Ready to process this annotatedClass: " + annotatedClass);
        Schema annotation = annotatedClass.getAnnotation(Schema.class);
        logger.log("Schema class found: " + annotation);
        if (annotation == null) {
            return;
        }

        String name = annotation.name().isEmpty() ? annotatedClass.getSimpleName() : annotation.name();
        String description = annotation.description();


        try {
            // Create an instance of the annotated class
            Tool instance = (Tool) annotatedClass.getDeclaredConstructor().newInstance();
            // Now you can work with the instance
            logger.log("Successfully created an instance of: " + annotatedClass.getName());
            // Register the class with the SchemaRegistry
            SchemaRegistry.getInstance().register(annotatedClass, name, description, instance);
        } catch (Exception e) {
            logger.error("Error: " + annotatedClass.getName() + " must have a no-argument constructor", e);
        }
    }
}
    



