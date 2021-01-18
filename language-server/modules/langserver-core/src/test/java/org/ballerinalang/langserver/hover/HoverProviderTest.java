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
import java.util.ArrayList;
import java.util.List;

/**
 * Test hover feature in language server.
 */
public class HoverProviderTest {
    private final Path balPath = FileUtils.RES_DIR.resolve("hover").resolve("source").resolve("hover.bal");
    private Endpoint serviceEndpoint;
    private final JsonParser parser = new JsonParser();

    @BeforeClass
    public void loadLangServer() throws IOException {
        serviceEndpoint = TestUtil.initializeLanguageSever();
        TestUtil.openDocument(serviceEndpoint, balPath);
    }

    @Test(description = "Test Hover provider", dataProvider = "hover-data-provider")
    public void testHover(String config) throws IOException {
        JsonObject configJson = FileUtils.fileContentAsObject("hover" + File.separator + "configs"
                + File.separator + config);
        Position position = getPosition(configJson);
        String response = TestUtil.getHoverResponse(balPath.toString(), position, serviceEndpoint);
        String expected = configJson.getAsJsonObject("expected").toString();

        boolean result = response.equals(expected);
        if (!result) {
            // Fix test cases replacing expected using responses
//            JsonObject obj = new JsonObject();
//            obj.add("position", configJson.get("position"));
//            obj.add("expected", parser.parse(response));
//            java.nio.file.Files.write(FileUtils.RES_DIR.resolve("hover").resolve("configs").resolve(config),
//                                      obj.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            Assert.fail("Failed Test for: " + config);
        }
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
        return new Object[0][];
    }

    private List<String> skipList() {
        return new ArrayList<>();
    }

    /**
     * Get the expected value from the config json.
     *
     * @param config configuration json to extract the expected value
     * @return {@link String} expected result in the config
     */
    private String getExpectedValue(JsonObject config) {
        return config.get("result").getAsString();
    }

    private Position getPosition(JsonObject config) {
        Position position = new Position();
        JsonObject positionObj = config.get("position").getAsJsonObject();
        position.setLine(positionObj.get("line").getAsInt());
        position.setCharacter(positionObj.get("character").getAsInt());

        return position;
    }
}
