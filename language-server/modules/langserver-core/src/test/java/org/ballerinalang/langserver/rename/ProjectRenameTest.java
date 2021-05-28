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
package org.ballerinalang.langserver.rename;

import com.google.gson.Gson;
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

import java.io.IOException;
import java.nio.file.Path;

/**
 * Test renaming across multiple files in a ballerina project.
 */
public class ProjectRenameTest {

    protected Path configRoot;
    protected Path sourceRoot;
    protected Gson gson = new Gson();
    protected JsonParser parser = new JsonParser();
    protected Endpoint serviceEndpoint;
    private static final Logger log = LoggerFactory.getLogger(ProjectRenameTest.class);

    @BeforeClass
    public void init() throws Exception {
        configRoot = FileUtils.RES_DIR.resolve("rename").resolve("expected").resolve("project");
        sourceRoot = FileUtils.RES_DIR.resolve("rename").resolve("sources").resolve("project");
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    @Test(description = "Test reference", dataProvider = "testDataProvider")
    public void test(String configPath, String varName) throws IOException {
        JsonObject configObject = FileUtils.fileContentAsObject(configRoot.resolve(configPath).toString());
        JsonObject source = configObject.getAsJsonObject("source");
        Path sourcePath = sourceRoot.resolve(source.get("file").getAsString());
        Position position = gson.fromJson(configObject.get("position"), Position.class);

        TestUtil.openDocument(serviceEndpoint, sourcePath);
        String actualStr = TestUtil.getRenameResponse(sourcePath.toString(), position, varName, serviceEndpoint);
        TestUtil.closeDocument(serviceEndpoint, sourcePath);

        JsonObject expected = configObject.getAsJsonObject("result");
        JsonObject actual = parser.parse(actualStr).getAsJsonObject().get("result").getAsJsonObject();
        RenameTestUtil.alterExpectedUri(expected, this.sourceRoot);
        RenameTestUtil.alterActualUri(actual);

        Assert.assertEquals(actual, expected);
    }

    @DataProvider
    private Object[][] testDataProvider() {
        log.info("Test textDocument/definition for Basic Cases");
        return new Object[][]{
                {"rename_class_result.json", "Student"},
                {"rename_function_result.json", "getStudents"},
                {"rename_global_var_result.json", "path"},
                {"rename_error_config1.json", "Mod2Error"},
                // TODO type Err error; type symbols cannot be renamed due to #30688
                // {"rename_error_config2.json", "Mod2Error"},
                {"rename_package_alias_result1.json", "mod1"},
                {"rename_package_alias_result2.json", "mod"},
                {"rename_package_alias_result3.json", "mod"},
                {"rename_package_alias_result4.json", "mod1"},
                
                // Negative/invalid cases
                {"rename_keyword_result1.json", "fn"},
                {"rename_invalid_token_result1.json", "invalid"},
        };
    }

    @AfterClass
    public void shutDownLanguageServer() throws IOException {
        TestUtil.shutdownLanguageServer(this.serviceEndpoint);
    }
}
