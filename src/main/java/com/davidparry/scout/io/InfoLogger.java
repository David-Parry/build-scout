package com.davidparry.scout.io;

public class InfoLogger extends BaseLogger {

    public InfoLogger(String logDirectory, String level) {
        super(logDirectory, level);
    }

    @Override
    public void info(String message) {
        write(INFO_PREFIX + message);
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
