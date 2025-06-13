package com.davidparry.scout.io;

public class DebugLogger extends BaseLogger {


    public DebugLogger(LogFile logFile) {
        super(logFile);
    }

    @Override
    public void log(String message) {
        getLogFile().write(DEBUG_PREFIX + message);
    }

    @Override
    public void log(String message, Throwable exception) {
        getLogFile().write(DEBUG_PREFIX + message, exception);
    }

    @Override
    public void info(String message) {
        getLogFile().write(INFO_PREFIX + message);
    }

    @Override
    public void api(String message) {
        getLogFile().write(API_PREFIX + message);
    }

}
