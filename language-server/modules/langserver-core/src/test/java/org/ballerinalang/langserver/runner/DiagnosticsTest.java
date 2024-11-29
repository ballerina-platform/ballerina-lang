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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Tests {@link org.ballerinalang.langserver.extensions.ballerina.runner.BallerinaRunnerService} diagnostics api.
 *
 * @since 2201.11.0
 */
public class DiagnosticsTest {

    private Path resourceRoot;
    private Endpoint serviceEndpoint;

    @BeforeClass
    public void init() {
        this.resourceRoot = FileUtils.RES_DIR.resolve("runner").resolve("diagnostics");
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    @Test(description = "Test getting all diagnostics in a package", dataProvider = "data-provider")
    public void testDiagnosticsInPackage(String projectDir, String expected) throws IOException {
        Path sourceRoot = this.resourceRoot.resolve("source");
        JsonObject expectedObj = FileUtils.fileContentAsObject(this.resourceRoot.resolve("config")
                .resolve(expected).toString());

        Path projectPath = this.resourceRoot.resolve("source").resolve(projectDir);
        String response = TestUtil.getRunnerDiagnosticsResponse(this.serviceEndpoint, projectPath.toString());
        JsonObject actualJson = getResponseJson(response);

        JsonObject actualErrorDiagnosticMap = alterActualErrorMapPaths(actualJson.get("result")
                .getAsJsonObject().get("errorDiagnosticMap").getAsJsonObject());
        JsonObject expectedErrorDiagnosticMap = alterErrorMapPaths(expectedObj.getAsJsonObject("result")
                .getAsJsonObject("errorDiagnosticMap"), sourceRoot);

        compareResponse(actualErrorDiagnosticMap, expectedErrorDiagnosticMap);
    }

    private void compareResponse(JsonObject actualMap, JsonObject expectedMap) {
        for (Map.Entry<String, JsonElement> entry : expectedMap.entrySet()) {
            String key = entry.getKey();
            JsonArray expectedDiagnostics = entry.getValue().getAsJsonArray();
            if (!actualMap.has(key)) {
                Assert.fail("Expected errors not found in file: " + key);
            }
            JsonArray actualDiagnostics = actualMap.getAsJsonArray(key);
            Assert.assertEquals(actualDiagnostics, expectedDiagnostics);
        }
    }

    protected JsonObject alterErrorMapPaths(JsonObject errMap, Path root) throws IOException {
        JsonObject newErrMap = new JsonObject();
        for (Map.Entry<String, JsonElement> entry : errMap.entrySet()) {
            String key = entry.getKey();
            JsonArray diagnostics = entry.getValue().getAsJsonArray();
            String[] uriComponents = key.replace("\"", "").split("/");
            Path expectedPath = Paths.get(root.toUri());
            for (String uriComponent : uriComponents) {
                expectedPath = expectedPath.resolve(uriComponent);
            }
            newErrMap.add(expectedPath.toFile().getCanonicalPath(), diagnostics);
        }
        return newErrMap;
    }

    protected JsonObject alterActualErrorMapPaths(JsonObject errMap) throws IOException {
        JsonObject newErrMap = new JsonObject();
        for (Map.Entry<String, JsonElement> entry : errMap.entrySet()) {
            String key = entry.getKey();
            JsonArray diagnostics = entry.getValue().getAsJsonArray();
            String uri = key.replace("\"", "");
            newErrMap.add(new File(URI.create(uri)).getCanonicalPath(), diagnostics);
        }
        return newErrMap;
    }

    @DataProvider(name = "data-provider")
    public Object[][] getDataProvider() {
        return new Object[][]{
                {"project1", "project1.json"}
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
