package com.davidparry.scout.handlers;

import com.davidparry.scout.spec.JSONResponse;
import com.davidparry.scout.spec.JsonRpcRequest;

public interface Handler {

    HandlerResponse handle(JsonRpcRequest request);

}
