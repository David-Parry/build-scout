package com.davidparry.scout.io;

public class InfoLogger extends ApplicationLogger {
    public InfoLogger(String logDirectory) {
        super(logDirectory);
    }

    @Override
    public void info(String message) {
        write(message);
    }

    @Override
    public void error(String message) {
        write(message);
    }

    @Override
    public void error(String message, Throwable exception) {
        write(message, exception);
    }

}
