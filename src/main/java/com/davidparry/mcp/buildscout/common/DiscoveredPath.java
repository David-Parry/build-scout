package com.davidparry.mcp.buildscout.common;

import java.nio.file.Path;
import java.util.Optional;

public record DiscoveredPath(PathType type, Optional<Path> path) {
}
