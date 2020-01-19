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
package org.ballerinalang.langserver.highlighting;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.ballerinalang.langserver.compiler.exception.CompilationFailedException;
import org.ballerinalang.langserver.extensions.ballerina.semantichighlighter.SemanticHighlightingParams;
import org.ballerinalang.langserver.util.FileUtils;
import org.ballerinalang.langserver.util.TestUtil;
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
 * Test semantic highlighting feature in language server.
 *
 * @since 1.1.0
 */
public class HighlightingTest {

    private Endpoint serviceEndpoint;
    private Path sourceRoot = FileUtils.RES_DIR.resolve("highlighting").resolve("sources").resolve("endpoint");
    private Path expectedRoot = FileUtils.RES_DIR.resolve("highlighting").resolve("expected").resolve("endpoint");
    private JsonParser parser = new JsonParser();
    private static final Logger log = LoggerFactory.getLogger(HighlightingTest.class);

    @BeforeClass
    public void loadLangServer() throws IOException {
        serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    @Test(description = "Test highlighting endpoint variables", dataProvider = "endpointDataProvider")
    public void testEndpointHighlighting(String expectedFile, String sourceFile)
            throws IOException, CompilationFailedException {
        Path sourceFilePath = sourceRoot.resolve(sourceFile);
        this.compareResults(sourceFilePath, expectedFile);
    }

    private void compareResults(Path sourceFilePath, String expectedFile)
            throws IOException, CompilationFailedException {
        SemanticHighlightingParams semanticHighlightingParams = TestUtil.getHighlightingResponse(sourceFilePath);
        TestUtil.closeDocument(serviceEndpoint, sourceFilePath);

        //Convert response to JSON
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String actualStr = ow.writeValueAsString(semanticHighlightingParams);

        //Get expected value
        JsonObject expectedObject = FileUtils.fileContentAsObject(expectedRoot.resolve(expectedFile).toString());
        JsonArray expected = expectedObject.getAsJsonArray("result");

        //Compare actual vs expected
        JsonArray actual = parser.parse(actualStr).getAsJsonObject().getAsJsonArray("lines");
        Assert.assertEquals(actual, expected);
    }

    @DataProvider
    public Object[][] endpointDataProvider() throws IOException {
        log.info("Test endpoint variable highlighting");
        return new Object[][]{
                {"toplevel.json", "toplevel.bal"},
                {"ifelse.json", "ifelse.bal"},
                {"function.json", "function.bal"},
                {"foreach.json", "foreach.bal"},
                {"fork.json", "fork.bal"},
                {"match.json", "match.bal"},
                {"object.json", "object.bal"},
                {"record.json", "record.bal"},
                {"assignment.json", "assignment.bal"},
                {"return.json", "return.bal"},
                {"service.json", "service.bal"}
        };
    }

    @AfterClass
    public void shutDownLanguageServer() {
        TestUtil.shutdownLanguageServer(this.serviceEndpoint);
    }
}
