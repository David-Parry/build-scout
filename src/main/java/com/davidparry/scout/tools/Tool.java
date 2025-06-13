package com.davidparry.scout.tools;

import com.davidparry.scout.spec.InputSchema;
import com.davidparry.scout.spec.JsonRpcRequest;
import com.davidparry.scout.spec.ToolOutputResponse;

import java.util.List;

public interface Tool {

    /**
     * Get the JSON Schema for this tool.
     *
     * @return The JSON schema
     */
    InputSchema schema();

    ToolOutputResponse action(JsonRpcRequest args);


}
