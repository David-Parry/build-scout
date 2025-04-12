package com.davidparry.mcp.buildscout;

import com.davidparry.mcp.buildscout.common.DependencyResolver;
import com.davidparry.mcp.buildscout.tools.*;
import com.davidparry.mcp.buildscout.common.BuildSystemImpl;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpServerTransportProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main application class for running the ProjectExplorer.
 */
public class ExplorerApplication {
    private static final Logger logger = LoggerFactory.getLogger(ExplorerApplication.class);
    public McpServerTransportProvider transportProvider = new StdioServerTransportProvider();
    public static final String MCP_SERVER_NAME = "buildscout";
    public static final String VERSION = "0.1.12";

    public static void main(String[] args) {
        ExplorerApplication application = new ExplorerApplication();


        try (ProjectExplorer explorer = new ProjectExplorer(application.transportProvider, MCP_SERVER_NAME, VERSION)) {
            // Initialize all tools
            JsonValidator jv = new JsonValidator();
            FindBuildSystem cc = new FindBuildSystem(new BuildSystemImpl());
            BuildSystemFilePaths bsf = new BuildSystemFilePaths(new BuildSystemImpl());
            GetFileInfo fi = new GetFileInfo();
            ListDependencies ld = new ListDependencies(new DependencyResolver());
            LatestDependencyVersion ldv = new LatestDependencyVersion(new DependencyResolver());
            // Add all tools to the explorer
            explorer.addTool(cc.name(), cc.description(), cc.schema(), cc::handle)
                    .addTool(jv.name(), jv.description(), jv.schema(), jv::handle)
                    .addTool(fi.name(), fi.description(), fi.schema(), fi::handle)
                    .addTool(bsf.name(), bsf.description(), bsf.schema(), bsf::handle)
                    .addTool(ld.name(), ld.description(), ld.schema(), ld::handle)
                    .addTool(ldv.name(), ldv.description(), ldv.schema(), ldv::handle)
            ;

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Shutting down ProjectExplorer...");
                explorer.close();
            }));

            // Wait indefinitely
            Thread.currentThread().join();
        } catch (Exception e) {
            logger.error("Error running ProjectExplorer", e);
            System.exit(1);
        }
    }

}
