package com.davidparry.scout.spec;

import java.util.List;

public record ToolOutputResponse(List<Content> content , Boolean isError) {
}
