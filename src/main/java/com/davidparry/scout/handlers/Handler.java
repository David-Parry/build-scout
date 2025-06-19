package com.davidparry.scout.handlers;

import com.davidparry.scout.spec.JSONResponse;
import com.davidparry.scout.spec.JsonRpcRequest;
import com.davidparry.scout.spec.Tool;

public interface Handler {

    HandlerResponse handle(JsonRpcRequest request);


}
