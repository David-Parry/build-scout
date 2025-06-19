package com.davidparry.scout.handlers;

import com.davidparry.scout.ApplicationState;
import com.davidparry.scout.Main;
import com.davidparry.scout.common.LogFactory;
import com.davidparry.scout.io.ApplicationLogger;
import com.davidparry.scout.io.LogFileWriter;
import com.davidparry.scout.io.Logger;
import com.davidparry.scout.spec.*;

public class InitializeHandler implements Handler {
    private final Logger logger = new ApplicationLogger().getLogger(LogFileWriter.getInstance(new LogFactory()));


    @Override
    public HandlerResponse handle(JsonRpcRequest request) {
        logger.info("InitializeHandler - handle " + request);
        String protocolVersion = request.params().protocolVersion();

        ApplicationState.instance().clientInformation(request.params());

        ApplicationState.instance().jsonrpc(request.jsonrpc());

        ToolCapabilities tools = new ToolCapabilities();

        PromptsCapabilities prompts = new PromptsCapabilities();

        ServerCapabilities capabilities = new ServerCapabilities(tools, prompts);

        Implementation serverInfo = new Implementation(Main.MCP_SERVER_NAME,  ApplicationState.instance().getVersion());

        InitializeResult result = new InitializeResult(protocolVersion, capabilities, serverInfo);

        return new HandlerResponse(result);

    }
}
