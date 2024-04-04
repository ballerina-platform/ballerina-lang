/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com)
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

    private final Path sample5XML = RES_DIR.resolve(XML_DIR)
            .resolve("sample_5.xml");
    private final Path sample5Bal = RES_DIR.resolve(BAL_DIR)
            .resolve("sample_5.bal");

    private final Path sample6XML = RES_DIR.resolve(XML_DIR)
            .resolve("sample_6.xml");
    private final Path sample6Bal = RES_DIR.resolve(BAL_DIR)
            .resolve("sample_6.bal");

    private final Path sample7XML = RES_DIR.resolve(XML_DIR)
            .resolve("sample_7.xml");
    private final Path sample7Bal = RES_DIR.resolve(BAL_DIR)
            .resolve("sample_7.bal");

    private final Path sample8XML = RES_DIR.resolve(XML_DIR)
            .resolve("sample_8.xml");
    private final Path sample8Bal = RES_DIR.resolve(BAL_DIR)
            .resolve("sample_8.bal");

    private final Path sample9XML = RES_DIR.resolve(XML_DIR)
            .resolve("sample_9.xml");
    private final Path sample9Bal = RES_DIR.resolve(BAL_DIR)
            .resolve("sample_9.bal");

    private final Path sample10XML = RES_DIR.resolve(XML_DIR)
            .resolve("sample_10.xml");
    private final Path sample10Bal = RES_DIR.resolve(BAL_DIR)
            .resolve("sample_10.bal");

    private final Path sample11XML = RES_DIR.resolve(XML_DIR)
            .resolve("sample_11.xml");
    private final Path sample11Bal = RES_DIR.resolve(BAL_DIR)
            .resolve("sample_11.bal");

    private final Path sample12XML = RES_DIR.resolve(XML_DIR)
            .resolve("sample_12.xml");
    private final Path sample12Bal = RES_DIR.resolve(BAL_DIR)
            .resolve("sample_12.bal");

    private final Path sample13XML = RES_DIR.resolve(XML_DIR)
            .resolve("sample_13.xml");
    private final Path sample13Bal = RES_DIR.resolve(BAL_DIR)
            .resolve("sample_13.bal");

    private final Path sample14XML = RES_DIR.resolve(XML_DIR)
            .resolve("sample_14.xml");
    private final Path sample14Bal = RES_DIR.resolve(BAL_DIR)
            .resolve("sample_14.bal");

    private final Path sample15XML = RES_DIR.resolve(XML_DIR)
            .resolve("sample_15.xml");
    private final Path sample15Bal = RES_DIR.resolve(BAL_DIR)
            .resolve("sample_15.bal");

    private final Path sample16XML = RES_DIR.resolve(XML_DIR)
            .resolve("sample_16.xml");
    private final Path sample16Bal = RES_DIR.resolve(BAL_DIR)
            .resolve("sample_16.bal");

    private final Path sample17XML = RES_DIR.resolve(XML_DIR)
            .resolve("sample_17.xml");
    private final Path sample17Bal = RES_DIR.resolve(BAL_DIR)
            .resolve("sample_17.bal");

    private final Path sample18XML = RES_DIR.resolve(XML_DIR)
            .resolve("sample_18.xml");
    private final Path sample18Bal = RES_DIR.resolve(BAL_DIR)
            .resolve("sample_18.bal");

    private final Path sample19XML = RES_DIR.resolve(XML_DIR)
            .resolve("sample_19.xml");
    private final Path sample19Bal = RES_DIR.resolve(BAL_DIR)
            .resolve("sample_19.bal");

    private final Path sample20XML = RES_DIR.resolve(XML_DIR)
            .resolve("sample_20.xml");
    private final Path sample20Bal = RES_DIR.resolve(BAL_DIR)
            .resolve("sample_20.bal");

    private final Path sample21XML = RES_DIR.resolve(XML_DIR)
            .resolve("sample_21.xml");
    private final Path sample21Bal = RES_DIR.resolve(BAL_DIR)
            .resolve("sample_21.bal");

    private final Path sample22XML = RES_DIR.resolve(XML_DIR)
            .resolve("sample_22.xml");
    private final Path sample22Bal = RES_DIR.resolve(BAL_DIR)
            .resolve("sample_22.bal");

    private final Path sample23XML = RES_DIR.resolve(XML_DIR)
            .resolve("sample_23.xml");
    private final Path sample23Bal = RES_DIR.resolve(BAL_DIR)
            .resolve("sample_23.bal");

    private final Path sample24XML = RES_DIR.resolve(XML_DIR)
            .resolve("sample_24.xml");
    private final Path sample24Bal = RES_DIR.resolve(BAL_DIR)
            .resolve("sample_24.bal");

    private static final String XMLToRecordServiceEP = "xmlToRecord/convert";


    @Test(description = "testBasicXML")
    public void testBasicXML() throws IOException {
        String xmlFileContent = Files.readString(sample0XML);
        String generatedCodeBlock = XMLToRecordConverter.convert(xmlFileContent, false, false, false)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample0Bal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "testBasicXMLWithTwoLevels")
    public void testBasicXMLWithTwoLevels() throws IOException {
        String xmlFileContent = Files.readString(sample1XML);
        String generatedCodeBlock = XMLToRecordConverter.convert(xmlFileContent, false, false, false)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample1Bal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "testBasicXMLWithTwoLevelsWithDifferentElements")
    public void testBasicXMLWithTwoLevelsWithDifferentElements() throws IOException {
        String xmlFileContent = Files.readString(sample2XML);
        String generatedCodeBlock = XMLToRecordConverter.convert(xmlFileContent, false, false, false)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample2Bal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "testBasicXMLWithNodesWithSameName")
    public void testBasicXMLWithNodesWithSameName() throws IOException {
        String xmlFileContent = Files.readString(sample3XML);
        String generatedCodeBlock = XMLToRecordConverter.convert(xmlFileContent, false, false, false)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample3Bal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "testBasicXMLForObjectArray")
    public void testBasicXMLForObjectArray() throws IOException {
        String xmlFileContent = Files.readString(sample4XML);
        String generatedCodeBlock = XMLToRecordConverter.convert(xmlFileContent, false, false, false)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample4Bal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "testBasicXMLWithNodesWithSameNameAndDifferentDataTypes")
    public void testBasicXMLWithNodesWithSameNameAndDifferentDataTypes() throws IOException {
        String xmlFileContent = Files.readString(sample5XML);
        String generatedCodeBlock = XMLToRecordConverter.convert(xmlFileContent, false, false, false)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample5Bal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "testBasicXMLWithNodesWithPrimitiveAndOtherDataTypes")
    public void testBasicXMLWithNodesWithPrimitiveAndOtherDataTypes() throws IOException {
        String xmlFileContent = Files.readString(sample6XML);
        String generatedCodeBlock = XMLToRecordConverter.convert(xmlFileContent, false, false, false)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample6Bal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "testBasicXMLWithMultipleLevelsOfNodes")
    public void testBasicXMLWithMultipleLevelsOfNodes() throws IOException {
        String xmlFileContent = Files.readString(sample7XML);
        String generatedCodeBlock = XMLToRecordConverter.convert(xmlFileContent, false, false, false)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample7Bal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "testBasicXMLWithNamespace")
    public void testBasicXMLWithNamespace() throws IOException {
        String xmlFileContent = Files.readString(sample8XML);
        String generatedCodeBlock = XMLToRecordConverter.convert(xmlFileContent, false, false, false)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample8Bal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "testComplexXMLWithNamespace")
    public void testComplexXMLWithNamespace() throws IOException {
        String xmlFileContent = Files.readString(sample9XML);
        String generatedCodeBlock = XMLToRecordConverter.convert(xmlFileContent, false, false, false)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample9Bal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "testXMLWithOptionalFields1")
    public void testXMLWithOptionalFields1() throws IOException {
        String xmlFileContent = Files.readString(sample10XML);
        String generatedCodeBlock = XMLToRecordConverter.convert(xmlFileContent, false, false, false)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample10Bal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "testXMLWithOptionalFields2")
    public void testXMLWithOptionalFields2() throws IOException {
        String xmlFileContent = Files.readString(sample11XML);
        String generatedCodeBlock = XMLToRecordConverter.convert(xmlFileContent, false, false, false)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample11Bal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "testXMLWithOptionalFieldsInNestedNodes")
    public void testXMLWithOptionalFieldsInNestedNodes() throws IOException {
        String xmlFileContent = Files.readString(sample12XML);
        String generatedCodeBlock = XMLToRecordConverter.convert(xmlFileContent, false, false, false)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample12Bal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "testXMLWithOptionalFieldsInMultipleNestedNodes")
    public void testXMLWithOptionalFieldsInMultipleNestedNodes() throws IOException {
        String xmlFileContent = Files.readString(sample13XML);
        String generatedCodeBlock = XMLToRecordConverter.convert(xmlFileContent, false, false, false)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample13Bal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "testXMLWithMultipleNamespaces")
    public void testXMLWithMultipleNamespaces() throws IOException {
        String xmlFileContent = Files.readString(sample14XML);
        String generatedCodeBlock = XMLToRecordConverter.convert(xmlFileContent, false, false, false)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample14Bal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "testComplexXMLWithMultipleNamespaces")
    public void testComplexXMLWithMultipleNamespaces() throws IOException {
        String xmlFileContent = Files.readString(sample15XML);
        String generatedCodeBlock = XMLToRecordConverter.convert(xmlFileContent, false, false, false)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample15Bal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "testComplexXMLWithMultipleNamespacesAndRecurringNodes")
    public void testComplexXMLWithMultipleNamespacesAndRecurringNodes() throws IOException {
        String xmlFileContent = Files.readString(sample16XML);
        String generatedCodeBlock = XMLToRecordConverter.convert(xmlFileContent, false, false, false)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample16Bal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "testWithAttribute")
    public void testWithAttribute() throws IOException {
        String xmlFileContent = Files.readString(sample17XML);
        String generatedCodeBlock = XMLToRecordConverter.convert(xmlFileContent, false, false, false)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample17Bal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "testWithMultipleAttributes")
    public void testWithMultipleAttributes() throws IOException {
        String xmlFileContent = Files.readString(sample18XML);
        String generatedCodeBlock = XMLToRecordConverter.convert(xmlFileContent, false, false, false)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample18Bal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "testXMLWithNamespacesWithoutNamespaceAnnotation")
    public void testXMLWithNamespacesWithoutNamespaceAttribute() throws IOException {
        String xmlFileContent = Files.readString(sample19XML);
        String generatedCodeBlock = XMLToRecordConverter.convertXMLToRecord(xmlFileContent, false, false, false,
                        "amount", false).getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample19Bal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "testXMLWithMultipleAttributesAndNamespacesWithoutAnnotations")
    public void testXMLWithMultipleAttributesAndNamespacesWithoutAnnotations() throws IOException {
        String xmlFileContent = Files.readString(sample20XML);
        String generatedCodeBlock = XMLToRecordConverter.convertXMLToRecord(xmlFileContent, false, false, false,
                null, false).getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample20Bal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "testXMLWithMultipleAttributesAndNamespacesWithAnnotations")
    public void testXMLWithMultipleAttributesAndNamespacesWithAnnotations() throws IOException {
        String xmlFileContent = Files.readString(sample21XML);
        String generatedCodeBlock = XMLToRecordConverter.convertXMLToRecord(xmlFileContent, false, false, false,
                null, true).getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample21Bal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "testXMLWithoutNamespacePrefix")
    public void testXMLWithoutNamespacePrefix() throws IOException {
        String xmlFileContent = Files.readString(sample22XML);
        String generatedCodeBlock = XMLToRecordConverter.convertXMLToRecord(xmlFileContent, false, false, false,
                null, true).getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample22Bal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "testXMLWithConflictingElementAndAttributeNames")
    public void testXMLWithConflictingElementAndAttributeNames() throws IOException {
        String xmlFileContent = Files.readString(sample23XML);
        String generatedCodeBlock = XMLToRecordConverter.convertXMLToRecord(xmlFileContent, false, false, false,
                null, true).getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample23Bal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "testXMLWithoutNamespaces")
    public void testXMLWithoutNamespaces() throws IOException {
        String xmlFileContent = Files.readString(sample24XML);
        String generatedCodeBlock = XMLToRecordConverter.convertXMLToRecord(xmlFileContent, false, false, false,
                null, false).getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample24Bal).replaceAll("\\s+", "");
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
