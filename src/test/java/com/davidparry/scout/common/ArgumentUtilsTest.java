package com.davidparry.scout.common;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.davidparry.scout.spec.JsonRpcRequest;
import com.davidparry.scout.spec.RequestParams;
import com.davidparry.scout.spec.Capabilities;
import com.davidparry.scout.spec.ClientInfo;

import java.util.HashMap;
import java.util.Map;

public class ArgumentUtilsTest {

    @Test
    public void testGetArgumentReturnsCorrectlyTypedValue() {
        // Prepare arguments map
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("intKey", 42);
        arguments.put("stringKey", "testValue");

        // Prepare RequestParams
        RequestParams params = new RequestParams(
                "2.0",
                new Capabilities(), // Assuming default constructor exists
                new ClientInfo("",""),   // Assuming default constructor exists
                "testName",
                arguments
        );

        // Prepare JsonRpcRequest
        JsonRpcRequest request = new JsonRpcRequest(
                "2.0",
                "testMethod",
                "testName",
                1,
                params
        );

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