package com.davidparry.scout.io;

public class DevNullLogger implements Logger {


    @Override
    public void api(String message) {

    }

    @Override
    public void log(String message) {

    }

    @Override
    public void log(String message, Throwable exception) {

    }

    @Override
    public void info(String message) {

    }

    @Override
    public void error(String message) {

    }

    @Override
    public void error(String message, Throwable exception) {

    }

    @Override
    public String level() {
        return "DEV/NULL";
    }

    @Override
    public String path() {
        return "DEV/NULL/PATH";
    }
}
