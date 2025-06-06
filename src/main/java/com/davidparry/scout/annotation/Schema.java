package com.davidparry.scout.annotation;

import com.davidparry.scout.spec.Tool;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark classes that should be registered in the SchemaRegistry.
 * Classes annotated with @Schema will be automatically registered when loaded,
 * along with their name, description, and JSON schema representation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface Schema {
    /**
     * The name of the schema. If not specified, the class simple name will be used.
     * This will be used as the key in the registry.
     */
    String name() default "";
    
    /**
     * Description of the schema.
     */
    String description() default "";

    
}