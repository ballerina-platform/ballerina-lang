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
import org.ballerinalang.langserver.util.FileUtils;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Test performance of openDocument in completions feature in language server.
 */
public class OpenDocumentPerformanceTest extends CompletionPerformanceTest {

    private Endpoint serviceEndpoint;
    private final Path testRoot = FileUtils.RES_DIR.resolve("performance");
    private final String configDir = "config";

    @BeforeClass
    public void init() {
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    @Test(dataProvider = "performance-data-provider")
    public void testCompletion(String config, String configPath) throws IOException {
        String configJsonPath = "performance" + File.separator + configPath
                + File.separator + configDir + File.separator + config;
        JsonObject configJsonObject = FileUtils.fileContentAsObject(configJsonPath);

        long actualResponseTime = getResponseTimeCompletion(configJsonObject);
        int expectedResponseTime = Integer.parseInt(System.getProperty("responseTimeThreshold"));
        Assert.assertTrue(actualResponseTime < expectedResponseTime,
                String.format("Expected response time = %d, received %d.", expectedResponseTime, actualResponseTime));
    }

    long getResponseTimeCompletion(JsonObject configJsonObject) throws IOException {
        Path sourcePath = testRoot.resolve(configJsonObject.get("source").getAsString());
        long start = System.currentTimeMillis();
        TestUtil.openDocument(serviceEndpoint, sourcePath);
        long end = System.currentTimeMillis();
        long responseTime = end - start;
        TestUtil.closeDocument(serviceEndpoint, sourcePath);
        return responseTime;
    }

    @AfterClass
    public void cleanupLanguageServer() {
        TestUtil.shutdownLanguageServer(this.serviceEndpoint);
    }

    @DataProvider(name = "performance-data-provider")
    @Override
    public Object[][] dataProvider() {
        return this.getConfigsList();
    }

    @Override
    public String getTestResourceDir() {
        return "performance_completion";
    }
}
