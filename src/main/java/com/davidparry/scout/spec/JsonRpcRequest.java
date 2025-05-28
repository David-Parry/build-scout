package com.davidparry.scout.spec;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * {"jsonrpc":"2.0","id":1,"result":{"roots":[{"uri":"file://tmp","name":""}]}}
 * Record representing a JSON-RPC request according to the JSON-RPC 2.0 specification.
 */
public record JsonRpcRequest(@SerializedName("jsonrpc") String jsonrpc, @SerializedName("method") String method,
                             @SerializedName("name") String name, @SerializedName("id") int id,
                             @SerializedName("params") RequestParams params, @SerializedName("error") RpcError error,
                             @SerializedName("result") Map<String, Object> result

) {

    public JsonRpcRequest(String jsonrpc, String method, int id) {
        this(jsonrpc, method,null, id, null, null, null);
    }

    /**
     * Parse a JSON string into a JsonRpcRequest object.
     *
     * @param json The JSON string to parse
     * @return The parsed JsonRpcRequest object
     */
    public static JsonRpcRequest fromJson(String json) {
        return new Gson().fromJson(json, JsonRpcRequest.class);
    }

    /**
     * Convert this JsonRpcRequest object to a JSON string.
     *
     * @return The JSON string representation
     */
    public String toJson() {
        return new Gson().toJson(this);
    }
}

