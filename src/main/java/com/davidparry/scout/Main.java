package com.davidparry.scout;

import com.davidparry.scout.common.*;
import com.davidparry.scout.handlers.*;
import com.davidparry.scout.io.*;
import com.davidparry.scout.tools.*;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main {
    private static final AtomicBoolean isShuttingDown = new AtomicBoolean(false);
    private static final CountDownLatch shutdownLatch = new CountDownLatch(1);
    public static String MCP_SERVER_NAME = "scout-server";
    private static IOHandler io;
    public final String mcpVersionNumber;
    private final LogFactory logFactory = new LogFactory();
    private final LogFile logFile = LogFileWriter.getInstance(logFactory);
    private final Logger logger = new ApplicationLogger().getLogger(logFile);
    private final Map<String, Handler> handlers = new HashMap<>();
    private final List<com.davidparry.scout.spec.Tool> tools = new ArrayList<>();
    private Router router;

    public Main() {
        mcpVersionNumber = loadVersion();
        ApplicationState.instance().setVersion(mcpVersionNumber);
    }

    public static void main(String[] args) {
        Main main = new Main();
        main.start();
    }

    private void initializeHandlers() {
        BuildSystem buildSystem = new BuildSystemImpl();
        GradleProcessExecutor gradleProcessExecutor = new GradleProcessExecutor();
        DependencyFetch dependencyFetch = new DependencyFetch(buildSystem, gradleProcessExecutor);
        JarDownloader jarDownloader = new JarDownloader();
        JarComparatorService jarComparatorService = new JarComparatorService(jarDownloader);

        // tools with handlers
        //1
        ListDependencies listDependencies = new ListDependencies(dependencyFetch, buildSystem);
        tools.add(listDependencies.tool());
        handlers.put(listDependencies.tool().name(), listDependencies);

        //2
        BuildGradleProject buildGradleProject = new BuildGradleProject(gradleProcessExecutor);
        tools.add(buildGradleProject.tool());
        handlers.put(buildGradleProject.tool().name(), buildGradleProject);

        //3
        BuildSystemFilePaths buildSystemFilePaths = new BuildSystemFilePaths(buildSystem);
        tools.add(buildSystemFilePaths.tool());
        handlers.put(buildSystemFilePaths.tool().name(), buildSystemFilePaths);

        //4
        DownloadCurrentLatestSource downloadCurrentLatestSource = new DownloadCurrentLatestSource(dependencyFetch);
        tools.add(downloadCurrentLatestSource.tool());
        handlers.put(downloadCurrentLatestSource.tool().name(), downloadCurrentLatestSource);

        //5
        FindClassUsage findClassUsage = new FindClassUsage(new SourceClassUsageService());
        tools.add(findClassUsage.tool());
        handlers.put(findClassUsage.tool().name(), findClassUsage);

        //6
        GetFileInfo getFileInfo = new GetFileInfo();
        tools.add(getFileInfo.tool());
        handlers.put(getFileInfo.tool().name(), getFileInfo);

        //7
        GetResourceInfo getResourceInfo = new GetResourceInfo();
        tools.add(getResourceInfo.tool());
        handlers.put(getResourceInfo.tool().name(), getResourceInfo);

        //8
        JarDiffReporter jarDiffReporter = new JarDiffReporter(jarComparatorService);
        tools.add(jarDiffReporter.tool());
        handlers.put(jarDiffReporter.tool().name(), jarDiffReporter);

        //9
        ReplaceSourceCodeComplete replaceSourceCodeComplete = new ReplaceSourceCodeComplete();
        tools.add(replaceSourceCodeComplete.tool());
        handlers.put(replaceSourceCodeComplete.tool().name(), replaceSourceCodeComplete);

        //10
        UnitTestGradleProject unitTestGradleProject = new UnitTestGradleProject(gradleProcessExecutor);
        tools.add(unitTestGradleProject.tool());
        handlers.put(unitTestGradleProject.tool().name(), unitTestGradleProject);

        //11
        UpdateDependencyVersion updateDependencyVersion = new UpdateDependencyVersion(buildSystem);
        tools.add(updateDependencyVersion.tool());
        handlers.put(updateDependencyVersion.tool().name(), updateDependencyVersion);

        //12
        LatestDependencyVersion latestDependencyVersion = new LatestDependencyVersion(dependencyFetch);
        tools.add(latestDependencyVersion.tool());
        handlers.put(latestDependencyVersion.tool().name(), latestDependencyVersion);

        //13
        FindBuildSystem findBuildSystem = new FindBuildSystem(buildSystem);
        tools.add(findBuildSystem.tool());
        handlers.put(findBuildSystem.tool().name(), findBuildSystem);


        // other handlers
        handlers.put("initialize", new InitializeHandler());
        handlers.put("notifications", new NotificationHandler());
        handlers.put("notifications/roots/list_changed", new NotificationRootsHandler(io, ApplicationState.instance()));
        handlers.put("notifications/initialized", new NotificationInitializedHandler(io, ApplicationState.instance()));
        handlers.put("tools/list", new ToolsListHandler(tools));
        handlers.put("prompts/list", new PromptsListHandler());
        handlers.put("prompts/get", new PromptDispatchHandler(ApplicationState.instance()));
        handlers.put("completion/complete", new CompletionComplete());
        handlers.put("roots", new ClientConsumer(ApplicationState.instance()));
    }


    public void start() {
        logger.info("Starting Scout version " + mcpVersionNumber + " Logger Level " + logger.level());

        // Create an IOHandler instance for console I/O
        io = new IOHandlerImpl();

        initializeHandlers();


        logger.log("Controller initialized ");

        router = new Router(io, ApplicationState.instance(), handlers);


        try {
            // Add a listener for individual lines
            io.addLineListener(this::process);

            // Create a shutdown hook to close resources properly
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (isShuttingDown.compareAndSet(false, true)) {
                    // Stop the async input reader and close resources
                    //io.stopAsyncInputReader();
                    stop();
                    logFile.close();
                    shutdownLatch.countDown();
                }
            }));
            logger.info("Scout version " + mcpVersionNumber + " started.");
            // Start the async input reader
            io.startInputReader();
            keepRunning();
            logger.log("Scout shutting down");
        } catch (Exception e) {
            logger.log("Error in main method", e);
            stop();
            logFile.close();
            System.exit(1);
        }
    }

    private void stop() {
        if (io != null) {
            io.stopRunning();
        }
        logFile.close();
    }

    /**
     * Keeps the application running until terminated by the user or until IO processing stops
     */
    private void keepRunning() {
        try {
            // Create a polling mechanism to check if IO is still running
            while (!isShuttingDown.get()) {
                // Check if IO has stopped running
                if (io != null && !io.isRunning()) {
                    logger.log("IO processing has stopped. Initiating shutdown.");
                    if (isShuttingDown.compareAndSet(false, true)) {
                        logFile.close();
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
    private void process(String line) {
        if (line.isEmpty()) {
            return;
        }
        try {
            router.route(line);
        } catch (Exception e) {
            logger.log("Error processing input", e);
        }
    }

    private String loadVersion() {
        logger.log("Loading Scout version !!!! ");
        try {
            InputStream is = Main.class.getClassLoader().getResourceAsStream("version.txt");
            if (is != null) {
                try (Scanner scanner = new Scanner(is, StandardCharsets.UTF_8)) {
                    logger.log("Loading version from file scanner ");
                    return scanner.hasNextLine() ? scanner.nextLine().trim() : "file_missing_version";
                }
            }
        } catch (Exception e) {
            logger.error("Could not load version from version.txt", e);
        }
        return "unknown";
    }

}
