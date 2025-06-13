package com.davidparry.scout.io;

public interface Logger {
    String BUILD_SCOUT_LOGGING = "BUILD_SCOUT_LOGGING";
    String LOG_DIRECTORY = "/log";
    String ERROR_PREFIX = "[ERROR] ";
    String INFO_PREFIX = "[INFO] ";
    String DEBUG_PREFIX = "[DEBUG] ";
    String API_PREFIX = "[API] ";

    void api(String message);

    void log(String message);

    void log(String message, Throwable exception);

    void info(String message);

    void error(String message);

    void error(String message, Throwable exception);

    String level();

    String path();
}
