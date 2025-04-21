package com.integration.common;

import com.davidparry.mcp.buildscout.common.DependencyFetch;
import org.junit.Test;

public class DependencyFetchTest {

    @Test
    public void test_getting_latest_version() throws Exception {
        DependencyFetch dependencyFetch = new DependencyFetch();
        String version = dependencyFetch.lookupLatestVersion("io.modelcontextprotocol.sdk", "mcp");

        assert version != null;
        assert version.equals("0.9.0");

    }
}
