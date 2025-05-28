package com.davidparry.scout.common;

import com.davidparry.scout.spec.JsonRpcRequest;

public interface Consumer {

    void consume(JsonRpcRequest  request);
}
