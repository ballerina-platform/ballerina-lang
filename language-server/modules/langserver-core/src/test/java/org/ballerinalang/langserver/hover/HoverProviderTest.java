/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.hover;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.ballerinalang.langserver.util.FileUtils;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

/**
 * Test hover feature in language server.
 */
public class HoverProviderTest {

    protected Endpoint serviceEndpoint;
    protected Path configRoot;
    protected Path sourceRoot;

    @BeforeClass
    public void loadLangServer() throws IOException {
        serviceEndpoint = TestUtil.initializeLanguageSever();
        configRoot = FileUtils.RES_DIR.resolve("hover").resolve("configs");
        sourceRoot = FileUtils.RES_DIR.resolve("hover").resolve("source");
    }

    @Test(description = "Test Hover provider", dataProvider = "hover-data-provider")
    public void testHover(String config) throws IOException {
        JsonObject configJson = FileUtils.fileContentAsObject(configRoot.resolve(config).toString());
        Position position = getPosition(configJson);
        JsonObject source = configJson.getAsJsonObject("source");
        Path sourcePath = sourceRoot.resolve(source.get("file").getAsString());
        TestUtil.openDocument(serviceEndpoint, sourcePath);
        String response = getResponse(sourcePath, position);
        String expected = configJson.getAsJsonObject("expected").toString();
        TestUtil.closeDocument(serviceEndpoint, sourcePath);

        boolean result = response.equals(expected);
        if (!result) {
            // Fix test cases replacing expected using responses
//            JsonObject obj = new JsonObject();
//            obj.add("position", configJson.get("position"));
//            obj.add("source", configJson.get("source"));
//            obj.add("expected", JsonParser.parseString(response));
//            String objStr = obj.toString().concat(System.lineSeparator());
//            java.nio.file.Files.write(FileUtils.RES_DIR.resolve("hover").resolve("configs").resolve(config),
//                                      objStr.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            Assert.fail("Failed Test for: " + config);
        }
    }

    public String getResponse(Path sourcePath, Position position) {
        return JsonParser.parseString(TestUtil.getHoverResponse(sourcePath.toString(), position, serviceEndpoint))
                .getAsJsonObject().toString();
    }

    @DataProvider(name = "hover-data-provider")
    protected Object[][] dataProvider() {
        if (this.testSubset().length != 0) {
            return this.testSubset();
        }
        List<String> skippedTests = this.skipList();
        try {
            return Files.walk(FileUtils.RES_DIR.resolve("hover").resolve("configs"))
                    .filter(path -> {
                        File file = path.toFile();
                        return file.isFile() && file.getName().endsWith(".json")
                                && !skippedTests.contains(file.getName());
                    })
                    .map(path -> new Object[]{path.toFile().getName()})
                    .toArray(size -> new Object[size][2]);
        } catch (IOException e) {
            return new Object[0][];
        }
    }

    private Object[][] testSubset() {
        return new Object[][]{};
    }

    private List<String> skipList() {
        return Collections.emptyList();
    }

    private Position getPosition(JsonObject config) {
        Position position = new Position();
        JsonObject positionObj = config.get("position").getAsJsonObject();
        position.setLine(positionObj.get("line").getAsInt());
        position.setCharacter(positionObj.get("character").getAsInt());

        return position;
    }
}
