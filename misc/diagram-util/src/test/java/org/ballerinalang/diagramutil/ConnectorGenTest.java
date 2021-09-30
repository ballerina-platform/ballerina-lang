package org.ballerinalang.diagramutil;

import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectEnvironmentBuilder;
import io.ballerina.projects.directory.ProjectLoader;
import io.ballerina.projects.repos.TempDirCompilationCache;
import org.ballerinalang.diagramutil.connector.generator.ConnectorGenerator;
import org.ballerinalang.diagramutil.connector.models.connector.Connector;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Connector metadata generation test case.
 */
public class ConnectorGenTest {
    private final Path testConnectorBalaFile = TestUtil.RES_DIR
            .resolve("connector")
            .resolve("test-TestConnector-any-0.1.0.bala");
    // refer misc/diagram-util/src/test/resources/connector/TestConnector package for testConnector bala implementation

    @Test(description = "Test connector metadata generation")
    public void testConnectorGen() throws IOException {
        ProjectEnvironmentBuilder defaultBuilder = ProjectEnvironmentBuilder.getDefaultBuilder();
        defaultBuilder.addCompilationCacheFactory(TempDirCompilationCache::from);

        Project balaProject = ProjectLoader.loadProject(this.testConnectorBalaFile, defaultBuilder);
        List<Connector> connectors = ConnectorGenerator.generateConnectorModel(balaProject);
        Assert.assertEquals(connectors.size(), 1);
        Connector connector = connectors.get(0);

        Assert.assertEquals(connector.orgName, "test");
        Assert.assertEquals(connector.moduleName, "TestConnector");
        Assert.assertEquals(connector.version, "0.1.0");
        Assert.assertEquals(connector.documentation, "Test Client documentation\n\n");
        Assert.assertTrue(connector.displayAnnotation.containsKey("label"));
        Assert.assertTrue(connector.displayAnnotation.containsKey("iconPath"));
        Assert.assertEquals(connector.displayAnnotation.get("label"), "Test Client");
        Assert.assertEquals(connector.displayAnnotation.get("iconPath"), "logo.svg");
        Assert.assertEquals(connector.functions.size(), 3);

        Assert.assertEquals(connector.functions.get(0).name, "init");
        Assert.assertEquals(connector.functions.get(0).parameters.size(), 0);

        Assert.assertEquals(connector.functions.get(2).name, "sendMessage");
        Assert.assertEquals(connector.functions.get(2).parameters.size(), 1);
        Assert.assertEquals(connector.functions.get(2).parameters.get(0).name, "message");
        Assert.assertEquals(connector.functions.get(2).parameters.get(0).typeName, "string");
        Assert.assertEquals(connector.functions.get(2).parameters.get(0).documentation,
                "Message to send\n");
        Assert.assertEquals(connector.functions.get(2).parameters.get(0).displayAnnotation.get("label"),
                "Message");
    }
}
