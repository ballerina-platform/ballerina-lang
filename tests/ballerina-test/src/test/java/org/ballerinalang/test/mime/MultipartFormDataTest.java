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

import io.netty.handler.codec.http.multipart.HttpPostRequestEncoder;
import io.netty.util.internal.StringUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BServiceUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXMLItem;
import org.ballerinalang.net.http.Constants;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.test.services.testutils.HTTPTestRequest;
import org.ballerinalang.test.services.testutils.MessageUtils;
import org.ballerinalang.test.services.testutils.Services;
import org.ballerinalang.test.utils.ResponseReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.messaging.Header;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.ballerinalang.mime.util.Constants.APPLICATION_JSON;
import static org.ballerinalang.mime.util.Constants.APPLICATION_XML;
import static org.ballerinalang.mime.util.Constants.BYTE_DATA_INDEX;
import static org.ballerinalang.mime.util.Constants.CONTENT_TRANSFER_ENCODING;
import static org.ballerinalang.mime.util.Constants.CONTENT_TRANSFER_ENCODING_7_BIT;
import static org.ballerinalang.mime.util.Constants.CONTENT_TRANSFER_ENCODING_8_BIT;
import static org.ballerinalang.mime.util.Constants.CONTENT_TYPE;
import static org.ballerinalang.mime.util.Constants.ENTITY_HEADERS_INDEX;
import static org.ballerinalang.mime.util.Constants.ENTITY_NAME_INDEX;
import static org.ballerinalang.mime.util.Constants.FILE;
import static org.ballerinalang.mime.util.Constants.FILE_PATH_INDEX;
import static org.ballerinalang.mime.util.Constants.JSON_DATA_INDEX;
import static org.ballerinalang.mime.util.Constants.MEDIA_TYPE;
import static org.ballerinalang.mime.util.Constants.MESSAGE_ENTITY;
import static org.ballerinalang.mime.util.Constants.MULTIPART_DATA_INDEX;
import static org.ballerinalang.mime.util.Constants.MULTIPART_ENCODER;
import static org.ballerinalang.mime.util.Constants.MULTIPART_FORM_DATA;
import static org.ballerinalang.mime.util.Constants.MULTIPART_MIXED;
import static org.ballerinalang.mime.util.Constants.OCTET_STREAM;
import static org.ballerinalang.mime.util.Constants.OVERFLOW_DATA_INDEX;
import static org.ballerinalang.mime.util.Constants.PROTOCOL_PACKAGE_FILE;
import static org.ballerinalang.mime.util.Constants.PROTOCOL_PACKAGE_MIME;
import static org.ballerinalang.mime.util.Constants.TEXT_DATA_INDEX;
import static org.ballerinalang.mime.util.Constants.TEXT_PLAIN;
import static org.ballerinalang.mime.util.Constants.XML_DATA_INDEX;

/**
 * Test cases for multipart/form-data handling.
 */
public class MultipartFormDataTest {
    private static final Logger LOG = LoggerFactory.getLogger(MultipartFormDataTest.class);

    private CompileResult result, serviceResult;
    private final String requestStruct = Constants.IN_REQUEST;
    private final String protocolPackageHttp = Constants.PROTOCOL_PACKAGE_HTTP;
    private final String protocolPackageMime = PROTOCOL_PACKAGE_MIME;
    private final String protocolPackageFile = PROTOCOL_PACKAGE_FILE;
    private final String entityStruct = Constants.ENTITY;
    private final String mediaTypeStruct = MEDIA_TYPE;
    private String sourceFilePath = "test-src/mime/multipart-request.bal";
    private final String carbonMessage = "CarbonMessage";
    private final String ballerinaRequest = "BallerinaRequest";
    private final String multipartEntity = "MultipartEntity";

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile(sourceFilePath);
        serviceResult = BServiceUtil.setupProgramFile(this, sourceFilePath);
    }

    @Test(description = "Test sending a multipart request with a text body part which is kept in memory")
    public void testTextBodyPart() {
        String path = "/test/textbodypart";
        Map<String, Object> messageMap = createPrerequisiteMessages(path, MULTIPART_FORM_DATA);
        ArrayList<BStruct> bodyParts = new ArrayList<>();
        bodyParts.add(getTextBodyPart());
        HTTPTestRequest cMsg = getCarbonMessageWithBodyParts(messageMap, getArrayOfBodyParts(bodyParts));
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(ResponseReader.getReturnValue(response), "Ballerina text body part");
    }

    @Test(description = "Test sending a multipart request with a text body part where the content is kept in a file")
    public void testTextBodyPartAsFileUpload() {
        String path = "/test/textbodypart";
        Map<String, Object> messageMap = createPrerequisiteMessages(path, MULTIPART_FORM_DATA);
        ArrayList<BStruct> bodyParts = new ArrayList<>();
        bodyParts.add(getTextFilePart());
        HTTPTestRequest cMsg = getCarbonMessageWithBodyParts(messageMap, getArrayOfBodyParts(bodyParts));
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(ResponseReader.getReturnValue(response), "Ballerina text as a file part");
    }

    @Test(description = "Test sending a multipart request with a json body part which is kept in memory")
    public void testJsonBodyPart() {
        String path = "/test/jsonbodypart";
        Map<String, Object> messageMap = createPrerequisiteMessages(path, MULTIPART_FORM_DATA);
        ArrayList<BStruct> bodyParts = new ArrayList<>();
        bodyParts.add(getJsonBodyPart());
        HTTPTestRequest cMsg = getCarbonMessageWithBodyParts(messageMap, getArrayOfBodyParts(bodyParts));
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(new BJSON(ResponseReader.getReturnValue(response)).value().get("bodyPart").asText(),
                "jsonPart");
    }

    @Test(description = "Test sending a multipart request with a json body part where the content is kept in a file")
    public void testJsonBodyPartAsFileUpload() {
        String path = "/test/jsonbodypart";
        Map<String, Object> messageMap = createPrerequisiteMessages(path, MULTIPART_FORM_DATA);
        ArrayList<BStruct> bodyParts = new ArrayList<>();
        bodyParts.add(getJsonFilePart());
        HTTPTestRequest cMsg = getCarbonMessageWithBodyParts(messageMap, getArrayOfBodyParts(bodyParts));
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(new BJSON(ResponseReader.getReturnValue(response)).value().get("name").asText(),
                "wso2");
    }

    @Test(description = "Test sending a multipart request with a xml body part which is kept in memory")
    public void testXmlBodyPart() {
        String path = "/test/xmlbodypart";
        Map<String, Object> messageMap = createPrerequisiteMessages(path, MULTIPART_FORM_DATA);
        ArrayList<BStruct> bodyParts = new ArrayList<>();
        bodyParts.add(getXmlBodyPart());
        HTTPTestRequest cMsg = getCarbonMessageWithBodyParts(messageMap, getArrayOfBodyParts(bodyParts));
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(new BXMLItem(ResponseReader.getReturnValue(response)).getTextValue().stringValue(),
                "Ballerina");
    }

    @Test(description = "Test sending a multipart request with a json body part where the content is kept in a file")
    public void testXmlBodyPartAsFileUpload() {
        String path = "/test/xmlbodypart";
        Map<String, Object> messageMap = createPrerequisiteMessages(path, MULTIPART_FORM_DATA);
        ArrayList<BStruct> bodyParts = new ArrayList<>();
        bodyParts.add(getXmlFilePart());
        HTTPTestRequest cMsg = getCarbonMessageWithBodyParts(messageMap, getArrayOfBodyParts(bodyParts));
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(new BXMLItem(ResponseReader.getReturnValue(response)).getTextValue().stringValue(),
                "Ballerina" +
                        " xml file part");
    }

    @Test(description = "Test sending a multipart request with a binary body part which is kept in memory")
    public void testBinaryBodyPart() {
        String path = "/test/binarybodypart";
        Map<String, Object> messageMap = createPrerequisiteMessages(path, MULTIPART_FORM_DATA);
        ArrayList<BStruct> bodyParts = new ArrayList<>();
        bodyParts.add(getBinaryBodyPart());
        HTTPTestRequest cMsg = getCarbonMessageWithBodyParts(messageMap, getArrayOfBodyParts(bodyParts));
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(ResponseReader.getReturnValue(response), "Ballerina binary part");
    }

    @Test(description = "Test sending a multipart request with a binary body part where the content " +
            "is kept in a file")
    public void testBinaryBodyPartAsFileUpload() {
        String path = "/test/binarybodypart";
        Map<String, Object> messageMap = createPrerequisiteMessages(path, MULTIPART_FORM_DATA);
        ArrayList<BStruct> bodyParts = new ArrayList<>();
        bodyParts.add(getBinaryFilePart());
        HTTPTestRequest cMsg = getCarbonMessageWithBodyParts(messageMap, getArrayOfBodyParts(bodyParts));
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(ResponseReader.getReturnValue(response), "Ballerina binary file part");
    }

    @Test(description = "Test sending a multipart request as multipart/form-data with multiple body parts")
    public void testMultiplePartsForFormData() {
        String path = "/test/multipleparts";
        Map<String, Object> messageMap = createPrerequisiteMessages(path, MULTIPART_FORM_DATA);
        ArrayList<BStruct> bodyParts = new ArrayList<>();
        bodyParts.add(getJsonBodyPart());
        bodyParts.add(getXmlFilePart());
        bodyParts.add(getTextBodyPart());
        bodyParts.add(getBinaryFilePart());
        HTTPTestRequest cMsg = getCarbonMessageWithBodyParts(messageMap, getArrayOfBodyParts(bodyParts));
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(ResponseReader.getReturnValue(response), " -- jsonPart -- Ballerina xml " +
                "file part -- Ballerina text body part -- Ballerina binary file part");
    }

    @Test(description = "Test sending a multipart request with a text body part that has 7 bit tranfer encoding")
    public void testTextBodyPartWith7BitEncoding() {
        String path = "/test/textbodypart";
        Map<String, Object> messageMap = createPrerequisiteMessages(path, MULTIPART_FORM_DATA);
        ArrayList<BStruct> bodyParts = new ArrayList<>();
        bodyParts.add(getTextFilePartWithEncoding(CONTENT_TRANSFER_ENCODING_7_BIT, "èiiii"));
        HTTPTestRequest cMsg = getCarbonMessageWithBodyParts(messageMap, getArrayOfBodyParts(bodyParts));
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(ResponseReader.getReturnValue(response), "èiiii");
    }

    @Test(description = "Test sending a multipart request with a text body part that has 8 bit transfer encoding")
    public void testTextBodyPartWith8BitEncoding() {
        String path = "/test/textbodypart";
        Map<String, Object> messageMap = createPrerequisiteMessages(path, MULTIPART_FORM_DATA);
        ArrayList<BStruct> bodyParts = new ArrayList<>();
        bodyParts.add(getTextFilePartWithEncoding(CONTENT_TRANSFER_ENCODING_8_BIT, "èlllll"));
        HTTPTestRequest cMsg = getCarbonMessageWithBodyParts(messageMap, getArrayOfBodyParts(bodyParts));
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(ResponseReader.getReturnValue(response), "èlllll");
    }

    /**
     * Get a text body part from a given text content.
     *
     * @return A ballerina struct that represent a body part
     */
    private BStruct getTextBodyPart() {
        BStruct bodyPart = getEntityStruct();
        bodyPart.setStringField(TEXT_DATA_INDEX, "Ballerina text body part");
        bodyPart.setStringField(ENTITY_NAME_INDEX, "Text Body Part");
        MimeUtil.setContentType(getMediaTypeStruct(), bodyPart, TEXT_PLAIN);
        return bodyPart;
    }

    /**
     * Get a text body part as a file upload.
     *
     * @return a body part with text content in a file
     */
    private BStruct getTextFilePart() {
        try {
            File file = File.createTempFile("test", ".txt");
            file.deleteOnExit();
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            bufferedWriter.write("Ballerina text as a file part");
            bufferedWriter.close();
            BStruct fileStruct = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageFile, FILE);
            fileStruct.setStringField(FILE_PATH_INDEX, file.getAbsolutePath());
            BStruct bodyPart = getEntityStruct();
            bodyPart.setRefField(OVERFLOW_DATA_INDEX, fileStruct);
            bodyPart.setStringField(ENTITY_NAME_INDEX, "Text File Part");
            MimeUtil.setContentType(getMediaTypeStruct(), bodyPart, TEXT_PLAIN);
            return bodyPart;
        } catch (IOException e) {
            LOG.error("Error occured while creating a temp file for json file part in getTextFilePart",
                    e.getMessage());
        }
        return null;
    }

    /**
     * Get a text body part from a given text content and content transfer encoding.
     *
     * @return A ballerina struct that represent a body part
     */
    private BStruct getTextFilePartWithEncoding(String contentTransferEncoding, String message) {

        try {
            File file = File.createTempFile("test", ".txt");
            file.deleteOnExit();
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            bufferedWriter.write(message);
            bufferedWriter.close();
            BStruct fileStruct = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageFile, FILE);
            fileStruct.setStringField(FILE_PATH_INDEX, file.getAbsolutePath());
            BStruct bodyPart = getEntityStruct();
            bodyPart.setRefField(OVERFLOW_DATA_INDEX, fileStruct);
            bodyPart.setStringField(ENTITY_NAME_INDEX, "Text File Part");
            MimeUtil.setContentType(getMediaTypeStruct(), bodyPart, TEXT_PLAIN);
            BMap<String, BValue> headerMap = new BMap<>();
            headerMap.put(CONTENT_TRANSFER_ENCODING, new BStringArray(new String[]{contentTransferEncoding}));
            bodyPart.setRefField(ENTITY_HEADERS_INDEX, headerMap);
            return bodyPart;
        } catch (IOException e) {
            LOG.error("Error occured while creating a temp file for json file part in getTextFilePart",
                    e.getMessage());
        }
        return null;
    }

    /**
     * Get a json body part from a given json content.
     *
     * @return A ballerina struct that represent a body part
     */
    private BStruct getJsonBodyPart() {
        String key = "bodyPart";
        String value = "jsonPart";
        String jsonContent = "{\"" + key + "\":\"" + value + "\"}";
        BStruct bodyPart = getEntityStruct();
        bodyPart.setRefField(JSON_DATA_INDEX, new BJSON(jsonContent));
        bodyPart.setStringField(ENTITY_NAME_INDEX, "Json Body Part");
        MimeUtil.setContentType(getMediaTypeStruct(), bodyPart, APPLICATION_JSON);
        return bodyPart;
    }

    /**
     * Get a json body part as a file upload.
     *
     * @return a body part with json content in a file
     */
    private BStruct getJsonFilePart() {
        try {
            File file = File.createTempFile("test", ".json");
            file.deleteOnExit();
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            bufferedWriter.write("{'name':'wso2'}");
            bufferedWriter.close();
            BStruct fileStruct = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageFile, FILE);
            fileStruct.setStringField(FILE_PATH_INDEX, file.getAbsolutePath());
            BStruct bodyPart = getEntityStruct();
            bodyPart.setRefField(OVERFLOW_DATA_INDEX, fileStruct);
            bodyPart.setStringField(ENTITY_NAME_INDEX, "Json File Part");
            MimeUtil.setContentType(getMediaTypeStruct(), bodyPart, APPLICATION_JSON);
            return bodyPart;
        } catch (IOException e) {
            LOG.error("Error occured while creating a temp file for json file part in getJsonFilePart",
                    e.getMessage());
        }
        return null;
    }

    /**
     * Get a xml body part from a given xml content.
     *
     * @return A ballerina struct that represent a body part
     */
    private BStruct getXmlBodyPart() {
        BXMLItem xmlContent = new BXMLItem("<name>Ballerina</name>");
        BStruct bodyPart = getEntityStruct();
        bodyPart.setRefField(XML_DATA_INDEX, xmlContent);
        bodyPart.setStringField(ENTITY_NAME_INDEX, "Xml Body Part");
        MimeUtil.setContentType(getMediaTypeStruct(), bodyPart, APPLICATION_XML);
        return bodyPart;
    }

    /**
     * Get a xml body part as a file upload.
     *
     * @return a body part with xml content in a file
     */
    private BStruct getXmlFilePart() {
        try {
            File file = File.createTempFile("test", ".xml");
            file.deleteOnExit();
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            bufferedWriter.write("<name>Ballerina xml file part</name>");
            bufferedWriter.close();
            BStruct fileStruct = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageFile, FILE);
            fileStruct.setStringField(FILE_PATH_INDEX, file.getAbsolutePath());
            BStruct bodyPart = getEntityStruct();
            bodyPart.setRefField(OVERFLOW_DATA_INDEX, fileStruct);
            bodyPart.setStringField(ENTITY_NAME_INDEX, "Xml File Part");
            MimeUtil.setContentType(getMediaTypeStruct(), bodyPart, APPLICATION_XML);
            return bodyPart;
        } catch (IOException e) {
            LOG.error("Error occured while creating a temp file for xml file part in getXmlFilePart",
                    e.getMessage());
        }
        return null;
    }

    /**
     * Get a binary body part from a given blob content.
     *
     * @return A ballerina struct that represent a body part
     */
    private BStruct getBinaryBodyPart() {
        BStruct bodyPart = getEntityStruct();
        bodyPart.setBlobField(BYTE_DATA_INDEX, "Ballerina binary part".getBytes());
        bodyPart.setStringField(ENTITY_NAME_INDEX, "Binary Body Part");
        MimeUtil.setContentType(getMediaTypeStruct(), bodyPart, OCTET_STREAM);
        return bodyPart;
    }

    /**
     * Get a binary body part as a file upload.
     *
     * @return a body part with blob content in a file
     */
    private BStruct getBinaryFilePart() {
        try {
            File file = File.createTempFile("test", ".tmp");
            file.deleteOnExit();
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            bufferedWriter.write("Ballerina binary file part");
            bufferedWriter.close();
            BStruct fileStruct = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageFile, FILE);
            fileStruct.setStringField(FILE_PATH_INDEX, file.getAbsolutePath());
            BStruct bodyPart = getEntityStruct();
            bodyPart.setRefField(OVERFLOW_DATA_INDEX, fileStruct);
            bodyPart.setStringField(ENTITY_NAME_INDEX, "Binary File Part");
            MimeUtil.setContentType(getMediaTypeStruct(), bodyPart, OCTET_STREAM);
            return bodyPart;
        } catch (IOException e) {
            LOG.error("Error occured while creating a temp file for binary file part in getBinaryFilePart",
                    e.getMessage());
        }
        return null;
    }

    /**
     * Create prerequisite messages that are needed to proceed with the test cases.
     *
     * @param path Represent path to the resource
     * @return A map of relevant messages
     */
    private Map<String, Object> createPrerequisiteMessages(String path, String topLevelContentType) {
        Map<String, Object> messageMap = new HashMap<>();
        BStruct request = getRequestStruct();
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessageForMultiparts(path, Constants.HTTP_METHOD_POST);
        HttpUtil.addCarbonMsg(request, cMsg);
        BStruct entity = getEntityStruct();
        MimeUtil.setContentType(getMediaTypeStruct(), entity, topLevelContentType);
        messageMap.put(carbonMessage, cMsg);
        messageMap.put(ballerinaRequest, request);
        messageMap.put(multipartEntity, entity);
        return messageMap;
    }

    /**
     * Create multipart entity and fill the carbon message with body parts.
     *
     * @param messageMap Represent the map of prerequisite messages
     * @param bodyParts  Represent body parts that needs to be added to multipart entity
     * @return A test carbon message to be used for invoking the service with.
     */
    private HTTPTestRequest getCarbonMessageWithBodyParts(Map<String, Object> messageMap, BRefValueArray bodyParts) {
        HTTPTestRequest cMsg = (HTTPTestRequest) messageMap.get(carbonMessage);
        BStruct request = (BStruct) messageMap.get(ballerinaRequest);
        BStruct entity = (BStruct) messageMap.get(multipartEntity);
        entity.setRefField(MULTIPART_DATA_INDEX, bodyParts);
        request.addNativeData(MESSAGE_ENTITY, entity);
        setCarbonMessageWithMultiparts(request, cMsg);
        return cMsg;
    }

    /**
     * From a given list of body parts get a ballerina value array.
     *
     * @param bodyParts List of body parts
     * @return BRefValueArray representing an array of entities
     */
    private BRefValueArray getArrayOfBodyParts(ArrayList<BStruct> bodyParts) {
        BStructType typeOfBodyPart = bodyParts.get(0).getType();
        BStruct[] result = bodyParts.toArray(new BStruct[bodyParts.size()]);
        return new BRefValueArray(result, typeOfBodyPart);
    }

    /**
     * Add body parts to carbon message.
     *
     * @param request Ballerina request struct
     * @param cMsg    Represent carbon message
     */
    private void setCarbonMessageWithMultiparts(BStruct request, HTTPTestRequest cMsg) {
        HttpUtil.prepareRequestWithMultiparts(cMsg, request);
        try {
            HttpPostRequestEncoder nettyEncoder = (HttpPostRequestEncoder) request.getNativeData(MULTIPART_ENCODER);
            HttpUtil.addMultipartsToCarbonMessage(cMsg, nettyEncoder);
        } catch (Exception e) {
            LOG.error("Error occured while adding multiparts to carbon message in setCarbonMessageWithMultiparts",
                    e.getMessage());
        }
    }

    private BStruct getRequestStruct() {
        return BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, requestStruct);
    }

    private BStruct getEntityStruct() {
        return BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
    }

    private BStruct getMediaTypeStruct() {
        return BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime,
                mediaTypeStruct);
    }
}
