/*
 *  Copyright (c) 2023, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.xmltorecordconverter;

import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Tests for XMLToRecordConverter.
 */
public class XMLToRecordConverterTests {

    private static final Path RES_DIR = Paths.get("src/test/resources/").toAbsolutePath();
    private static final String XML_DIR = "xml";
    private static final String BAL_DIR = "ballerina";

    private final Path sample0XML = RES_DIR.resolve(XML_DIR)
            .resolve("sample_0.xml");
    private final Path sample0Bal = RES_DIR.resolve(BAL_DIR)
            .resolve("sample_0.bal");

    private final Path sample1XML = RES_DIR.resolve(XML_DIR)
            .resolve("sample_1.xml");
    private final Path sample1Bal = RES_DIR.resolve(BAL_DIR)
            .resolve("sample_1.bal");

    private final Path sample2XML = RES_DIR.resolve(XML_DIR)
            .resolve("sample_2.xml");
    private final Path sample2Bal = RES_DIR.resolve(BAL_DIR)
            .resolve("sample_2.bal");

    private final Path sample3XML = RES_DIR.resolve(XML_DIR)
            .resolve("sample_3.xml");
    private final Path sample3Bal = RES_DIR.resolve(BAL_DIR)
            .resolve("sample_3.bal");

    private final Path sample4XML = RES_DIR.resolve(XML_DIR)
            .resolve("sample_4.xml");
    private final Path sample4Bal = RES_DIR.resolve(BAL_DIR)
            .resolve("sample_4.bal");

    private static final String XMLToRecordServiceEP = "xmlToRecord/convert";


    @Test(description = "testBasicXML")
    public void testBasicXML() throws IOException {
        String xmlFileContent = Files.readString(sample0XML);
        String generatedCodeBlock = XMLToRecordConverter.convert(xmlFileContent, false, false, false, false)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample0Bal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "testBasicXMLWithTwoLevels")
    public void testBasicXMLWithTwoLevels() throws IOException {
        String xmlFileContent = Files.readString(sample1XML);
        String generatedCodeBlock = XMLToRecordConverter.convert(xmlFileContent, false, false, false, false)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample1Bal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "testBasicXMLWithTwoLevelsWithDifferentElements")
    public void testBasicXMLWithTwoLevelsWithDifferentElements() throws IOException {
        String xmlFileContent = Files.readString(sample2XML);
        String generatedCodeBlock = XMLToRecordConverter.convert(xmlFileContent, false, false, false, false)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample2Bal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "testBasicXMLWithNodesWithSameName")
    public void testBasicXMLWithNodesWithSameName() throws IOException {
        String xmlFileContent = Files.readString(sample3XML);
        String generatedCodeBlock = XMLToRecordConverter.convert(xmlFileContent, false, false, false, false)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample3Bal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "testBasicXMLForObjectArray")
    public void testBasicXMLForObjectArray() throws IOException {
        String xmlFileContent = Files.readString(sample4XML);
        String generatedCodeBlock = XMLToRecordConverter.convert(xmlFileContent, false, false, false, false)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample4Bal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "testXMLToRecordService")
    public void testXMLToRecordService() throws IOException, ExecutionException, InterruptedException {
        Endpoint serviceEndpoint = TestUtil.initializeLanguageSever();
        String xmlValue = Files.readString(sample0XML);

        XMLToRecordRequest request = new XMLToRecordRequest(xmlValue, false, false, false);
        CompletableFuture<?> result = serviceEndpoint.request(XMLToRecordServiceEP, request);
        XMLToRecordResponse response = (XMLToRecordResponse) result.get();
        String generatedCodeBlock = response.getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample0Bal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }
}
