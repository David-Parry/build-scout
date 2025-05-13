package com.davidparry.scout.handlers;

import com.davidparry.scout.annotation.SchemaRegistry;
import com.davidparry.scout.spec.InputSchema;
import com.davidparry.scout.spec.JsonRpcRequest;
import com.davidparry.scout.spec.Tool;
import com.davidparry.scout.spec.ToolsListResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ToolsListHandler implements Handler<ToolsListResponse> {
    private final SchemaRegistry schemaRegistry;

    public ToolsListHandler(SchemaRegistry schemaRegistry) {
        this.schemaRegistry = schemaRegistry;
    }

    @Override
    public ToolsListResponse handle(JsonRpcRequest request) {
        List<Tool> tools = new ArrayList<>();
        Map<String, SchemaRegistry.SchemaMetadata> registries = schemaRegistry.getAllSchemas();

        for (String key : registries.keySet()) {
            SchemaRegistry.SchemaMetadata metadata = registries.get(key);
            InputSchema inputSchema = metadata.getTool().schema();
            Tool tool = new Tool(key, metadata.getDescription(), inputSchema);
            tools.add(tool);
        }

        return new ToolsListResponse(tools);
    }
}
