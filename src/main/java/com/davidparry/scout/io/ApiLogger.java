package com.davidparry.scout.io;

public class ApiLogger extends BaseLogger {


    public ApiLogger(String logDirectory, String level) {
        super(logDirectory, level);
    }


    @Override
    public void api(String message) {
        rawWrite(message);
    }
}
