/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.diagramutil;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectEnvironmentBuilder;
import io.ballerina.projects.directory.ProjectLoader;
import io.ballerina.projects.repos.TempDirCompilationCache;
import org.ballerinalang.diagramutil.connector.generator.ConnectorGenerator;
import org.ballerinalang.diagramutil.connector.models.connector.Connector;
import org.ballerinalang.docgen.docs.BallerinaDocGenerator;
import org.ballerinalang.docgen.generator.model.ModuleDoc;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.ballerinalang.diagramutil.connector.generator.ConnectorGenerator.getConnectorModelFromSyntaxTree;

/**
 * Connector metadata generation test case.
 */
public class TestConnectorGenerator {
    private final String orgName = "test";
    private final String packageName = "test";
    private final String moduleName = "TestConnector";
    private final String version = "0.1.0";
    private final String initFunc = "init";
    private final Path testConnectorBalaFile = TestUtil.RES_DIR
            .resolve("connector")
            .resolve("test-TestConnector-any-0.1.0.bala");
    private Project balaProject;

    @BeforeClass
    public void initMetadataGenerator() {
        ProjectEnvironmentBuilder defaultBuilder = ProjectEnvironmentBuilder.getDefaultBuilder();
        defaultBuilder.addCompilationCacheFactory(TempDirCompilationCache::from);
        balaProject = ProjectLoader.loadProject(this.testConnectorBalaFile, defaultBuilder);
    }

    @Test(description = "Test connector metadata generation")
    public void getConnectorMetadata() throws IOException {
        List<Connector> connectors = ConnectorGenerator.generateConnectorModel(balaProject);
        Assert.assertEquals(connectors.size(), 1);
        Connector connector = connectors.get(0);

        Assert.assertEquals(connector.moduleName, moduleName);
        Assert.assertEquals(connector.documentation, "Test Client documentation\n\n");
        Assert.assertEquals(connector.packageInfo.getOrganization(), orgName);
        Assert.assertTrue(connector.displayAnnotation.containsKey("label"));
        Assert.assertTrue(connector.displayAnnotation.containsKey("iconPath"));
        Assert.assertEquals(connector.displayAnnotation.get("label"), "Test Client");
        Assert.assertEquals(connector.displayAnnotation.get("iconPath"), "logo.svg");

        Assert.assertEquals(connector.functions.size(), 3);
        Assert.assertEquals(connector.functions.get(0).name, initFunc);
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

    @Test(description = "Test connector metadata generation")
    public void getFunctionMetadata() throws IOException {
        Map<String, ModuleDoc> moduleDocMap = BallerinaDocGenerator.generateModuleDocMap(balaProject);
        ModuleDoc testConnector = moduleDocMap.get(moduleName);
        SyntaxTree st = testConnector.syntaxTreeMap.get("main.bal");
        SemanticModel model = testConnector.semanticModel;

        List<Connector> modelFromSyntaxTree = getConnectorModelFromSyntaxTree(st, model,
                orgName, packageName, version, moduleName);

        Assert.assertEquals(modelFromSyntaxTree.size(), 1);
        Assert.assertEquals(modelFromSyntaxTree.get(0).moduleName, moduleName);
        Assert.assertEquals(modelFromSyntaxTree.get(0).packageInfo.getVersion(), version);

        Assert.assertEquals(modelFromSyntaxTree.get(0).functions.size(), 3);
        Assert.assertEquals(modelFromSyntaxTree.get(0).functions.get(0).name, initFunc);
    }
}
