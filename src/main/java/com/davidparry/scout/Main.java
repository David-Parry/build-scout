package com.davidparry.scout;

import com.davidparry.scout.annotation.SchemaInitializer;
import com.davidparry.scout.annotation.SchemaRegistry;
import com.davidparry.scout.common.ClientConsumer;
import com.davidparry.scout.io.ApplicationLogger;
import com.davidparry.scout.io.IOHandler;
import com.davidparry.scout.io.IOHandlerImpl;
import com.davidparry.scout.io.Logger;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main {
    private static final AtomicBoolean isShuttingDown = new AtomicBoolean(false);
    private static final CountDownLatch shutdownLatch = new CountDownLatch(1);
    private static final Logger logger = ApplicationLogger.getInstance();
    public static String MCP_SERVER_NAME = "scout-server";
    public static String MCP_SERVER_VERSION = loadVersion();
    private static IOHandler io;
    private static RequestController controller;

    public static void main(String[] args) {
        logger.log("Starting Scout version " + MCP_SERVER_VERSION);
        // Create an IOHandler instance for console I/O
        io = new IOHandlerImpl();

        // Initialize the Schema annotation system
        SchemaInitializer.initialize();
        SchemaInitializer.registerCoreClasses(io, ApplicationState.instance());
        SchemaRegistry registry = SchemaRegistry.getInstance();


        // Create the request controller
        controller = new RequestController(io, registry, new ClientConsumer(ApplicationState.instance()));
        logger.log("Controller initialized ");
        try {
            // Add a listener for individual lines
            io.addLineListener(Main::process);

            // Create a shutdown hook to close resources properly
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (isShuttingDown.compareAndSet(false, true)) {
                    // Stop the async input reader and close resources
                    //io.stopAsyncInputReader();
                    stop();
                    logger.close();
                    shutdownLatch.countDown();
                }
            }));
            logger.info("Scout version " + MCP_SERVER_VERSION+ " started.");
            // Start the async input reader
            io.startInputReader();
            keepRunning();
            logger.log("Scout shutting down");
        } catch (Exception e) {
            logger.log("Error in main method", e);
            stop();
            logger.close();
            System.exit(1);
        }
    }

    private static void stop() {
        if (io != null) {
            io.stopRunning();
        }
    }

    /**
     * Keeps the application running until terminated by the user or until IO processing stops
     */
    private static void keepRunning() {
        try {
            // Create a polling mechanism to check if IO is still running
            while (!isShuttingDown.get()) {
                // Check if IO has stopped running
                if (io != null && !io.isRunning()) {
                    logger.log("IO processing has stopped. Initiating shutdown.");
                    if (isShuttingDown.compareAndSet(false, true)) {
                        logger.close();
                        shutdownLatch.countDown();
                    }
                }

                // Check if shutdown has been triggered
                if (shutdownLatch.await(500, TimeUnit.MILLISECONDS)) {
                    // Shutdown was triggered, exit the loop
                    break;
                }
            }

            // Give a small delay to allow final messages to be printed
            Thread.sleep(1);

            // Force exit with success code
            System.exit(0);
        } catch (InterruptedException e) {
            // Thread was interrupted, exit gracefully
            Thread.currentThread().interrupt();
            logger.log("Application interrupted during shutdown", e);
            // Force exit with error code
            System.exit(1);
        }
    }


    /**
     * Processes an incoming line by delegating to the RequestController
     *
     * @param line The input line to process
     */
    private static void process(String line) {
        if (line.isEmpty()) {
            return;
        }

        try {
            // Delegate processing to the controller
            controller.processRequest(line);
        } catch (Exception e) {
            logger.log("Error processing input", e);
        }
    }

    private static String loadVersion() {
        try {
            java.io.InputStream is = Main.class.getClassLoader().getResourceAsStream("version.txt");
            if (is != null) {
                try (java.util.Scanner scanner = new java.util.Scanner(is, StandardCharsets.UTF_8)) {
                    return scanner.hasNextLine() ? scanner.nextLine().trim() : "unknown";
                }
            }
        } catch (Exception e) {
            logger.log("Could not load version from version.txt", e);
        }
        return "unknown";
    }

}
