package com.davidparry.mcp.buildscout.tools;

import com.davidparry.mcp.buildscout.common.BuildFile;
import com.davidparry.mcp.buildscout.common.DiscoveredPath;
import io.modelcontextprotocol.spec.McpSchema;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public interface BuildSystem {

  DiscoveredPath discover(String path);

  void walkDirectory(Path directory, Set<BuildFile> builds);

  void checkBuildFile(String path, Set<BuildFile> builds);

  McpSchema.CallToolResult createTypeResults(Set<BuildFile> builds);

  int processRootFolder(String rootPath, Set<String> absolutePaths);

  Set<BuildFile> identifyBuildFiles(List<String> potentialPaths);

  McpSchema.CallToolResult createPathResults(Set<BuildFile> builds);
}
