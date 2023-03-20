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

import com.google.gson.Gson;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectEnvironmentBuilder;
import io.ballerina.projects.directory.ProjectLoader;
import io.ballerina.projects.repos.TempDirCompilationCache;
import org.ballerinalang.diagramutil.connector.generator.ConnectorGenerator;
import org.ballerinalang.diagramutil.connector.models.connector.Connector;
import org.ballerinalang.diagramutil.connector.models.connector.Function;
import org.ballerinalang.diagramutil.connector.models.connector.Type;
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
    private final String version = "0.3.0";
    private final String initFunc = "init";
    private final Path testConnectorBalaFile = TestUtil.RES_DIR
            .resolve("connector")
            .resolve("test-TestConnector-any-0.3.0.bala");
    private final Path testClientBalaFile = TestUtil.RES_DIR
            .resolve("connector/TestClient")
            .resolve("test-TestClient-any-0.1.1.bala");
    private Project testConProject;

    @BeforeClass
    public void initMetadataGenerator() {
        ProjectEnvironmentBuilder defaultBuilder = ProjectEnvironmentBuilder.getDefaultBuilder();
        defaultBuilder.addCompilationCacheFactory(TempDirCompilationCache::from);
        testConProject = ProjectLoader.loadProject(this.testConnectorBalaFile, defaultBuilder);
    }

    @Test(description = "Test getting project all connectors")
    public void getProjectAllConnectors() {
        List<Connector> connectors = ConnectorGenerator.getProjectConnectors(testConProject, false, "");
        Assert.assertEquals(connectors.size(), 1);
        Connector connector = connectors.get(0);

        Assert.assertEquals(connector.moduleName, moduleName);
        Assert.assertEquals(connector.documentation, "Test Client documentation\n\n");
        Assert.assertEquals(connector.packageInfo.getOrganization(), orgName);
    }

    @Test(description = "Test getting project connectors with query filter")
    public void getProjectConnectorsWithFilter() {
        List<Connector> connectors = ConnectorGenerator.getProjectConnectors(testConProject, false, orgName);
        Assert.assertEquals(connectors.size(), 1);
        Connector connector = connectors.get(0);

        Assert.assertEquals(connector.moduleName, moduleName);
        Assert.assertEquals(connector.documentation, "Test Client documentation\n\n");
        Assert.assertEquals(connector.packageInfo.getOrganization(), orgName);
    }

    @Test(description = "Test getting connector with function metadata")
    public void getProjectConnectorWithFunctionMetadata() {
        List<Connector> connectors = ConnectorGenerator.getProjectConnectors(testConProject, true, orgName);
        Assert.assertEquals(connectors.size(), 1);
        Connector connector = connectors.get(0);

        Assert.assertEquals(connector.moduleName, moduleName);
        Assert.assertEquals(connector.documentation, "Test Client documentation\n\n");
        Assert.assertEquals(connector.packageInfo.getOrganization(), orgName);

        Assert.assertEquals(connector.functions.size(), 5);
        List<Function> functionList = connector.functions;

        Assert.assertEquals(functionList.get(0).name, initFunc);
        Assert.assertEquals(functionList.get(0).parameters.size(), 0);
        Assert.assertEquals(functionList.get(2).name, "sendMessage");
        Assert.assertEquals(functionList.get(2).parameters.size(), 2);
    }

    @Test(description = "Test connector metadata generation")
    public void getConnectorMetadata() throws IOException {
        List<Connector> connectors = ConnectorGenerator.generateConnectorModel(testConProject);
        Assert.assertEquals(connectors.size(), 1);
        Connector connector = connectors.get(0);

        Assert.assertEquals(connector.moduleName, moduleName);
        Assert.assertEquals(connector.documentation, "Test Client documentation\n\n");
        Assert.assertEquals(connector.packageInfo.getOrganization(), orgName);
        Assert.assertTrue(connector.displayAnnotation.containsKey("label"));
        Assert.assertTrue(connector.displayAnnotation.containsKey("iconPath"));
        Assert.assertEquals(connector.displayAnnotation.get("label"), "Test Client");
        Assert.assertEquals(connector.displayAnnotation.get("iconPath"), "logo.svg");
        Assert.assertNull(connector.icon);

        Assert.assertEquals(connector.functions.size(), 5);
        List<Function> functionList = connector.functions;

        Assert.assertEquals(functionList.get(0).name, initFunc);
        Assert.assertEquals(functionList.get(0).parameters.size(), 0);
        Assert.assertEquals(functionList.get(2).name, "sendMessage");
        Assert.assertEquals(functionList.get(2).parameters.size(), 2);
        List<Type> sendMsgFuncParams = functionList.get(2).parameters;

        Assert.assertEquals(sendMsgFuncParams.get(0).name, "user");
        Assert.assertEquals(sendMsgFuncParams.get(0).typeName, "record");
        Assert.assertEquals(sendMsgFuncParams.get(1).name, "message");
        Assert.assertEquals(sendMsgFuncParams.get(1).typeName, "string");
        Assert.assertEquals(sendMsgFuncParams.get(1).documentation, "Message to send\n");
        Assert.assertEquals(sendMsgFuncParams.get(1).displayAnnotation.get("label"), "Message");

        Assert.assertEquals(functionList.get(3).name, "viewMessage");
        Assert.assertEquals(functionList.get(3).parameters.size(), 3);
        List<Type> viewMessageFuncParams = functionList.get(3).parameters;

        Assert.assertEquals(viewMessageFuncParams.get(0).name, "uid");
        Assert.assertEquals(viewMessageFuncParams.get(0).typeName, "string");
        Assert.assertFalse(viewMessageFuncParams.get(0).optional);
        Assert.assertFalse(viewMessageFuncParams.get(0).defaultable);
        Assert.assertEquals(viewMessageFuncParams.get(1).name, "name");
        Assert.assertEquals(viewMessageFuncParams.get(1).typeName, "string");
        Assert.assertFalse(viewMessageFuncParams.get(1).optional);
        Assert.assertTrue(viewMessageFuncParams.get(1).defaultable);
        Assert.assertEquals(viewMessageFuncParams.get(2).name, "age");
        Assert.assertEquals(viewMessageFuncParams.get(2).typeName, "int");
        Assert.assertTrue(viewMessageFuncParams.get(2).optional);
        Assert.assertTrue(viewMessageFuncParams.get(2).defaultable);
    }

    @Test(description = "Test connector metadata generation")
    public void getFunctionMetadata() throws IOException {
        Map<String, ModuleDoc> moduleDocMap = BallerinaDocGenerator.generateModuleDocMap(testConProject);
        ModuleDoc testConnector = moduleDocMap.get(moduleName);
        SyntaxTree st = testConnector.syntaxTreeMap.get("main.bal");
        SemanticModel model = testConnector.semanticModel;

        List<Connector> modelFromSyntaxTree = getConnectorModelFromSyntaxTree(st, model,
                orgName, packageName, version, moduleName);

        Assert.assertEquals(modelFromSyntaxTree.size(), 1);
        Assert.assertEquals(modelFromSyntaxTree.get(0).moduleName, moduleName);
        Assert.assertEquals(modelFromSyntaxTree.get(0).packageInfo.getVersion(), version);

        Assert.assertEquals(modelFromSyntaxTree.get(0).functions.size(), 5);
        Assert.assertEquals(modelFromSyntaxTree.get(0).functions.get(0).name, initFunc);
    }

    @Test(description = "Test all types in connector metadata generation")
    public void getAllTypesInConnectorMetadata() throws IOException {
        ProjectEnvironmentBuilder defaultBuilder = ProjectEnvironmentBuilder.getDefaultBuilder();
        defaultBuilder.addCompilationCacheFactory(TempDirCompilationCache::from);
        Project testTypesConProject = ProjectLoader.loadProject(this.testClientBalaFile, defaultBuilder);
        List<Connector> connectors = ConnectorGenerator.generateConnectorModel(testTypesConProject);
        Assert.assertEquals(connectors.size(), 1);
        Connector connector = connectors.get(0);
        (new Gson()).toJson(connector);

        Assert.assertEquals(connector.moduleName, "TestClient");
        Assert.assertEquals(connector.functions.size(), 12);
        // Path parameters
        Assert.assertEquals(connector.functions.get(3).pathParams.size(), 2);
        Assert.assertEquals(connector.functions.get(3).pathParams.get(0).name, "users");
        Assert.assertEquals(connector.functions.get(3).pathParams.get(0).typeName, "token");
        Assert.assertFalse(connector.functions.get(3).pathParams.get(0).isRestType);
        Assert.assertTrue(connector.functions.get(4).pathParams.get(1).isRestType);
        // Query parameters
        Assert.assertEquals(connector.functions.get(3).parameters.size(), 2);
        Assert.assertEquals(connector.functions.get(3).parameters.get(0).name, "i");
        Assert.assertEquals(connector.functions.get(3).parameters.get(0).typeName, "int");
        Assert.assertFalse(connector.functions.get(3).parameters.get(0).optional);
        Assert.assertFalse(connector.functions.get(3).parameters.get(0).defaultable);
        Assert.assertFalse(connector.functions.get(4).parameters.get(0).isRestType);
        Assert.assertTrue(connector.functions.get(4).parameters.get(1).isRestType);
        // Return type
        Assert.assertEquals(connector.functions.get(3).returnType.typeName, "string");
        // Qualifiers
        Assert.assertEquals(connector.functions.get(3).qualifiers.length, 2);
        Assert.assertEquals(connector.functions.get(3).qualifiers[0], "resource");
        // Documentation and annotation
        Assert.assertEquals(connector.functions.get(2).documentation.trim(),
                "Resource function - no query parameter or path parameters");
        Assert.assertEquals(connector.functions.get(2).displayAnnotation.get("label"), "Test client get function");
        // Enum
        Assert.assertEquals(connector.functions.get(8).parameters.get(0).typeName, "enum");
        Assert.assertEquals(connector.functions.get(8).parameters.get(0).typeInfo.name, "Color");
        // Table
        Assert.assertEquals(connector.functions.get(6).parameters.get(3).typeName, "table");
        // Map
        Assert.assertEquals(connector.functions.get(6).parameters.get(2).typeName, "map");
    }
}
