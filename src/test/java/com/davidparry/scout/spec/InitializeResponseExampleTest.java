package com.davidparry.scout.spec;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.Gson;
import com.google.gson.JsonPrimitive;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;

class InitializeResponseExampleTest {



    @Test
    public void testInitializeWithCapabilitiesParameters() {
        // Arrange
        String protocolVersion = "1.0";

        ToolCapabilities tools = new ToolCapabilities();

        PromptsCapabilities prompts = new PromptsCapabilities();

        ServerCapabilities capabilities = new ServerCapabilities(tools, prompts);

        Implementation serverInfo = new Implementation("TestServer", "2.0");
        String instructions = "Test instructions";

        // Act
        InitializeResult result = new InitializeResult(
                protocolVersion, capabilities, serverInfo);

        JSONResponse<InitializeResult> jsonResponse = new JSONResponse<>("2.0", new JsonPrimitive("0"), result);

        // Assert
        assertEquals(protocolVersion, result.getProtocolVersion());
        assertEquals(capabilities, result.getCapabilities());
        assertEquals(serverInfo, result.getServerInfo());
        // Verify nested objects
        assertEquals("TestServer", result.getServerInfo().getName());
        assertEquals("2.0", result.getServerInfo().getVersion());
        Gson gson = new Gson();
        String out = gson.toJson(jsonResponse, JSONResponse.class);
        String check = "{\"id\":\"0\",\"jsonrpc\":\"2.0\",\"result\":{\"protocolVersion\":\"1.0\",\"capabilities\":{\"tools\":{\"listChanged\":true},\"prompts\":{\"listChanged\":true}},\"serverInfo\":{\"name\":\"TestServer\",\"version\":\"2.0\"}}}";
        assertEquals(check, out);

        System.out.println(out);
    }


}
