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
package org.ballerinalang.langserver.execpositions;

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
 * Tests executorPositions API in Language Server.
 */
public class ExecutorPositionsTest {

    private static final String END_LINE = "endLine";
    private static final String EXEC_POSITIONS = "executorPositions";
    private static final String FILE_PATH = "filePath";
    private static final String KIND = "kind";
    private static final String NAME = "name";
    private static final String RANGE = "range";
    private static final String START_LINE = "startLine";

    private Path resourceRoot;
    private Endpoint serviceEndpoint;

    @BeforeClass
    public void init() {
        this.resourceRoot = FileUtils.RES_DIR.resolve("execpositions");
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    @Test(description = "Test executor positions API", dataProvider = "exec-positions-data-provider")
    public void executorPositionsTestCase(String[] filePaths, String expected) throws IOException {
        Path configsPath = this.resourceRoot.resolve("configs");
        for (String path : filePaths) {
            configsPath = configsPath.resolve(path);
        }
        TestUtil.openDocument(serviceEndpoint, configsPath);
        String response = TestUtil.getExecutorPositionsResponse(serviceEndpoint, configsPath.toString());
        compareResponse(expected, response);
    }

    /**
     * Compares actual response and expected response.
     *
     * @param expected Expected response file
     * @param response JSON rpc response
     */
    private void compareResponse(String expected, String response) {
        Path expectedPath = this.resourceRoot.resolve("expected").resolve(expected + ".json");
        JsonArray expectedJsonArray =
                FileUtils.fileContentAsObject(expectedPath.toAbsolutePath().toString()).getAsJsonArray(EXEC_POSITIONS);
        JsonArray actualJsonArray = JsonParser.parseString(response)
                .getAsJsonObject().getAsJsonObject("result").getAsJsonArray(EXEC_POSITIONS);
        actualJsonArray.forEach(jsonExec -> {
            JsonPrimitive name = jsonExec.getAsJsonObject().getAsJsonPrimitive(NAME);
            List<JsonObject> execObjects = new ArrayList<>();
            StreamSupport.stream(expectedJsonArray.spliterator(), false)
                    .map(JsonElement::getAsJsonObject)
                    .filter(jsonObject -> jsonObject.get(NAME).getAsString().equals(name.getAsString()))
                    .forEach(execObjects::add);
            Assert.assertEquals(execObjects.size(), 1, "Executor position test's object size assert fails with " +
                    expected + " test case.");
            Assert.assertEquals(name, execObjects.get(0).getAsJsonPrimitive(NAME), "Executor position test's name " +
                    "assert fails with " + expected + " test case.");
            Assert.assertNotNull(jsonExec.getAsJsonObject().getAsJsonPrimitive(FILE_PATH), "Executor position test's" +
                    " filePath assert fails with " + expected + " test case.");
            Assert.assertEquals(jsonExec.getAsJsonObject().getAsJsonPrimitive(KIND),
                    execObjects.get(0).getAsJsonPrimitive(KIND), "Executor position test's kind assert fails with " +
                            expected + " test case.");
            Assert.assertEquals(jsonExec.getAsJsonObject().getAsJsonObject(RANGE).getAsJsonObject(START_LINE),
                    execObjects.get(0).getAsJsonObject(RANGE).getAsJsonObject(START_LINE), "Executor position test's" +
                            " start line assert fails with " + expected + " test case.");
            Assert.assertEquals(jsonExec.getAsJsonObject().getAsJsonObject(RANGE).getAsJsonObject(END_LINE),
                    execObjects.get(0).getAsJsonObject(RANGE).getAsJsonObject(END_LINE), "Executor position test's " +
                            "end line assert fails with " + expected + " test case.");
        });
    }

    @AfterClass
    public void cleanupLanguageServer() {
        TestUtil.shutdownLanguageServer(this.serviceEndpoint);
    }

    @DataProvider(name = "exec-positions-data-provider")
    public Object[][] getDataProvider() {
        return new Object[][]{
                {new String[]{"project", "main.bal"}, "project_source_default"},
                {new String[]{"project", "tests", "test.bal"}, "project_test_default"},
                {new String[]{"project", "modules", "module", "hello.bal"}, "project_source_module"},
                {new String[]{"project", "modules", "module", "tests", "test.bal"}, "project_test_module"},
                {new String[]{"single-file", "test.bal"}, "single_file_test"},
                {new String[]{"single-file", "main.bal"}, "single_file_source"}
        };
    }
}
