package com.davidparry.scout.io;

public class InfoLogger implements Logger {
    private final LogFile logFile;

    public InfoLogger(LogFile logFile) {
        this.logFile = logFile;
        info("InfoLogger Logger initialized " + this.getClass().getSimpleName());
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

    @Override
    public void log(String message) {

    }

    @Override
    public void log(String message, Throwable exception) {

    }
}
