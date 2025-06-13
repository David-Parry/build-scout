package com.davidparry.scout.handlers;

public record HandlerResponse(Object response)  {

    public HandlerResponse() {
        this(null);
    }
}
