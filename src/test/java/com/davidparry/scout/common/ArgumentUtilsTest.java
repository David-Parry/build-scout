package com.davidparry.scout.common;

import com.davidparry.scout.spec.Capabilities;
import com.davidparry.scout.spec.ClientInfo;
import com.davidparry.scout.spec.JsonRpcRequest;
import com.davidparry.scout.spec.RequestParams;
import com.google.gson.JsonPrimitive;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ArgumentUtilsTest {

    @Test
    public void testGetArgumentReturnsCorrectlyTypedValue() {
        // Prepare arguments map
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("intKey", 42);
        arguments.put("stringKey", "testValue");

        // Prepare RequestParams
        RequestParams params = new RequestParams("2.0", new Capabilities(), // Assuming default constructor exists
                new ClientInfo("", ""),   // Assuming default constructor exists
                "testName", arguments);

        // Prepare JsonRpcRequest
        JsonRpcRequest request = new JsonRpcRequest("2.0", "testMethod", "testName", new JsonPrimitive(1), params, null, null);

        // Test integer extraction
        Integer intValue = ArgumentUtils.getArgument(request, "intKey");
        assertNotNull(intValue);
        assertEquals(42, intValue);

        // Test string extraction
        String stringValue = ArgumentUtils.getArgument(request, "stringKey");
        assertNotNull(stringValue);
        assertEquals("testValue", stringValue);
    }
}