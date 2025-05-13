package com.davidparry.scout.spec;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * Record representing the parameters of a JSON-RPC initialize request.
 */
public record RequestParams(@SerializedName("protocolVersion") String protocolVersion,
                            @SerializedName("capabilities") Capabilities capabilities,
                            @SerializedName("clientInfo") ClientInfo clientInfo, @SerializedName("name") String name,
                            @SerializedName("arguments") Map<String, Object> arguments

) {
}
