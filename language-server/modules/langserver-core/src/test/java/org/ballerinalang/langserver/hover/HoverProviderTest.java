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
package org.ballerinalang.langserver.hover;

import com.google.gson.JsonParser;
import org.ballerinalang.langserver.compiler.LSContextManager;
import org.ballerinalang.langserver.util.FileUtils;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Test hover feature in language server.
 */
public class HoverProviderTest {
    private Path balPath = FileUtils.RES_DIR.resolve("hover").resolve("hover.bal");
    private Endpoint serviceEndpoint;
    private JsonParser parser = new JsonParser();
    private static final Logger log = LoggerFactory.getLogger(HoverProviderTest.class);

    @BeforeClass
    public void loadLangServer() throws IOException {
        serviceEndpoint = TestUtil.initializeLanguageSever();
        TestUtil.openDocument(serviceEndpoint, balPath);
    }
    
    @BeforeMethod
    public void clearPackageCache() {
        LSContextManager.getInstance().clearAllContexts();
    }

    @Test(description = "Test Hover for built in functions", dataProvider = "hoverBuiltinFuncPosition")
    public void hoverForBuiltInFunctionTest(Position position, String expectedFile) throws IOException {
        String response = TestUtil.getHoverResponse(balPath.toString(), position, serviceEndpoint);
        String expected = getExpectedValue(expectedFile);

        Assert.assertEquals(parser.parse(expected).getAsJsonObject(), parser.parse(response).getAsJsonObject(),
                "Did not match the hover content for " + expectedFile + " and position line:" + position.getLine()
                + " character:" + position.getCharacter());
    }

    @Test(description = "Test Hover for current package's functions", dataProvider = "hoverCurrentPackageFuncPosition")
    public void hoverForCurrentPackageFunctionTest(Position position, String expectedFile) throws IOException {
        String response = TestUtil.getHoverResponse(balPath.toString(), position, serviceEndpoint);
        String expected = getExpectedValue(expectedFile);

        Assert.assertEquals(parser.parse(expected).getAsJsonObject(), parser.parse(response).getAsJsonObject(),
                "Did not match the hover content for " + expectedFile + " and position line:" + position.getLine()
                        + " character:" + position.getCharacter());
    }

    @Test(description = "Test Hover for current package's records", dataProvider = "hoverCurrentPackageRecordPosition")
    public void hoverForCurrentPackageRecordTest(Position position, String expectedFile) throws IOException {
        String response = TestUtil.getHoverResponse(balPath.toString(), position, serviceEndpoint);
        String expected = getExpectedValue(expectedFile);

        Assert.assertEquals(parser.parse(expected).getAsJsonObject(), parser.parse(response).getAsJsonObject(),
                "Did not match the hover content for " + expectedFile + " and position line:" + position.getLine()
                        + " character:" + position.getCharacter());
    }

    @Test(description = "Test Hover for stdlib actions", dataProvider = "hoverActionPosition")
    public void hoverForActionInvocationTest(Position position, String expectedFile) throws IOException {
        String response = TestUtil.getHoverResponse(balPath.toString(), position, serviceEndpoint);
        String expected = getExpectedValue(expectedFile);

        Assert.assertEquals(parser.parse(expected).getAsJsonObject(), parser.parse(response).getAsJsonObject(),
                "Did not match the hover content for " + expectedFile + " and position line:" + position.getLine()
                        + " character:" + position.getCharacter());
    }

    @Test(description = "Test Hover for stdlib actions", dataProvider = "hoverAnnotationPosition")
    public void hoverForAnnotationsTest(Position position, String expectedFile) throws IOException {
        String response = TestUtil.getHoverResponse(balPath.toString(), position, serviceEndpoint);
        String expected = getExpectedValue(expectedFile);

        Assert.assertEquals(parser.parse(expected).getAsJsonObject(), parser.parse(response).getAsJsonObject(),
                "Did not match the hover content for " + expectedFile + " and position line:" + position.getLine()
                        + " character:" + position.getCharacter());
    }

    @AfterClass
    public void shutDownLanguageServer() {
        TestUtil.closeDocument(this.serviceEndpoint, balPath);
        TestUtil.shutdownLanguageServer(this.serviceEndpoint);
    }

    @DataProvider(name = "hoverBuiltinFuncPosition")
    public Object[][] getBuiltinFunctionPositions() {
        log.info("Test textDocument/hover for builtin functions");
        return new Object[][]{
                {new Position(43, 11), "builtin-function1.json"},
                {new Position(44, 19), "builtin-function2.json"},
                {new Position(59, 60), "hoverOverConstant.json"},
                {new Position(57, 35), "builtin-service1.json"}
        };
    }

    @DataProvider(name = "hoverCurrentPackageFuncPosition")
    public Object[][] getCurrentPackageFunctionPositions() {
        log.info("Test textDocument/hover for current package functions");
        return new Object[][]{
                {new Position(45, 14), "currentPkg-function1.json"}
        };
    }

    @DataProvider(name = "hoverCurrentPackageEnumPosition")
    public Object[][] getCurrentPackageEnumPositions() {
        return new Object[][]{
                {new Position(31, 20), "currentPkg-enum.json"},
                {new Position(32, 7), "currentPkg-enum.json"},
                {new Position(32, 20), "currentPkg-enum.json"},
                {new Position(33, 12), "currentPkg-enum.json"},
                {new Position(34, 8), "currentPkg-enum.json"},
                {new Position(34, 14), "currentPkg-enum.json"}
        };
    }

    @DataProvider(name = "hoverCurrentPackageRecordPosition")
    public Object[][] getCurrentPackageStructPositions() {
        log.info("Test textDocument/hover for current package records");
        return new Object[][]{
                {new Position(46, 7), "currentPkg-record.json"},
                {new Position(51, 22), "currentPkg-record.json"},
                {new Position(52, 11), "currentPkg-record.json"}
        };
    }

    @DataProvider(name = "hoverAnnotationPosition")
    public Object[][] getAnnotationPositions() {
        log.info("Test textDocument/hover for Annotations");
        return new Object[][]{
                {new Position(68, 13), "annotations.json"}
        };
    }

    @DataProvider(name = "hoverActionPosition")
    public Object[][] getActionPositions() {
        log.info("Test textDocument/hover for actions");
        return new Object[][]{
                {new Position(65, 60), "hover-over-async-send.json"},
        };
    }

    /**
     * Get the expected value from the expected file.
     *
     * @param expectedFile json file which contains expected content.
     * @return string content read from the json file.
     */
    private String getExpectedValue(String expectedFile) throws IOException {
        Path expectedFilePath = FileUtils.RES_DIR.resolve("hover").resolve("expected").resolve(expectedFile);
        byte[] expectedByte = Files.readAllBytes(expectedFilePath);
        return new String(expectedByte);
    }
}
