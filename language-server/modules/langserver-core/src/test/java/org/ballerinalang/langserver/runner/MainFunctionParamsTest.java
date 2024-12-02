/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.langserver.runner;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.ballerinalang.langserver.util.FileUtils;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.nio.file.Path;

/**
 * Tests {@link org.ballerinalang.langserver.extensions.ballerina.runner.BallerinaRunnerService} mainFunctionParams api.
 *
 * @since 2201.11.0
 */
public class MainFunctionParamsTest {

    private Path resourceRoot;
    private Endpoint serviceEndpoint;

    @BeforeClass
    public void init() {
        this.resourceRoot = FileUtils.RES_DIR.resolve("runner").resolve("mainFuncArgs");
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    @Test(description = "Test main function params in a project", dataProvider = "data-provider")
    public void testDiagnosticsInPackage(String projectDir, String expected) {
        JsonObject expectedObj = FileUtils.fileContentAsObject(this.resourceRoot.resolve("config")
                .resolve(expected).toString());

        Path projectPath = this.resourceRoot.resolve("source").resolve(projectDir);
        String response = TestUtil.getRunnerMainFuncParamsResponse(this.serviceEndpoint, projectPath.toString());
        JsonObject actualJson = getResponseJson(response);
        Assert.assertEquals(actualJson.get("result").getAsJsonObject(),
                expectedObj.getAsJsonObject("result").getAsJsonObject());
    }

    @DataProvider(name = "data-provider")
    public Object[][] getDataProvider() {
        return new Object[][]{
                {"project1", "project1.json"},
                {"project2", "project2.json"}
        };
    }

    @AfterClass
    public void cleanupLanguageServer() {
        TestUtil.shutdownLanguageServer(this.serviceEndpoint);
    }

    private JsonObject getResponseJson(String response) {
        JsonObject responseJson = JsonParser.parseString(response).getAsJsonObject();
        responseJson.remove("id");
        return responseJson;
    }
}
