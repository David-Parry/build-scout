package com.davidparry.mcp.buildscout.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.davidparry.mcp.buildscout.ProjectExplorer;
import io.modelcontextprotocol.spec.McpSchema.LoggingLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Logback appender that forwards log messages to the MCP client via ProjectExplorer.
 */
public class McpLogbackAppender extends AppenderBase<ILoggingEvent> {
    private static final Logger logger = LoggerFactory.getLogger(McpLogbackAppender.class);

    private static ProjectExplorer projectExplorer;

    /**
     * Sets the ProjectExplorer instance to use for forwarding logs.
     * This must be called before any logging occurs.
     *
     * @param explorer the ProjectExplorer instance
     */
    public static void setProjectExplorer(ProjectExplorer explorer) {
        projectExplorer = explorer;
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (projectExplorer == null || !projectExplorer.isRunning()) {
            return;
        }

        LoggingLevel level = mapLevel(event.getLevel().levelStr);
        String message = event.getFormattedMessage();

       // projectExplorer.log(level, message);
    }

    /**
     * Maps SLF4J log levels to MCP LoggingLevel.
     *
     * @param slf4jLevel the SLF4J level string
     * @return the corresponding MCP LoggingLevel
     */
    private LoggingLevel mapLevel(String slf4jLevel) {
        logger.error("LOGLEVEL_DP: {}", slf4jLevel);
        return switch (slf4jLevel) {
            case "ERROR" -> LoggingLevel.ERROR;
            case "WARN" -> LoggingLevel.WARNING;
            case "DEBUG", "TRACE" -> LoggingLevel.DEBUG;
            default -> LoggingLevel.INFO;
        };
    }
}