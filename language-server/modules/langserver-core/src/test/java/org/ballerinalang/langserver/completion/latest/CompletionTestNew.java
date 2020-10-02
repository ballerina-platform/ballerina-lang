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
package org.ballerinalang.langserver.completion.latest;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * Completion Test Interface.
 */
public abstract class CompletionTestNew {

    private Endpoint serviceEndpoint;

    private final Path testRoot = FileUtils.RES_DIR.resolve("completion").resolve("new");
    
    private final String configDir = "config";
    
    private final String sourceDir = "source";

    private JsonParser parser = new JsonParser();

    private Gson gson = new Gson();

    @BeforeClass
    public void init() {
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    @Test(dataProvider = "completion-data-provider")
    public void test(String config, String configPath) throws WorkspaceDocumentException, IOException {
        String configJsonPath = "completion" + File.separator + "new" + File.separator + configPath
                + File.separator + configDir + File.separator + config;
        JsonObject configJsonObject = FileUtils.fileContentAsObject(configJsonPath);

        String response = getResponse(configJsonObject);
        JsonObject json = parser.parse(response).getAsJsonObject();
        Type collectionType = new TypeToken<List<CompletionItem>>() {
        }.getType();
        JsonArray resultList = json.getAsJsonObject("result").getAsJsonArray("left");
        List<CompletionItem> responseItemList = gson.fromJson(resultList, collectionType);
        List<CompletionItem> expectedList = getExpectedList(configJsonObject);

        boolean result = CompletionTestUtil.isSubList(expectedList, responseItemList);
        if (!result) {
            // Fix test cases replacing expected using responses
//            JsonObject obj = new JsonObject();
//            obj.add("position", configJsonObject.get("position"));
//            obj.add("source", configJsonObject.get("source"));
//            obj.add("items", resultList);
//            java.nio.file.Files.write(FileUtils.RES_DIR.resolve(configJsonPath),
//                                      obj.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
//
//             //This will print nice comparable text in IDE
//            Assert.assertEquals(responseItemList.toString(), expectedList.toString(),
//                        "Failed Test for: " + configJsonPath);
            Assert.fail("Failed Test for: " + configJsonPath);
        }
    }

    String getResponse(JsonObject configJsonObject) throws IOException {
        Path sourcePath = testRoot.resolve(configJsonObject.get("source").getAsString());
        String responseString;
        Position position = new Position();
        JsonObject positionObj = configJsonObject.get("position").getAsJsonObject();
        position.setLine(positionObj.get("line").getAsInt());
        position.setCharacter(positionObj.get("character").getAsInt());

        TestUtil.openDocument(serviceEndpoint, sourcePath);
        responseString = TestUtil.getCompletionResponse(sourcePath.toString(), position, this.serviceEndpoint);
        TestUtil.closeDocument(serviceEndpoint, sourcePath);

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
        return Arrays.asList("config3.json", "config4.json");
    }
    
    public abstract String getTestResourceDir();

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
            return new Object[0][];
        }
    }
}
