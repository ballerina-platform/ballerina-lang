/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.rename;

import com.google.gson.Gson;
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
import java.nio.file.Paths;

/**
 * Test class for Renaming.
 *
 * @since 0.982.0
 */
public class RenameTest {
    private Path configRoot;
    private Path sourceRoot;
    protected Gson gson = new Gson();
    protected JsonParser parser = new JsonParser();
    protected Endpoint serviceEndpoint;

    @BeforeClass
    public void init() throws Exception {
        this.configRoot = FileUtils.RES_DIR.resolve("rename").resolve("expected");
        this.sourceRoot = FileUtils.RES_DIR.resolve("rename").resolve("sources");
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    @Test(description = "Test Rename", dataProvider = "testDataProvider")
    public void test(String configPath, String configDir) throws IOException {
        JsonObject configObject = FileUtils.fileContentAsObject(configRoot.resolve(configDir)
                .resolve(configPath).toString());
        JsonObject source = configObject.getAsJsonObject("source");
        Path sourcePath = sourceRoot.resolve(source.get("file").getAsString());
        Position position = gson.fromJson(configObject.get("position"), Position.class);

        TestUtil.openDocument(serviceEndpoint, sourcePath);
        String actualStr = TestUtil.getRenameResponse(sourcePath.toString(), position, "renamedVal", serviceEndpoint);
        TestUtil.closeDocument(serviceEndpoint, sourcePath);
        JsonObject expected = configObject.getAsJsonObject("result");
        JsonObject actual = parser.parse(actualStr).getAsJsonObject().getAsJsonObject("result");
        this.alterExpectedUri(expected);
        this.alterActualUri(actual);
        Assert.assertEquals(actual, expected);
    }


    @DataProvider
    public Object[][] testDataProvider() throws IOException {
        return new Object[][]{
                {"renameFunction1.json", "function"},
                {"renameObject1.json", "object"},
                {"renameRecord1.json", "record"}
        };
    }

    @AfterClass
    public void shutDownLanguageServer() throws IOException {
        TestUtil.shutdownLanguageServer(this.serviceEndpoint);
    }

    private void alterExpectedUri(JsonObject expected) throws IOException {
        JsonObject newChanges = new JsonObject();
        expected.getAsJsonObject("changes").entrySet().forEach(jEntry -> {
            String[] uriComponents = jEntry.getKey().replace("\"", "").split("/");
            Path expectedPath = Paths.get(this.sourceRoot.toUri());
            for (String uriComponent : uriComponents) {
                expectedPath = expectedPath.resolve(uriComponent);
            }
            try {
                newChanges.add(expectedPath.toFile().getCanonicalPath(), jEntry.getValue());
            } catch (IOException e) {
                // Ignore the exception
            }
        });
        expected.add("changes", newChanges);
    }

    private void alterActualUri(JsonObject actual) throws IOException {
        JsonObject newChanges = new JsonObject();
        actual.getAsJsonObject("changes").entrySet().forEach(jEntry -> {
            String uri = jEntry.getKey().replace("\"", "");
            try {
                String canonicalPath = new File(URI.create(uri)).getCanonicalPath();
                newChanges.add(canonicalPath, jEntry.getValue());
            } catch (IOException e) {
                // Ignore exception
            }
        });
        actual.add("changes", newChanges);
    }
}
