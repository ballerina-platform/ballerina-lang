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
package org.ballerinalang.langserver.stnode;

import com.google.gson.JsonParser;
import org.ballerinalang.langserver.util.FileUtils;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Tests syntaxTreeNode API in Language Server.
 */
public class SyntaxTreeNodeTest {

    private static final String STRING_LITERAL = "STRING_LITERAL";
    private static final String MINUTIAE = "WHITESPACE_MINUTIAE";

    private Path resource;
    private Endpoint serviceEndpoint;

    @BeforeClass
    public void init() {
        this.resource = FileUtils.RES_DIR.resolve("stnode").resolve("stnode_test.bal");
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    @Test(description = "Test syntaxTreeNode API", dataProvider = "stnode-data-provider")
    public void syntaxTreeNodeTestCase(int start, int end, String expected) throws IOException {
        TestUtil.openDocument(serviceEndpoint, resource);
        Range range = new Range(new Position(start, end), new Position(start, end));
        String response = TestUtil.getSyntaxTreeNodeResponse(serviceEndpoint,
                this.resource.toAbsolutePath().toString(), range);
        String actual = JsonParser.parseString(response).getAsJsonObject().getAsJsonObject("result")
                .getAsJsonPrimitive("kind").getAsString();
        Assert.assertEquals(actual, expected,
                "Document syntaxTreeNode testcase failed for range " + start + " and " + end + ".");
    }

    @AfterClass
    public void cleanupLanguageServer() {
        TestUtil.shutdownLanguageServer(this.serviceEndpoint);
    }

    @DataProvider(name = "stnode-data-provider")
    public Object[][] getDataProvider() {
        return new Object[][]{
                {0, 0, MINUTIAE},
                {0, 1, "IMPORT_DECLARATION"},
                {2, 25, STRING_LITERAL},
                {4, 20, "FUNCTION_DEFINITION"},
                {5, 30, STRING_LITERAL},
                {8, 15, "CAPTURE_BINDING_PATTERN"},
                {9, 25, STRING_LITERAL},
                {14, 10, "MODULE_VAR_DECL"},
                {14, 30, STRING_LITERAL},
                {19, 10, "RETURN_STATEMENT"},
                {19, 30, STRING_LITERAL},
                {16, 5, "SERVICE_DECLARATION"},
                {12, 5, "RECORD_TYPE_DESC"},
                {8, 3, "MAP_TYPE_DESC"},
                {2, 5, "CONST_DECLARATION"},
                {18, 45, "RETURN_TYPE_DESCRIPTOR"},
                {9, 40, MINUTIAE},
                {19, 42, MINUTIAE}
        };
    }
}
