package com.davidparry.scout;

import com.davidparry.scout.spec.IdType;
import com.davidparry.scout.spec.JSONResponse;
import com.davidparry.scout.spec.JsonRpcRequest;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test to demonstrate the complete ID policy workflow.
 * This shows how the system would behave with different first messages.
 */
public class IdPolicyIntegrationTest {

    @Test
    void testCompleteNumericIdWorkflow() {
        // Simulate receiving a JSON-RPC request with numeric ID
        String incomingJson = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"initialize\"}";
        JsonRpcRequest request = JsonRpcRequest.fromJson(incomingJson);
        
        // Verify the ID was parsed as a number
        assertTrue(request.id().isJsonPrimitive());
        assertTrue(request.id().getAsJsonPrimitive().isNumber());
        assertEquals(1, request.id().getAsInt());
        
        // Create a response that echoes back the same ID
        JSONResponse<String> response = new JSONResponse<>("2.0", request.id(), "success");
        String responseJson = response.toString();
        
        // The response should contain the numeric ID
        assertTrue(responseJson.contains("id=1"));
        
        // Simulate generating a server-initiated request ID
        JsonElement numericId = new JsonPrimitive(42);
        JsonRpcRequest serverRequest = new JsonRpcRequest("2.0", "notification", numericId);
        String serverJson = serverRequest.toJson();
        
        // Server request should have numeric ID (unquoted in JSON)
        assertTrue(serverJson.contains("\"id\":42"));
        assertFalse(serverJson.contains("\"id\":\"42\""));
    }

    @Test
    void testStringIdParsing() {
        // Simulate receiving a JSON-RPC request with string ID
        String incomingJson = "{\"jsonrpc\":\"2.0\",\"id\":\"req-123\",\"method\":\"initialize\"}";
        JsonRpcRequest request = JsonRpcRequest.fromJson(incomingJson);
        
        // Verify the ID was parsed as a string
        assertTrue(request.id().isJsonPrimitive());
        assertTrue(request.id().getAsJsonPrimitive().isString());
        assertEquals("req-123", request.id().getAsString());
        
        // The key point is that string IDs are preserved as strings
        JsonElement stringId = new JsonPrimitive("server-1");
        assertTrue(stringId.getAsJsonPrimitive().isString());
        assertEquals("server-1", stringId.getAsString());
    }

    @Test
    void testIdTypePreservationInSerialization() {
        // Test that JsonElement preserves the correct JSON type during serialization
        
        // Numeric ID
        JsonElement numericId = new JsonPrimitive(999);
        JsonRpcRequest numericRequest = new JsonRpcRequest("2.0", "test", numericId);
        String numericJson = numericRequest.toJson();
        assertTrue(numericJson.contains("\"id\":999"), "Numeric ID should be unquoted: " + numericJson);
        
        // String ID
        JsonElement stringId = new JsonPrimitive("test-id");
        JsonRpcRequest stringRequest = new JsonRpcRequest("2.0", "test", stringId);
        String stringJson = stringRequest.toJson();
        assertTrue(stringJson.contains("\"id\":\"test-id\""), "String ID should be quoted: " + stringJson);
        
        // Large numeric ID (should remain numeric)
        JsonElement largeId = new JsonPrimitive(9223372036854775807L); // Long.MAX_VALUE
        JsonRpcRequest largeRequest = new JsonRpcRequest("2.0", "test", largeId);
        String largeJson = largeRequest.toJson();
        assertTrue(largeJson.contains("\"id\":9223372036854775807"), "Large numeric ID should remain unquoted: " + largeJson);
    }

    @Test
    void testRoundTripConsistency() {
        // Test that parsing and serializing maintains ID type consistency
        
        // Numeric round trip
        String originalNumeric = "{\"jsonrpc\":\"2.0\",\"id\":42,\"method\":\"test\"}";
        JsonRpcRequest parsedNumeric = JsonRpcRequest.fromJson(originalNumeric);
        String serializedNumeric = parsedNumeric.toJson();
        
        // Parse again to verify consistency
        JsonRpcRequest reparsedNumeric = JsonRpcRequest.fromJson(serializedNumeric);
        assertTrue(reparsedNumeric.id().getAsJsonPrimitive().isNumber());
        assertEquals(42, reparsedNumeric.id().getAsInt());
        
        // String round trip
        String originalString = "{\"jsonrpc\":\"2.0\",\"id\":\"test-42\",\"method\":\"test\"}";
        JsonRpcRequest parsedString = JsonRpcRequest.fromJson(originalString);
        String serializedString = parsedString.toJson();
        
        // Parse again to verify consistency
        JsonRpcRequest reparsedString = JsonRpcRequest.fromJson(serializedString);
        assertTrue(reparsedString.id().getAsJsonPrimitive().isString());
        assertEquals("test-42", reparsedString.id().getAsString());
    }
}