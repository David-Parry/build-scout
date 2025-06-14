package com.davidparry.scout.io;

import com.davidparry.scout.common.LogFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogFileWriter implements LogFile {
    private static volatile LogFileWriter instance;
    private static final Object lock = new Object();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
    private final SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private PrintWriter logWriter;
    private final LogFactory logFactory;
    private LogFileWriter(LogFactory logFactory) {
        this.logFactory = logFactory;
    }

    public LogFactory getLogFactory() {
        return logFactory;
    }

    /**
     * Get the singleton instance of LogFileWriter
     * @param logFactory The directory where log files will be stored
     * @return LogFile interface instance
     */
    public static LogFile getInstance(LogFactory logFactory) {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new LogFileWriter(logFactory);
                }
            }
        }
        return instance;
    }




    /**
     * Initialize the log directory
     */
    private void initialize() {
        if (logFactory.getLogDirectory() != null) {
            File logDir = new File(logFactory.getLogDirectory());
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
        initialize();
        String timestamp = dateFormat.format(new Date());
        String currentLogFile = logFactory.getLogDirectory() + "/scoutServer.log";

        // Check if the current log file exists and rename it with timestamp
        File existingLogFile = new File(currentLogFile);
        if (existingLogFile.exists()) {
            String archivedLogFile = logFactory.getLogDirectory() + "/scoutServer_" + timestamp + ".log";
            File archivedFile = new File(archivedLogFile);
            if (!existingLogFile.renameTo(archivedFile)) {
                // If rename fails, try to delete the existing file to avoid conflicts
                existingLogFile.delete();
            }
        }

        try {
            logWriter = new PrintWriter(new FileWriter(currentLogFile, true));
        } catch (IOException e) {
            // If we can't write to the specified log directory, try the system temp directory
            String tempLogFile = System.getProperty("java.io.tmpdir") + "/scoutServer" + timestamp + ".log";
            logWriter = new PrintWriter(new FileWriter(tempLogFile, true));
        }
    }

    public void write(String message) {
        String timestamp = timestampFormat.format(new Date());
        rawWrite("[" + timestamp + "] " + message);
    }

    public void rawWrite(String message) {
        try {
            if (logWriter == null) {
                createNewLogFile();
            }
            logWriter.println(message);
            logWriter.flush();
        } catch (IOException e) {
            // just dont output
        }
    }

    public void write(String message, Throwable exception) {
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
}