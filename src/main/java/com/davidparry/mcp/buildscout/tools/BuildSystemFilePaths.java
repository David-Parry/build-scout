package com.davidparry.mcp.buildscout.tools;

import com.davidparry.mcp.buildscout.common.BuildFile;
import io.modelcontextprotocol.spec.McpSchema;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BuildSystemFilePaths implements Tool {
  private static final Logger logger = LoggerFactory.getLogger(BuildSystemFilePaths.class);
  private final BuildSystem buildSystem;

  public BuildSystemFilePaths(BuildSystem buildSystem) {
    this.buildSystem = buildSystem;
  }

  @Override
  public McpSchema.CallToolResult handle(Object args) {
    return handleGetCodeContext(args);
  }

  @Override
  public String name() {
    return "build_system_file_paths";
  }

  @Override
  public String description() {
    return "This tool will return a single absolute path to the build system file that is present in the project, if there are multiple build system files it will return multiple text responses with the absolute file paths, given the root directory of the project.";
  }

  @Override
  public String schema() {
    return "{\n" + "    \"type\": \"object\",\n" + "    \"properties\": {\n" + "        \"paths\": {\n"
        + "            \"type\": \"array\",\n" + "            \"items\": {\n" + "                \"type\": \"string\"\n"
        + "            },\n" + "            \"description\": \"Paths to the files\"\n" + "        }\n" + "    },\n"
        + "    \"required\": [\n" + "        \"paths\"\n" + "    ]\n" + "}";

  }

  private McpSchema.CallToolResult handleGetCodeContext(Object args) {
    Set<BuildFile> builds = new HashSet<>(); // Using Set to avoid duplicates
    try {
      Map<String, List<String>> request = (Map<String, List<String>>) args;
      if (request != null && request.containsKey("paths")) {
        builds = buildSystem.identifyBuildFiles(request.get("paths"));
      }
    } catch (Exception e) {
      logger.error("Failed to process paths", e);
    }

    logger.debug("Args class: {}", args != null ? args.getClass().getName() : "null");
    return buildSystem.createPathResults(builds);
  }


}
