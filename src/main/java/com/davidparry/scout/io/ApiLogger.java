package com.davidparry.scout.io;

public class ApiLogger extends BaseLogger {


    public ApiLogger(LogFile logFile) {
        super(logFile);
    }


    @Override
    public void api(String message) {
       getLogFile().rawWrite(message);
    }
}
