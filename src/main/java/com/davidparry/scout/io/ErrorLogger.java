package com.davidparry.scout.io;

public class ErrorLogger extends BaseLogger {

    public ErrorLogger(String logDirectory, String level) {
        super(logDirectory, level);
    }

    @Override
    public void error(String message) {
        write(ERROR_PREFIX + message);
    }

    @Override
    public void error(String message, Throwable exception) {
        write(ERROR_PREFIX + message, exception);
    }

}
