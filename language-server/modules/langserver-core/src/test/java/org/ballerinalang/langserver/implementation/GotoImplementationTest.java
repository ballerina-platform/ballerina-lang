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
package org.ballerinalang.langserver.implementation;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.ballerinalang.langserver.common.utils.CommonUtil;
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

import java.nio.file.Path;

/**
 * Tests for goto implementation operation.
 *
 * @since 0.990.3
 */
public class GotoImplementationTest {
    private Endpoint serviceEndpoint;
    private String configRoot = "implementation" + CommonUtil.FILE_SEPARATOR + "expected";
    private JsonParser jsonParser = new JsonParser();
    private static final Logger log = LoggerFactory.getLogger(GotoImplementationTest.class);

    @BeforeClass
    public void init() {
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    @Test(dataProvider = "goto-impl-data-provider")
    public void testGotoImplementation(String configPath, String source) {
        Path sourcePath = FileUtils.RES_DIR.resolve(source);
        try {
            TestUtil.openDocument(serviceEndpoint, sourcePath);
            JsonObject configJsonObject = FileUtils.fileContentAsObject(configRoot + CommonUtil.FILE_SEPARATOR
                    + configPath);
            JsonObject position = configJsonObject.getAsJsonObject("position");
            Position cursor = new Position(position.get("line").getAsInt(), position.get("col").getAsInt());
            String response = TestUtil.getGotoImplementationResponse(serviceEndpoint, sourcePath.toString(), cursor);
            JsonObject responseJson = jsonParser.parse(response).getAsJsonObject();
            responseJson.getAsJsonArray("result").forEach(jsonElement -> jsonElement.getAsJsonObject().remove("uri"));
            JsonObject expected = configJsonObject.getAsJsonObject("expected");
            Assert.assertEquals(expected, responseJson, "Test Failed for" + configPath);

        } catch (Exception e) {
            // Ignore
        } finally {
            TestUtil.closeDocument(serviceEndpoint, sourcePath);
        }
    }

    @DataProvider(name = "goto-impl-data-provider")
    public Object[][] dataProvider() {
        log.info("Test textDocument/implementation");
        String sourcePath1 = "implementation" + CommonUtil.FILE_SEPARATOR + "source" + CommonUtil.FILE_SEPARATOR
                + "gotoImplProject" + CommonUtil.FILE_SEPARATOR + "src" + CommonUtil.FILE_SEPARATOR  + "pkg1"
                + CommonUtil.FILE_SEPARATOR + "main.bal";
        String sourcePath2 = "implementation" + CommonUtil.FILE_SEPARATOR + "source" + CommonUtil.FILE_SEPARATOR
                + "gotoImplProject" + CommonUtil.FILE_SEPARATOR + "src" + CommonUtil.FILE_SEPARATOR  + "pkg1"
                + CommonUtil.FILE_SEPARATOR + "tests" + CommonUtil.FILE_SEPARATOR + "main_test.bal";
        return new Object[][]{
                {"gotoImplTest1.json", sourcePath1},
                {"gotoImplTest2.json", sourcePath1},
                {"gotoImplTest3.json", sourcePath2}
        };
    }

    @AfterClass
    public void cleanupLanguageServer() {
        TestUtil.shutdownLanguageServer(this.serviceEndpoint);
    }
}
