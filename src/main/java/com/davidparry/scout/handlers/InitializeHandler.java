package com.davidparry.scout.handlers;

import com.davidparry.scout.Main;
import com.davidparry.scout.spec.*;

public class InitializeHandler implements Handler<InitializeResult> {


    @Override
    public InitializeResult handle(JsonRpcRequest request) {
        String protocolVersion = request.params().protocolVersion();

        ToolCapabilities tools = new ToolCapabilities();

        ServerCapabilities capabilities = new ServerCapabilities(tools);

        Implementation serverInfo = new Implementation(Main.MCP_SERVER_NAME, Main.MCP_SERVER_VERSION);

        InitializeResult result = new InitializeResult(protocolVersion, capabilities, serverInfo);

        return result;

    }
}
