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
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.completion.util.FileUtils;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Test suit for testing find all references.
 */
public class ReferencesTest {
    private Path balPath1 = FileUtils.RES_DIR.resolve("references").resolve("project").resolve("references1.bal");
    private Path balPath2 = FileUtils.RES_DIR.resolve("references").resolve("project").resolve("pkg1")
            .resolve("references2.bal");
    private Path balPath3 = FileUtils.RES_DIR.resolve("references").resolve("project").resolve("pkg1").resolve("tests")
            .resolve("test1.bal");
    private Endpoint serviceEndpoint;

    @BeforeClass
    public void loadLangServer() throws IOException {
        serviceEndpoint = TestUtil.initializeLanguageSever();
        TestUtil.openDocument(serviceEndpoint, balPath1);
        TestUtil.openDocument(serviceEndpoint, balPath2);
    }

    @Test(description = "Test Find references", dataProvider = "referencesDataProvider")
    public void testReferences(ReferencesTestDTO referencesTestDTO, Position position) throws IOException {
        String expected = getExpectedValue(referencesTestDTO.getExpectedFileName());
        String actual = TestUtil.getReferencesResponse(referencesTestDTO.getBallerinaFilePath(), position,
                serviceEndpoint);
        Assert.assertEquals(matchReferences(expected, actual), true, 
                "Did not match the references content for " + referencesTestDTO.getExpectedFileName() 
                        + " and position line:" + position.getLine() + " character:" + position.getCharacter()
                        + "[expected: " + expected + "]" + CommonUtil.LINE_SEPARATOR + "[actual: " + actual + "]");
    }

    @AfterClass
    public void shutDownLanguageServer() throws IOException {
        TestUtil.closeDocument(serviceEndpoint, balPath1);
        TestUtil.closeDocument(serviceEndpoint, balPath2);
        TestUtil.shutdownLanguageServer(this.serviceEndpoint);
    }

    @DataProvider
    public Object[][] referencesDataProvider() throws IOException {
        return new Object[][]{
                {new ReferencesTestDTO("functionReferencesInSameFile.json", balPath1), new Position(15, 12)},
                {new ReferencesTestDTO("functionReferencesInDifferentPkg.json", balPath1), new Position(16, 33)},
                {new ReferencesTestDTO("recordReferencesInSameFile.json", balPath1), new Position(14, 33)},
                {new ReferencesTestDTO("recordReferencesInSameFile.json", balPath1), new Position(18, 8)},
                {new ReferencesTestDTO("recordVarReferencesInSameFile.json", balPath1), new Position(23, 26)},
                {new ReferencesTestDTO("recordVarReferencesInSameFile.json", balPath1), new Position(24, 15)},
                {new ReferencesTestDTO("readOnlyVarInSameFile.json", balPath2), new Position(3, 15)},
                {new ReferencesTestDTO("referencesInTests.json", balPath2), new Position(6, 22)}
        };
    }

    private boolean matchReferences(String expected, String actual) {
        JsonParser parser = new JsonParser();
        JsonArray expectedResult = parser.parse(expected).getAsJsonObject().get("result").getAsJsonArray();
        JsonArray actualResult = parser.parse(actual).getAsJsonObject().get("result").getAsJsonArray();
        
        if (expectedResult.size() != actualResult.size()) {
            return false;
        }

        for (int i = 0; i < expectedResult.size(); i++) {
            if (!actualResult.contains(expectedResult.get(i))) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * Get the expected value from the expected file.
     *
     * @param expectedFile json file which contains expected content.
     * @return string content read from the json file.
     */
    private String getExpectedValue(String expectedFile) throws IOException {
        Path expectedFilePath = FileUtils.RES_DIR.resolve("references").resolve("expected").resolve(expectedFile);
        byte[] expectedByte = Files.readAllBytes(expectedFilePath);
        String balPath1URI = balPath1.toUri().toString();
        String balPath2URI = balPath2.toUri().toString();
        String balPath3URI = balPath3.toUri().toString();
        String expectedContent = new String(expectedByte);
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(expectedContent).getAsJsonObject();

        JsonArray jsonArray = jsonObject.getAsJsonArray("result");
        for (JsonElement jsonElement : jsonArray) {
            JsonObject location = jsonElement.getAsJsonObject();
            String uri = location.get("uri").toString();
            if (uri.contains("references1.bal")) {
                location.addProperty("uri", balPath1URI);
            } else if (uri.contains("test1.bal")) {
                location.addProperty("uri", balPath3URI);
            } else {
                location.addProperty("uri", balPath2URI);
            }
        }
        return jsonObject.toString();
    }
}
