package com.davidparry.scout.io;

import static com.davidparry.scout.io.Logger.DEBUG_PREFIX;

public class DebugLogger implements  Logger {
    private LogFile logFile;


    public DebugLogger(LogFile logFile) {
        this.logFile = logFile;
        log("Debugger Logger initialized " + this.getClass().getSimpleName());
    }

    @Override
    public void log(String message) {
        logFile.write(DEBUG_PREFIX + message);
    }

    @Override
    public void log(String message, Throwable exception) {
        logFile.write(DEBUG_PREFIX + message, exception);
    }

    @Override
    public void info(String message) {
        logFile.write(INFO_PREFIX + message);
    }

    @Override
    public void error(String message) {
        logFile.write(ERROR_PREFIX + message);
    }

    @Override
    public void error(String message, Throwable exception) {
        logFile.write(ERROR_PREFIX + message, exception);
    }

    @Override
    public String level() {
        return logFile.getLogFactory().getLoggingLevel();
    }

    @Override
    public String path() {
        return logFile.getLogFactory().getLogDirectory();
    }

    @Override
    public void api(String message) {
        logFile.write(API_PREFIX + message);
    }

}
