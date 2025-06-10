package com.davidparry.scout.common;

import com.davidparry.scout.io.*;

public class LogFactory {
    private static volatile String loggingLevel;
    private static volatile String logDirectory;

    private String getLoggingLevel() {
        String level = loggingLevel;
        if (level == null) {
            synchronized (LogFactory.class) {
                level = loggingLevel;
                if (level == null) {
                    loggingLevel = level = System.getenv("BUILD_SCOUT_LOGGING");
                }
            }
        }
        return level;
    }

    private String getLogDirectory() {
        String directory = logDirectory;
        if (directory == null) {
            synchronized (LogFactory.class) {
                directory = logDirectory;
                if (directory == null) {
                    logDirectory = directory = System.getProperty("user.home") + Logger.LOG_DIRECTORY;
                }
            }
        }
        return directory;
    }

    public Logger getLogger() {
        String loggingLevel = getLoggingLevel();
        String directory = getLogDirectory();
        if ("DEBUG".equalsIgnoreCase(loggingLevel)) {
            return new DebugLogger(directory, loggingLevel);
        } else if ("INFO".equalsIgnoreCase(loggingLevel)) {
            return new InfoLogger(directory, loggingLevel);
        } else if ("ERROR".equalsIgnoreCase(loggingLevel)) {
            return new ErrorLogger(directory, loggingLevel);
        } else if ("API".equalsIgnoreCase(loggingLevel)) {
            return new ApiLogger(directory, loggingLevel);
        } else {
            return new DevNullLogger();
        }
    }


}