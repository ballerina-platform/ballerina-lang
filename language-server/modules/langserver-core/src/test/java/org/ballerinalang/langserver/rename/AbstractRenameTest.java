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

import java.io.IOException;
import java.nio.file.Path;

/**
 * Abstract class to test rename functionality.
 */
public abstract class AbstractRenameTest {

    private Path configRoot;
    private Path sourceRoot;
    protected Gson gson = new Gson();
    protected Endpoint serviceEndpoint;

    @BeforeClass
    public void init() {
        this.configRoot = FileUtils.RES_DIR.resolve("rename").resolve("expected").resolve(configRoot());
        this.sourceRoot = FileUtils.RES_DIR.resolve("rename").resolve("sources").resolve(sourceRoot());
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    public void performTest(String configPath, String varName) throws IOException {
        JsonObject resultJson = FileUtils.fileContentAsObject(configRoot.resolve(configPath).toString());
        JsonObject source = resultJson.getAsJsonObject("source");
        Path sourcePath = sourceRoot.resolve(source.get("file").getAsString());
        Position position = gson.fromJson(resultJson.get("position"), Position.class);

        TestUtil.openDocument(serviceEndpoint, sourcePath);

        JsonObject prepareRename = resultJson.getAsJsonObject("prepareRename");
        boolean isValidRename = prepareRename.get("valid").getAsBoolean();
        String prepareResponse = TestUtil.getPrepareRenameResponse(sourcePath.toString(), position, serviceEndpoint);

        // For invalid positions, response result=null, and is removed by GSON.
        Assert.assertEquals(JsonParser.parseString(prepareResponse).getAsJsonObject().has("result"), isValidRename,
                "Expected valid rename position: " + String.valueOf(isValidRename));

        String actualStr = TestUtil.getRenameResponse(sourcePath.toString(), position, varName, serviceEndpoint);
        TestUtil.closeDocument(serviceEndpoint, sourcePath);
        JsonObject expected = resultJson.getAsJsonObject("result");
        JsonObject actual = JsonParser.parseString(actualStr).getAsJsonObject().getAsJsonObject("result");
        if (actual == null) {
            actual = new JsonObject();
        } else {

            RenameTestUtil.alterExpectedUri(expected, this.sourceRoot);
            RenameTestUtil.alterActualUri(actual);
        }
        Assert.assertEquals(actual, expected);
    }

    @AfterClass
    public void shutDownLanguageServer() {
        TestUtil.shutdownLanguageServer(this.serviceEndpoint);
    }

    protected abstract String configRoot();

    protected abstract String sourceRoot();
}
