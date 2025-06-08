package com.davidparry.scout.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BaseLogger implements Logger {
    public final String loggingLevel;
    public final String logDirectory;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
    private final SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private PrintWriter logWriter;

    public BaseLogger(String logDirectory, String loggingLevel) {
        this.logDirectory = logDirectory;
        this.loggingLevel = loggingLevel;
        initialize();
    }

    /**
     * Initialize the log directory
     */
    private void initialize() {
        if (logDirectory != null) {
            File logDir = new File(logDirectory);
            if (!logDir.exists()) {
                logDir.mkdirs();
            }
        }
    }

    /**
     * Create a new log file with timestamp
     *
     * @throws IOException If there's an error creating the log file
     */
    private void createNewLogFile() throws IOException {
        if (logWriter != null) {
            logWriter.close();
        }

        String timestamp = dateFormat.format(new Date());
        String currentLogFile = logDirectory + "/scoutServer" + timestamp + ".log";
        try {
            logWriter = new PrintWriter(new FileWriter(currentLogFile, true));
        } catch (IOException e) {
            // If we can't write to the specified log directory, try the system temp directory
            String tempLogFile = System.getProperty("java.io.tmpdir") + "/scoutServer" + timestamp + ".log";
            logWriter = new PrintWriter(new FileWriter(tempLogFile, true));
        }
    }

    protected void write(String message) {
        try {
            if (logWriter == null) {
                createNewLogFile();
            }

            String timestamp = timestampFormat.format(new Date());
            logWriter.println("[" + timestamp + "] " + message);
            logWriter.flush();
        } catch (IOException e) {
            // just dont output
        }
    }

    protected void write(String message, Throwable exception) {
        try {
            if (logWriter == null) {
                createNewLogFile();
            }

            String timestamp = timestampFormat.format(new Date());
            logWriter.println("[" + timestamp + "] " + message);
            exception.printStackTrace(logWriter);
            logWriter.flush();
        } catch (IOException e) {
            // just dont output
        }
    }

    /**
     * Close the logger and release resources
     */
    @Override
    public void close() {
        try {
            if (logWriter != null) {
                logWriter.close();
                logWriter = null;
            }
        } catch (Exception e) {
            // Ignore any exceptions during close
        }
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
        return loggingLevel;
    }

    @Override
    public String path() {
        return logDirectory;
    }
}
