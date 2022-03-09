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

        // Validate local endpoint in visible endpoints
        JsonObject localEndpoint = visibleEndpoints.get(0).getAsJsonObject();
        Assert.assertEquals(localEndpoint.get("typeName").getAsString(), "MyMainClient");
        Assert.assertEquals(localEndpoint.get("name").getAsString(), "clientEndpoint");
        Assert.assertEquals(localEndpoint.get("orgName").getAsString(), "marcus");
        Assert.assertEquals(localEndpoint.get("packageName").getAsString(), "test");
        Assert.assertEquals(localEndpoint.get("moduleName").getAsString(), "test");
        Assert.assertEquals(localEndpoint.get("version").getAsString(), "0.1.0");
        Assert.assertEquals(localEndpoint.get("isModuleVar").getAsString(), "false");

        // Validate external endpoint in visible endpoints
        JsonObject moduleEndpoint = visibleEndpoints.get(2).getAsJsonObject();
        Assert.assertEquals(moduleEndpoint.get("typeName").getAsString(), "MyMainClient");
        Assert.assertEquals(moduleEndpoint.get("name").getAsString(), "myClient");
        Assert.assertEquals(moduleEndpoint.get("orgName").getAsString(), "marcus");
        Assert.assertEquals(moduleEndpoint.get("packageName").getAsString(), "test");
        Assert.assertEquals(moduleEndpoint.get("moduleName").getAsString(), "test");
        Assert.assertEquals(moduleEndpoint.get("version").getAsString(), "0.1.0");
        Assert.assertEquals(moduleEndpoint.get("isModuleVar").getAsString(), "true");
        Assert.assertEquals(moduleEndpoint.get("isExternal").getAsString(), "true");

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
}
