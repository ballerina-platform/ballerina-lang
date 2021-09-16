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
    private final Path endpointDefinedOutside = TestUtil.RES_DIR.resolve("endpointDefinedOutside");

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
            } else {
                Assert.fail("Additional endpoint has been found");
            }
        });
    }

    @Test(description = "Generate ST for client outside a main bal file.")
    public void testClientOutside() throws IOException {
        Path inputFile = TestUtil.createTempProject(endpointDefinedOutside);
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
        JsonObject function = members.get(1).getAsJsonObject();
        JsonObject functionBody = function.get("functionBody").getAsJsonObject();
        JsonArray visibleEndpoints = functionBody.get("VisibleEndpoints").getAsJsonArray();
        Assert.assertEquals(visibleEndpoints.size(), 1);
        visibleEndpoints.forEach(jsonElement -> {
            JsonObject endpoint = jsonElement.getAsJsonObject();
            if (endpoint.get("name").getAsString().equals("myClient")) {
                Assert.assertTrue(true);
            } else {
                Assert.fail("Additional endpoint has been found");
            }
        });
    }
}
