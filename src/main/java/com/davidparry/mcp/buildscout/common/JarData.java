package com.davidparry.mcp.buildscout.common;

import java.nio.file.Path;

public record JarData(String version, String groupId, String artifactId, String classifier) {
}
