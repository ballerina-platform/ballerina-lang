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
package org.ballerinalang.langserver.definition;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.exception.LSStdlibCacheException;
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
public class DefinitionTest {
    protected Path configRoot;
    protected Path sourceRoot;
    private Path projectPath = FileUtils.RES_DIR.resolve("referencesProject");
    protected Gson gson = new Gson();
    protected JsonParser parser = new JsonParser();
    protected Endpoint serviceEndpoint;
    private static final Logger log = LoggerFactory.getLogger(DefinitionTest.class);

    @BeforeClass
    public void init() throws Exception {
        configRoot = FileUtils.RES_DIR.resolve("definition").resolve("expected");
        sourceRoot = FileUtils.RES_DIR.resolve("definition").resolve("sources");
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    @Test(description = "Test goto definitions", dataProvider = "testDataProvider")
    public void test(String configPath, String configDir) throws IOException, LSStdlibCacheException {
        JsonObject configObject = FileUtils.fileContentAsObject(configRoot.resolve(configDir)
                .resolve(configPath).toString());
        JsonObject source = configObject.getAsJsonObject("source");
        Path sourcePath = sourceRoot.resolve(source.get("file").getAsString());
        Position position = gson.fromJson(configObject.get("position"), Position.class);
        this.compareResults(sourcePath, position, configObject, sourceRoot);
    }

    @Test(description = "Test Go to definition between two files in same module", enabled = false)
    public void testDifferentFiles() throws IOException, LSStdlibCacheException {
        log.info("Test textDocument/definition for Two Files in same module");
        JsonObject configObject = FileUtils.fileContentAsObject(configRoot.resolve("multifile")
                .resolve("defMultiFile1.json").toString());
        JsonObject source = configObject.getAsJsonObject("source");
        String dirPath = source.get("dir").getAsString().replace("/", CommonUtil.FILE_SEPARATOR);
        Path sourcePath = projectPath.resolve(dirPath).resolve(source.get("file").getAsString());
        Position position = gson.fromJson(configObject.get("position"), Position.class);
        this.compareResults(sourcePath, position, configObject, projectPath);
    }

    @Test(description = "Test Go to definition between two modules", enabled = false)
    public void testDifferentModule() throws IOException, LSStdlibCacheException {
        log.info("Test textDocument/definition for two modules");
        JsonObject configObject = FileUtils.fileContentAsObject(configRoot.resolve("multipkg")
                .resolve("defMultiPkg1.json").toString());
        JsonObject source = configObject.getAsJsonObject("source");
        String dirPath = source.get("dir").getAsString().replace("/", CommonUtil.FILE_SEPARATOR);
        Path sourcePath = projectPath.resolve(dirPath).resolve(source.get("file").getAsString());
        Position position = gson.fromJson(configObject.get("position"), Position.class);
        this.compareResults(sourcePath, position, configObject, projectPath);
    }

    protected void compareResults(Path sourcePath, Position position, JsonObject configObject, Path root)
            throws IOException, LSStdlibCacheException {
        TestUtil.openDocument(serviceEndpoint, sourcePath);
        String actualStr = TestUtil.getDefinitionResponse(sourcePath.toString(), position, serviceEndpoint);
        TestUtil.closeDocument(serviceEndpoint, sourcePath);

        JsonArray expected = configObject.getAsJsonArray("result");
        JsonArray actual = parser.parse(actualStr).getAsJsonObject().getAsJsonObject("result").getAsJsonArray("left");
        this.alterExpectedUri(expected, root);
        this.alterActualUri(actual);
        Assert.assertEquals(actual, expected);
    }

    @DataProvider
    private Object[][] testDataProvider() throws IOException {
        log.info("Test textDocument/definition for Basic Cases");
        return new Object[][]{
                {"defProject1.json", "project"},
                {"defProject2.json", "project"},
                {"defProject3.json", "project"},
                {"defProject4.json", "project"},
                {"defProject5.json", "project"},
                {"defProject6.json", "project"},
                {"defProject7.json", "project"},
        };
    }
    
    @AfterClass
    public void shutDownLanguageServer() throws IOException {
        TestUtil.shutdownLanguageServer(this.serviceEndpoint);
    }

    protected void alterExpectedUri(JsonArray expected, Path root) throws IOException, LSStdlibCacheException {
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
