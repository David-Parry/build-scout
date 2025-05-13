package com.davidparry.scout.spec;

import com.google.gson.annotations.SerializedName;

/**
 * Record representing the client information in the request parameters.
 */
public record ClientInfo(
        @SerializedName("name") String name,
        @SerializedName("version") String version
) {
}
