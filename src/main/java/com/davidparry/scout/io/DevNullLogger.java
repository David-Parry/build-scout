package com.davidparry.scout.io;

public class DevNullLogger implements Logger {
    @Override
    public void log(String message) {
        // empty intential
    }

    @Override
    public void log(String message, Throwable exception) {
        // empty intential
    }

    @Override
    public void close() {
        // empty intential
    }

    @Override
    public void setLogDirectory(String directory) {
        // empty intential
    }
}
