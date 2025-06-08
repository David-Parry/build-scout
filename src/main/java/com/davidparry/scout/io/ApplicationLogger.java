package com.davidparry.scout.io;

/**
 * Logger factory class for the application.
 * Provides centralized logging functionality with thread-safe operations.
 */
public class ApplicationLogger {
    private static Logger INSTANCE;

    private ApplicationLogger() {
    }


    public static Logger getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DevNullLogger();
        }
        return INSTANCE;
    }

    public static void setLogger(Logger logger) {
        INSTANCE = logger;
    }

}
