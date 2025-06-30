package com.davidparry.scout.io;

/**
 * Logger factory class for the application.
 * Provides centralized logging functionality with thread-safe operations.
 */
public class ApplicationLogger {
    public ApplicationLogger() {
    }

    public Logger getLogger(LogFile logFile) {
        String loggingLevel = logFile.getLogFactory().getLoggingLevel();
        if ("DEBUG".equalsIgnoreCase(loggingLevel)) {
            return new DebugLogger(logFile);
        } else if ("INFO".equalsIgnoreCase(loggingLevel)) {
            return new InfoLogger(logFile);
        } else if ("ERROR".equalsIgnoreCase(loggingLevel)) {
            return new ErrorLogger(logFile);
        } else if ("API".equalsIgnoreCase(loggingLevel)) {
            return new ApiLogger(logFile);
        } else {
            return new DevNullLogger();
        }


    }

}
