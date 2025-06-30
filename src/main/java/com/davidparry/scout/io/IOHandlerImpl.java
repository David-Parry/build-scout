package com.davidparry.scout.io;

import com.davidparry.scout.common.LogFactory;

import java.io.PrintWriter;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * IOHandler provides methods for handling input and output operations.
 * It supports console input/output, file operations, and formatted output.
 * It can also publish events when input is received.
 */
public class IOHandlerImpl implements IOHandler {
    private final Logger logger = new ApplicationLogger().getLogger(LogFileWriter.getInstance(new LogFactory()));
    private final PrintWriter writer;
    private final List<Consumer<String>> lineListeners;
    private final AtomicBoolean running;

    /**
     * Constructs an IOHandler that uses System.in and System.out
     */
    public IOHandlerImpl() {
        this.writer = new PrintWriter(System.out, true);
        this.lineListeners = new CopyOnWriteArrayList<>();
        this.running = new AtomicBoolean(false);
    }


    /**
     * Adds a listener that will be notified when a line of input is read
     *
     * @param listener Consumer that will receive the line of input
     */
    public void addLineListener(Consumer<String> listener) {
        lineListeners.add(listener);
    }

    /**
     * Removes a line listener
     *
     * @param listener The listener to remove
     */
    public void removeLineListener(Consumer<String> listener) {
        lineListeners.remove(listener);
    }

    /**
     * Notifies all line listeners of a new line of input
     *
     * @param line The line to publish
     */
    private void publishLine(String line) {
        for (Consumer<String> listener : lineListeners) {
            try {
                listener.accept(line);
            } catch (Exception e) {
                logger.log("Error in line listener",e);
            }
        }
    }

    /**
     * Writes a string to the output followed by a line break
     *
     * @param text The text to write
     */
    public void writeLine(String text) {
        logger.api("sent:"+text);
        writer.println(text);
        writer.flush();
    }


    /**
     * Starts a background virtual thread that continuously reads input and publishes events.
     * This is useful for asynchronous input handling.
     * Uses Java's virtual thread feature for lightweight concurrency.
     */
    public void startInputReader() {
        if (running.get()) {
            return; // Already running
        }

        try (Scanner scanner = new Scanner(System.in)) {
            running.set(true);
            try {
                while (running.get()) {
                    if (!scanner.hasNextLine()) {
                        logger.log("Input stream closed. Exiting.");
                        running.set(false);
                        break;
                    }
                    String line = scanner.nextLine();
                    logger.api("received:"+line);
                    publishLine(line);
                }
                logger.log("Input stream closed.");
            } catch (Exception e) {
                if (running.get()) { // Only log if we're still supposed to be running
                    logger.log("Error while reading next line from input reader: ", e);
                }
                throw e; // Re-throw to ensure outer catch handles it
            } finally {
                stopRunning();
            }
        } catch (Exception e) {
            logger.log("Fatal error in startInputReader: ", e);
            stopRunning(); // Ensure shutdown on any exception
        }
    }

    public void stopRunning() {
        running.set(false);
    }

    public boolean isRunning() {
        return running.get();
    }

}
