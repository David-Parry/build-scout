package com.integration.common;

import com.davidparry.mcp.buildscout.common.DependencyResolver;
import org.junit.Test;

public class DependencyResolverTest {

    @Test
    public void test_getting_latest_version() throws Exception {
        DependencyResolver dependencyResolver = new DependencyResolver();
        String version = dependencyResolver.lookupLatestVersion("io.modelcontextprotocol.sdk", "mcp");

        assert version != null;
        assert version.equals("0.9.0");

    }
}
