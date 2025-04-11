package com.davidparry.mcp.buildscout.tools;

import com.davidparry.mcp.buildscout.common.BuildSystemImpl;
import org.junit.Test;

public class FindBuildSystemTest {

  @Test
  public void test_schema () throws Exception {
    FindBuildSystem codeContext = new FindBuildSystem(new BuildSystemImpl());
    assert codeContext.schema() != null;
   // assert codeContext.description().contains("search and retrieve relevant snippets of source code from the project’s existing codebase related to the ask and context which is provided in the form or a query");
    assert codeContext.name().equals("find_build_system");
    System.out.println(codeContext.schema());
  }

}
