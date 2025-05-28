package com.davidparry.scout.tools;

import com.davidparry.scout.common.BuildOutput;
import com.davidparry.scout.common.GradleTasks;
import com.davidparry.scout.io.ApplicationLogger;
import com.davidparry.scout.io.Logger;
import com.davidparry.scout.spec.*;
import com.davidparry.scout.spec.Capabilities;
import com.davidparry.scout.spec.ClientInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BuildGradleProjectTest {

    @TempDir
    File tempDir;
    @Mock
    private Logger logger;

    @Mock
    private GradleTasks gradleTasks;
    private BuildGradleProject buildGradleProject;
    private MockedStatic<ApplicationLogger> mockedStatic;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockedStatic = Mockito.mockStatic(ApplicationLogger.class);
        mockedStatic.when(ApplicationLogger::getInstance).thenReturn(logger);
        buildGradleProject = new BuildGradleProject(gradleTasks);

    }

    @AfterEach
    void tearDown() {
        mockedStatic.close();
    }

    @Test
    void testSchemas() {
        InputSchema schema = buildGradleProject.schema();
        Map<String, InputProperty> properties = buildGradleProject.getProperties();
        for (String key : properties.keySet()) {
            assertEquals(schema.properties().get(key), properties.get(key));
        }
    }

    @Test
    void testSuccessfulBuild() {
        // Arrange
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("projectRoot", tempDir.getAbsolutePath());
        arguments.put("check", true);

        // Create required objects for RequestParams
        Capabilities capabilities = new Capabilities();
        ClientInfo clientInfo = new ClientInfo("name","0.0.1");

        RequestParams params = new RequestParams(
            "2.0", // protocolVersion
            capabilities,
            clientInfo,
            "buildGradle", // name
            arguments
        );
        JsonRpcRequest request = new JsonRpcRequest("2.0", "buildGradle", "test", 1, params, null, null);

        BuildOutput buildOutput = new BuildOutput("Build successful", null, false);
        when(gradleTasks.buildGradleProject(any(File.class), anyBoolean())).thenReturn(buildOutput);
        when(gradleTasks.formatOutput(any(BuildOutput.class))).thenReturn("Formatted build successful");

        // Act
        ToolOutputResponse response = buildGradleProject.action(request);

        // Assert
        assertNotNull(response);
        assertFalse(response.isError());
        assertEquals(1, response.content().size());
        assertEquals("Formatted build successful", response.content().get(0).text());

        verify(gradleTasks).buildGradleProject(any(File.class), eq(true));
        verify(gradleTasks).formatOutput(buildOutput);
    }

    @Test
    void testBuildWithNullProjectRoot() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("projectRoot", null);
        arguments.put("check", true);

        // Create required objects for RequestParams
        Capabilities capabilities = new Capabilities();
        ClientInfo clientInfo = new ClientInfo("name","0.0.1");

        RequestParams params = new RequestParams(
                "2.0", // protocolVersion
                capabilities,
                clientInfo,
                "buildGradle", // name
                arguments
        );
        JsonRpcRequest request = new JsonRpcRequest("2.0", "buildGradle", "test", 1, params,null,null);

        // Act
        ToolOutputResponse response = buildGradleProject.action(request);

        // Assert
        assertNotNull(response);
        assertTrue(response.isError());
        assertEquals(1, response.content().size());
        assertEquals("Error: Missing project root path", response.content().get(0).text());

        verify(gradleTasks, never()).buildGradleProject(any(), anyBoolean());
    }

    @Test
    void testBuildWithNonExistentDirectory() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("projectRoot", "/non-existent-directory");
        arguments.put("check", true);

        // Create required objects for RequestParams
        Capabilities capabilities = new Capabilities();
        ClientInfo clientInfo = new ClientInfo("name","0.0.1");

        RequestParams params = new RequestParams(
                "2.0", // protocolVersion
                capabilities,
                clientInfo,
                "buildGradle", // name
                arguments
        );
        JsonRpcRequest request = new JsonRpcRequest("2.0", "buildGradle", "test", 1, params, null,null);

        // Act
        ToolOutputResponse response = buildGradleProject.action(request);

        // Assert
        assertNotNull(response);
        assertTrue(response.isError());
        assertEquals(1, response.content().size());
        assertEquals("Error: Project root directory does not exist or is not a directory", response.content().get(0).text());

        verify(gradleTasks, never()).buildGradleProject(any(), anyBoolean());
    }

    @Test
    void testBuildWithFailedOutput() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("projectRoot", tempDir.getAbsolutePath());
        arguments.put("check", true);

        // Create required objects for RequestParams
        Capabilities capabilities = new Capabilities();
        ClientInfo clientInfo = new ClientInfo("name","0.0.1");

        RequestParams params = new RequestParams(
                "2.0", // protocolVersion
                capabilities,
                clientInfo,
                "buildGradle", // name
                arguments
        );
        JsonRpcRequest request = new JsonRpcRequest("2.0", "buildGradle", "test", 1, params, null,null);

        BuildOutput buildOutput = new BuildOutput("Build failed", "Error message", true);
        when(gradleTasks.buildGradleProject(any(File.class), anyBoolean())).thenReturn(buildOutput);
        when(gradleTasks.formatOutput(any(BuildOutput.class))).thenReturn("Formatted build failed");

        // Act
        ToolOutputResponse response = buildGradleProject.action(request);

        // Assert
        assertNotNull(response);
        assertTrue(response.isError());
        assertEquals(1, response.content().size());
        assertEquals("Formatted build failed", response.content().get(0).text());

        verify(gradleTasks).buildGradleProject(any(File.class), eq(true));
        verify(gradleTasks).formatOutput(buildOutput);
    }
}
