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
import java.nio.file.Path;

/**
 * Test performance of hover feature in language server.
 */
public class HoverProviderPerformanceTest {
    private Endpoint serviceEndpoint;
    private final Path testRoot = FileUtils.RES_DIR.resolve("performance");
    private final JsonParser parser = new JsonParser();

    @BeforeClass
    public void loadLangServer() throws IOException {
        serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    @Test(description = "Test Hover provider", dataProvider = "performance-data-provider")
    public void testHover(String config, String source) throws IOException {
        String configJsonPath = getConfigJsonPath(config);
        Path sourcePath = testRoot.resolve(getResourceDir()).resolve("source").resolve(source);
        JsonObject configJsonObject = FileUtils.fileContentAsObject(configJsonPath);
        Position position = getPosition(configJsonObject);
        TestUtil.openDocument(serviceEndpoint, sourcePath);

        long start = System.currentTimeMillis();
        String response = parser.parse(TestUtil.getHoverResponse(sourcePath.toString(), position, serviceEndpoint))
                .getAsJsonObject().toString();
        long end = System.currentTimeMillis();
        long actualResponseTime = end - start;
        int expectedResponseTime = Integer.parseInt(System.getProperty("responseTimeThreshold"));
        Assert.assertTrue(actualResponseTime < expectedResponseTime,
                String.format("Expected response time = %d, received %d.", expectedResponseTime, actualResponseTime));
        String expected = configJsonObject.getAsJsonObject("expected").toString();
        TestUtil.closeDocument(serviceEndpoint, sourcePath);

        boolean result = response.equals(expected);
        if (!result) {
            Assert.fail("Failed Test for: " + config);
        }
    }

    @DataProvider(name = "performance-data-provider")
    protected Object[][] dataProvider() {
        return new Object[][]{
                {"performance_hover.json", "performance_hover.bal"},
        };
    }

    private String getConfigJsonPath(String configFilePath) {
        return "performance" + File.separator + getResourceDir() + File.separator + "config" + File.separator +
                configFilePath;
    }

    private Position getPosition(JsonObject config) {
        Position position = new Position();
        JsonObject positionObj = config.get("position").getAsJsonObject();
        position.setLine(positionObj.get("line").getAsInt());
        position.setCharacter(positionObj.get("character").getAsInt());

        return position;
    }

    public String getResourceDir() {
        return "performance_hover";
    }
}
