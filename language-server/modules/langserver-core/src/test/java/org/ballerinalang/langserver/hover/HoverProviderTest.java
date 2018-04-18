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

import org.ballerinalang.langserver.LSAnnotationCache;
import org.ballerinalang.langserver.common.util.CommonUtil;
import org.ballerinalang.langserver.compiler.LSContextManager;
import org.eclipse.lsp4j.Position;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Test hover feature in language server.
 */
@Test(groups = "broken")
public class HoverProviderTest {
    private static final String TESTS_SAMPLES = "src" + File.separator + "test" + File.separator + "resources"
            + File.separator + "hover";
    private static final String ROOT_DIR = Paths.get("").toAbsolutePath().toString() + File.separator;
    private static final String SAMPLES_COPY_DIR = ROOT_DIR + "samples" + File.separator + "hover";
    private static final String METHOD = "textDocument/hover";
    private String balPath = SAMPLES_COPY_DIR + File.separator + "hover.bal";
    private String balFileContent;

    @BeforeClass
    public void loadLangServer() throws IOException {
        File source = new File(TESTS_SAMPLES);
        File destination = new File(SAMPLES_COPY_DIR);
        org.apache.commons.io.FileUtils.copyDirectory(source, destination);
        byte[] encoded = Files.readAllBytes(Paths.get(balPath));
        balFileContent = new String(encoded);
        LSAnnotationCache.initiate();
    }
    
    @BeforeMethod
    public void clearPackageCache() {
        LSContextManager.getInstance().clearAllContexts();
    }

    @Test(description = "Test Hover for built in functions", dataProvider = "hoverBuiltinFuncPosition",
            enabled = false)
    public void hoverForBuiltInFunctionTest(Position position, String expectedFile)
            throws URISyntaxException, InterruptedException, IOException {
        Assert.assertEquals(CommonUtil.getLanguageServerResponseMessageAsString(position, balPath,
                balFileContent, METHOD), getExpectedValue(expectedFile), "Did not match the hover content for "
                + expectedFile + " and position line:" + position.getLine() + " character:" + position.getCharacter());
    }

    @Test(description = "Test Hover for current package's functions",
            dataProvider = "hoverCurrentPackageFuncPosition")
    public void hoverForCurrentPackageFunctionTest(Position position, String expectedFile)
            throws InterruptedException, IOException {
        Assert.assertEquals(CommonUtil.getLanguageServerResponseMessageAsString(position, balPath,
                balFileContent, METHOD), getExpectedValue(expectedFile), "Did not match the hover content for "
                + expectedFile + " and position line:" + position.getLine() + " character:" + position.getCharacter());
    }

    @Test(description = "Test Hover for current package's enums", dataProvider = "hoverCurrentPackageEnumPosition",
            enabled = false)
    public void hoverForCurrentPackageEnumTest(Position position, String expectedFile)
            throws InterruptedException, IOException {
        Assert.assertEquals(CommonUtil.getLanguageServerResponseMessageAsString(position, balPath,
                balFileContent, METHOD), getExpectedValue(expectedFile), "Did not match the hover content for "
                + expectedFile + " and position line:" + position.getLine() + " character:" + position.getCharacter());
    }

    @Test(description = "Test Hover for current package's records",
            dataProvider = "hoverCurrentPackageRecordPosition")
    public void hoverForCurrentPackageRecordTest(Position position, String expectedFile)
            throws InterruptedException, IOException {
        Assert.assertEquals(CommonUtil.getLanguageServerResponseMessageAsString(position, balPath,
                balFileContent, METHOD), getExpectedValue(expectedFile), "Did not match the hover content for "
                + expectedFile + " and position line:" + position.getLine() + " character:" + position.getCharacter());
    }

    @DataProvider(name = "hoverBuiltinFuncPosition")
    public Object[][] getBuiltinFunctionPositions() {
        return new Object[][]{
                {new Position(41, 7), "builtin-function1.json"},
                {new Position(42, 19), "builtin-function2.json"}
        };
    }

    @DataProvider(name = "hoverCurrentPackageFuncPosition")
    public Object[][] getCurrentPackageFunctionPositions() {
        return new Object[][]{
                {new Position(43, 15), "currentPkg-function1.json"}
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
        return new Object[][]{
                {new Position(44, 7), "currentPkg-record.json"},
                {new Position(49, 19), "currentPkg-record.json"},
                {new Position(50, 8), "currentPkg-record.json"}
        };
    }

    @AfterClass
    public void cleanSamplesCopy() throws IOException {
        org.apache.commons.io.FileUtils.deleteDirectory(new File(ROOT_DIR + "samples"));
    }

    /**
     * Get the expected value from the expected file.
     *
     * @param expectedFile json file which contains expected content.
     * @return string content read from the json file.
     */
    private String getExpectedValue(String expectedFile) throws IOException {
        String expectedFilePath = SAMPLES_COPY_DIR + File.separator + "expected" + File.separator + expectedFile;
        byte[] expectedByte = Files.readAllBytes(Paths.get(expectedFilePath));
        return new String(expectedByte);
    }
}
