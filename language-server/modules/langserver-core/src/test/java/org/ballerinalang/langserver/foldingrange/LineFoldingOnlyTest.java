/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.foldingrange;

import com.google.gson.JsonArray;
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
import java.nio.file.Path;

/**
 * Tests folding range feature in Language Server.
 */
public class LineFoldingOnlyTest {

    private final Path resourcesPath =
            new File(getClass().getClassLoader().getResource("foldingrange").getFile()).toPath();
    private Endpoint serviceEndpoint;

    @BeforeClass
    public void init() {
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    @Test(description = "Test folding ranges", dataProvider = "foldingrange-data-provider")
    public void foldingRangeTestCase(String source, String expected) throws IOException {
        Path sourcePath = resourcesPath.resolve("source").resolve(source);
        TestUtil.openDocument(serviceEndpoint, sourcePath);
        String response = TestUtil.getFoldingRangeResponse(serviceEndpoint, sourcePath.toAbsolutePath().toString());
        compareResponse(expected, response);
    }

    /**
     * Compares actual response and expected response.
     *
     * @param expected expected response
     * @param response JSON rpc response
     */
    private void compareResponse(String expected, String response) {
        Path expectedPath = resourcesPath.resolve("expected").resolve(expected);
        JsonArray expectedJsonArray =
                FileUtils.fileContentAsObject(expectedPath.toAbsolutePath().toString()).getAsJsonArray("result");
        JsonArray responseJsonArray = JsonParser.parseString(response).getAsJsonObject().getAsJsonArray("result");
        Assert.assertEquals(responseJsonArray, expectedJsonArray, "LineFoldingOnlyTest fails with " + expected
                + " test case.");
    }

    @AfterClass
    public void cleanupLanguageServer() {
        TestUtil.shutdownLanguageServer(this.serviceEndpoint);
    }

    @DataProvider(name = "foldingrange-data-provider")
    public Object[][] getDataProvider() {
        return new Object[][]{
                {"class.bal", "class_expected.json"},
                {"function.bal", "function_expected.json"},
                {"service.bal", "service_expected.json"},
                {"type.bal", "type_expected.json"}
        };
    }
}
