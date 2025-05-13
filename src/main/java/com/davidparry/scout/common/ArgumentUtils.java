package com.davidparry.scout.common;

import com.davidparry.scout.spec.JsonRpcRequest;

public class ArgumentUtils {

    /**
     * Extracts a named argument from the request and casts it to the specified type.
     *
     * @param <T>     The type to cast the argument to.
     * @param request The request object containing parameters.
     * @param key     The key of the argument to retrieve.
     * @return The argument cast to the requested type, or null if not found/cast fails.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getArgument(JsonRpcRequest request, String key) {
        if (request != null && request.params() != null && request.params().arguments() != null) {
            return (T) request.params().arguments().get(key); // Replace YourRequestType with the actual request class.
        } else {
            return null;
        }
    }
}