/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

/**
 * Test suit for testing find all references.
 */
@Deprecated
public class ReferenceTest {
    private Path configRoot;
    private Path sourceRoot;
    protected Gson gson = new Gson();
    protected Endpoint serviceEndpoint;

    @BeforeClass
    public void init() {
        this.configRoot = FileUtils.RES_DIR.resolve("reference").resolve("expected");
        this.sourceRoot = FileUtils.RES_DIR.resolve("reference").resolve("sources");
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    @Test(description = "Test Find all references", dataProvider = "testDataProvider", enabled = false)
    public void test(String configPath, String configDir) throws IOException {
        JsonObject configObject = FileUtils.fileContentAsObject(configRoot.resolve(configDir)
                .resolve(configPath).toString());
        JsonObject source = configObject.getAsJsonObject("source");
        Path sourcePath = sourceRoot.resolve(source.get("file").getAsString());
        Position position = gson.fromJson(configObject.get("position"), Position.class);

        TestUtil.openDocument(serviceEndpoint, sourcePath);
        String actualStr = TestUtil.getReferencesResponse(sourcePath.toUri().toString(), position, serviceEndpoint);
        TestUtil.closeDocument(serviceEndpoint, sourcePath);

        JsonArray expected = configObject.getAsJsonArray("result");
        JsonArray actual = JsonParser.parseString(actualStr).getAsJsonObject().getAsJsonArray("result");
        this.alterExpectedUri(expected);
        this.alterActualUri(actual);
        Assert.assertEquals(actual, expected);
    }


    @DataProvider
    public Object[][] testDataProvider() {
        return new Object[][]{
                {"refFunction1.json", "function"}
        };
    }

    @AfterClass
    public void shutDownLanguageServer() {
        TestUtil.shutdownLanguageServer(this.serviceEndpoint);
    }

    private void alterExpectedUri(JsonArray expected) throws IOException {
        for (JsonElement jsonElement : expected) {
            JsonObject item = jsonElement.getAsJsonObject();
            String[] uriComponents = item.get("uri").toString().replace("\"", "").split("/");
            Path expectedPath = Path.of(this.sourceRoot.toUri());
            for (String uriComponent : uriComponents) {
                expectedPath = expectedPath.resolve(uriComponent);
            }
            item.remove("uri");
            item.addProperty("uri", expectedPath.toFile().getCanonicalPath());
        }
    }

    private void alterActualUri(JsonArray actual) throws IOException {
        for (JsonElement jsonElement : actual) {
            JsonObject item = jsonElement.getAsJsonObject();
            String uri = item.get("uri").toString().replace("\"", "");
            String canonicalPath = new File(URI.create(uri)).getCanonicalPath();
            item.remove("uri");
            item.addProperty("uri", canonicalPath);
        }
    }
}
