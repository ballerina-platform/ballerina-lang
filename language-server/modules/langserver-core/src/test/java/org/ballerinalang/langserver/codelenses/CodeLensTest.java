/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.codelenses;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.ballerinalang.langserver.compiler.LSCompilerCache;
import org.ballerinalang.langserver.compiler.LSContextManager;
import org.ballerinalang.langserver.util.FileUtils;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Test code lens feature in language server.
 */
public class CodeLensTest {
    private Path servicesBalPath = FileUtils.RES_DIR.resolve("codelens").resolve("services.bal");
    private Path functionsBalPath = FileUtils.RES_DIR.resolve("codelens").resolve("functions.bal");
    private Endpoint serviceEndpoint;
    private static final JsonParser JSON_PARSER = new JsonParser();
    private static final Logger log = LoggerFactory.getLogger(CodeLensTest.class);
    private static final Gson GSON = new Gson();

    @BeforeClass
    public void loadLangServer() throws IOException {
        serviceEndpoint = TestUtil.initializeLanguageSever();
        TestUtil.openDocument(serviceEndpoint, servicesBalPath);
        TestUtil.openDocument(serviceEndpoint, functionsBalPath);
    }

    @BeforeMethod
    public void clearCache() {
        LSCompilerCache.getInstance().clearAll();
        LSContextManager.getInstance().clearAllContexts();
    }

    @Test(description = "Test Code Lenses for functions", dataProvider = "codeLensFunctionPositions")
    public void codeLensForBuiltInFunctionTest(String expectedConfigName) throws IOException {
        String response = TestUtil.getCodeLensesResponse(functionsBalPath.toString(), serviceEndpoint);
        testCodeLenses(expectedConfigName, response);
    }

    @Test(description = "Test Code Lenses for services", dataProvider = "codeLensServicesPositions")
    public void codeLensForCurrentPackageFunctionTest(String expectedConfigName) throws IOException {
        String response = TestUtil.getCodeLensesResponse(servicesBalPath.toString(), serviceEndpoint);
        testCodeLenses(expectedConfigName, response);
    }
    
    private void testCodeLenses(String expectedConfigName, String response) throws IOException {
        String expected = getExpectedValue(expectedConfigName);

        List<CodeLens> expectedItemList = getFlattenItemList(
                JSON_PARSER.parse(expected).getAsJsonObject().getAsJsonArray("result"));
        List<CodeLens> responseItemList = getFlattenItemList(
                JSON_PARSER.parse(response).getAsJsonObject().getAsJsonArray("result"));

        boolean isSubList = getStringListForEvaluation(responseItemList).containsAll(
                getStringListForEvaluation(expectedItemList));
        Assert.assertTrue(isSubList, "Did not match the codelens content for " + expectedConfigName);
    }

    @AfterClass
    public void shutDownLanguageServer() {
        TestUtil.closeDocument(this.serviceEndpoint, servicesBalPath);
        TestUtil.closeDocument(this.serviceEndpoint, functionsBalPath);
        TestUtil.shutdownLanguageServer(this.serviceEndpoint);
    }

    @DataProvider(name = "codeLensFunctionPositions")
    public Object[][] getCodeLensFunctionPositions() {
        log.info("Test textDocument/codeLens for functions");
        return new Object[][]{
                {"functions.json"},
        };
    }

    @DataProvider(name = "codeLensServicesPositions")
    public Object[][] getCodeLensServicesPositions() {
        log.info("Test textDocument/codeLens for services");
        return new Object[][]{
                {"services.json"}
        };
    }

    /**
     * Get the expected value from the expected file.
     *
     * @param expectedFile json file which contains expected content.
     * @return string content read from the json file.
     */
    private String getExpectedValue(String expectedFile) throws IOException {
        Path expectedFilePath = FileUtils.RES_DIR.resolve("codelens").resolve("expected").resolve(expectedFile);
        byte[] expectedByte = Files.readAllBytes(expectedFilePath);
        return new String(expectedByte);
    }

    private static List<CodeLens> getFlattenItemList(JsonArray expectedItems) {
        List<CodeLens> flattenList = new ArrayList<>();
        expectedItems.forEach(jsonElement -> {
            CodeLens codeLens = GSON.fromJson(jsonElement, CodeLens.class);
            flattenList.add(codeLens);
        });

        return flattenList;
    }

    private static String getCompletionItemPropertyString(CodeLens completionItem) {
        String additionalTextEdits = "";
        if (completionItem.getRange() != null) {
            additionalTextEdits = "," + GSON.toJson(completionItem.getRange());
        }

        Command command = completionItem.getCommand();
        if (command != null) {
            additionalTextEdits = "," + command.getTitle() + ", " + command.getCommand();
        }

        return ("{" + additionalTextEdits + "}");
    }

    private static List<String> getStringListForEvaluation(List<CodeLens> completionItems) {
        List<String> evalList = new ArrayList<>();
        completionItems.forEach(completionItem -> evalList.add(getCompletionItemPropertyString(completionItem)));
        return evalList;
    }
}
