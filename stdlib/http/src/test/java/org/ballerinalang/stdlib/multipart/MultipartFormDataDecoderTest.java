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

package org.ballerinalang.stdlib.multipart;

import io.ballerina.runtime.XMLFactory;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BXML;
import org.ballerinalang.core.model.util.JsonParser;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.stdlib.utils.HTTPTestRequest;
import org.ballerinalang.stdlib.utils.ResponseReader;
import org.ballerinalang.stdlib.utils.Services;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.util.ArrayList;
import java.util.Map;

import static org.ballerinalang.mime.util.MimeConstants.CONTENT_TRANSFER_ENCODING_7_BIT;
import static org.ballerinalang.mime.util.MimeConstants.CONTENT_TRANSFER_ENCODING_8_BIT;
import static org.ballerinalang.mime.util.MimeConstants.MULTIPART_FORM_DATA;
import static org.ballerinalang.stdlib.mime.Util.getArrayOfBodyParts;
import static org.ballerinalang.stdlib.mime.Util.getBinaryBodyPart;
import static org.ballerinalang.stdlib.mime.Util.getBinaryFilePart;
import static org.ballerinalang.stdlib.mime.Util.getJsonBodyPart;
import static org.ballerinalang.stdlib.mime.Util.getJsonFilePart;
import static org.ballerinalang.stdlib.mime.Util.getTextBodyPart;
import static org.ballerinalang.stdlib.mime.Util.getTextFilePart;
import static org.ballerinalang.stdlib.mime.Util.getTextFilePartWithEncoding;
import static org.ballerinalang.stdlib.mime.Util.getXmlBodyPart;
import static org.ballerinalang.stdlib.mime.Util.getXmlFilePart;
import static org.ballerinalang.stdlib.utils.MultipartUtils.createPrerequisiteMessages;
import static org.ballerinalang.stdlib.utils.MultipartUtils.getCarbonMessageWithBodyParts;

/**
 * Test cases for multipart/form-data handling.
 *
 * @since 0.962.0
 */
public class MultipartFormDataDecoderTest {

    private static final int TEST_PORT = 9090;

    @BeforeClass
    public void setup() {
        String sourceFilePath = "test-src/multipart/multipart-request.bal";
        CompileResult result = BCompileUtil.compile(sourceFilePath);
        if (result.getErrorCount() > 0) {
            Assert.fail("Compilation errors");
        }
    }

    @Test(description = "Test sending a multipart request with a text body part which is kept in memory")
    public void testTextBodyPart() {
        String path = "/test/textbodypart";
        Map<String, Object> messageMap = createPrerequisiteMessages(path, MULTIPART_FORM_DATA);
        ArrayList<BObject> bodyParts = new ArrayList<>();
        bodyParts.add(getTextBodyPart());
        HTTPTestRequest cMsg = getCarbonMessageWithBodyParts(messageMap, getArrayOfBodyParts(bodyParts));
        HttpCarbonMessage response = Services.invoke(TEST_PORT, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(ResponseReader.getReturnValue(response), "Ballerina text body part");
    }

    @Test(description = "Test sending a multipart request with a text body part where the content is kept in a file")
    public void testTextBodyPartAsFileUpload() {
        String path = "/test/textbodypart";
        Map<String, Object> messageMap = createPrerequisiteMessages(path, MULTIPART_FORM_DATA);
        ArrayList<BObject> bodyParts = new ArrayList<>();
        bodyParts.add(getTextFilePart());
        HTTPTestRequest cMsg = getCarbonMessageWithBodyParts(messageMap, getArrayOfBodyParts(bodyParts));
        HttpCarbonMessage response = Services.invoke(TEST_PORT, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(ResponseReader.getReturnValue(response), "Ballerina text as a file part");
    }

    @Test(description = "Test sending a multipart request with a json body part which is kept in memory")
    public void testJsonBodyPart() {
        String path = "/test/jsonbodypart";
        Map<String, Object> messageMap = createPrerequisiteMessages(path, MULTIPART_FORM_DATA);
        ArrayList<BObject> bodyParts = new ArrayList<>();
        bodyParts.add(getJsonBodyPart());
        HTTPTestRequest cMsg = getCarbonMessageWithBodyParts(messageMap, getArrayOfBodyParts(bodyParts));
        HttpCarbonMessage response = Services.invoke(TEST_PORT, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        BValue json = JsonParser.parse(ResponseReader.getReturnValue(response));
        Assert.assertEquals(((BMap) json).get("bodyPart").stringValue(), "jsonPart");
    }

    @Test(description = "Test sending a multipart request with a json body part where the content is kept in a file")
    public void testJsonBodyPartAsFileUpload() {
        String path = "/test/jsonbodypart";
        Map<String, Object> messageMap = createPrerequisiteMessages(path, MULTIPART_FORM_DATA);
        ArrayList<BObject> bodyParts = new ArrayList<>();
        bodyParts.add(getJsonFilePart());
        HTTPTestRequest cMsg = getCarbonMessageWithBodyParts(messageMap, getArrayOfBodyParts(bodyParts));
        HttpCarbonMessage response = Services.invoke(TEST_PORT, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        BValue json = JsonParser.parse(ResponseReader.getReturnValue(response));
        Assert.assertEquals(((BMap) json).get("name").stringValue(), "wso2");
    }

    @Test(description = "Test sending a multipart request with a xml body part which is kept in memory")
    public void testXmlBodyPart() {
        String path = "/test/xmlbodypart";
        Map<String, Object> messageMap = createPrerequisiteMessages(path, MULTIPART_FORM_DATA);
        ArrayList<BObject> bodyParts = new ArrayList<>();
        bodyParts.add(getXmlBodyPart());
        HTTPTestRequest cMsg = getCarbonMessageWithBodyParts(messageMap, getArrayOfBodyParts(bodyParts));
        HttpCarbonMessage response = Services.invoke(TEST_PORT, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        BXML value = XMLFactory.parse(ResponseReader.getReturnValue(response));
        Assert.assertEquals(value.getTextValue(), "Ballerina");
    }

    @Test(description = "Test sending a multipart request with a json body part where the content is kept in a file")
    public void testXmlBodyPartAsFileUpload() {
        String path = "/test/xmlbodypart";
        Map<String, Object> messageMap = createPrerequisiteMessages(path, MULTIPART_FORM_DATA);
        ArrayList<BObject> bodyParts = new ArrayList<>();
        bodyParts.add(getXmlFilePart());
        HTTPTestRequest cMsg = getCarbonMessageWithBodyParts(messageMap, getArrayOfBodyParts(bodyParts));
        HttpCarbonMessage response = Services.invoke(TEST_PORT, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        BXML omNode = XMLFactory.parse(ResponseReader.getReturnValue(response));
        Assert.assertEquals(omNode.getTextValue(), "Ballerina xml file part");
    }

    @Test(description = "Test sending a multipart request with a binary body part which is kept in memory")
    public void testBinaryBodyPart() {
        String path = "/test/binarybodypart";
        Map<String, Object> messageMap = createPrerequisiteMessages(path, MULTIPART_FORM_DATA);
        ArrayList<BObject> bodyParts = new ArrayList<>();
        bodyParts.add(getBinaryBodyPart());
        HTTPTestRequest cMsg = getCarbonMessageWithBodyParts(messageMap, getArrayOfBodyParts(bodyParts));
        HttpCarbonMessage response = Services.invoke(TEST_PORT, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(ResponseReader.getReturnValue(response), "Ballerina binary part");
    }

    @Test(description = "Test sending a multipart request with a binary body part where the content " +
            "is kept in a file")
    public void testBinaryBodyPartAsFileUpload() {
        String path = "/test/binarybodypart";
        Map<String, Object> messageMap = createPrerequisiteMessages(path, MULTIPART_FORM_DATA);
        ArrayList<BObject> bodyParts = new ArrayList<>();
        bodyParts.add(getBinaryFilePart());
        HTTPTestRequest cMsg = getCarbonMessageWithBodyParts(messageMap, getArrayOfBodyParts(bodyParts));
        HttpCarbonMessage response = Services.invoke(TEST_PORT, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(ResponseReader.getReturnValue(response), "Ballerina binary file part");
    }

    @Test(description = "Test sending a multipart request as multipart/form-data with multiple body parts")
    public void testMultiplePartsForFormData() {
        String path = "/test/multipleparts";
        Map<String, Object> messageMap = createPrerequisiteMessages(path, MULTIPART_FORM_DATA);
        ArrayList<BObject> bodyParts = new ArrayList<>();
        bodyParts.add(getJsonBodyPart());
        bodyParts.add(getXmlFilePart());
        bodyParts.add(getTextBodyPart());
        bodyParts.add(getBinaryFilePart());
        HTTPTestRequest cMsg = getCarbonMessageWithBodyParts(messageMap, getArrayOfBodyParts(bodyParts));
        HttpCarbonMessage response = Services.invoke(TEST_PORT, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(ResponseReader.getReturnValue(response), " -- jsonPart -- Ballerina xml " +
                "file part -- Ballerina text body part -- Ballerina binary file part");
    }

    @Test(description = "Test sending a multipart request with a text body part that has 7 bit tranfer encoding")
    public void testTextBodyPartWith7BitEncoding() {
        String path = "/test/textbodypart";
        Map<String, Object> messageMap = createPrerequisiteMessages(path, MULTIPART_FORM_DATA);
        ArrayList<BObject> bodyParts = new ArrayList<>();
        bodyParts.add(getTextFilePartWithEncoding(CONTENT_TRANSFER_ENCODING_7_BIT, "èiiii"));
        HTTPTestRequest cMsg = getCarbonMessageWithBodyParts(messageMap, getArrayOfBodyParts(bodyParts));
        HttpCarbonMessage response = Services.invoke(TEST_PORT, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(ResponseReader.getReturnValue(response), "èiiii");
    }

    @Test(description = "Test sending a multipart request with a text body part that has 8 bit transfer encoding")
    public void testTextBodyPartWith8BitEncoding() {
        String path = "/test/textbodypart";
        Map<String, Object> messageMap = createPrerequisiteMessages(path, MULTIPART_FORM_DATA);
        ArrayList<BObject> bodyParts = new ArrayList<>();
        bodyParts.add(getTextFilePartWithEncoding(CONTENT_TRANSFER_ENCODING_8_BIT, "èlllll"));
        HTTPTestRequest cMsg = getCarbonMessageWithBodyParts(messageMap, getArrayOfBodyParts(bodyParts));
        HttpCarbonMessage response = Services.invoke(TEST_PORT, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(ResponseReader.getReturnValue(response), "èlllll");
    }
}
