package com.davidparry.scout.handlers;

import com.davidparry.scout.ApplicationState;
import com.davidparry.scout.Main;
import com.davidparry.scout.spec.*;

public class InitializeHandler implements Handler<InitializeResult> {


    @Override
    public InitializeResult handle(JsonRpcRequest request) {
        String protocolVersion = request.params().protocolVersion();

        ApplicationState.instance().clientInformation(request.params());

        ApplicationState.instance().jsonrpc(request.jsonrpc());

        ToolCapabilities tools = new ToolCapabilities();

        PromptsCapabilities prompts = new PromptsCapabilities();

        ServerCapabilities capabilities = new ServerCapabilities(tools, prompts);

        Implementation serverInfo = new Implementation(Main.MCP_SERVER_NAME,  ApplicationState.instance().getVersion());

        InitializeResult result = new InitializeResult(protocolVersion, capabilities, serverInfo);

        return result;

    }
}
