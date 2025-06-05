package com.davidparry.scout.io;

public class DebugLogger extends ApplicationLogger {

    public DebugLogger(String logDirectory) {
        super(logDirectory);
    }

    @Override
    public void log(String message) {
        write(message);
    }

    @Override
    public void log(String message, Throwable exception) {
        write(message, exception);
    }

    @Override
    public void error(String message) {
        write(message);
    }

    @Override
    public void error(String message, Throwable exception) {
        write(message, exception);
    }

    @Override
    public void info(String message) {
        write(message);
    }

}
