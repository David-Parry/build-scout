package com.davidparry.scout.spec;

import java.util.List;

public record JsonRpcTextResponse(List<Content> content , Boolean isError) {
}
