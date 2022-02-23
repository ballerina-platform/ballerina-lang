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

import java.io.IOException;
import java.nio.file.Path;

/**
 * Tests Package Config Schema API in Language Server.
 */
public class ConfigSchemaTest {
    private static final JsonParser JSON_PARSER = new JsonParser();

    private Path resourceRoot;
    private Endpoint serviceEndpoint;

    @BeforeClass
    public void init() {
        this.resourceRoot = FileUtils.RES_DIR.resolve("packages");
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    @Test(description = "Test package config schema capability", dataProvider = "config-schema-data-provider")
    public void packageConfigSchemaTestCase(String projectName, String assertFileName) throws IOException {
        Path sourcePath = this.resourceRoot.resolve("configs").resolve(projectName).resolve("main.bal");
        TestUtil.openDocument(serviceEndpoint, sourcePath);
        String response = TestUtil.getPackageConfigSchemaResponse(serviceEndpoint,
                sourcePath.toAbsolutePath().toString());

        if (compareResponse(assertFileName, response)) {
            // If the elements in the JSON objects are in order, a simple comparison can be done.
            Assert.assertTrue(true);
            return;
        }

        JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
        Assert.assertFalse(jsonObject.getAsJsonObject("result").getAsJsonObject("configSchema")
                .getAsJsonObject("properties").getAsJsonObject("wso2")
                .getAsJsonPrimitive("additionalProperties").getAsBoolean());
    }

    /**
     * Compares actual response and expected response.
     *
     * @param assertFileName File name of the assert JSON
     * @param response       JSON RPC response
     */
    private boolean compareResponse(String assertFileName, String response) {
        Path expectedPath = this.resourceRoot.resolve("config-schema").resolve(assertFileName);
        JsonObject expectedJsonObject = FileUtils.fileContentAsObject(expectedPath.toAbsolutePath().toString());
        JsonObject responseJsonObject = JsonParser.parseString(response).getAsJsonObject()
                .getAsJsonObject("result").getAsJsonObject("configSchema");

        return expectedJsonObject.equals(responseJsonObject);
    }

    @AfterClass
    public void cleanupLanguageServer() {
        TestUtil.shutdownLanguageServer(this.serviceEndpoint);
    }

    @DataProvider(name = "config-schema-data-provider")
    public Object[][] getDataProvider() {
        return new Object[][]{
                {"project-config-schema", "config-json-schema.json"}
        };
    }
}
