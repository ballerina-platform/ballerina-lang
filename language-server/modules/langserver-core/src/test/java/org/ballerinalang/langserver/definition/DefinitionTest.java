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
package org.ballerinalang.langserver.definition;

import org.ballerinalang.langserver.common.util.CommonUtil;
import org.eclipse.lsp4j.Position;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Test goto definition language server feature.
 */
public class DefinitionTest {
    private static final String METHOD = "textDocument/definition";
    private Path definitionsPath = new File(getClass().getClassLoader().getResource("definition").getFile()).toPath();
    private Path balPath1 = definitionsPath.resolve("test.definition.pkg").resolve("definition1.bal");
    private Path balPath2 = definitionsPath.resolve("test.definition.pkg").resolve("definition2.bal");
    private String balFile1Content;
    private String balFile2Content;

    @BeforeClass
    public void init() throws Exception {
        byte[] encoded1 = Files.readAllBytes(balPath1);
        balFile1Content = new String(encoded1);
        byte[] encoded2 = Files.readAllBytes(balPath2);
        balFile2Content = new String(encoded2);
    }

    @Test(description = "Test goto definition for local functions", dataProvider = "localFuncPosition")
    public void definitionForLocalFunctionsTest(Position position, DefinitionTestDataModel dataModel)
            throws InterruptedException, IOException {
        Assert.assertEquals(CommonUtil.getLanguageServerResponseMessageAsString(position,
                dataModel.getBallerinaFilePath(), dataModel.getBallerinaFileContent(), METHOD),
                getExpectedValue(dataModel.getExpectedFileName(), dataModel.getDefinitionFileURI()),
                "Did not match the definition content for " + dataModel.getExpectedFileName()
                        + " and position line:" + position.getLine() + " character:" + position.getCharacter());
    }

    @Test(description = "Test goto definition for records", dataProvider = "recordPositions")
    public void definitionForRecordsTest(Position position, DefinitionTestDataModel dataModel)
            throws InterruptedException, IOException {
        Assert.assertEquals(CommonUtil.getLanguageServerResponseMessageAsString(position,
                dataModel.getBallerinaFilePath(), dataModel.getBallerinaFileContent(), METHOD),
                getExpectedValue(dataModel.getExpectedFileName(), dataModel.getDefinitionFileURI()),
                "Did not match the definition content for " + dataModel.getExpectedFileName()
                        + " and position line:" + position.getLine() + " character:" + position.getCharacter());
    }

    @Test(description = "Test goto definition for readonly variables", dataProvider = "readOnlyVariablePositions")
    public void definitionForReadOnlyVariablesTest(Position position, DefinitionTestDataModel dataModel)
            throws InterruptedException, IOException {
        Assert.assertEquals(CommonUtil.getLanguageServerResponseMessageAsString(position,
                dataModel.getBallerinaFilePath(), dataModel.getBallerinaFileContent(), METHOD),
                getExpectedValue(dataModel.getExpectedFileName(), dataModel.getDefinitionFileURI()),
                "Did not match the definition content for " + dataModel.getExpectedFileName() +
                        " and position line:" + position.getLine() + " character:" + position.getCharacter());
    }

    @Test(description = "Test goto definition for local variables", dataProvider = "localVariablePositions",
            enabled = false)
    public void definitionForLocalVariablesTest(Position position, DefinitionTestDataModel dataModel)
            throws InterruptedException, IOException {
        Assert.assertEquals(CommonUtil.getLanguageServerResponseMessageAsString(position,
                dataModel.getBallerinaFilePath(), dataModel.getBallerinaFileContent(), METHOD),
                getExpectedValue(dataModel.getExpectedFileName(), dataModel.getDefinitionFileURI()),
                "Did not match the definition content for " + dataModel.getExpectedFileName() +
                        " and position line:" + position.getLine() + " character:" + position.getCharacter());
    }

    @DataProvider(name = "localFuncPosition")
    public Object[][] getLocalFunctionPositions() {
        return new Object[][]{
                {new Position(23, 7),
                        new DefinitionTestDataModel("localFunctionInSameFile.json",
                                balPath1.toUri().toString(), balPath1.toString(), balFile1Content)},
                {new Position(44, 7),
                        new DefinitionTestDataModel("localFunctionInAnotherFile.json",
                                balPath2.toUri().toString(), balPath1.toString(), balFile1Content)}
        };
    }

    @DataProvider(name = "recordPositions")
    public Object[][] getRecordPositions() {
        return new Object[][]{
                {new Position(36, 7),
                        new DefinitionTestDataModel("recordInSameFile.json",
                                balPath1.toUri().toString(), balPath1.toString(), balFile1Content)},
                {new Position(13, 7),
                        new DefinitionTestDataModel("recordInAnotherFile.json",
                                balPath2.toUri().toString(), balPath1.toString(), balFile1Content)}
        };
    }

    @DataProvider(name = "readOnlyVariablePositions")
    public Object[][] getReadOnlyVariablePositions() {
        return new Object[][]{
                {new Position(41, 53),
                        new DefinitionTestDataModel("readOnlyVariableInSameFile.json",
                                balPath1.toUri().toString(), balPath1.toString(), balFile1Content)},
                {new Position(11, 18),
                        new DefinitionTestDataModel("readOnlyVariableInAnotherFile.json",
                                balPath1.toUri().toString(), balPath2.toString(), balFile2Content)}
        };
    }

    @DataProvider(name = "localVariablePositions")
    public Object[][] getLocalVariablePositions() {
        return new Object[][]{
                {new Position(47, 9),
                        new DefinitionTestDataModel("localVariableInFunction.json",
                                balPath1.toUri().toString(), balPath1.toString(), balFile1Content)},
                {new Position(51, 12),
                        new DefinitionTestDataModel("localVariableInIfStatement.json",
                                balPath1.toUri().toString(), balPath1.toString(), balFile1Content)},
                {new Position(40, 10),
                        new DefinitionTestDataModel("localVariableInForeachStatement.json",
                                balPath1.toUri().toString(), balPath1.toString(), balFile1Content)},
                {new Position(39, 25),
                        new DefinitionTestDataModel("localVariableOnForeachStatement.json",
                                balPath1.toUri().toString(), balPath1.toString(), balFile1Content)},
                {new Position(39, 25),
                        new DefinitionTestDataModel("localVariableOfRecord.json",
                                balPath1.toUri().toString(), balPath1.toString(), balFile1Content)}
        };
    }

    /**
     * Get the expected value from the expected file.
     *
     * @param expectedFile    json file which contains expected content.
     * @param expectedFileURI string value of the expected file URI.
     * @return string content read from the json file.
     */
    private String getExpectedValue(String expectedFile, String expectedFileURI) throws IOException {
        Path expectedFilePath = definitionsPath.resolve("expected").resolve(expectedFile);

        byte[] expectedByte = Files.readAllBytes(expectedFilePath);
        String positionRange = new String(expectedByte);

        return "{\"id\":\"324\",\"result\":[{\"uri\":" +
                "\"" + expectedFileURI + "\"," +
                "\"range\":" + positionRange + "}]," +
                "\"jsonrpc\":\"2.0\"}";
    }
}
