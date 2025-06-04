package com.davidparry.scout.spec;

import com.google.gson.annotations.SerializedName;

/**
 * @param roots
 */
public record Capabilities(@SerializedName("roots") ListChanged roots,
                           @SerializedName("sampling") ListChanged sampling) {
    public Capabilities() {
        this(null, null);
    }
}
