package com.davidparry.scout.io;

public class InfoLogger extends BaseLogger {

    public InfoLogger(LogFile logFile) {
        super(logFile);
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
