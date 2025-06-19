package com.davidparry.scout.common;

import com.davidparry.scout.io.*;

public class LogFactory {
    private static volatile String loggingLevel;
    private static volatile String logDirectory;


    public String getLoggingLevel() {
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

    public String getLogDirectory() {
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



}