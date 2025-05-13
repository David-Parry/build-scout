package com.davidparry.scout.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Singleton logger class for the application.
 * Provides centralized logging functionality with thread-safe operations.
 */
public class ApplicationLogger implements Logger {
    private static Logger INSTANCE;
    private final ReentrantLock lock = new ReentrantLock();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
    private final SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private String logDirectory;
    private String currentLogFile;
    private PrintWriter logWriter;
    private static boolean loggingEnabled = false;

    static {
        try {
            String envValue = System.getenv(BUILD_SCOUT_LOGGING);
            loggingEnabled = "true".equalsIgnoreCase(envValue);
        } catch (Exception e) {}
    }
    /**
     * Private constructor to enforce singleton pattern
     * 
     * @param logDirectory The directory path where logs should be stored
     */
    private ApplicationLogger(String logDirectory) {
        this.logDirectory = logDirectory;
        initialize();
    }

    /**
     * Get the singleton instance of the logger with default log directory
     *
     * @return The ApplicationLogger instance
     */
    public static synchronized Logger getInstance() {
        if (INSTANCE == null) {
            if(loggingEnabled) {
                INSTANCE = new ApplicationLogger(System.getProperty("user.home") + "/logs");
            } else {
                INSTANCE = new DevNullLogger();
            }
        }
        return INSTANCE;
    }
    
    /**
     * Get the singleton instance of the logger with specified log directory
     *
     * @param logDirectory The directory path where logs should be stored
     * @return The ApplicationLogger instance
     */
    public static synchronized Logger getInstance(String logDirectory) {
        if (INSTANCE == null) {
            if(loggingEnabled) {
                INSTANCE = new ApplicationLogger(logDirectory);
            } else {
                INSTANCE = new DevNullLogger();
            }
        } else {
            // If instance already exists, update the log directory
            INSTANCE.setLogDirectory(logDirectory);
        }
        return INSTANCE;
    }

    /**
     * Set a custom log directory
     *
     * @param logDirectory The directory path where logs should be stored
     */
    public  void setLogDirectory(String logDirectory) {
        lock.lock();
        try {
            this.logDirectory = logDirectory;
            initialize();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Initialize the log directory
     */
    private void initialize() {
        File logDir = new File(logDirectory);
        if (!logDir.exists()) {
            logDir.mkdirs();
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
        currentLogFile = logDirectory + "/scoutServer" + timestamp + ".log";
        logWriter = new PrintWriter(new FileWriter(currentLogFile, true));
    }

    /**
     * Log a message to the current log file
     *
     * @param message The message to log
     */
    @Override
    public void log(String message) {
        lock.lock();
        try {
            if (logWriter == null) {
                createNewLogFile();
            }

            String timestamp = timestampFormat.format(new Date());
            logWriter.println("[" + timestamp + "] " + message);
            logWriter.flush();
        } catch (IOException e) {
            // just dont output
        } finally {
            lock.unlock();
        }
    }

    /**
     * Log an exception with a message
     *
     * @param message   The message to log
     * @param exception The exception to log
     */
    @Override
    public void log(String message, Throwable exception) {
        lock.lock();
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
        } finally {
            lock.unlock();
        }
    }

    /**
     * Close the logger and release resources
     */
    @Override
    public void close() {
        lock.lock();
        try {
            if (logWriter != null) {
                logWriter.close();
                logWriter = null;
            }
        } finally {
            lock.unlock();
        }
    }
}
