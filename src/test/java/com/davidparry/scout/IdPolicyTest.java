package com.davidparry.scout;

import com.davidparry.scout.spec.IdType;
import com.davidparry.scout.spec.JsonRpcRequest;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class to verify ID policy enforcement based on the first message received.
 * Note: These tests verify the JSON parsing and serialization behavior.
 * The actual ApplicationState is a singleton and would need integration testing.
 */
public class IdPolicyTest {

    @Test
    void testNumericIdPolicyLogic() {
        // Test the logic for determining ID type from JsonElement
        JsonElement numericId = new JsonPrimitive(1);
        
        assertTrue(numericId.isJsonPrimitive());
        assertTrue(numericId.getAsJsonPrimitive().isNumber());
        assertEquals(1, numericId.getAsInt());
    }

    @Test
    void testStringIdPolicyLogic() {
        // Test the logic for determining ID type from JsonElement
        JsonElement stringId = new JsonPrimitive("abc");
        
        assertTrue(stringId.isJsonPrimitive());
        assertTrue(stringId.getAsJsonPrimitive().isString());
        assertEquals("abc", stringId.getAsString());
    }

    @Test
    void testIdTypeGeneration() {
        // Test generating different ID types
        JsonElement numericId = new JsonPrimitive(42L);
        JsonElement stringId = new JsonPrimitive("test-id");
        
        assertTrue(numericId.isJsonPrimitive());
        assertTrue(numericId.getAsJsonPrimitive().isNumber());
        
        assertTrue(stringId.isJsonPrimitive());
        assertTrue(stringId.getAsJsonPrimitive().isString());
    }

    @Test
    void testJsonRpcRequestParsing() {
        // Test that JsonRpcRequest correctly parses numeric and string IDs
        String numericIdJson = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"initialize\"}";
        String stringIdJson = "{\"jsonrpc\":\"2.0\",\"id\":\"abc\",\"method\":\"initialize\"}";
        
        JsonRpcRequest numericRequest = JsonRpcRequest.fromJson(numericIdJson);
        assertTrue(numericRequest.id().isJsonPrimitive());
        assertTrue(numericRequest.id().getAsJsonPrimitive().isNumber());
        assertEquals(1, numericRequest.id().getAsInt());
        
        JsonRpcRequest stringRequest = JsonRpcRequest.fromJson(stringIdJson);
        assertTrue(stringRequest.id().isJsonPrimitive());
        assertTrue(stringRequest.id().getAsJsonPrimitive().isString());
        assertEquals("abc", stringRequest.id().getAsString());
    }

    @Test
    void testJsonRpcRequestSerialization() {
        // Test that JsonRpcRequest correctly serializes numeric and string IDs
        JsonElement numericId = new JsonPrimitive(42);
        JsonElement stringId = new JsonPrimitive("test-id");
        
        JsonRpcRequest numericRequest = new JsonRpcRequest("2.0", "test", numericId);
        JsonRpcRequest stringRequest = new JsonRpcRequest("2.0", "test", stringId);
        
        String numericJson = numericRequest.toJson();
        String stringJson = stringRequest.toJson();
        
        // Numeric ID should be unquoted in JSON
        assertTrue(numericJson.contains("\"id\":42"));
        
        // String ID should be quoted in JSON
        assertTrue(stringJson.contains("\"id\":\"test-id\""));
    }
}