package com.davidparry.mcp.buildscout.tools;

import com.davidparry.mcp.buildscout.common.BuildFile;
import com.davidparry.mcp.buildscout.common.BuildSystem;
import io.modelcontextprotocol.spec.McpSchema;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FindBuildSystem implements Tool {
  private static final Logger logger = LoggerFactory.getLogger(FindBuildSystem.class);
  private final BuildSystem buildSystem;

  public FindBuildSystem(BuildSystem buildSystem) {
    this.buildSystem = buildSystem;
  }

  @Override
  public McpSchema.CallToolResult handle(Object args) {
    return handleGetCodeContext(args);
  }

  @Override
  public String name() {
    return "find_build_system";
  }

  @Override
  public String description() {
    return "This tool can find the build system given the root directory of the project. if you have more than one directory add it to the paths variable.";
  }

  @Override
  public String schema() {
    return "{\n" + "    \"type\": \"object\",\n" + "    \"properties\": {\n" + "        \"paths\": {\n"
        + "            \"type\": \"array\",\n" + "            \"items\": {\n" + "                \"type\": \"string\"\n"
        + "            },\n" + "            \"description\": \"Paths to the files\"\n" + "        }\n" + "    },\n"
        + "    \"required\": [\n" + "        \"paths\"\n" + "    ]\n" + "}";

  }

  private McpSchema.CallToolResult handleGetCodeContext(Object args) {
    Set<BuildFile> builds = new HashSet<>();
    try {
      Map<String, List<String>> request = (Map<String, List<String>>) args;
      if (request != null && request.containsKey("paths")) {
        builds = buildSystem.identifyBuildFiles(request.get("paths"));
      }
    } catch (Exception e) {
      logger.error("Failed to process paths", e);
    }
    logger.debug("Args class: {}", args != null ? args.getClass().getName() : "null");
    return buildSystem.createTypeResults(builds);
  }


}
