package com.davidparry.scout.spec;

/**
 * Represents the type of JSON-RPC id used in a session.
 * Once determined from the first message, all subsequent messages must use the same type.
 */
public enum IdType {
    /** Id type has not been determined yet */
    UNKNOWN,
    /** Id should be a JSON number (unquoted) */
    NUMERIC,
    /** Id should be a JSON string (quoted) */
    STRING
}