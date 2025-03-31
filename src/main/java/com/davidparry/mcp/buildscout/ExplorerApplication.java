package com.davidparry.mcp.buildscout;

import com.davidparry.mcp.buildscout.tools.BuildSystemFilePaths;
import com.davidparry.mcp.buildscout.tools.BuildSystemImpl;
import com.davidparry.mcp.buildscout.tools.FindBuildSystem;
import com.davidparry.mcp.buildscout.tools.GetFileInfo;
import com.davidparry.mcp.buildscout.tools.JsonValidator;
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

  public static void main(String[] args) {
    ExplorerApplication application = new ExplorerApplication();


    try (ProjectExplorer explorer = new ProjectExplorer(application.transportProvider)) {
      // Initialize all tools
      JsonValidator jv = new JsonValidator();
      FindBuildSystem cc = new FindBuildSystem(new BuildSystemImpl());
      BuildSystemFilePaths bsf = new BuildSystemFilePaths(new BuildSystemImpl());
      GetFileInfo fi = new GetFileInfo();

      // Add all tools to the explorer
      explorer.addTool(cc.name(), cc.description(), cc.schema(), cc::handle)
          .addTool(jv.name(), jv.description(), jv.schema(), jv::handle)
          .addTool(fi.name(), fi.description(), fi.schema(), fi::handle)
          .addTool(bsf.name(), bsf.description(), bsf.schema(), bsf::handle)
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
