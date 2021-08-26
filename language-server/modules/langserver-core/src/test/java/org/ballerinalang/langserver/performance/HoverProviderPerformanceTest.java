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
 * Test performance of hover feature in language server.
 */
public class HoverProviderPerformanceTest {
    private Endpoint serviceEndpoint;
    protected Path configRoot;
    protected Path sourceRoot;
    private final JsonParser parser = new JsonParser();

    @BeforeClass
    public void loadLangServer() throws IOException {
        serviceEndpoint = TestUtil.initializeLanguageSever();
        configRoot = FileUtils.RES_DIR.resolve("performance").resolve("performance_hover").resolve("config");
        sourceRoot = FileUtils.RES_DIR.resolve("performance").resolve("performance_hover").resolve("source");
    }

    @Test(description = "Test Hover provider", dataProvider = "performance-data-provider")
    public void testHover(String config) throws IOException {
        JsonObject configJson = FileUtils.fileContentAsObject(configRoot.resolve(config).toString());
        Position position = getPosition(configJson);
        JsonObject source = configJson.getAsJsonObject("source");
        Path sourcePath = sourceRoot.resolve(source.get("file").getAsString());
        TestUtil.openDocument(serviceEndpoint, sourcePath);
        long start = System.currentTimeMillis();
        String response = parser.parse(TestUtil.getHoverResponse(sourcePath.toString(), position, serviceEndpoint))
                .getAsJsonObject().toString();
        long end = System.currentTimeMillis();
        long responseTime = end - start;
        boolean isResponseWithinTime = TestUtil.isResponseWithinExpected(responseTime);
        Assert.assertEquals(isResponseWithinTime, true);
        String expected = configJson.getAsJsonObject("expected").toString();
        TestUtil.closeDocument(serviceEndpoint, sourcePath);

        boolean result = response.equals(expected);
        if (!result) {
            Assert.fail("Failed Test for: " + config);
        }
    }

    @DataProvider(name = "performance-data-provider")
    protected Object[][] dataProvider() {
        if (this.testSubset().length != 0) {
            return this.testSubset();
        }
        List<String> skippedTests = this.skipList();
        try {
            return Files.walk(FileUtils.RES_DIR.resolve("performance").resolve("performance_hover").resolve("config"))
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

    private Position getPosition(JsonObject config) {
        Position position = new Position();
        JsonObject positionObj = config.get("position").getAsJsonObject();
        position.setLine(positionObj.get("line").getAsInt());
        position.setCharacter(positionObj.get("character").getAsInt());

        return position;
    }
}
