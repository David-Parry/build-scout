package com.davidparry.scout.io;

public interface Logger {
    String BUILD_SCOUT_LOGGING = "BUILD_SCOUT_LOGGING";

    void log(String message);

    void log(String message, Throwable exception);

    void close();

    void setLogDirectory(String directory);
}
