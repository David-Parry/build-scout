package com.davidparry.scout.io;

import com.davidparry.scout.common.LogFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogFileWriter implements LogFile {
    private static volatile LogFileWriter instance;
    private static final Object lock = new Object();

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
    private final SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    /** Resolved once – cached for subsequent log rotations. */
    private final String processId = resolveProcessId();

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
     *
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
                //noinspection ResultOfMethodCallIgnored – best-effort creation
                logDir.mkdirs();
            }
        }
    }

    /**
     * Create a new log file with a PID-specific name. If the file already exists,
     * archive the previous one by appending a timestamp before starting a new
     * log for the current process.
     *
     * @throws IOException If there's an error creating the log file
     */
    private void createNewLogFile() throws IOException {
        if (logWriter != null) {
            logWriter.close();
        }

        initialize();

        String timestamp = dateFormat.format(new Date());

        // e.g. /logs/scoutServer-12345.log
        String baseLogFilePath = logFactory.getLogDirectory() + "/scoutServer-" + processId;
        String currentLogFile = baseLogFilePath + ".log";

        // Check if the current log file exists and rename it with timestamp
        File existingLogFile = new File(currentLogFile);
        if (existingLogFile.exists()) {
            // e.g. /logs/scoutServer-12345_20240101-121314.log
            String archivedLogFile = baseLogFilePath + "_" + timestamp + ".log";
            File archivedFile = new File(archivedLogFile);
            if (!existingLogFile.renameTo(archivedFile)) {
                // If rename fails, try to delete the existing file to avoid conflicts
                //noinspection ResultOfMethodCallIgnored – best-effort cleanup
                existingLogFile.delete();
            }
        }

        try {
            logWriter = new PrintWriter(new FileWriter(currentLogFile, true));
        } catch (IOException e) {
            // If we can't write to the specified log directory, try the system temp directory
            String tempLogFile = System.getProperty("java.io.tmpdir")
                    + "/scoutServer-" + processId + "_" + timestamp + ".log";
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
            // Ignore logging failures – prevent cascading errors
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
            // Ignore logging failures – prevent cascading errors
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
        } catch (Exception ignored) {
            // Ignore any exceptions during close
        }
    }

    /**
     * Obtain the current process ID in a JVM-agnostic way (works on Java 8+).
     *
     * @return a non-empty string representing the process ID
     */
    private String resolveProcessId() {
        // Attempt to use the Java 9+ API reflectively to avoid compile-time dependency
        try {
            Class<?> processHandleClass = Class.forName("java.lang.ProcessHandle");
            Object currentHandle = processHandleClass.getMethod("current").invoke(null);
            long pid = (long) processHandleClass.getMethod("pid").invoke(currentHandle);
            return String.valueOf(pid);
        } catch (Exception ignored) {
            // Fallback for Java 8: parse RuntimeMXBean name (format pid@hostname)
            String jvmName = ManagementFactory.getRuntimeMXBean().getName();
            int idx = jvmName.indexOf('@');
            return (idx > 0) ? jvmName.substring(0, idx) : jvmName;
        }
    }
}