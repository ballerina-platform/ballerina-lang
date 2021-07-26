/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.references;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.ballerinalang.langserver.util.FileUtils;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

/**
 * Test goto definition language server feature.
 */
public class ReferencesTest {
    protected Path configRoot;
    protected Path sourceRoot;
    protected Gson gson = new Gson();
    protected JsonParser parser = new JsonParser();
    protected Endpoint serviceEndpoint;
    private static final Logger log = LoggerFactory.getLogger(ReferencesTest.class);

    @BeforeClass
    public void init() throws Exception {
        configRoot = FileUtils.RES_DIR.resolve("references").resolve("expected");
        sourceRoot = FileUtils.RES_DIR.resolve("references").resolve("sources");
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    @Test(description = "Test reference", dataProvider = "testDataProvider")
    public void test(String configPath) throws IOException {
        JsonObject configObject = FileUtils.fileContentAsObject(configRoot.resolve(configPath).toString());
        JsonObject source = configObject.getAsJsonObject("source");
        Path sourcePath = sourceRoot.resolve(source.get("file").getAsString());
        Position position = gson.fromJson(configObject.get("position"), Position.class);

        TestUtil.openDocument(serviceEndpoint, sourcePath);
        String actualStr = TestUtil.getReferencesResponse(sourcePath.toString(), position, serviceEndpoint);
        TestUtil.closeDocument(serviceEndpoint, sourcePath);

        JsonArray expected = configObject.getAsJsonArray("result");
        JsonArray actual = parser.parse(actualStr).getAsJsonObject().get("result").getAsJsonArray();
        this.alterExpectedUri(expected, sourceRoot);
        this.alterActualUri(actual);
        
        expected.forEach(jsonElement -> Assert.assertTrue(actual.contains(jsonElement)));
    }

    @DataProvider
    private Object[][] testDataProvider() {
        log.info("Test textDocument/definition for Basic Cases");
        return new Object[][]{
                {"ref_config1.json"},
                {"ref_error_types_config1.json"},
                {"ref_error_types_config2.json"},
                {"ref_error_types_config3.json"},
                {"ref_record_types_config1.json"},
                // TODO type Err error; user defined types from other modules are not detected due to their parent
                //  being set to lang.annotations.
                {"ref_package_alias_config1.json"},
                {"ref_retry_spec_config1.json"},
        };
    }

    @AfterClass
    public void shutDownLanguageServer() throws IOException {
        TestUtil.shutdownLanguageServer(this.serviceEndpoint);
    }

    protected void alterExpectedUri(JsonArray expected, Path root) throws IOException {
        for (JsonElement jsonElement : expected) {
            JsonObject item = jsonElement.getAsJsonObject();
            String[] uriComponents = item.get("uri").toString().replace("\"", "").split("/");
            Path expectedPath = Paths.get(root.toUri());
            for (String uriComponent : uriComponents) {
                expectedPath = expectedPath.resolve(uriComponent);
            }
            item.remove("uri");
            item.addProperty("uri", expectedPath.toFile().getCanonicalPath());
        }
    }

    protected void alterActualUri(JsonArray actual) throws IOException {
        for (JsonElement jsonElement : actual) {
            JsonObject item = jsonElement.getAsJsonObject();
            String uri = item.get("uri").toString().replace("\"", "");
            String canonicalPath = new File(URI.create(uri)).getCanonicalPath();
            item.remove("uri");
            item.addProperty("uri", canonicalPath);
        }
    }
}
