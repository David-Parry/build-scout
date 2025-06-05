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
public abstract class ApplicationLogger implements Logger {
    private static Logger INSTANCE;
    private static String loggingLevel;

    static {
        try {
            loggingLevel = System.getenv(BUILD_SCOUT_LOGGING);
        } catch (Exception e) {
        }
    }

    private final ReentrantLock lock = new ReentrantLock();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
    private final SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private String logDirectory;
    private String currentLogFile;
    private PrintWriter logWriter;

    /**
     * Private constructor to enforce singleton pattern
     *
     * @param logDirectory The directory path where logs should be stored
     */
    protected ApplicationLogger(String logDirectory) {
        this.logDirectory = logDirectory;
        initialize();
    }

    /**
     * Get the singleton instance of the logger with default log directory
     *
     * @return The ApplicationLogger instance
     */
    public static synchronized Logger getInstance() {
       return getInstance(System.getProperty("user.home")+ LOG_DIRECTORY);
    }

    /**
     * Get the singleton instance of the logger with specified log directory
     *
     * @param logDirectory The directory path where logs should be stored
     * @return The ApplicationLogger instance
     */
    public static synchronized Logger getInstance(String logDirectory) {
        if (INSTANCE == null) {
            if ("DEBUG".equalsIgnoreCase(loggingLevel)) {
                INSTANCE = new DebugLogger(logDirectory );
            } else if ("INFO".equalsIgnoreCase(loggingLevel)) {
                INSTANCE = new InfoLogger(logDirectory);
            } else if ("ERROR".equalsIgnoreCase(loggingLevel)) {
                INSTANCE = new ErrorLogger(logDirectory);
            } else {
                INSTANCE = new DevNullLogger();
            }
        }
        return INSTANCE;
    }

    /**
     * Set a custom log directory
     *
     * @param logDirectory The directory path where logs should be stored
     */
    private void setLogDirectory(String logDirectory) {
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

    protected void write(String message) {
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

    protected void write(String message, Throwable exception) {
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
}
