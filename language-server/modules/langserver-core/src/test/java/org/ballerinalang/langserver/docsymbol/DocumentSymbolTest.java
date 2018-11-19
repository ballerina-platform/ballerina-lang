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
package org.ballerinalang.langserver.docsymbol;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.ballerinalang.langserver.completion.util.FileUtils;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Ballerina Language Server Workspace Test.
 * 
 * @since 0.982.0
 */
public class DocumentSymbolTest {
    
    private Endpoint serviceEndpoint;

    private JsonParser parser = new JsonParser();

    private Path sourcesPath = new File(getClass().getClassLoader().getResource("docsymbol").getFile()).toPath();

    @BeforeClass
    public void init() throws Exception {
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }
    
    @Test(description = "Test the Document symbol", dataProvider = "document-data-provider")
    public void testDocumentSymbol(String config, String source) throws IOException {
        String configJsonPath = "docsymbol" + File.separator + config;
        Path sourcePath = sourcesPath.resolve("source").resolve(source);
        TestUtil.openDocument(serviceEndpoint, sourcePath);
        JsonObject configJsonObject = FileUtils.fileContentAsObject(configJsonPath);
        JsonObject expected = configJsonObject.get("expected").getAsJsonObject();
        String response = TestUtil.getDocumentSymbolResponse(this.serviceEndpoint, sourcePath.toString());
        JsonObject jsonResponse = parser.parse(response).getAsJsonObject();
        JsonArray result = jsonResponse.getAsJsonArray("result");
        TestUtil.closeDocument(serviceEndpoint, sourcePath);
        for (JsonElement element : result) {
            element.getAsJsonObject().get("location").getAsJsonObject().remove("uri");
        }

        JsonArray expectedJsonArr = expected.getAsJsonArray("result");
        Assert.assertTrue(TestUtil.isArgumentsSubArray(expectedJsonArr, result)
                && expectedJsonArr.size() == result.size());
    }

    @DataProvider(name = "document-data-provider")
    public Object[][] documentSymbolDataProvider() {
        return new Object[][] {
                {"documentSymbol.json", "docSymbol.bal"},
        };
    }
    
    @AfterClass
    public void shutdownLanguageServer() {
        TestUtil.shutdownLanguageServer(this.serviceEndpoint);
    }
}
