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
    private final Path endpointOrder = TestUtil.RES_DIR.resolve("endpointOrder");
    private final Path classEndpoint = TestUtil.RES_DIR.resolve("classEndpoint");
    private final Path annotatedEndpoint = TestUtil.RES_DIR.resolve("annotatedEndpoint");
    private final Path regexTestFile = TestUtil.RES_DIR.resolve("regexTest").resolve("main.bal");

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
        Assert.assertTrue(stJson.getAsJsonObject().get("members").getAsJsonArray().size() == 10);
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
        checkClientVisibleEndpoints(exEp0, "exEp0", "ExternalClient", "gayanOrg", "testEps",
                "testEps", "0.1.0", 14, 14, true, true);

        JsonObject exEp1 = mainFunctionVEp.get(1).getAsJsonObject();
        checkClientVisibleEndpoints(exEp1, "exEp1", "ExternalClient", "gayanOrg", "testEps",
                "testEps", "0.1.0", 17, 17, false, false);

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
        checkClientVisibleEndpoints(exEp2, "exEp2", "ExternalClient", "gayanOrg", "testEps",
                "testEps", "0.1.0", 22, 22, false, false);

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
        checkClientVisibleEndpoints(exEp3, "exEp3", "ExternalClient", "gayanOrg", "testEps",
                "testEps", "0.1.0", 26, 26, false, false);

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
        checkClientVisibleEndpoints(exEpP1, "exEpP1", "ExternalClient", "gayanOrg", "testEps",
                "testEps", "0.1.0", 31, 31, false, true);

        JsonObject exEp6 = secondFunctionVEp.get(2).getAsJsonObject();
        checkClientVisibleEndpoints(exEp6, "exEp6", "ExternalClient", "gayanOrg", "testEps",
                "testEps", "0.1.0", 41, 41, false, false);

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
        checkClientVisibleEndpoints(exEp4, "exEp4", "ExternalClient", "gayanOrg", "testEps",
                "testEps", "0.1.0", 34, 34, false, false);

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
        checkClientVisibleEndpoints(exEp5, "exEp5", "ExternalClient", "gayanOrg", "testEps",
                "testEps", "0.1.0", 38, 38, false, false);

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
        checkClientVisibleEndpoints(exEp7, "exEp7", "ExternalClient", "gayanOrg", "testEps",
                "testEps", "0.1.0", 46, 46, false, false);

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
        checkClientVisibleEndpoints(exEp8, "exEp8", "ExternalClient", "gayanOrg", "testEps",
                "testEps", "0.1.0", 53, 53, false, false);

        JsonObject exEp62 = thirdFunctionVEp.get(2).getAsJsonObject();
        checkClientVisibleEndpoints(exEp62, "exEp6", "ExternalClient", "gayanOrg", "testEps",
                "testEps", "0.1.0", 57, 57, false, false);

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
        checkClientVisibleEndpoints(exEpOut, "exEpOut", "ExternalClient", "gayanOrg", "testEps",
                "testEps", "0.1.0", 72, 72, false, true);

        JsonObject exEpP2 = fourthFunctionVEp.get(2).getAsJsonObject();
        checkClientVisibleEndpoints(exEpP2, "exEpP2", "ExternalClient", "gayanOrg", "testEps",
                "testEps", "0.1.0", 62, 62, false, true, false, true);

        JsonObject tempVar = fourthFunctionVEp.get(3).getAsJsonObject();
        checkClientVisibleEndpoints(tempVar, "temp", "ExternalClient", "gayanOrg", "testEps",
                "testEps", "0.1.0", 63, 63, false, false);


        JsonObject exEp10 = fourthFunctionVEp.get(4).getAsJsonObject();
        checkClientVisibleEndpoints(exEp10, "exEp10", "ExternalClient", "gayanOrg", "testEps",
                "testEps", "0.1.0", 64, 64, false, false);

        JsonObject inEp1 = fourthFunctionVEp.get(5).getAsJsonObject();
        checkClientVisibleEndpoints(inEp1, "inEp1", "InternalClient", "gayanOrg", "testEps",
                "testEps", "0.1.0", 70, 70, false, false);

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
        checkClientVisibleEndpoints(inEp2, "inEp2", "InternalClient", "gayanOrg", "testEps",
                "testEps", "0.1.0", 78, 78, false, true, true, false);

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
        checkClientVisibleEndpoints(exEp11, "exEp11", "ExternalClient", "gayanOrg", "testEps",
                "testEps", "0.1.0", 90, 90, false, false);

        // Verify /abc service declaration
        Assert.assertEquals(members.get(9).getAsJsonObject().get("kind").getAsString(), "ServiceDeclaration");
        JsonObject abcServiceDec = members.get(9).getAsJsonObject();
        JsonArray abcServiceMembers = abcServiceDec.get("members").getAsJsonArray();
        Assert.assertTrue(abcServiceMembers.size() == 3);
        JsonArray abcServiceVEp = abcServiceDec.get("VisibleEndpoints").getAsJsonArray();
        Assert.assertTrue(serviceVEp.size() == 3);

        Assert.assertEquals(abcServiceVEp.get(0).getAsJsonObject().get("name").getAsString(), "exEp0");
        Assert.assertEquals(abcServiceVEp.get(1).getAsJsonObject().get("name").getAsString(), "exEpOut");
        Assert.assertEquals(abcServiceVEp.get(2).getAsJsonObject().get("name").getAsString(), "inEp3");

        JsonArray abcGetResourceVEps = abcServiceMembers.get(2).getAsJsonObject().get("functionBody").getAsJsonObject()
                .get("VisibleEndpoints").getAsJsonArray();

        Assert.assertEquals(abcGetResourceVEps.get(0).getAsJsonObject().get("name").getAsString(), "exEp0");
        Assert.assertEquals(abcGetResourceVEps.get(1).getAsJsonObject().get("name").getAsString(), "exEpOut");

        JsonObject inEp3 = abcGetResourceVEps.get(2).getAsJsonObject();
        checkClientVisibleEndpoints(inEp3, "inEp3", "InternalClient", "gayanOrg", "testEps",
                "testEps", "0.1.0", 110, 114, false, true, true, false);
    }

    @Test(description = "Test visible endpoint defined after the invocations", enabled = false)
    public void testVisibleEndpointOrder() throws IOException {
        Path inputFile = TestUtil.createTempProject(endpointOrder);

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
        JsonArray members = stJson.getAsJsonObject().get("members").getAsJsonArray();

        // Verify user service
        JsonObject userService = members.get(1).getAsJsonObject();
        JsonObject userGetFunction = userService.get("members").getAsJsonArray().get(2).getAsJsonObject();
        JsonArray userGetFunctionVEp = userGetFunction.get("functionBody").getAsJsonObject().get("VisibleEndpoints")
                .getAsJsonArray();
        Assert.assertEquals(userGetFunctionVEp.size(), 7);

        // Verify user service > get resource > visible endpoints
        checkClientVisibleEndpoints(userGetFunctionVEp.get(0).getAsJsonObject(), "httpEpM0", "Client", "ballerina",
                "http", "http", "2.8.0", 2, 2, true, true);
        checkClientVisibleEndpoints(userGetFunctionVEp.get(1).getAsJsonObject(), "httpEpM1", "Client", "ballerina",
                "http", "http", "2.8.0", 34, 37, true, true);
        checkClientVisibleEndpoints(userGetFunctionVEp.get(2).getAsJsonObject(), "httpEpM2", "Client", "ballerina",
                "http", "http", "2.8.0", 79, 79, true, true);
        checkClientVisibleEndpoints(userGetFunctionVEp.get(3).getAsJsonObject(), "httpEpS10", "Client", "ballerina",
                "http", "http", "2.8.0", 6, 6, false, true, true, false);
        checkClientVisibleEndpoints(userGetFunctionVEp.get(4).getAsJsonObject(), "httpEpS11", "Client", "ballerina",
                "http", "http", "2.8.0", 18, 18, false, true, true, false);
        checkClientVisibleEndpoints(userGetFunctionVEp.get(5).getAsJsonObject(), "httpEpS12", "Client", "ballerina",
                "http", "http", "2.8.0", 27, 27, false, true, true, false);
        checkClientVisibleEndpoints(userGetFunctionVEp.get(6).getAsJsonObject(), "httpEpL0", "Client", "ballerina",
                "http", "http", "2.8.0", 15, 15, false, false);

        JsonObject userPostFunction = userService.get("members").getAsJsonArray().get(4).getAsJsonObject();
        JsonArray userPostFunctionVEp = userPostFunction.get("functionBody").getAsJsonObject().get("VisibleEndpoints")
                .getAsJsonArray();
        Assert.assertEquals(userPostFunctionVEp.size(), 7);

        checkClientVisibleEndpoints(userPostFunctionVEp.get(6).getAsJsonObject(), "httpEpL1", "Client", "ballerina",
                "http", "http", "2.8.0", 21, 24, false, false);

        // Verify main function > visible endpoints
        JsonObject mainFunction = members.get(2).getAsJsonObject();
        JsonArray mainFunctionVEp = mainFunction.get("functionBody").getAsJsonObject().get("VisibleEndpoints")
                .getAsJsonArray();
        Assert.assertEquals(mainFunctionVEp.size(), 3);

        // Verify product service > get resource > visible endpoints
        JsonObject productService = members.get(4).getAsJsonObject();
        JsonObject productGetFunction = productService.get("members").getAsJsonArray().get(2).getAsJsonObject();
        JsonArray productGetFunctionVEp = productGetFunction.get("functionBody").getAsJsonObject()
                .get("VisibleEndpoints").getAsJsonArray();
        Assert.assertEquals(productGetFunctionVEp.size(), 5);

        // Verify user class > getUser function > visible endpoints
        JsonObject userClass = members.get(6).getAsJsonObject();
        JsonObject getUserFunction = userClass.get("members").getAsJsonArray().get(3).getAsJsonObject();
        JsonArray getUserFunctionVEp = getUserFunction.get("functionBody").getAsJsonObject().get("VisibleEndpoints")
                .getAsJsonArray();
        Assert.assertEquals(getUserFunctionVEp.size(), 7);

        checkClientVisibleEndpoints(getUserFunctionVEp.get(5).getAsJsonObject(), "httpEpC2", "Client", "ballerina",
                "http", "http", "2.8.0", 74, 74, false, true, true, false);
    }

    @Test(description = "Test syntax tree generation works with Regex syntax")
    public void testRegexSyntax() throws IOException {
        Path inputFile = TestUtil.createTempFile(regexTestFile);

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
        // Check syntax tree JSON
        Assert.assertTrue(stJson.isJsonObject());

        // Test regex statement in the syntax tree JSON
        Assert.assertEquals(stJson.getAsJsonObject().get("kind").getAsString(), "ModulePart");
        JsonArray members = stJson.getAsJsonObject().get("members").getAsJsonArray();
        JsonObject mainFunction = members.get(0).getAsJsonObject();
        JsonObject regexStmt = mainFunction.get("functionBody").getAsJsonObject().get("statements").getAsJsonArray()
                .get(0).getAsJsonObject();
        String kind = regexStmt.get("initializer").getAsJsonObject().get("kind").getAsString();
        Assert.assertEquals(kind, "RegexTemplateExpression");
    }


    @Test(description = "Test visible endpoint defined in Class/Service level")
    public void testClassLevelVisibleEndpoint() throws IOException {
        Path inputFile = TestUtil.createTempProject(classEndpoint);

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
        JsonArray members = stJson.getAsJsonObject().get("members").getAsJsonArray();

        // Verify user service
        JsonObject userService = members.get(0).getAsJsonObject();
        JsonObject userPostFunction = userService.get("members").getAsJsonArray().get(2).getAsJsonObject();
        JsonArray userPostFunctionVEp = userPostFunction.get("functionBody").getAsJsonObject().get("VisibleEndpoints")
                .getAsJsonArray();
        Assert.assertEquals(userPostFunctionVEp.size(), 1);
        checkClientVisibleEndpoints(userPostFunctionVEp.get(0).getAsJsonObject(), "httpEpS10", "Client", "ballerina",
                "http", "http", "2.8.0", 4, 4, false, true, true, false);
    }

    @Test(description = "Test visible endpoint with annotations and access modifiers")
    public void testAnnotatedVisibleEndpoint() throws IOException {
        Path inputFile = TestUtil.createTempProject(annotatedEndpoint);

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
        JsonArray members = stJson.getAsJsonObject().get("members").getAsJsonArray();

        // Verify user service
        JsonObject userService = members.get(0).getAsJsonObject();
        JsonObject userPostFunction = userService.get("members").getAsJsonArray().get(5).getAsJsonObject();
        JsonArray userPostFunctionVEp = userPostFunction.get("functionBody").getAsJsonObject().get("VisibleEndpoints")
                .getAsJsonArray();
        Assert.assertEquals(userPostFunctionVEp.size(), 5);
        checkClientVisibleEndpoints(userPostFunctionVEp.get(0).getAsJsonObject(), "httpEp", "Client", "ballerina",
                "http", "http", "2.8.0", 4, 4, false, true, true, false);
        checkClientVisibleEndpoints(userPostFunctionVEp.get(1).getAsJsonObject(), "httpEpPvt", "Client", "ballerina",
                "http", "http", "2.8.0", 6, 6, false, true, true, false);
        checkClientVisibleEndpoints(userPostFunctionVEp.get(2).getAsJsonObject(), "httpEpAnt", "Client", "ballerina",
                "http", "http", "2.8.0", 8, 11, false, true, true, false);
        checkClientVisibleEndpoints(userPostFunctionVEp.get(3).getAsJsonObject(), "httpEpAntPvt", "Client", "ballerina",
                "http", "http", "2.8.0", 13, 16, false, true, true, false);
    }

    private void checkClientVisibleEndpoints(JsonObject ep, String varName, String typeName, String orgName,
                                             String packageName, String moduleName, String version, int startLine,
                                             int endLine, boolean isModuleVar, boolean isExternal, boolean isClassField,
                                             boolean isParameter) {

        Assert.assertEquals(ep.get("name").getAsString(), varName);
        Assert.assertEquals(ep.get("typeName").getAsString(), typeName);

        Assert.assertEquals(ep.get("orgName").getAsString(), orgName);
        Assert.assertEquals(ep.get("packageName").getAsString(), packageName);
        Assert.assertEquals(ep.get("moduleName").getAsString(), moduleName);
        Assert.assertTrue(version.matches("^[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}$"));

        Assert.assertEquals(ep.get("isModuleVar").getAsBoolean(), isModuleVar);
        Assert.assertEquals(ep.get("isExternal").getAsBoolean(), isExternal);
        Assert.assertFalse(ep.get("isCaller").getAsBoolean()); // TODO: Update test cases to check this field
        if (isClassField) {
            Assert.assertTrue(ep.get("isClassField").getAsBoolean());
        }
        if (isParameter) {
            Assert.assertTrue(ep.get("isParameter").getAsBoolean());
        }

        JsonObject epPosition = ep.get("position").getAsJsonObject();
        Assert.assertEquals(epPosition.get("startLine").getAsInt(), startLine);
        Assert.assertEquals(epPosition.get("endLine").getAsInt(), endLine);
    }

    private void checkClientVisibleEndpoints(JsonObject ep, String varName, String typeName, String orgName,
                                             String packageName, String moduleName, String version, int startLine,
                                             int endLine, boolean isModuleVar, boolean isExternal) {

        checkClientVisibleEndpoints(ep, varName, typeName, orgName, packageName,
                moduleName, version, startLine, endLine, isModuleVar, isExternal, false, false);
    }
}
