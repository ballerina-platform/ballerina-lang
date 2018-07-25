/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.mime;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BServiceUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXMLItem;
import org.ballerinalang.test.services.testutils.HTTPTestRequest;
import org.ballerinalang.test.services.testutils.Services;
import org.ballerinalang.test.utils.ResponseReader;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import java.util.ArrayList;
import java.util.Map;

import static org.ballerinalang.mime.util.MimeConstants.CONTENT_TRANSFER_ENCODING_7_BIT;
import static org.ballerinalang.mime.util.MimeConstants.CONTENT_TRANSFER_ENCODING_8_BIT;
import static org.ballerinalang.mime.util.MimeConstants.MULTIPART_FORM_DATA;

/**
 * Test cases for multipart/form- handling.
 *
 * @since 0.962.0
 */
public class MultipartFormDataDecoderTest {

    private CompileResult result, serviceResult;
    private static final String MOCK_ENDPOINT_NAME = "mockEP";

    @BeforeClass
    public void setup() {
        String sourceFilePath = "test-src/mime/multipart-request.bal";
        result = BCompileUtil.compile(sourceFilePath);
        serviceResult = BServiceUtil.setupProgramFile(this, sourceFilePath);
    }

    @Test(description = "Test sending a multipart request with a text body part which is kept in memory")
    public void testTextBodyPart() {
        String path = "/test/textbodypart";
        Map<String, Object> messageMap = Util.createPrerequisiteMessages(path, MULTIPART_FORM_DATA, result);
        ArrayList<BMap<String, BValue>> bodyParts = new ArrayList<>();
        bodyParts.add(Util.getTextBodyPart(result));
        HTTPTestRequest cMsg = Util.getCarbonMessageWithBodyParts(messageMap, Util.getArrayOfBodyParts(bodyParts));
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, MOCK_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(ResponseReader.getReturnValue(response), "Ballerina text body part");
    }

    @Test(description = "Test sending a multipart request with a text body part where the content is kept in a file")
    public void testTextBodyPartAsFileUpload() {
        String path = "/test/textbodypart";
        Map<String, Object> messageMap = Util.createPrerequisiteMessages(path, MULTIPART_FORM_DATA, result);
        ArrayList<BMap<String, BValue>> bodyParts = new ArrayList<>();
        bodyParts.add(Util.getTextFilePart(result));
        HTTPTestRequest cMsg = Util.getCarbonMessageWithBodyParts(messageMap, Util.getArrayOfBodyParts(bodyParts));
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, MOCK_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(ResponseReader.getReturnValue(response), "Ballerina text as a file part");
    }

    @Test(description = "Test sending a multipart request with a json body part which is kept in memory")
    public void testJsonBodyPart() {
        String path = "/test/jsonbodypart";
        Map<String, Object> messageMap = Util.createPrerequisiteMessages(path, MULTIPART_FORM_DATA, result);
        ArrayList<BMap<String, BValue>> bodyParts = new ArrayList<>();
        bodyParts.add(Util.getJsonBodyPart(result));
        HTTPTestRequest cMsg = Util.getCarbonMessageWithBodyParts(messageMap, Util.getArrayOfBodyParts(bodyParts));
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, MOCK_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(new BJSON(ResponseReader.getReturnValue(response)).value().get("bodyPart").asText(),
                "jsonPart");
    }

    @Test(description = "Test sending a multipart request with a json body part where the content is kept in a file")
    public void testJsonBodyPartAsFileUpload() {
        String path = "/test/jsonbodypart";
        Map<String, Object> messageMap = Util.createPrerequisiteMessages(path, MULTIPART_FORM_DATA, result);
        ArrayList<BMap<String, BValue>> bodyParts = new ArrayList<>();
        bodyParts.add(Util.getJsonFilePart(result));
        HTTPTestRequest cMsg = Util.getCarbonMessageWithBodyParts(messageMap, Util.getArrayOfBodyParts(bodyParts));
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, MOCK_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(new BJSON(ResponseReader.getReturnValue(response)).value().get("name").asText(),
                "wso2");
    }

    @Test(description = "Test sending a multipart request with a xml body part which is kept in memory")
    public void testXmlBodyPart() {
        String path = "/test/xmlbodypart";
        Map<String, Object> messageMap = Util.createPrerequisiteMessages(path, MULTIPART_FORM_DATA, result);
        ArrayList<BMap<String, BValue>> bodyParts = new ArrayList<>();
        bodyParts.add(Util.getXmlBodyPart(result));
        HTTPTestRequest cMsg = Util.getCarbonMessageWithBodyParts(messageMap, Util.getArrayOfBodyParts(bodyParts));
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, MOCK_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(new BXMLItem(ResponseReader.getReturnValue(response)).getTextValue().stringValue(),
                "Ballerina");
    }

    @Test(description = "Test sending a multipart request with a json body part where the content is kept in a file")
    public void testXmlBodyPartAsFileUpload() {
        String path = "/test/xmlbodypart";
        Map<String, Object> messageMap = Util.createPrerequisiteMessages(path, MULTIPART_FORM_DATA, result);
        ArrayList<BMap<String, BValue>> bodyParts = new ArrayList<>();
        bodyParts.add(Util.getXmlFilePart(result));
        HTTPTestRequest cMsg = Util.getCarbonMessageWithBodyParts(messageMap, Util.getArrayOfBodyParts(bodyParts));
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, MOCK_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(new BXMLItem(ResponseReader.getReturnValue(response)).getTextValue().stringValue(),
                "Ballerina" +
                        " xml file part");
    }

    @Test(description = "Test sending a multipart request with a binary body part which is kept in memory")
    public void testBinaryBodyPart() {
        String path = "/test/binarybodypart";
        Map<String, Object> messageMap = Util.createPrerequisiteMessages(path, MULTIPART_FORM_DATA, result);
        ArrayList<BMap<String, BValue>> bodyParts = new ArrayList<>();
        bodyParts.add(Util.getBinaryBodyPart(result));
        HTTPTestRequest cMsg = Util.getCarbonMessageWithBodyParts(messageMap, Util.getArrayOfBodyParts(bodyParts));
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, MOCK_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(ResponseReader.getReturnValue(response), "Ballerina binary part");
    }

    @Test(description = "Test sending a multipart request with a binary body part where the content " +
            "is kept in a file")
    public void testBinaryBodyPartAsFileUpload() {
        String path = "/test/binarybodypart";
        Map<String, Object> messageMap = Util.createPrerequisiteMessages(path, MULTIPART_FORM_DATA, result);
        ArrayList<BMap<String, BValue>> bodyParts = new ArrayList<>();
        bodyParts.add(Util.getBinaryFilePart(result));
        HTTPTestRequest cMsg = Util.getCarbonMessageWithBodyParts(messageMap, Util.getArrayOfBodyParts(bodyParts));
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, MOCK_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(ResponseReader.getReturnValue(response), "Ballerina binary file part");
    }

    @Test(description = "Test sending a multipart request as multipart/form- with multiple body parts")
    public void testMultiplePartsForFormData() {
        String path = "/test/multipleparts";
        Map<String, Object> messageMap = Util.createPrerequisiteMessages(path, MULTIPART_FORM_DATA, result);
        ArrayList<BMap<String, BValue>> bodyParts = new ArrayList<>();
        bodyParts.add(Util.getJsonBodyPart(result));
        bodyParts.add(Util.getXmlFilePart(result));
        bodyParts.add(Util.getTextBodyPart(result));
        bodyParts.add(Util.getBinaryFilePart(result));
        HTTPTestRequest cMsg = Util.getCarbonMessageWithBodyParts(messageMap, Util.getArrayOfBodyParts(bodyParts));
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, MOCK_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(ResponseReader.getReturnValue(response), " -- jsonPart -- Ballerina xml " +
                "file part -- Ballerina text body part -- Ballerina binary file part");
    }

    @Test(description = "Test sending a multipart request with a text body part that has 7 bit tranfer encoding")
    public void testTextBodyPartWith7BitEncoding() {
        String path = "/test/textbodypart";
        Map<String, Object> messageMap = Util.createPrerequisiteMessages(path, MULTIPART_FORM_DATA, result);
        ArrayList<BMap<String, BValue>> bodyParts = new ArrayList<>();
        bodyParts.add(Util.getTextFilePartWithEncoding(CONTENT_TRANSFER_ENCODING_7_BIT, "èiiii", result));
        HTTPTestRequest cMsg = Util.getCarbonMessageWithBodyParts(messageMap, Util.getArrayOfBodyParts(bodyParts));
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, MOCK_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(ResponseReader.getReturnValue(response), "èiiii");
    }

    @Test(description = "Test sending a multipart request with a text body part that has 8 bit transfer encoding")
    public void testTextBodyPartWith8BitEncoding() {
        String path = "/test/textbodypart";
        Map<String, Object> messageMap = Util.createPrerequisiteMessages(path, MULTIPART_FORM_DATA, result);
        ArrayList<BMap<String, BValue>> bodyParts = new ArrayList<>();
        bodyParts.add(Util.getTextFilePartWithEncoding(CONTENT_TRANSFER_ENCODING_8_BIT, "èlllll", result));
        HTTPTestRequest cMsg = Util.getCarbonMessageWithBodyParts(messageMap, Util.getArrayOfBodyParts(bodyParts));
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, MOCK_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(ResponseReader.getReturnValue(response), "èlllll");
    }
}
