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
import org.apache.commons.lang3.tuple.Pair;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Test performance of completions feature in language server.
 */
public class CompletionPerformanceTest {

    private Endpoint serviceEndpoint;
    private final Path testRoot = FileUtils.RES_DIR.resolve("performance");
    private final String configDir = "config";
    private final JsonParser parser = new JsonParser();
    private final Gson gson = new Gson();

    @BeforeClass
    public void init() {
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    @Test(dataProvider = "performance-data-provider")
    public void testCompletion(String config, String configPath) throws WorkspaceDocumentException, IOException {
        String configJsonPath = "performance" + File.separator + configPath
                + File.separator + configDir + File.separator + config;
        JsonObject configJsonObject = FileUtils.fileContentAsObject(configJsonPath);

        long actualResponseTime = getResponseCompletion(configJsonObject).getLeft();
        int expectedResponseTime = Integer.parseInt(System.getProperty("responseTimeThreshold"));
        Assert.assertTrue(actualResponseTime < expectedResponseTime,
                String.format("Expected response time = %d, received %d.", expectedResponseTime, actualResponseTime));
        String response = getResponseCompletion(configJsonObject).getRight();
        JsonObject json = parser.parse(response).getAsJsonObject();
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

    Pair<Long, String> getResponseCompletion(JsonObject configJsonObject) throws IOException {
        Path sourcePath = testRoot.resolve(configJsonObject.get("source").getAsString());
        String responseString;
        Position position = new Position();
        JsonObject positionObj = configJsonObject.get("position").getAsJsonObject();
        position.setLine(positionObj.get("line").getAsInt());
        position.setCharacter(positionObj.get("character").getAsInt());
        JsonElement triggerCharElement = configJsonObject.get("triggerCharacter");
        String triggerChar = triggerCharElement == null ? "" : triggerCharElement.getAsString();

        TestUtil.openDocument(serviceEndpoint, sourcePath);
        long start = System.currentTimeMillis();
        responseString = TestUtil.getCompletionResponse(sourcePath.toString(), position,
                this.serviceEndpoint, triggerChar);
        long end = System.currentTimeMillis();
        TestUtil.closeDocument(serviceEndpoint, sourcePath);
        long responseTime = end - start;
        return Pair.of(responseTime, responseString);
    }

    List<CompletionItem> getExpectedList(JsonObject configJsonObject) {
        JsonArray expectedItems = configJsonObject.get("items").getAsJsonArray();
        return CompletionTestUtil.getExpectedItemList(expectedItems);
    }

    @DataProvider(name = "performance-data-provider")
    public Object[][] dataProvider() {
        return this.getConfigsList();
    }

    public Object[][] testSubset() {
        return new Object[0][];
    }

    public List<String> skipList() {
        return new ArrayList<>();
    }

    public String getTestResourceDir() {
        return "performance_completion";
    }

    @AfterClass
    public void cleanupLanguageServer() {
        TestUtil.shutdownLanguageServer(this.serviceEndpoint);
    }

    protected Object[][] getConfigsList() {
        if (this.testSubset().length != 0) {
            return this.testSubset();
        }
        List<String> skippedTests = this.skipList();
        try {
            return Files.walk(this.testRoot.resolve(this.getTestResourceDir()).resolve(this.configDir))
                    .filter(path -> {
                        File file = path.toFile();
                        return file.isFile() && file.getName().endsWith(".json")
                                && !skippedTests.contains(file.getName());
                    })
                    .map(path -> new Object[]{path.toFile().getName(), this.getTestResourceDir()})
                    .toArray(size -> new Object[size][2]);
        } catch (IOException e) {
            // If failed to load tests, then it's a failure
            Assert.fail("Unable to load test config", e);
            return new Object[0][];
        }
    }
}
