/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.signature;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.util.FileUtils;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Test class for Signature help.
 *
 * @since 0.982.0
 */
public class SignatureHelpTest {
    
    private Endpoint serviceEndpoint;

    private JsonParser parser = new JsonParser();
    
    private Path sourcesPath = new File(getClass().getClassLoader().getResource("signature").getFile()).toPath();

    private static final Logger log = LoggerFactory.getLogger(SignatureHelpTest.class);

    @BeforeClass
    public void init() throws Exception {
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    @Test(dataProvider = "signature-help-data-provider", description = "Test Signature Help")
    public void test(String config, String source)
            throws WorkspaceDocumentException, IOException, InterruptedException {

        String configJsonPath = "signature" + File.separator + config;
        Path sourcePath = sourcesPath.resolve("source").resolve(source);
        JsonObject configJsonObject = FileUtils.fileContentAsObject(configJsonPath);
        JsonObject expected = configJsonObject.get("expected").getAsJsonObject();
        expected.remove("id");
        String response = this.getSignatureResponse(configJsonObject, sourcePath).replace("\\r\\n", "\\n");
        JsonObject responseJson = parser.parse(response).getAsJsonObject();
        responseJson.remove("id");
        boolean result = expected.equals(responseJson);
        if (!result) {
            Assert.fail("Failed Test for: " + configJsonPath);
        }
    }
    
    @DataProvider(name = "signature-help-data-provider")
    public Object[][] dataProvider() {
        log.info("Test textDocument/signatureHelp");
        return new Object[][] {
                {"functionInSameFile.json", "functionInSameFile.bal"},
                {"functionInSameFile2.json", "functionInSameFile2.bal"},
                {"functionInSameFileWithoutDocumentation.json", "functionInSameFileWithoutDocumentation.bal"},
                {"typeAttachedFunctions.json", "typeAttachedFunctions.bal"},
                {"functionInBuiltinPackage.json", "functionInBuiltinPackage.bal"},
                {"endpointActions.json", "endpointActions.bal"},
                {"signatureWithinResources.json", "signatureWithinResources.bal"},
                {"signatureWithinIfElse1.json", "signatureWithinIfElse1.bal"},
                {"signatureWithinIfElse2.json", "signatureWithinIfElse2.bal"},
                {"signatureWithinIfElse3.json", "signatureWithinIfElse3.bal"},
                {"signatureWithinIfElse1.json", "signatureWithinIfElse4.bal"},
                {"signatureWithinIfElse1.json", "signatureWithinWhile.bal"},
                {"signatureWithinForeach.json", "signatureWithinForeach.bal"},
                {"signatureWithinTransaction1.json", "signatureWithinTransaction1.bal"},
                {"signatureWithinObjectFunctions.json", "signatureWithinObjectFunctions.bal"},
                {"signatureWithinCheckPanic.json", "signatureWithinCheckPanic.bal"},
//                {"signatureWithinAnnotation.json", "signatureWithinAnnotations.bal"}
        };
    }

    private String getSignatureResponse(JsonObject config, Path sourcePath)
            throws InterruptedException, IOException {
        JsonObject positionObj = config.get("position").getAsJsonObject();
        Position position = new Position();
        position.setLine(positionObj.get("line").getAsInt());
        position.setCharacter(positionObj.get("character").getAsInt());
        TestUtil.openDocument(this.serviceEndpoint, sourcePath);
        String resp = TestUtil.getSignatureHelpResponse(sourcePath.toString(), position, this.serviceEndpoint);
        TestUtil.closeDocument(this.serviceEndpoint, sourcePath);
        return resp;
    }
    
    @AfterClass
    public void cleanupLanguageServer() {
        TestUtil.shutdownLanguageServer(this.serviceEndpoint);
    }
}
