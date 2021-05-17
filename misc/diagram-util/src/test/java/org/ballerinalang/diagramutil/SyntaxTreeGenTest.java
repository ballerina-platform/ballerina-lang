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

import com.google.gson.JsonElement;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.PackageCompilation;
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
    private final Path mainHttpFile = TestUtil.RES_DIR.resolve("mainHttpCall")
            .resolve("mainHttpCall.bal");

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

    @Test(description = "Generate ST for http invocation in a main bal file.")
    public void testHttpMainBalST() throws IOException {
        Path inputFile = TestUtil.createTempFile(mainHttpFile);
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
        Assert.assertTrue(stJson.getAsJsonObject().get("members").getAsJsonArray().size() > 0);
    }
}
