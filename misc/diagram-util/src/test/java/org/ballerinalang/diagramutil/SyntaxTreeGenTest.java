/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.diagramutil;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.directory.SingleFileProject;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Test suit for syntax tree generation.
 */
public class SyntaxTreeGenTest {
    private final Path emptyFile = TestUtil.RES_DIR.resolve("empty")
            .resolve("empty.bal");
    private final Path documentLevelClientInit = TestUtil.RES_DIR.resolve("documentLevelClientInit");
    private final Path externalClientInit = TestUtil.RES_DIR.resolve("externalClientInit");
    private final Path documentLevelClientOnly = TestUtil.RES_DIR.resolve("documentLevelClientOnly");
    private final Path externalClientInitOnly = TestUtil.RES_DIR.resolve("externalClientInitOnly");
    private final Path multiLevelEndpoints = TestUtil.RES_DIR.resolve("multiLevelEndpoints");

    @Test(description = "Generate ST for empty bal file.")
    public void testEmptyBalST() throws IOException {
        Path inputFile = TestUtil.createTempFile(emptyFile);
        SingleFileProject project = SingleFileProject.load(inputFile);
        Optional<ModuleId> optionalModuleId = project.currentPackage().moduleIds().stream().findFirst();
        if (optionalModuleId.isEmpty()) {
            Assert.fail("Failed to retrieve the module ID");
        }
        ModuleId moduleId = optionalModuleId.get();
        Module module = project.currentPackage().module(moduleId);
        PackageCompilation packageCompilation = project.currentPackage().getCompilation();
        SemanticModel semanticModel = packageCompilation.getSemanticModel(moduleId);
        Optional<DocumentId> optionalDocumentId = module.documentIds().stream().findFirst();
        if (optionalDocumentId.isEmpty()) {
            Assert.fail("Failed to retrieve the document ID");
        }
        DocumentId documentId = optionalDocumentId.get();
        Document document = module.document(documentId);
        JsonElement stJson = DiagramUtil.getSyntaxTreeJSON(document, semanticModel);
        Assert.assertTrue(stJson.isJsonObject());
        Assert.assertTrue(stJson.getAsJsonObject().get("kind").isJsonPrimitive());
        Assert.assertEquals(stJson.getAsJsonObject().get("kind").getAsString(), "ModulePart");
        Assert.assertTrue(stJson.getAsJsonObject().get("members").isJsonArray());
        Assert.assertEquals(stJson.getAsJsonObject().get("members").getAsJsonArray().size(), 0);
    }

    @Test(description = "Generate ST for client invocation in a main bal file.")
    public void testHttpMainBalST() throws IOException {
        Path inputFile = TestUtil.createTempProject(documentLevelClientInit);
        BuildProject project = BuildProject.load(inputFile);
        Optional<ModuleId> optionalModuleId = project.currentPackage().moduleIds().stream().findFirst();
        if (optionalModuleId.isEmpty()) {
            Assert.fail("Failed to retrieve the module ID");
        }
        ModuleId moduleId = optionalModuleId.get();
        Module module = project.currentPackage().module(moduleId);
        PackageCompilation packageCompilation = project.currentPackage().getCompilation();
        SemanticModel semanticModel = packageCompilation.getSemanticModel(moduleId);
        Optional<DocumentId> optionalDocumentId = module.documentIds().stream().findFirst();
        if (optionalDocumentId.isEmpty()) {
            Assert.fail("Failed to retrieve the document ID");
        }
        DocumentId documentId = optionalDocumentId.get();
        Document document = module.document(documentId);
        JsonElement stJson = DiagramUtil.getSyntaxTreeJSON(document, semanticModel);
        Assert.assertTrue(stJson.isJsonObject());
        Assert.assertTrue(stJson.getAsJsonObject().get("kind").isJsonPrimitive());
        Assert.assertEquals(stJson.getAsJsonObject().get("kind").getAsString(), "ModulePart");
        Assert.assertTrue(stJson.getAsJsonObject().get("members").isJsonArray());
        Assert.assertTrue(stJson.getAsJsonObject().get("members").getAsJsonArray().size() > 0);
        JsonArray members = stJson.getAsJsonObject().get("members").getAsJsonArray();

        // Validate module var is identified as an Endpoint.
        JsonObject moduleVar = members.get(1).getAsJsonObject();
        Assert.assertTrue(moduleVar.has("typeData"));
        Assert.assertTrue(moduleVar.get("typeData").getAsJsonObject().has("isEndpoint"));
        Assert.assertTrue(moduleVar.get("typeData").getAsJsonObject().get("isEndpoint").getAsBoolean());

        // Validate local var is identified as an Endpoint.
        JsonObject function = members.get(2).getAsJsonObject();
        JsonObject functionBody = function.get("functionBody").getAsJsonObject();
        JsonArray visibleEndpoints = functionBody.get("VisibleEndpoints").getAsJsonArray();

        visibleEndpoints.forEach(jsonElement -> {
            JsonObject endpoint = jsonElement.getAsJsonObject();
            if (endpoint.get("name").getAsString().equals("clientEndpoint")
                    || endpoint.get("name").getAsString().equals("myClient")) {
                Assert.assertTrue(true);
            } else {
                Assert.fail("Additional endpoint has been found");
            }
        });

        // Validate external endpoint in visible endpoints
        JsonObject moduleEndpoint = visibleEndpoints.get(0).getAsJsonObject();
        Assert.assertEquals(moduleEndpoint.get("typeName").getAsString(), "MyMainClient");
        Assert.assertEquals(moduleEndpoint.get("name").getAsString(), "myClient");
        Assert.assertEquals(moduleEndpoint.get("orgName").getAsString(), "marcus");
        Assert.assertEquals(moduleEndpoint.get("packageName").getAsString(), "test");
        Assert.assertEquals(moduleEndpoint.get("moduleName").getAsString(), "test");
        Assert.assertEquals(moduleEndpoint.get("version").getAsString(), "0.1.0");
        Assert.assertEquals(moduleEndpoint.get("isModuleVar").getAsString(), "true");
        Assert.assertEquals(moduleEndpoint.get("isExternal").getAsString(), "true");

        // Validate local endpoint in visible endpoints
        JsonObject localEndpoint = visibleEndpoints.get(1).getAsJsonObject();
        Assert.assertEquals(localEndpoint.get("typeName").getAsString(), "MyMainClient");
        Assert.assertEquals(localEndpoint.get("name").getAsString(), "clientEndpoint");
        Assert.assertEquals(localEndpoint.get("orgName").getAsString(), "marcus");
        Assert.assertEquals(localEndpoint.get("packageName").getAsString(), "test");
        Assert.assertEquals(localEndpoint.get("moduleName").getAsString(), "test");
        Assert.assertEquals(localEndpoint.get("version").getAsString(), "0.1.0");
        Assert.assertEquals(localEndpoint.get("isModuleVar").getAsString(), "false");

        // Validate module var statement
        Assert.assertTrue(moduleVar.get("typeData").getAsJsonObject().get("symbol").getAsJsonObject()
                .get("moduleID").isJsonObject());
        JsonObject moduleVarModuleId = moduleVar.get("typeData").getAsJsonObject().get("symbol").getAsJsonObject()
                .get("moduleID").getAsJsonObject();
        Assert.assertEquals(moduleVarModuleId.get("orgName").getAsString(), "marcus");
        Assert.assertEquals(moduleVarModuleId.get("packageName").getAsString(), "test");
        Assert.assertEquals(moduleVarModuleId.get("moduleName").getAsString(), "test");
        Assert.assertEquals(moduleVarModuleId.get("version").getAsString(), "0.1.0");

        // Validate local var statement
        Assert.assertTrue(functionBody.get("statements").getAsJsonArray().get(0).isJsonObject());
        JsonObject localVarStmt = functionBody.get("statements").getAsJsonArray().get(0).getAsJsonObject();
        Assert.assertEquals(localVarStmt.get("kind").getAsString(), "LocalVarDecl");
        Assert.assertTrue(localVarStmt.get("typeData").getAsJsonObject().get("symbol").getAsJsonObject()
                .get("moduleID").isJsonObject());
        JsonObject localVarModuleId = localVarStmt.get("typeData").getAsJsonObject().get("symbol").getAsJsonObject()
                .get("moduleID").getAsJsonObject();
        Assert.assertEquals(localVarModuleId.get("orgName").getAsString(), "marcus");
        Assert.assertEquals(localVarModuleId.get("packageName").getAsString(), "test");
        Assert.assertEquals(localVarModuleId.get("moduleName").getAsString(), "test");
        Assert.assertEquals(localVarModuleId.get("version").getAsString(), "0.1.0");

        // Validate assignment statement
        Assert.assertTrue(functionBody.get("statements").getAsJsonArray().get(4).isJsonObject());
        JsonObject assignmentStmt = functionBody.get("statements").getAsJsonArray().get(4).getAsJsonObject();
        Assert.assertEquals(assignmentStmt.get("kind").getAsString(), "AssignmentStatement");
        Assert.assertTrue(assignmentStmt.get("varRef").getAsJsonObject().get("typeData").getAsJsonObject()
                .get("symbol").getAsJsonObject().get("moduleID").isJsonObject());
        JsonObject asgmtModuleId = assignmentStmt.get("varRef").getAsJsonObject().get("typeData").getAsJsonObject()
                .get("symbol").getAsJsonObject().get("moduleID").getAsJsonObject();
        Assert.assertEquals(asgmtModuleId.get("orgName").getAsString(), "marcus");
        Assert.assertEquals(asgmtModuleId.get("packageName").getAsString(), "test");
        Assert.assertEquals(asgmtModuleId.get("moduleName").getAsString(), "test");
        Assert.assertEquals(asgmtModuleId.get("version").getAsString(), "0.1.0");

        // Validate required param
        JsonObject delFunction = members.get(3).getAsJsonObject();
        Assert.assertTrue(delFunction.get("functionSignature").getAsJsonObject().get("parameters")
                .getAsJsonArray().get(0).isJsonObject());
        JsonObject requiredParam = delFunction.get("functionSignature").getAsJsonObject().get("parameters")
                .getAsJsonArray().get(0).getAsJsonObject();
        Assert.assertTrue(requiredParam.get("typeData").getAsJsonObject().get("symbol").getAsJsonObject()
                .get("moduleID").isJsonObject());
        JsonObject reqParamModuleId = requiredParam.get("typeData").getAsJsonObject().get("symbol").getAsJsonObject()
                .get("moduleID").getAsJsonObject();
        Assert.assertEquals(reqParamModuleId.get("orgName").getAsString(), "marcus");
        Assert.assertEquals(reqParamModuleId.get("packageName").getAsString(), "test");
        Assert.assertEquals(reqParamModuleId.get("moduleName").getAsString(), "test");
        Assert.assertEquals(reqParamModuleId.get("version").getAsString(), "0.1.0");
    }

    @Test(description = "Generate ST for client outside a main bal file.")
    public void testExternalClient() throws IOException {
        Path inputFile = TestUtil.createTempProject(externalClientInit);
        BuildProject project = BuildProject.load(inputFile);
        Optional<ModuleId> optionalModuleId = project.currentPackage().moduleIds().stream().findFirst();
        if (optionalModuleId.isEmpty()) {
            Assert.fail("Failed to retrieve the module ID");
        }
        ModuleId moduleId = optionalModuleId.get();
        Module module = project.currentPackage().module(moduleId);
        PackageCompilation packageCompilation = project.currentPackage().getCompilation();
        SemanticModel semanticModel = packageCompilation.getSemanticModel(moduleId);
        Optional<DocumentId> optionalDocumentId = module.documentIds().stream()
                .filter(documentId -> module.document(documentId).name().equals("main.bal")).findFirst();
        if (optionalDocumentId.isEmpty()) {
            Assert.fail("Failed to retrieve the document ID");
        }
        DocumentId documentId = optionalDocumentId.get();
        Document document = module.document(documentId);
        JsonElement stJson = DiagramUtil.getSyntaxTreeJSON(document, semanticModel);
        Assert.assertTrue(stJson.isJsonObject());
        Assert.assertTrue(stJson.getAsJsonObject().get("kind").isJsonPrimitive());
        Assert.assertEquals(stJson.getAsJsonObject().get("kind").getAsString(), "ModulePart");
        Assert.assertTrue(stJson.getAsJsonObject().get("members").isJsonArray());
        Assert.assertTrue(stJson.getAsJsonObject().get("members").getAsJsonArray().size() > 0);
        JsonArray members = stJson.getAsJsonObject().get("members").getAsJsonArray();

        // Validate local var is identified as an Endpoint.
        JsonObject function = members.get(0).getAsJsonObject();
        JsonObject functionBody = function.get("functionBody").getAsJsonObject();
        JsonArray visibleEndpoints = functionBody.get("VisibleEndpoints").getAsJsonArray();

        visibleEndpoints.forEach(jsonElement -> {
            JsonObject endpoint = jsonElement.getAsJsonObject();
            if (endpoint.get("name").getAsString().equals("clientEndpoint")
                    || endpoint.get("name").getAsString().equals("myClient")) {
                Assert.assertTrue(true);
                Assert.assertEquals(endpoint.get("orgName").getAsString(), "marcus");
                Assert.assertEquals(endpoint.get("packageName").getAsString(), "test");
                Assert.assertEquals(endpoint.get("moduleName").getAsString(), "test");
                Assert.assertEquals(endpoint.get("version").getAsString(), "0.1.0");
            } else {
                Assert.fail("Additional endpoint has been found");
            }
        });
    }

    @Test(description = "Generate ST for client in the document level of a main bal file.")
    public void testDocumentLevelClientOnly() throws IOException {
        Path inputFile = TestUtil.createTempProject(documentLevelClientOnly);
        BuildProject project = BuildProject.load(inputFile);
        Optional<ModuleId> optionalModuleId = project.currentPackage().moduleIds().stream().findFirst();
        if (optionalModuleId.isEmpty()) {
            Assert.fail("Failed to retrieve the module ID");
        }
        ModuleId moduleId = optionalModuleId.get();
        Module module = project.currentPackage().module(moduleId);
        PackageCompilation packageCompilation = project.currentPackage().getCompilation();
        SemanticModel semanticModel = packageCompilation.getSemanticModel(moduleId);
        Optional<DocumentId> optionalDocumentId = module.documentIds().stream().findFirst();
        if (optionalDocumentId.isEmpty()) {
            Assert.fail("Failed to retrieve the document ID");
        }
        DocumentId documentId = optionalDocumentId.get();
        Document document = module.document(documentId);
        JsonElement stJson = DiagramUtil.getSyntaxTreeJSON(document, semanticModel);
        Assert.assertTrue(stJson.isJsonObject());
        Assert.assertTrue(stJson.getAsJsonObject().get("kind").isJsonPrimitive());
        Assert.assertEquals(stJson.getAsJsonObject().get("kind").getAsString(), "ModulePart");
        Assert.assertTrue(stJson.getAsJsonObject().get("members").isJsonArray());
        Assert.assertTrue(stJson.getAsJsonObject().get("members").getAsJsonArray().size() > 0);
        JsonArray members = stJson.getAsJsonObject().get("members").getAsJsonArray();

        // Validate module var is identified as an Endpoint.
        JsonObject moduleVar = members.get(1).getAsJsonObject();
        Assert.assertTrue(moduleVar.has("typeData"));
        Assert.assertTrue(moduleVar.get("typeData").getAsJsonObject().has("isEndpoint"));
        Assert.assertTrue(moduleVar.get("typeData").getAsJsonObject().get("isEndpoint").getAsBoolean());

        // Validate local var is identified as an Endpoint.
        JsonObject function = members.get(2).getAsJsonObject();
        JsonObject functionBody = function.get("functionBody").getAsJsonObject();
        JsonArray visibleEndpoints = functionBody.get("VisibleEndpoints").getAsJsonArray();

        Assert.assertEquals(visibleEndpoints.size(), 1, "No visible endpoints found.");

        visibleEndpoints.forEach(jsonElement -> {
            JsonObject endpoint = jsonElement.getAsJsonObject();
            if (endpoint.get("name").getAsString().equals("myClient")) {
                Assert.assertTrue(true);
                Assert.assertEquals(endpoint.get("orgName").getAsString(), "marcus");
                Assert.assertEquals(endpoint.get("packageName").getAsString(), "test");
                Assert.assertEquals(endpoint.get("moduleName").getAsString(), "test");
                Assert.assertEquals(endpoint.get("version").getAsString(), "0.1.0");
            } else {
                Assert.fail("Additional endpoint has been found");
            }
        });
    }

    @Test(description = "Generate ST for client outside a main bal file.")
    public void testExternalClientOnly() throws IOException {
        Path inputFile = TestUtil.createTempProject(externalClientInitOnly);
        BuildProject project = BuildProject.load(inputFile);
        Optional<ModuleId> optionalModuleId = project.currentPackage().moduleIds().stream().findFirst();
        if (optionalModuleId.isEmpty()) {
            Assert.fail("Failed to retrieve the module ID");
        }
        ModuleId moduleId = optionalModuleId.get();
        Module module = project.currentPackage().module(moduleId);
        PackageCompilation packageCompilation = project.currentPackage().getCompilation();
        SemanticModel semanticModel = packageCompilation.getSemanticModel(moduleId);
        Optional<DocumentId> optionalDocumentId = module.documentIds().stream()
                .filter(documentId -> module.document(documentId).name().equals("main.bal")).findFirst();
        if (optionalDocumentId.isEmpty()) {
            Assert.fail("Failed to retrieve the document ID");
        }
        DocumentId documentId = optionalDocumentId.get();
        Document document = module.document(documentId);
        JsonElement stJson = DiagramUtil.getSyntaxTreeJSON(document, semanticModel);
        Assert.assertTrue(stJson.isJsonObject());
        Assert.assertTrue(stJson.getAsJsonObject().get("kind").isJsonPrimitive());
        Assert.assertEquals(stJson.getAsJsonObject().get("kind").getAsString(), "ModulePart");
        Assert.assertTrue(stJson.getAsJsonObject().get("members").isJsonArray());
        Assert.assertTrue(stJson.getAsJsonObject().get("members").getAsJsonArray().size() > 0);
        JsonArray members = stJson.getAsJsonObject().get("members").getAsJsonArray();

        // Validate local var is identified as an Endpoint.
        JsonObject function = members.get(0).getAsJsonObject();
        JsonObject functionBody = function.get("functionBody").getAsJsonObject();
        JsonArray visibleEndpoints = functionBody.get("VisibleEndpoints").getAsJsonArray();

        Assert.assertEquals(visibleEndpoints.size(), 1, "No visible endpoints found.");

        visibleEndpoints.forEach(jsonElement -> {
            JsonObject endpoint = jsonElement.getAsJsonObject();
            if (endpoint.get("name").getAsString().equals("myClient")) {
                Assert.assertTrue(true);
                Assert.assertEquals(endpoint.get("orgName").getAsString(), "marcus");
                Assert.assertEquals(endpoint.get("packageName").getAsString(), "test");
                Assert.assertEquals(endpoint.get("moduleName").getAsString(), "test");
                Assert.assertEquals(endpoint.get("version").getAsString(), "0.1.0");
            } else {
                Assert.fail("Additional endpoint has been found");
            }
        });
    }

    @Test(description = "Test visible endpoint object in different nodes.")
    public void testVisibleEndpoints() throws IOException {
        Path inputFile = TestUtil.createTempProject(multiLevelEndpoints);

        BuildProject project = BuildProject.load(inputFile);
        Optional<ModuleId> optionalModuleId = project.currentPackage().moduleIds().stream().findFirst();
        if (optionalModuleId.isEmpty()) {
            Assert.fail("Failed to retrieve the module ID");
        }
        ModuleId moduleId = optionalModuleId.get();
        Module module = project.currentPackage().module(moduleId);
        PackageCompilation packageCompilation = project.currentPackage().getCompilation();
        SemanticModel semanticModel = packageCompilation.getSemanticModel(moduleId);
        Optional<DocumentId> optionalDocumentId = module.documentIds().stream()
                .filter(documentId -> module.document(documentId).name().equals("main.bal")).findFirst();
        if (optionalDocumentId.isEmpty()) {
            Assert.fail("Failed to retrieve the document ID");
        }
        DocumentId documentId = optionalDocumentId.get();
        Document document = module.document(documentId);
        JsonElement stJson = DiagramUtil.getSyntaxTreeJSON(document, semanticModel);
        Assert.assertTrue(stJson.isJsonObject());

        Assert.assertEquals(stJson.getAsJsonObject().get("kind").getAsString(), "ModulePart");
        Assert.assertTrue(stJson.getAsJsonObject().get("members").isJsonArray());
        Assert.assertTrue(stJson.getAsJsonObject().get("members").getAsJsonArray().size() == 9);
        JsonArray members = stJson.getAsJsonObject().get("members").getAsJsonArray();

        // Verify main function
        Assert.assertEquals(members.get(2).getAsJsonObject().get("kind").getAsString(), "FunctionDefinition");
        JsonObject mainFunction = members.get(2).getAsJsonObject();
        Assert.assertEquals(mainFunction.get("functionName").getAsJsonObject().get("value").getAsString(), "main");
        JsonObject mainFunctionBody = mainFunction.get("functionBody").getAsJsonObject();
        JsonArray mainFunctionVEp = mainFunctionBody.get("VisibleEndpoints").getAsJsonArray();
        Assert.assertTrue(mainFunctionVEp.size() == 2);

        // Verify main function visible endpoints
        JsonObject exEp0 = mainFunctionVEp.get(0).getAsJsonObject();
        Assert.assertEquals(exEp0.get("name").getAsString(), "exEp0");
        Assert.assertEquals(exEp0.get("typeName").getAsString(), "ExternalClient");
        Assert.assertEquals(exEp0.get("orgName").getAsString(), "gayanOrg");
        Assert.assertEquals(exEp0.get("packageName").getAsString(), "testEps");
        Assert.assertEquals(exEp0.get("moduleName").getAsString(), "testEps");
        Assert.assertEquals(exEp0.get("version").getAsString(), "0.1.0");
        Assert.assertEquals(exEp0.get("isModuleVar").getAsBoolean(), true);
        Assert.assertEquals(exEp0.get("isExternal").getAsBoolean(), true);
        JsonObject exEp0Position = exEp0.get("position").getAsJsonObject();
        Assert.assertEquals(exEp0Position.get("startLine").getAsInt(), 14);
        Assert.assertEquals(exEp0Position.get("endLine").getAsInt(), 14);

        JsonObject exEp1 = mainFunctionVEp.get(1).getAsJsonObject();
        Assert.assertEquals(exEp1.get("name").getAsString(), "exEp1");
        Assert.assertEquals(exEp1.get("typeName").getAsString(), "ExternalClient");
        Assert.assertEquals(exEp1.get("orgName").getAsString(), "gayanOrg");
        Assert.assertEquals(exEp1.get("packageName").getAsString(), "testEps");
        Assert.assertEquals(exEp1.get("moduleName").getAsString(), "testEps");
        Assert.assertEquals(exEp1.get("version").getAsString(), "0.1.0");
        Assert.assertEquals(exEp1.get("isModuleVar").getAsBoolean(), false);
        Assert.assertEquals(exEp1.get("isExternal").getAsBoolean(), false);
        JsonObject exEp1Position = exEp1.get("position").getAsJsonObject();
        Assert.assertEquals(exEp1Position.get("startLine").getAsInt(), 17);
        Assert.assertEquals(exEp1Position.get("endLine").getAsInt(), 17);

        // Verify main function if block
        Assert.assertEquals(mainFunctionBody.get("statements").getAsJsonArray().get(3).getAsJsonObject().get("ifBody")
                .getAsJsonObject().get("kind").getAsString(), "BlockStatement");
        JsonObject mainFunctionIfBody = mainFunctionBody.get("statements").getAsJsonArray().get(3).getAsJsonObject()
                .get("ifBody").getAsJsonObject();
        JsonArray mainFunctionIfBodyVEp = mainFunctionIfBody.get("VisibleEndpoints").getAsJsonArray();
        Assert.assertTrue(mainFunctionIfBodyVEp.size() == 3);

        // Verify main function if block visible endpoints
        Assert.assertEquals(mainFunctionIfBodyVEp.get(0).getAsJsonObject().get("name").getAsString(), "exEp0");

        Assert.assertEquals(mainFunctionIfBodyVEp.get(1).getAsJsonObject().get("name").getAsString(), "exEp1");

        JsonObject exEp2 = mainFunctionIfBodyVEp.get(2).getAsJsonObject();
        Assert.assertEquals(exEp2.get("name").getAsString(), "exEp2");
        Assert.assertEquals(exEp2.get("typeName").getAsString(), "ExternalClient");
        Assert.assertEquals(exEp2.get("orgName").getAsString(), "gayanOrg");
        Assert.assertEquals(exEp2.get("packageName").getAsString(), "testEps");
        Assert.assertEquals(exEp2.get("moduleName").getAsString(), "testEps");
        Assert.assertEquals(exEp2.get("version").getAsString(), "0.1.0");
        Assert.assertEquals(exEp2.get("isModuleVar").getAsBoolean(), false);
        Assert.assertEquals(exEp2.get("isExternal").getAsBoolean(), false);
        JsonObject exEp2Position = exEp2.get("position").getAsJsonObject();
        Assert.assertEquals(exEp2Position.get("startLine").getAsInt(), 22);
        Assert.assertEquals(exEp2Position.get("endLine").getAsInt(), 22);

        // Verify main function anonymous function block
        Assert.assertEquals(mainFunctionBody.get("statements").getAsJsonArray().get(4).getAsJsonObject()
                .get("initializer").getAsJsonObject().get("functionBody").getAsJsonObject().get("kind")
                .getAsString(), "FunctionBodyBlock");
        JsonObject mainFunctionAnonBody = mainFunctionBody.get("statements").getAsJsonArray().get(4)
                .getAsJsonObject().get("initializer").getAsJsonObject().get("functionBody").getAsJsonObject();
        JsonArray mainFunctionAnonVEp = mainFunctionAnonBody.get("VisibleEndpoints").getAsJsonArray();
        Assert.assertTrue(mainFunctionAnonVEp.size() == 3);

        // Verify main function anonymous function block visible endpoints
        Assert.assertEquals(mainFunctionAnonVEp.get(0).getAsJsonObject().get("name").getAsString(), "exEp0");

        Assert.assertEquals(mainFunctionAnonVEp.get(1).getAsJsonObject().get("name").getAsString(), "exEp1");

        JsonObject exEp3 = mainFunctionAnonVEp.get(2).getAsJsonObject();
        Assert.assertEquals(exEp3.get("name").getAsString(), "exEp3");
        Assert.assertEquals(exEp3.get("typeName").getAsString(), "ExternalClient");
        Assert.assertEquals(exEp3.get("orgName").getAsString(), "gayanOrg");
        Assert.assertEquals(exEp3.get("packageName").getAsString(), "testEps");
        Assert.assertEquals(exEp3.get("moduleName").getAsString(), "testEps");
        Assert.assertEquals(exEp3.get("version").getAsString(), "0.1.0");
        Assert.assertEquals(exEp3.get("isModuleVar").getAsBoolean(), false);
        Assert.assertEquals(exEp3.get("isExternal").getAsBoolean(), false);
        JsonObject exEp3Position = exEp3.get("position").getAsJsonObject();
        Assert.assertEquals(exEp3Position.get("startLine").getAsInt(), 26);
        Assert.assertEquals(exEp3Position.get("endLine").getAsInt(), 26);

        // Verify secondFunc function
        Assert.assertEquals(members.get(3).getAsJsonObject().get("kind").getAsString(), "FunctionDefinition");
        JsonObject secondFunction = members.get(3).getAsJsonObject();
        Assert.assertEquals(secondFunction.get("functionName").getAsJsonObject().get("value").getAsString(),
                "secondFunc");
        JsonObject secondFunctionBody = secondFunction.get("functionBody").getAsJsonObject();
        JsonArray secondFunctionVEp = secondFunctionBody.get("VisibleEndpoints").getAsJsonArray();
        Assert.assertTrue(secondFunctionVEp.size() == 3);

        // Verify secondFunc function visible endpoints
        Assert.assertEquals(secondFunctionVEp.get(0).getAsJsonObject().get("name").getAsString(), "exEp0");

        JsonObject exEpP1 = secondFunctionVEp.get(1).getAsJsonObject();
        Assert.assertEquals(exEpP1.get("name").getAsString(), "exEpP1");
        Assert.assertEquals(exEpP1.get("typeName").getAsString(), "ExternalClient");
        Assert.assertEquals(exEpP1.get("orgName").getAsString(), "gayanOrg");
        Assert.assertEquals(exEpP1.get("packageName").getAsString(), "testEps");
        Assert.assertEquals(exEpP1.get("moduleName").getAsString(), "testEps");
        Assert.assertEquals(exEpP1.get("version").getAsString(), "0.1.0");
        Assert.assertEquals(exEpP1.get("isModuleVar").getAsBoolean(), false);
        Assert.assertEquals(exEpP1.get("isExternal").getAsBoolean(), true);
        Assert.assertEquals(exEpP1.get("isParameter").getAsBoolean(), true);
        JsonObject exEpP1Position = exEpP1.get("position").getAsJsonObject();
        Assert.assertEquals(exEpP1Position.get("startLine").getAsInt(), 31);
        Assert.assertEquals(exEpP1Position.get("endLine").getAsInt(), 31);

        JsonObject exEp6 = secondFunctionVEp.get(2).getAsJsonObject();
        Assert.assertEquals(exEp6.get("name").getAsString(), "exEp6");
        Assert.assertEquals(exEp6.get("typeName").getAsString(), "ExternalClient");
        Assert.assertEquals(exEp6.get("orgName").getAsString(), "gayanOrg");
        Assert.assertEquals(exEp6.get("packageName").getAsString(), "testEps");
        Assert.assertEquals(exEp6.get("moduleName").getAsString(), "testEps");
        Assert.assertEquals(exEp6.get("version").getAsString(), "0.1.0");
        Assert.assertEquals(exEp6.get("isModuleVar").getAsBoolean(), false);
        Assert.assertEquals(exEp6.get("isExternal").getAsBoolean(), false);
        JsonObject exEp6Position = exEp6.get("position").getAsJsonObject();
        Assert.assertEquals(exEp6Position.get("startLine").getAsInt(), 41);
        Assert.assertEquals(exEp6Position.get("endLine").getAsInt(), 41);

        // Verify secondFunc function while block
        Assert.assertEquals(secondFunctionBody.get("statements").getAsJsonArray().get(1).getAsJsonObject()
                .get("whileBody").getAsJsonObject().get("kind").getAsString(), "BlockStatement");
        JsonObject secondFunctionWhileBody = secondFunctionBody.get("statements").getAsJsonArray().get(1)
                .getAsJsonObject().get("whileBody").getAsJsonObject();
        JsonArray secondFunctionWhileBodyVEp = secondFunctionWhileBody.get("VisibleEndpoints").getAsJsonArray();
        Assert.assertTrue(secondFunctionWhileBodyVEp.size() == 3);

        // Verify secondFunc function while block visible endpoints
        Assert.assertEquals(secondFunctionWhileBodyVEp.get(0).getAsJsonObject().get("name").getAsString(), "exEp0");

        Assert.assertEquals(secondFunctionWhileBodyVEp.get(1).getAsJsonObject().get("name").getAsString(), "exEpP1");

        JsonObject exEp4 = secondFunctionWhileBodyVEp.get(2).getAsJsonObject();
        Assert.assertEquals(exEp4.get("name").getAsString(), "exEp4");
        Assert.assertEquals(exEp4.get("typeName").getAsString(), "ExternalClient");
        Assert.assertEquals(exEp4.get("orgName").getAsString(), "gayanOrg");
        Assert.assertEquals(exEp4.get("packageName").getAsString(), "testEps");
        Assert.assertEquals(exEp4.get("moduleName").getAsString(), "testEps");
        Assert.assertEquals(exEp4.get("version").getAsString(), "0.1.0");
        Assert.assertEquals(exEp4.get("isModuleVar").getAsBoolean(), false);
        Assert.assertEquals(exEp4.get("isExternal").getAsBoolean(), false);
        JsonObject exEp4Position = exEp4.get("position").getAsJsonObject();
        Assert.assertEquals(exEp4Position.get("startLine").getAsInt(), 34);
        Assert.assertEquals(exEp4Position.get("endLine").getAsInt(), 34);

        // Verify secondFunc function do block
        Assert.assertEquals(secondFunctionBody.get("statements").getAsJsonArray().get(2).getAsJsonObject()
                .get("blockStatement").getAsJsonObject().get("kind").getAsString(), "BlockStatement");
        JsonObject secondFunctionDoBody = secondFunctionBody.get("statements").getAsJsonArray().get(2)
                .getAsJsonObject().get("blockStatement").getAsJsonObject();
        JsonArray secondFunctionDoBodyVEp = secondFunctionDoBody.get("VisibleEndpoints").getAsJsonArray();
        Assert.assertTrue(secondFunctionDoBodyVEp.size() == 3);

        // Verify secondFunc function do block visible endpoints
        Assert.assertEquals(secondFunctionDoBodyVEp.get(0).getAsJsonObject().get("name").getAsString(), "exEp0");

        Assert.assertEquals(secondFunctionDoBodyVEp.get(1).getAsJsonObject().get("name").getAsString(), "exEpP1");

        JsonObject exEp5 = secondFunctionDoBodyVEp.get(2).getAsJsonObject();
        Assert.assertEquals(exEp5.get("name").getAsString(), "exEp5");
        Assert.assertEquals(exEp5.get("typeName").getAsString(), "ExternalClient");
        Assert.assertEquals(exEp5.get("orgName").getAsString(), "gayanOrg");
        Assert.assertEquals(exEp5.get("packageName").getAsString(), "testEps");
        Assert.assertEquals(exEp5.get("moduleName").getAsString(), "testEps");
        Assert.assertEquals(exEp5.get("version").getAsString(), "0.1.0");
        Assert.assertEquals(exEp5.get("isModuleVar").getAsBoolean(), false);
        Assert.assertEquals(exEp5.get("isExternal").getAsBoolean(), false);
        JsonObject exEp5Position = exEp5.get("position").getAsJsonObject();
        Assert.assertEquals(exEp5Position.get("startLine").getAsInt(), 38);
        Assert.assertEquals(exEp5Position.get("endLine").getAsInt(), 38);

        // Verify secondFunc function else block
        Assert.assertEquals(secondFunctionBody.get("statements").getAsJsonArray().get(4).getAsJsonObject()
                        .get("elseBody").getAsJsonObject().get("elseBody").getAsJsonObject().get("kind").getAsString(),
                "BlockStatement");
        JsonObject secondFunctionElseBody = secondFunctionBody.get("statements").getAsJsonArray().get(4)
                .getAsJsonObject().get("elseBody").getAsJsonObject().get("elseBody").getAsJsonObject();
        JsonArray secondFunctionElseBodyVEp = secondFunctionElseBody.get("VisibleEndpoints").getAsJsonArray();
        Assert.assertTrue(secondFunctionElseBodyVEp.size() == 4);

        // Verify secondFunc function do block visible endpoints
        Assert.assertEquals(secondFunctionElseBodyVEp.get(0).getAsJsonObject().get("name").getAsString(), "exEp0");

        Assert.assertEquals(secondFunctionElseBodyVEp.get(1).getAsJsonObject().get("name").getAsString(), "exEpP1");

        Assert.assertEquals(secondFunctionElseBodyVEp.get(2).getAsJsonObject().get("name").getAsString(), "exEp6");

        JsonObject exEp7 = secondFunctionElseBodyVEp.get(3).getAsJsonObject();
        Assert.assertEquals(exEp7.get("name").getAsString(), "exEp7");
        Assert.assertEquals(exEp7.get("typeName").getAsString(), "ExternalClient");
        Assert.assertEquals(exEp7.get("orgName").getAsString(), "gayanOrg");
        Assert.assertEquals(exEp7.get("packageName").getAsString(), "testEps");
        Assert.assertEquals(exEp7.get("moduleName").getAsString(), "testEps");
        Assert.assertEquals(exEp7.get("version").getAsString(), "0.1.0");
        Assert.assertEquals(exEp7.get("isModuleVar").getAsBoolean(), false);
        Assert.assertEquals(exEp7.get("isExternal").getAsBoolean(), false);
        JsonObject exEp7Position = exEp7.get("position").getAsJsonObject();
        Assert.assertEquals(exEp7Position.get("startLine").getAsInt(), 46);
        Assert.assertEquals(exEp7Position.get("endLine").getAsInt(), 46);

        // Verify thirdFunc function
        Assert.assertEquals(members.get(4).getAsJsonObject().get("kind").getAsString(), "FunctionDefinition");
        JsonObject thirdFunction = members.get(4).getAsJsonObject();
        Assert.assertEquals(thirdFunction.get("functionName").getAsJsonObject().get("value").getAsString(),
                "thirdFunc");
        JsonObject thirdFunctionBody = thirdFunction.get("functionBody").getAsJsonObject();
        JsonArray thirdFunctionVEp = thirdFunctionBody.get("VisibleEndpoints").getAsJsonArray();
        Assert.assertTrue(thirdFunctionVEp.size() == 3);

        // Verify thirdFunc function visible endpoints
        Assert.assertEquals(thirdFunctionVEp.get(0).getAsJsonObject().get("name").getAsString(), "exEp0");

        JsonObject exEp8 = thirdFunctionVEp.get(1).getAsJsonObject();
        Assert.assertEquals(exEp8.get("name").getAsString(), "exEp8");
        Assert.assertEquals(exEp8.get("typeName").getAsString(), "ExternalClient");
        Assert.assertEquals(exEp8.get("orgName").getAsString(), "gayanOrg");
        Assert.assertEquals(exEp8.get("packageName").getAsString(), "testEps");
        Assert.assertEquals(exEp8.get("moduleName").getAsString(), "testEps");
        Assert.assertEquals(exEp8.get("version").getAsString(), "0.1.0");
        Assert.assertEquals(exEp8.get("isModuleVar").getAsBoolean(), false);
        Assert.assertEquals(exEp8.get("isExternal").getAsBoolean(), false);
        JsonObject exEp8Position = exEp8.get("position").getAsJsonObject();
        Assert.assertEquals(exEp8Position.get("startLine").getAsInt(), 53);
        Assert.assertEquals(exEp8Position.get("endLine").getAsInt(), 53);

        JsonObject exEp6Copy = thirdFunctionVEp.get(2).getAsJsonObject();
        Assert.assertEquals(exEp6Copy.get("name").getAsString(), "exEp6");
        Assert.assertEquals(exEp6Copy.get("typeName").getAsString(), "ExternalClient");
        Assert.assertEquals(exEp6Copy.get("orgName").getAsString(), "gayanOrg");
        Assert.assertEquals(exEp6Copy.get("packageName").getAsString(), "testEps");
        Assert.assertEquals(exEp6Copy.get("moduleName").getAsString(), "testEps");
        Assert.assertEquals(exEp6Copy.get("version").getAsString(), "0.1.0");
        Assert.assertEquals(exEp6Copy.get("isModuleVar").getAsBoolean(), false);
        Assert.assertEquals(exEp6Copy.get("isExternal").getAsBoolean(), false);
        JsonObject exEp6CopyPosition = exEp6Copy.get("position").getAsJsonObject();
        Assert.assertEquals(exEp6CopyPosition.get("startLine").getAsInt(), 57);
        Assert.assertEquals(exEp6CopyPosition.get("endLine").getAsInt(), 57);

        // Verify fourthFunc function
        Assert.assertEquals(members.get(5).getAsJsonObject().get("kind").getAsString(), "FunctionDefinition");
        JsonObject fourthFunction = members.get(5).getAsJsonObject();
        Assert.assertEquals(fourthFunction.get("functionName").getAsJsonObject().get("value").getAsString(),
                "fourthFunc");
        JsonObject fourthFunctionBody = fourthFunction.get("functionBody").getAsJsonObject();
        JsonArray fourthFunctionVEp = fourthFunctionBody.get("VisibleEndpoints").getAsJsonArray();
        Assert.assertTrue(fourthFunctionVEp.size() == 6);

        // Verify fourthFunc function visible endpoints
        Assert.assertEquals(fourthFunctionVEp.get(0).getAsJsonObject().get("name").getAsString(), "exEp0");

        JsonObject exEpOut = fourthFunctionVEp.get(1).getAsJsonObject();
        Assert.assertEquals(exEpOut.get("name").getAsString(), "exEpOut");
        Assert.assertEquals(exEpOut.get("typeName").getAsString(), "ExternalClient");
        Assert.assertEquals(exEpOut.get("orgName").getAsString(), "gayanOrg");
        Assert.assertEquals(exEpOut.get("packageName").getAsString(), "testEps");
        Assert.assertEquals(exEpOut.get("moduleName").getAsString(), "testEps");
        Assert.assertEquals(exEpOut.get("version").getAsString(), "0.1.0");
        Assert.assertEquals(exEpOut.get("isModuleVar").getAsBoolean(), false);
        Assert.assertEquals(exEpOut.get("isExternal").getAsBoolean(), true);
        JsonObject exEpOutPosition = exEpOut.get("position").getAsJsonObject();
        Assert.assertEquals(exEpOutPosition.get("startLine").getAsInt(), 72);
        Assert.assertEquals(exEpOutPosition.get("endLine").getAsInt(), 72);

        JsonObject exEpP2 = fourthFunctionVEp.get(2).getAsJsonObject();
        Assert.assertEquals(exEpP2.get("name").getAsString(), "exEpP2");
        Assert.assertEquals(exEpP2.get("typeName").getAsString(), "ExternalClient");
        Assert.assertEquals(exEpP2.get("orgName").getAsString(), "gayanOrg");
        Assert.assertEquals(exEpP2.get("packageName").getAsString(), "testEps");
        Assert.assertEquals(exEpP2.get("moduleName").getAsString(), "testEps");
        Assert.assertEquals(exEpP2.get("version").getAsString(), "0.1.0");
        Assert.assertEquals(exEpP2.get("isModuleVar").getAsBoolean(), false);
        Assert.assertEquals(exEpP2.get("isExternal").getAsBoolean(), true);
        Assert.assertEquals(exEpP2.get("isParameter").getAsBoolean(), true);
        JsonObject exEpP2Position = exEpP2.get("position").getAsJsonObject();
        Assert.assertEquals(exEpP2Position.get("startLine").getAsInt(), 62);
        Assert.assertEquals(exEpP2Position.get("endLine").getAsInt(), 62);

        JsonObject tempVar = fourthFunctionVEp.get(3).getAsJsonObject();
        Assert.assertEquals(tempVar.get("name").getAsString(), "temp");
        Assert.assertEquals(tempVar.get("typeName").getAsString(), "ExternalClient");
        Assert.assertEquals(tempVar.get("orgName").getAsString(), "gayanOrg");
        Assert.assertEquals(tempVar.get("packageName").getAsString(), "testEps");
        Assert.assertEquals(tempVar.get("moduleName").getAsString(), "testEps");
        Assert.assertEquals(tempVar.get("version").getAsString(), "0.1.0");
        Assert.assertEquals(tempVar.get("isModuleVar").getAsBoolean(), false);
        Assert.assertEquals(tempVar.get("isExternal").getAsBoolean(), false);
        JsonObject tempVarPosition = tempVar.get("position").getAsJsonObject();
        Assert.assertEquals(tempVarPosition.get("startLine").getAsInt(), 63);
        Assert.assertEquals(tempVarPosition.get("endLine").getAsInt(), 63);

        JsonObject exEp10 = fourthFunctionVEp.get(4).getAsJsonObject();
        Assert.assertEquals(exEp10.get("name").getAsString(), "exEp10");
        Assert.assertEquals(exEp10.get("typeName").getAsString(), "ExternalClient");
        Assert.assertEquals(exEp10.get("orgName").getAsString(), "gayanOrg");
        Assert.assertEquals(exEp10.get("packageName").getAsString(), "testEps");
        Assert.assertEquals(exEp10.get("moduleName").getAsString(), "testEps");
        Assert.assertEquals(exEp10.get("version").getAsString(), "0.1.0");
        Assert.assertEquals(exEp10.get("isModuleVar").getAsBoolean(), false);
        Assert.assertEquals(exEp10.get("isExternal").getAsBoolean(), false);
        JsonObject exEp10Position = exEp10.get("position").getAsJsonObject();
        Assert.assertEquals(exEp10Position.get("startLine").getAsInt(), 64);
        Assert.assertEquals(exEp10Position.get("endLine").getAsInt(), 64);

        JsonObject inEp1 = fourthFunctionVEp.get(5).getAsJsonObject();
        Assert.assertEquals(inEp1.get("name").getAsString(), "inEp1");
        Assert.assertEquals(inEp1.get("typeName").getAsString(), "InternalClient");
        Assert.assertEquals(inEp1.get("orgName").getAsString(), "gayanOrg");
        Assert.assertEquals(inEp1.get("packageName").getAsString(), "testEps");
        Assert.assertEquals(inEp1.get("moduleName").getAsString(), "testEps");
        Assert.assertEquals(inEp1.get("version").getAsString(), "0.1.0");
        Assert.assertEquals(inEp1.get("isModuleVar").getAsBoolean(), false);
        Assert.assertEquals(inEp1.get("isExternal").getAsBoolean(), false);
        JsonObject inEp1Position = inEp1.get("position").getAsJsonObject();
        Assert.assertEquals(inEp1Position.get("startLine").getAsInt(), 70);
        Assert.assertEquals(inEp1Position.get("endLine").getAsInt(), 70);

        // Verify service declaration
        Assert.assertEquals(members.get(6).getAsJsonObject().get("kind").getAsString(), "ServiceDeclaration");
        JsonObject serviceDec = members.get(6).getAsJsonObject();
        JsonArray serviceMembers = serviceDec.get("members").getAsJsonArray();
        Assert.assertTrue(serviceMembers.size() == 4);
        JsonArray serviceVEp = serviceDec.get("VisibleEndpoints").getAsJsonArray();
        Assert.assertTrue(serviceVEp.size() == 3);

        // Verify service declaration visible endpoints
        Assert.assertEquals(serviceVEp.get(0).getAsJsonObject().get("name").getAsString(), "exEp0");

        Assert.assertEquals(serviceVEp.get(1).getAsJsonObject().get("name").getAsString(), "exEpOut");

        JsonObject inEp2 = serviceVEp.get(2).getAsJsonObject();
        Assert.assertEquals(inEp2.get("name").getAsString(), "inEp2");
        Assert.assertEquals(inEp2.get("typeName").getAsString(), "InternalClient");
        Assert.assertEquals(inEp2.get("orgName").getAsString(), "gayanOrg");
        Assert.assertEquals(inEp2.get("packageName").getAsString(), "testEps");
        Assert.assertEquals(inEp2.get("moduleName").getAsString(), "testEps");
        Assert.assertEquals(inEp2.get("version").getAsString(), "0.1.0");
        Assert.assertEquals(inEp2.get("isModuleVar").getAsBoolean(), false);
        Assert.assertEquals(inEp2.get("isExternal").getAsBoolean(), true);
        Assert.assertEquals(inEp2.get("isClassField").getAsBoolean(), true);
        JsonObject inEp2Position = inEp2.get("position").getAsJsonObject();
        Assert.assertEquals(inEp2Position.get("startLine").getAsInt(), 78);
        Assert.assertEquals(inEp2Position.get("endLine").getAsInt(), 78);

        // Verify resource definitions
        JsonArray getResourceVEps = serviceMembers.get(2).getAsJsonObject().get("functionBody").getAsJsonObject()
                .get("VisibleEndpoints").getAsJsonArray();
        Assert.assertTrue(getResourceVEps.size() == 3);

        Assert.assertEquals(getResourceVEps.get(0).getAsJsonObject().get("name").getAsString(), "exEp0");
        Assert.assertEquals(getResourceVEps.get(1).getAsJsonObject().get("name").getAsString(), "exEpOut");
        Assert.assertEquals(getResourceVEps.get(2).getAsJsonObject().get("name").getAsString(), "inEp2");

        JsonArray postResourceVEps = serviceMembers.get(3).getAsJsonObject().get("functionBody").getAsJsonObject()
                .get("VisibleEndpoints").getAsJsonArray();
        Assert.assertTrue(postResourceVEps.size() == 4);

        Assert.assertEquals(postResourceVEps.get(0).getAsJsonObject().get("name").getAsString(), "exEp0");
        Assert.assertEquals(postResourceVEps.get(1).getAsJsonObject().get("name").getAsString(), "exEpOut");
        Assert.assertEquals(postResourceVEps.get(2).getAsJsonObject().get("name").getAsString(), "inEp2");

        JsonObject exEp11 = postResourceVEps.get(3).getAsJsonObject();
        Assert.assertEquals(exEp11.get("name").getAsString(), "exEp11");
        Assert.assertEquals(exEp11.get("typeName").getAsString(), "ExternalClient");
        Assert.assertEquals(exEp11.get("orgName").getAsString(), "gayanOrg");
        Assert.assertEquals(exEp11.get("packageName").getAsString(), "testEps");
        Assert.assertEquals(exEp11.get("moduleName").getAsString(), "testEps");
        Assert.assertEquals(exEp11.get("version").getAsString(), "0.1.0");
        Assert.assertEquals(exEp11.get("isModuleVar").getAsBoolean(), false);
        Assert.assertEquals(exEp11.get("isExternal").getAsBoolean(), false);
        JsonObject exEp11Position = exEp11.get("position").getAsJsonObject();
        Assert.assertEquals(exEp11Position.get("startLine").getAsInt(), 90);
        Assert.assertEquals(exEp11Position.get("endLine").getAsInt(), 90);

    }
}
