package com.davidparry.scout.io;

public class ErrorLogger implements Logger {
    private final LogFile logFile;

    public ErrorLogger(LogFile logFile) {
        this.logFile = logFile;
        info("Error Logger initialized " + this.getClass().getSimpleName());

    }

    @Override
    public void api(String message) {

    }

    @Override
    public void log(String message) {

    }

    @Override
    public void log(String message, Throwable exception) {

    }

    @Override
    public void info(String message) {

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

}
