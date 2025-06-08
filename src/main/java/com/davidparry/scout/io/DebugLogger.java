package com.davidparry.scout.io;

public class DebugLogger extends BaseLogger {


    public DebugLogger(String logDirectory, String level) {
        super(logDirectory, level);
    }

    @Override
    public void log(String message) {
        write(DEBUG_PREFIX + message);
    }

    @Override
    public void log(String message, Throwable exception) {
        write(DEBUG_PREFIX + message, exception);
    }

    @Override
    public void error(String message) {
        write(ERROR_PREFIX + message);
    }

    @Override
    public void error(String message, Throwable exception) {
        write(ERROR_PREFIX + message, exception);
    }

    @Override
    public void info(String message) {
        write(INFO_PREFIX + message);
    }

}
