package com.davidparry.scout.tools;

import com.davidparry.scout.spec.InputSchema;
import com.davidparry.scout.spec.JsonRpcRequest;
import java.util.List;

public interface Tool<T> {

    /**
     * Get the JSON Schema for this tool.
     *
     * @return The JSON schema
     */
    InputSchema schema();

    T action(JsonRpcRequest args);


}
