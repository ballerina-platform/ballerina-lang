/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.packages;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import org.ballerinalang.langserver.util.FileUtils;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

/**
 * Tests Package Service components API in Language Server.
 */
public class ComponentsTest {

    private static final String NAME = "name";
    private static final String MODULES = "modules";
    private static final JsonParser JSON_PARSER = new JsonParser();
    private static final Gson GSON = new Gson();

    private Path resourceRoot;
    private Endpoint serviceEndpoint;

    @BeforeClass
    public void init() {
        this.resourceRoot = FileUtils.RES_DIR.resolve("packages");
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    @Test(description = "Test package components API", dataProvider = "components-data-provider")
    public void packageComponentsTestCase(String[] projects, String expected) throws IOException {
        Path configsPath = this.resourceRoot.resolve("configs");
        List<String> filePaths = new ArrayList<>();
        for (String project : projects) {
            Path path = configsPath.resolve(project).resolve("main.bal").toAbsolutePath();
            filePaths.add(path.toString());
        }

        String response = TestUtil.getPackageComponentsResponse(serviceEndpoint, filePaths.iterator());
        compareResponse(expected, response);
    }

    /**
     * Compares actual response and expected response.
     *
     * @param expected Expected response
     * @param response JSON rpc response
     */
    private void compareResponse(String expected, String response) {
        Path expectedPath = this.resourceRoot.resolve("components").resolve(expected);
        JsonArray expectedJsonArray =
                FileUtils.fileContentAsObject(expectedPath.toAbsolutePath().toString()).getAsJsonArray("result");
        JsonArray responseJsonArray =
                JSON_PARSER.parse(response).getAsJsonObject().getAsJsonObject("result").getAsJsonArray("packages");

        Assert.assertEquals(responseJsonArray.size(), expectedJsonArray.size(), "Package ComponentsTest fails with " +
                "incorrect package count.");
        responseJsonArray.forEach(jsonPackage -> {
            JsonPrimitive name = jsonPackage.getAsJsonObject().getAsJsonPrimitive(NAME);
            if (name != null) {
                List<JsonObject> packageObjects = new ArrayList<>();
                StreamSupport.stream(expectedJsonArray.spliterator(), false)
                        .map(JsonElement::getAsJsonObject)
                        .filter(jsonObject -> jsonObject.get(NAME).getAsString().equals(name.getAsString()))
                        .forEach(packageObjects::add);
                Assert.assertEquals(packageObjects.size(), 1,
                        "Package ComponentsTest's object size assert fails with " + expected + " test case.");
                Assert.assertEquals(name, packageObjects.get(0).getAsJsonPrimitive(NAME), "Package ComponentsTest's " +
                        "packageName assert fails with " + expected + " test case.");
                Assert.assertNotNull(jsonPackage.getAsJsonObject().getAsJsonPrimitive("filePath"), "Package " +
                        "ComponentsTest's filePath assert fails with " + expected + " test case.");
                compareJsonArrays(jsonPackage.getAsJsonObject().getAsJsonArray(MODULES),
                        packageObjects.get(0).getAsJsonArray(MODULES),
                        expected);
            } else {
                compareJsonArrays(responseJsonArray, expectedJsonArray, expected);
            }
        });

    }

    /**
     * Compares two JSON arrays irrespective of the order.
     *
     * @param responseJsonArray Expected jSON array
     * @param expectedJsonArray Actual jSON array
     * @param testReference     Test case reference name
     */
    private void compareJsonArrays(JsonArray responseJsonArray, JsonArray expectedJsonArray, String testReference) {
        Assert.assertEqualsNoOrder(GSON.fromJson(responseJsonArray, Object[].class),
                GSON.fromJson(expectedJsonArray, Object[].class),
                "Package ComponentsTest fails with " + testReference + " test case.");
    }

    @AfterClass
    public void cleanupLanguageServer() {
        TestUtil.shutdownLanguageServer(this.serviceEndpoint);
    }

    @DataProvider(name = "components-data-provider")
    public Object[][] getDataProvider() {
        return new Object[][]{
                {new String[]{"project"}, "single-package_expected.json"},
                {new String[]{"project", "project-functions", "project-services", "single-file"},
                        "multiple-packages_expected.json"},
                {new String[]{"single-file"}, "single-file-package_expected.json"},
                {new String[]{"project-other"}, "project-other_expected.json"},
                {new String[]{"non-exist"}, "project-exception_expected.json"}
        };
    }
}
