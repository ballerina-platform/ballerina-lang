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

/**
 * Tests Package Service metadata API in Language Server.
 */
public class MetadataTest {

    private static final String ORG_NAME = "orgName";
    private static final String PACKAGE_NAME = "packageName";
    private static final String PATH = "path";
    private static final String KIND = "kind";

    private Path resourceRoot;
    private Endpoint serviceEndpoint;

    @BeforeClass
    public void init() {
        this.resourceRoot = FileUtils.RES_DIR.resolve("packages");
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    @Test(description = "Test package metadata API", dataProvider = "metadata-data-provider")
    public void packageMetadataTestCase(String projectName, String packageName) throws IOException {
        Path sourcePath = this.resourceRoot.resolve("configs").resolve(projectName).resolve("main.bal");
        TestUtil.openDocument(serviceEndpoint, sourcePath);
        String response = TestUtil.getPackageMetadataResponse(serviceEndpoint, sourcePath.toAbsolutePath().toString());
        compareResponse(packageName, response);
    }

    /**
     * Compares actual response and expected response.
     *
     * @param projectName Project name for test reference
     * @param response    JSON rpc response
     */
    private void compareResponse(String projectName, String response) {
        Path expectedPath = this.resourceRoot.resolve("metadata").resolve(projectName);
        JsonObject expectedJsonObject =
                FileUtils.fileContentAsObject(expectedPath.toAbsolutePath().toString()).getAsJsonObject();
        JsonObject responseJsonObject = JsonParser.parseString(response).getAsJsonObject().getAsJsonObject("result");
        JsonPrimitive packageName = expectedJsonObject.getAsJsonPrimitive(PACKAGE_NAME);
        if (packageName != null) {
            Assert.assertEquals(responseJsonObject.getAsJsonPrimitive(PACKAGE_NAME), packageName,
                    "Package MetadataTest " + PACKAGE_NAME + " fails with " + projectName + " test case.");
        } else {
            Assert.assertNull(responseJsonObject.getAsJsonPrimitive(PACKAGE_NAME),
                    "Package MetadataTest " + PACKAGE_NAME + " fails with " + projectName + " test case.");
        }

        JsonPrimitive projectKind = expectedJsonObject.getAsJsonPrimitive(KIND);
        Assert.assertEquals(responseJsonObject.getAsJsonPrimitive(KIND), projectKind,
                "Package MetadataTest " + KIND + " fails with " + projectName + " test case.");
        Assert.assertNotNull(responseJsonObject.getAsJsonPrimitive(PATH),
                "Package MetadataTest " + PATH + " fails with " + projectName + " test case.");

        Assert.assertNotNull(responseJsonObject.getAsJsonPrimitive(ORG_NAME),
                "Package MetadataTest " + ORG_NAME + " fails with " + projectName + " test case.");

        if (projectKind != null && !"SINGLE_FILE_PROJECT".equals(projectKind.getAsString())) {
            Assert.assertEquals(responseJsonObject.getAsJsonPrimitive(ORG_NAME),
                    expectedJsonObject.getAsJsonPrimitive(ORG_NAME),
                    "Package MetadataTest " + ORG_NAME + " fails with " + projectName + " test case.");
        }
    }

    @AfterClass
    public void cleanupLanguageServer() {
        TestUtil.shutdownLanguageServer(this.serviceEndpoint);
    }

    @DataProvider(name = "metadata-data-provider")
    public Object[][] getDataProvider() {
        return new Object[][]{
                {"project-functions", "project-functions_expected.json"},
                {"project-services", "project-services_expected.json"},
                {"single-file", "single-file_expected.json"}
        };
    }
}
