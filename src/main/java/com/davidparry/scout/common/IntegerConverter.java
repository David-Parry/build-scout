package com.davidparry.scout.common;

/**
 * Small utility for safe integer conversions that default to 0 on invalid or null inputs.
 */
public final class IntegerConverter {

    private IntegerConverter() {}

    /**
     * Parse a String into an int, returning 0 if the input is null, blank, or not a valid integer.
     */
    public static int parseOrZero(String input) {
        if (input == null) {
            return 0;
        }
        String s = input.trim();
        if (s.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Parse a String using parseOrZero and return its String representation.
     * Returns "0" for null/blank/invalid inputs.
     */
    public static String parseOrZeroString(String input) {
        return String.valueOf(parseOrZero(input));
    }

    /**
     * Convert an arbitrary Object to an int, returning 0 if null or not parsable.
     * - If input is a Number, returns intValue().
     * - If input is a String, parses it (trimmed) or returns 0 if invalid.
     * - Otherwise attempts to parse String.valueOf(obj) and falls back to 0 on failure.
     */
    public static int toIntOrZero(Object input) {
        if (input == null) {
            return 0;
        }
        if (input instanceof Number n) {
            return n.intValue();
        }
        if (input instanceof String s) {
            return parseOrZero(s);
        }
        try {
            return Integer.parseInt(String.valueOf(input).trim());
        } catch (Exception e) {
            return 0;
        }
    }
}
