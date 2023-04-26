/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.completion;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.ballerinalang.langserver.AbstractLSTest;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.completion.util.CompletionTestUtil;
import org.ballerinalang.langserver.completions.providers.context.util.ServiceTemplateGenerator;
import org.ballerinalang.langserver.util.FileUtils;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Completion Test Interface.
 */
public abstract class CompletionTest extends AbstractLSTest {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractLSTest.class);

    private final Path testRoot = FileUtils.RES_DIR.resolve("completion");

    private final String configDir = "config";

    private final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    @Test(dataProvider = "completion-data-provider")
    public void test(String config, String configPath) throws WorkspaceDocumentException, IOException {
        Path configJsonPath = FileUtils.RES_DIR.resolve("completion")
                .resolve(configPath).resolve(configDir).resolve(config);
        TestConfig testConfig = gson.fromJson(Files.newBufferedReader(configJsonPath), TestConfig.class);
        String response = getResponse(testConfig);
        JsonObject json = JsonParser.parseString(response).getAsJsonObject();
        Type collectionType = new TypeToken<List<CompletionItem>>() {
        }.getType();
        JsonArray resultList = json.getAsJsonObject("result").getAsJsonArray("left");
        List<CompletionItem> responseItemList = gson.fromJson(resultList, collectionType);

        boolean result = CompletionTestUtil.isSubList(testConfig.getItems(), responseItemList);
        if (!result) {
            // Fix test cases replacing expected using responses
//            updateConfig(configJsonPath, testConfig, responseItemList);
            List<CompletionItem> mismatchedList1 = responseItemList.stream()
                    .filter(item -> !testConfig.getItems().contains(item)).collect(Collectors.toList());
            List<CompletionItem> mismatchedList2 = testConfig.getItems().stream()
                    .filter(item -> !responseItemList.contains(item)).collect(Collectors.toList());
            LOG.info("Completion items which are in response but not in test config : " + mismatchedList1);
            LOG.info("Completion items which are in test config but not in response : " + mismatchedList2);
            Assert.fail(String.format("Failed test: '%s' (%s)", testConfig.getDescription(), configPath));
        }
    }

    protected String getResponse(TestConfig testConfig) throws IOException {
        Path sourcePath = testRoot.resolve(testConfig.source);
        String triggerChar = testConfig.triggerCharacter == null ? "" : testConfig.triggerCharacter;
        return getResponse(sourcePath, testConfig.position, triggerChar);
    }

    public String getResponse(Path sourcePath, Position position, String triggerChar) throws IOException {
        Endpoint endpoint = getServiceEndpoint();
        TestUtil.openDocument(endpoint, sourcePath);
        String responseString = TestUtil.getCompletionResponse(sourcePath.toString(), position,
                endpoint, triggerChar);
        TestUtil.closeDocument(endpoint, sourcePath);
        return responseString;
    }

    List<CompletionItem> getExpectedList(JsonObject configJsonObject) {
        JsonArray expectedItems = configJsonObject.get("items").getAsJsonArray();
        return CompletionTestUtil.getExpectedItemList(expectedItems);
    }

    @DataProvider(name = "completion-data-provider")
    public abstract Object[][] dataProvider();

    public Object[][] testSubset() {
        return new Object[0][];
    }

    public List<String> skipList() {
        return new ArrayList<>();
    }

    public abstract String getTestResourceDir();

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

    /**
     * Update the config JSON while preserving the order of the existing completion items.
     */
    private void updateConfig(Path configJsonPath, TestConfig testConfig, List<CompletionItem> responseItemList)
            throws IOException {
        TestConfig updatedConfig = new TestConfig();
        updatedConfig.setPosition(testConfig.getPosition());
        updatedConfig.setSource(testConfig.getSource());
        updatedConfig.setDescription(testConfig.getDescription());

        List<CompletionItem> results = new ArrayList<>();
        List<CompletionItem> copyOfResultList = new ArrayList<>(responseItemList);
        List<CompletionItem> expectedList = testConfig.getItems();
        for (CompletionItem expectedItem : expectedList) {
            // Find this item in results
            int i = 0;
            for (; i < copyOfResultList.size(); i++) {
                String actualInsertText = copyOfResultList.get(i).getInsertText();
                CompletionItemKind actualKind = copyOfResultList.get(i).getKind();
                if (expectedItem.getInsertText().equals(actualInsertText) && expectedItem.getKind() == actualKind) {
                    break;
                }
            }

            // Add if found and remove it from the result list
            if (i != copyOfResultList.size()) {
                results.add(copyOfResultList.get(i));
                copyOfResultList.remove(i);
            }
        }

        // Add the rest of the items
        results.addAll(copyOfResultList);

        updatedConfig.setItems(results);

        if (testConfig.triggerCharacter != null) {
            updatedConfig.setTriggerCharacter(testConfig.getTriggerCharacter());
        }
        String objStr = gson.toJson(updatedConfig).concat(System.lineSeparator());
        Files.write(configJsonPath, objStr.getBytes(StandardCharsets.UTF_8));

        //This will print nice comparable text in IDE
//        Assert.assertEquals(responseItemList.toString(), expectedItemList.toString(),
//                "Failed Test for: " + configJsonPath);
    }

    protected void preLoadAndInit() throws InterruptedException {

        ServiceTemplateGenerator serviceTemplateGenerator =
                ServiceTemplateGenerator.getInstance(getLanguageServer().getServerContext());
        long initTime = System.currentTimeMillis();
        while (!serviceTemplateGenerator.initialized() && System.currentTimeMillis() < initTime + 60 * 1000) {
            Thread.sleep(2000);
        }
        if (!serviceTemplateGenerator.initialized()) {
            Assert.fail("Service template generator initialization failed!");
        }
    }

    @Override
    public boolean loadMockedPackages() {
        return true;
    }

    /**
     * Represents a test configuration.
     */
    protected static class TestConfig {

        private Position position;
        private String source;
        private String description;
        private List<CompletionItem> items;
        private String triggerCharacter;

        public Position getPosition() {
            return position;
        }

        public void setPosition(Position position) {
            this.position = position;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public String getDescription() {
            return description == null ? "" : description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<CompletionItem> getItems() {
            return items;
        }

        public void setItems(List<CompletionItem> items) {
            this.items = items;
        }

        public String getTriggerCharacter() {
            return triggerCharacter;
        }

        public void setTriggerCharacter(String triggerCharacter) {
            this.triggerCharacter = triggerCharacter;
        }
    }
}
