package com.davidparry.scout.io;

public class ErrorLogger extends ApplicationLogger {

    public ErrorLogger(String logDirectory) {
        super(logDirectory);
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
