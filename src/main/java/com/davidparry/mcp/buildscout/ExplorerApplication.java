package com.davidparry.mcp.buildscout;

import com.davidparry.mcp.buildscout.common.*;
import com.davidparry.mcp.buildscout.logging.McpLogbackAppender;
import com.davidparry.mcp.buildscout.tools.*;
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
    public static final String VERSION = "0.1.15";

    public static void main(String[] args) {
        ExplorerApplication application = new ExplorerApplication();

        try (ProjectExplorer explorer = new ProjectExplorer(application.transportProvider, MCP_SERVER_NAME, VERSION)) {
            // Set the ProjectExplorer instance for the McpLogbackAppender
            McpLogbackAppender.setProjectExplorer(explorer);
            
            // Log a startup message that will now go to both file and MCP client
            logger.info("BuildScout starting up with version {}", VERSION);
            
            // Initialize all tools
            BuildSystem buildSystem = new BuildSystemImpl();
            DependencyFetch fetch = new DependencyFetch(buildSystem);

            JsonValidator jv = new JsonValidator();
            FindBuildSystem cc = new FindBuildSystem(buildSystem);
            BuildSystemFilePaths bsf = new BuildSystemFilePaths(buildSystem);
            GetFileInfo fi = new GetFileInfo();
            ListDependencies ld = new ListDependencies(fetch);
            LatestDependencyVersion ldv = new LatestDependencyVersion(fetch);
            JarDiffReporter jdr = new JarDiffReporter(new JarComparatorService(new JarDownloader()));
            FindClassUsage fcu = new FindClassUsage(new SourceClassUsageService());
            DownloadCurrentLatestSource dcs = new DownloadCurrentLatestSource(fetch);
            GetResourceInfo gri = new GetResourceInfo();
            UpdateDependencyVersion udv = new UpdateDependencyVersion(new BuildSystemImpl());
            BuildGradleProject bgp = new BuildGradleProject(new GradleTasksImpl());
            // Add all tools to the explorer
            explorer.addTool(cc.name(), cc.description(), cc.schema(), cc::handle)
                    .addTool(jv.name(), jv.description(), jv.schema(), jv::handle)
                    .addTool(fi.name(), fi.description(), fi.schema(), fi::handle)
                    .addTool(bsf.name(), bsf.description(), bsf.schema(), bsf::handle)
                    .addTool(ld.name(), ld.description(), ld.schema(), ld::handle)
                    .addTool(ldv.name(), ldv.description(), ldv.schema(), ldv::handle)
                    .addTool(jdr.name(), jdr.description(), jdr.schema(), jdr::handle)
                    .addTool(fcu.name(), fcu.description(), fcu.schema(), fcu::handle)
                    .addTool(dcs.name(), dcs.description(), dcs.schema(), dcs::handle)
                    .addTool(gri.name(), gri.description(), gri.schema(), gri::handle)
                    .addTool(udv.name(), udv.description(), udv.schema(), udv::handle)
                    .addTool(bgp.name(), bgp.description(), bgp.schema(), bgp::handle)
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
