package com.davidparry.scout.io;

public class ErrorLogger extends BaseLogger {

    public ErrorLogger(LogFile logFile) {
        super(logFile);
    }

    @Override
    public void error(String message) {
        getLogFile().write(ERROR_PREFIX + message);
    }

    @Override
    public void error(String message, Throwable exception) {
        getLogFile().write(ERROR_PREFIX + message, exception);
    }

}
