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
package org.ballerinalang.langserver.performance;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.completion.util.CompletionTestUtil;
import org.ballerinalang.langserver.util.FileUtils;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.List;

/**
 * Test performance of completions feature in language server.
 */
public class CompletionPerformanceTest {

    private Endpoint serviceEndpoint;
    private final Path testRoot = FileUtils.RES_DIR.resolve("performance");
    private final JsonParser parser = new JsonParser();
    private final Gson gson = new Gson();

    @BeforeClass
    public void init() {
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    @Test(dataProvider = "performance-data-provider")
    public void testCompletion(String config) throws WorkspaceDocumentException, IOException {
        String configJsonPath = getConfigJsonPath(config);
        JsonObject configJsonObject = FileUtils.fileContentAsObject(configJsonPath);
        Path sourcePath = testRoot.resolve(configJsonObject.get("source").getAsString());

        Position position = new Position();
        JsonObject positionObj = configJsonObject.get("position").getAsJsonObject();
        position.setLine(positionObj.get("line").getAsInt());
        position.setCharacter(positionObj.get("character").getAsInt());
        JsonElement triggerCharElement = configJsonObject.get("triggerCharacter");
        String triggerChar = triggerCharElement == null ? "" : triggerCharElement.getAsString();
        TestUtil.openDocument(serviceEndpoint, sourcePath);

        long start = System.currentTimeMillis();
        String responseString = TestUtil.getCompletionResponse(sourcePath.toString(), position,
                this.serviceEndpoint, triggerChar);
        long end = System.currentTimeMillis();
        TestUtil.closeDocument(serviceEndpoint, sourcePath);
        long actualResponseTime = end - start;
        int expectedResponseTime = Integer.parseInt(System.getProperty("responseTimeThreshold"));
        Assert.assertTrue(actualResponseTime < expectedResponseTime,
                String.format("Expected response time = %d, received %d.", expectedResponseTime, actualResponseTime));

        JsonObject json = parser.parse(responseString).getAsJsonObject();
        Type collectionType = new TypeToken<List<CompletionItem>>() {
        }.getType();
        JsonArray resultList = json.getAsJsonObject("result").getAsJsonArray("left");
        List<CompletionItem> responseItemList = gson.fromJson(resultList, collectionType);
        List<CompletionItem> expectedList = getExpectedList(configJsonObject);
        boolean result = CompletionTestUtil.isSubList(expectedList, responseItemList);
        if (!result) {
            Assert.fail("Failed Test for: " + configJsonPath);
        }
    }

    List<CompletionItem> getExpectedList(JsonObject configJsonObject) {
        JsonArray expectedItems = configJsonObject.get("items").getAsJsonArray();
        return CompletionTestUtil.getExpectedItemList(expectedItems);
    }

    @DataProvider(name = "performance-data-provider")
    public Object[][] dataProvider() {
        return new Object[][]{
                {"performance_completion.json"},
        };
    }

    @AfterClass
    public void cleanupLanguageServer() {
        TestUtil.shutdownLanguageServer(this.serviceEndpoint);
    }

    public String getConfigJsonPath(String configFilePath) {
        return "performance" + File.separator + "configs" + File.separator + getResourceDir() + File.separator
                + configFilePath;
    }

    public String getResourceDir() {
        return "performance_completion";
    }
}
