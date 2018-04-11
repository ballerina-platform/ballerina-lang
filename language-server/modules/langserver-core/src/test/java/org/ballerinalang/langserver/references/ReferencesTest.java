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
package org.ballerinalang.langserver.references;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.ballerinalang.langserver.common.util.CommonUtil;
import org.eclipse.lsp4j.Position;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Test suit for testing find all references.
 */
@Test(groups = "broken")
public class ReferencesTest {
    private static final String TESTS_SAMPLES = "src" + File.separator + "test" + File.separator + "resources"
            + File.separator + "references";
    private static final String ROOT_DIR = Paths.get("").toAbsolutePath().toString() + File.separator;
    private static final String SAMPLES_COPY_DIR = ROOT_DIR + "samples";
    private static final String METHOD = "textDocument/references";
    private String balPath1 = SAMPLES_COPY_DIR + File.separator + "project" + File.separator + "references1.bal";
    private String balPath2 = SAMPLES_COPY_DIR + File.separator + "project" + File.separator + "pkg1" + File.separator
            + "references2.bal";
    private String balFileContent1;
    private String balFileContent2;

    @BeforeClass
    public void loadLangServer() throws IOException {
        File source = new File(TESTS_SAMPLES);
        File destination = new File(SAMPLES_COPY_DIR);
        org.apache.commons.io.FileUtils.copyDirectory(source, destination);
        File dotBallerinaDir = new File(ROOT_DIR + "samples" + File.separator + "project"
                + File.separator + ".ballerina");
        dotBallerinaDir.mkdir();
        byte[] encoded1 = Files.readAllBytes(Paths.get(balPath1));
        balFileContent1 = new String(encoded1);
        byte[] encoded2 = Files.readAllBytes(Paths.get(balPath2));
        balFileContent2 = new String(encoded2);
    }

    @Test(description = "Test references for function in the same file",
            dataProvider = "referencesForFunctionInSameFile")
    public void referencesForFunctionInSameFile(ReferencesTestDTO referencesTestDTO, Position position)
            throws InterruptedException, IOException {
        String expected = getExpectedValue(referencesTestDTO.getExpectedFileName());
        String actual = CommonUtil.getLanguageServerResponseMessageAsString(position,
                referencesTestDTO.getBallerinaFilePath(), referencesTestDTO.getBallerinaFileContent(), METHOD);
        Assert.assertEquals(actual, expected,
                "Did not match the definition content for " + referencesTestDTO.getExpectedFileName()
                        + " and position line:" + position.getLine() + " character:" + position.getCharacter());
    }


    @Test(description = "Test references for function in the same file",
            dataProvider = "referencesForFunctionInDifferentPkg", enabled = false)
    public void referencesForFunctionInDifferentPkg(ReferencesTestDTO referencesTestDTO, Position position)
            throws InterruptedException, IOException {
        String expected = getExpectedValue(referencesTestDTO.getExpectedFileName());
        String actual = CommonUtil.getLanguageServerResponseMessageAsString(position,
                referencesTestDTO.getBallerinaFilePath(), referencesTestDTO.getBallerinaFileContent(), METHOD);
        Assert.assertEquals(actual, expected,
                "Did not match the definition content for " + referencesTestDTO.getExpectedFileName()
                        + " and position line:" + position.getLine() + " character:" + position.getCharacter());
    }

    @Test(description = "Test references for record in the same file",
            dataProvider = "referencesForRecordInSameFile")
    public void referencesForRecordInSameFile(ReferencesTestDTO referencesTestDTO, Position position)
            throws InterruptedException, IOException {
        String expected = getExpectedValue(referencesTestDTO.getExpectedFileName());
        String actual = CommonUtil.getLanguageServerResponseMessageAsString(position,
                referencesTestDTO.getBallerinaFilePath(), referencesTestDTO.getBallerinaFileContent(), METHOD);
        Assert.assertEquals(actual, expected,
                "Did not match the definition content for " + referencesTestDTO.getExpectedFileName()
                        + " and position line:" + position.getLine() + " character:" + position.getCharacter());
    }

    @Test(description = "Test references for readonly var in the same file",
            dataProvider = "referencesForReadOnlyVarInSameFile")
    public void referencesForReadOnlyVarInSameFile(ReferencesTestDTO referencesTestDTO, Position position)
            throws InterruptedException, IOException {
        String expected = getExpectedValue(referencesTestDTO.getExpectedFileName());
        String actual = CommonUtil.getLanguageServerResponseMessageAsString(position,
                referencesTestDTO.getBallerinaFilePath(), referencesTestDTO.getBallerinaFileContent(), METHOD);
        Assert.assertEquals(actual, expected,
                "Did not match the definition content for " + referencesTestDTO.getExpectedFileName()
                        + " and position line:" + position.getLine() + " character:" + position.getCharacter());
    }

    @DataProvider
    public Object[][] referencesForFunctionInSameFile() {
        return new Object[][]{
                {new ReferencesTestDTO("functionReferencesInSameFile.json", balPath1, balFileContent1),
                        new Position(15, 12)}
        };
    }

    @DataProvider
    public Object[][] referencesForFunctionInDifferentPkg() {
        return new Object[][]{
                {new ReferencesTestDTO("functionReferencesInDifferentPkg.json", balPath1,
                        balFileContent1), new Position(16, 33)}
        };
    }

    @DataProvider
    public Object[][] referencesForRecordInSameFile() {
        return new Object[][]{
                {new ReferencesTestDTO("recordReferencesInSameFile.json", balPath1,
                        balFileContent1), new Position(14, 33)},
                {new ReferencesTestDTO("recordReferencesInSameFile.json", balPath1,
                        balFileContent1), new Position(18, 8)},
                {new ReferencesTestDTO("recordVarReferencesInSameFile.json", balPath1,
                        balFileContent1), new Position(23, 26)},
                {new ReferencesTestDTO("recordVarReferencesInSameFile.json", balPath1,
                        balFileContent1), new Position(24, 15)}
        };
    }

    @DataProvider
    public Object[][] referencesForReadOnlyVarInSameFile() {
        return new Object[][]{
                {new ReferencesTestDTO("readOnlyVarInSameFile.json", balPath2,
                        balFileContent2), new Position(3, 15)}
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
        String balPath1URI = Paths.get(balPath1).toUri().toString();
        String balPath2URI = Paths.get(balPath2).toUri().toString();
        String expectedContent = new String(expectedByte);
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(expectedContent).getAsJsonObject();

        JsonArray jsonArray = jsonObject.getAsJsonArray("result");
        for (JsonElement jsonElement : jsonArray) {
            JsonObject location = jsonElement.getAsJsonObject();
            String uri = location.get("uri").toString();
            if (uri.contains("references1.bal")) {
                location.addProperty("uri", balPath1URI);
            } else {
                location.addProperty("uri", balPath2URI);
            }
        }
        return jsonObject.toString();
    }
}
