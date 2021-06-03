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

import java.io.IOException;
import java.nio.file.Path;

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
        this.configRoot = FileUtils.RES_DIR.resolve("rename").resolve("expected").resolve("single");
        this.sourceRoot = FileUtils.RES_DIR.resolve("rename").resolve("sources").resolve("single");
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    @Test(description = "Test Rename", dataProvider = "testDataProvider")
    public void test(String resultJsonPath, String varName) throws IOException {
        JsonObject resultJson = FileUtils.fileContentAsObject(configRoot.resolve(resultJsonPath).toString());
        JsonObject source = resultJson.getAsJsonObject("source");
        Path sourcePath = sourceRoot.resolve(source.get("file").getAsString());
        Position position = gson.fromJson(resultJson.get("position"), Position.class);
        
        TestUtil.openDocument(serviceEndpoint, sourcePath);
        String actualStr = TestUtil.getRenameResponse(sourcePath.toString(), position, varName, serviceEndpoint);
        TestUtil.closeDocument(serviceEndpoint, sourcePath);
        JsonObject expected = resultJson.getAsJsonObject("result");
        JsonObject actual = parser.parse(actualStr).getAsJsonObject().getAsJsonObject("result");
        RenameTestUtil.alterExpectedUri(expected, this.sourceRoot);
        RenameTestUtil.alterActualUri(actual);
        Assert.assertEquals(actual, expected);
    }


    @DataProvider
    public Object[][] testDataProvider() throws IOException {
        return new Object[][]{
                {"rename_method_param_result.json", "newA"},
                {"rename_rec_result.json", "NewRecData"},
                {"rename_with_invalid_identifier.json", "NewRecData]"},
                {"rename_with_cursor_at_end.json", "myIntVal"},
                {"rename_with_cursor_at_end2.json", "x"},
                {"rename_with_cursor_at_end3.json", "CyclicUnionNew"},
                {"rename_with_cursor_at_end4.json", "IntegerList"},
                {"rename_on_fail.json", "myVarName"},
                {"rename_within_service1.json", "ep1"},
                {"rename_within_service2.json", "baz"},
                {"rename_within_service3.json", "baz"},
                {"rename_enum.json", "Color"},
                {"rename_enum_member.json", "DARK_RED"}
        };
    }

    @AfterClass
    public void shutDownLanguageServer() throws IOException {
        TestUtil.shutdownLanguageServer(this.serviceEndpoint);
    }
}
