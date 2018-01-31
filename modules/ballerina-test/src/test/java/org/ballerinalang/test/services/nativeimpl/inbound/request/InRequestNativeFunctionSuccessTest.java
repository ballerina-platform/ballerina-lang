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
package org.ballerinalang.test.services.nativeimpl.inbound.request;

import io.netty.handler.codec.http.HttpHeaders;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.BServiceUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.model.util.StringUtils;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXMLItem;
import org.ballerinalang.net.http.Constants;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.test.services.testutils.HTTPTestRequest;
import org.ballerinalang.test.services.testutils.MessageUtils;
import org.ballerinalang.test.services.testutils.Services;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.messaging.Header;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.mime.util.Constants.APPLICATION_FORM;
import static org.ballerinalang.mime.util.Constants.APPLICATION_JSON;
import static org.ballerinalang.mime.util.Constants.APPLICATION_XML;
import static org.ballerinalang.mime.util.Constants.BYTE_DATA_INDEX;
import static org.ballerinalang.mime.util.Constants.CONTENT_TYPE;
import static org.ballerinalang.mime.util.Constants.ENTITY_HEADERS_INDEX;
import static org.ballerinalang.mime.util.Constants.IS_ENTITY_BODY_PRESENT;
import static org.ballerinalang.mime.util.Constants.JSON_DATA_INDEX;
import static org.ballerinalang.mime.util.Constants.MEDIA_TYPE;
import static org.ballerinalang.mime.util.Constants.MESSAGE_ENTITY;
import static org.ballerinalang.mime.util.Constants.OCTET_STREAM;
import static org.ballerinalang.mime.util.Constants.PROTOCOL_PACKAGE_MIME;
import static org.ballerinalang.mime.util.Constants.TEXT_DATA_INDEX;
import static org.ballerinalang.mime.util.Constants.TEXT_PLAIN;
import static org.ballerinalang.mime.util.Constants.UTF_8;
import static org.ballerinalang.mime.util.Constants.XML_DATA_INDEX;

/**
 * Test cases for ballerina.net.http inbound inRequest success native functions.
 */
public class InRequestNativeFunctionSuccessTest {
    private static final Logger LOG = LoggerFactory.getLogger(InRequestNativeFunctionSuccessTest.class);

    private CompileResult result, serviceResult;
    private final String inReqStruct = Constants.IN_REQUEST;
    private final String protocolPackageHttp = Constants.PROTOCOL_PACKAGE_HTTP;
    private final String protocolPackageMime = PROTOCOL_PACKAGE_MIME;
    private final String entityStruct = Constants.ENTITY;
    private final String mediaTypeStruct = MEDIA_TYPE;

    @BeforeClass
    public void setup() {
        String sourceFilePath =
                "test-src/statements/services/nativeimpl/inbound/request/in-request-native-function.bal";
        result = BCompileUtil.compile(sourceFilePath);
        serviceResult = BServiceUtil.setupProgramFile(this, sourceFilePath);
    }

    @Test(description = "Test getBinaryPayload method of the request")
    public void testGetBinaryPayloadMethod() {
        BStruct inRequest = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inReqStruct);
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        BStruct mediaType = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, mediaTypeStruct);

        String payload = "ballerina";
        MimeUtil.setContentType(mediaType, entity, OCTET_STREAM);
        entity.setBlobField(BYTE_DATA_INDEX, payload.getBytes());
        inRequest.addNativeData(MESSAGE_ENTITY, entity);
        inRequest.addNativeData(IS_ENTITY_BODY_PRESENT, true);

        BValue[] inputArg = {inRequest};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetBinaryPayload", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(returnVals[0].stringValue(), payload);
    }

    @Test
    public void testGetContentLength() {
        BStruct inRequest = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inReqStruct);
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        String payload = "ballerina";
        BMap<String, BStringArray> headersMap = new BMap<>();
        headersMap.put(Constants.HTTP_CONTENT_LENGTH, new BStringArray(new String[]{String.valueOf(payload.length())}));
        entity.setRefField(ENTITY_HEADERS_INDEX, headersMap);
        inRequest.addNativeData(MESSAGE_ENTITY, entity);

        BValue[] inputArg = {inRequest};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetContentLength", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(payload.length(), ((BInteger) returnVals[0]).intValue());
    }

    @Test(description = "Test GetContentLength function within a service")
    public void testServiceGetContentLength() {
        String key = "lang";
        String value = "ballerina";
        String path = "/hello/getContentLength";
        String jsonString = "{\"" + key + "\":\"" + value + "\"}";
        int length = jsonString.length();
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, Constants.HTTP_METHOD_POST, jsonString);
        inRequestMsg.setHeader(Constants.HTTP_CONTENT_LENGTH, String.valueOf(length));

        HTTPCarbonMessage response = Services.invokeNew(serviceResult, inRequestMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("value").asText(), String.valueOf(length));
    }

    @Test
    public void testGetHeader() {
        BStruct inRequest = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inReqStruct);
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage("", Constants.HTTP_METHOD_GET);
        inRequestMsg.setHeader(CONTENT_TYPE, APPLICATION_FORM);

        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        BStruct mediaType = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, mediaTypeStruct);
        HttpUtil.populateInboundRequest(inRequest, entity, mediaType, inRequestMsg);

        BString key = new BString(CONTENT_TYPE);
        BValue[] inputArg = {inRequest, key};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetHeader", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(returnVals[0].stringValue(), APPLICATION_FORM);
    }

    @Test(description = "Test GetHeader function within a service")
    public void testServiceGetHeader() {
        String path = "/hello/getHeader";
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, Constants.HTTP_METHOD_GET);
        inRequestMsg.setHeader(CONTENT_TYPE, APPLICATION_FORM);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, inRequestMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("value").asText(), APPLICATION_FORM);
    }

    @Test(description = "Test GetHeaders function within a function")
    public void testGetHeaders() {
        BStruct inRequest = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inReqStruct);
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage("", Constants.HTTP_METHOD_GET);
        HttpHeaders headers = inRequestMsg.getHeaders();
        headers.set("test-header", APPLICATION_FORM);
        headers.add("test-header", TEXT_PLAIN);

        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        BStruct mediaType = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, mediaTypeStruct);
        HttpUtil.populateInboundRequest(inRequest, entity, mediaType, inRequestMsg);

        BString key = new BString("test-header");
        BValue[] inputArg = {inRequest, key};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetHeaders", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(((BStringArray) returnVals[0]).get(0), APPLICATION_FORM);
        Assert.assertEquals(((BStringArray) returnVals[0]).get(1), TEXT_PLAIN);
    }

    @Test
    public void testGetJsonPayload() {
        BStruct inRequest = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inReqStruct);
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        BStruct mediaType = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, mediaTypeStruct);

        String payload = "{'code':'123'}";
        MimeUtil.setContentType(mediaType, entity, APPLICATION_JSON);
        entity.setRefField(JSON_DATA_INDEX, new BJSON(payload));
        inRequest.addNativeData(MESSAGE_ENTITY, entity);
        inRequest.addNativeData(IS_ENTITY_BODY_PRESENT, true);
        BValue[] inputArg = {inRequest};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetJsonPayload", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(((BJSON) returnVals[0]).value().get("code").asText(), "123");
    }

    @Test(description = "Test GetJsonPayload function within a service")
    public void testServiceGetJsonPayload() {
        String key = "lang";
        String value = "ballerina";
        String path = "/hello/getJsonPayload";
        String jsonString = "{\"" + key + "\":\"" + value + "\"}";
        List<Header> headers = new ArrayList<>();
        headers.add(new Header("Content-Type", APPLICATION_JSON));
        HTTPTestRequest inRequestMsg = MessageUtils
                .generateHTTPMessage(path, Constants.HTTP_METHOD_POST, headers, jsonString);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, inRequestMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(new BJSON(getReturnValue(response)).value().stringValue(), value);
    }

    @Test
    public void testGetProperty() {
        BStruct inRequest = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inReqStruct);
        HTTPCarbonMessage inRequestMsg = HttpUtil.createHttpCarbonMessage(true);
        String propertyName = "wso2";
        String propertyValue = "Ballerina";
        inRequestMsg.setProperty(propertyName, propertyValue);
        HttpUtil.addCarbonMsg(inRequest, inRequestMsg);
        BString name = new BString(propertyName);
        BValue[] inputArg = {inRequest, name};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetProperty", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(returnVals[0].stringValue(), propertyValue);
    }

    @Test(description = "Test GetProperty function within a service")
    public void testServiceGetProperty() {
        String propertyName = "wso2";
        String propertyValue = "Ballerina";
        String path = "/hello/GetProperty";
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, Constants.HTTP_METHOD_GET);
        inRequestMsg.setProperty(propertyName, propertyValue);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, inRequestMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("value").asText(), propertyValue);
    }

    @Test
    public void testGetStringPayload() {
        BStruct inRequest = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inReqStruct);
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        BStruct mediaType = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, mediaTypeStruct);

        String payload = "ballerina";
        MimeUtil.setContentType(mediaType, entity, TEXT_PLAIN);
        entity.setStringField(TEXT_DATA_INDEX, payload);
        inRequest.addNativeData(MESSAGE_ENTITY, entity);
        inRequest.addNativeData(IS_ENTITY_BODY_PRESENT, true);

        BValue[] inputArg = {inRequest};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetStringPayload", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(returnVals[0].stringValue(), payload);
    }

    @Test(description = "Test GetStringPayload function within a service")
    public void testServiceGetStringPayload() {
        String value = "ballerina";
        String path = "/hello/GetStringPayload";
        List<Header> headers = new ArrayList<>();
        headers.add(new Header("Content-Type", TEXT_PLAIN));
        HTTPTestRequest inRequestMsg = MessageUtils
                .generateHTTPMessage(path, Constants.HTTP_METHOD_POST, headers, value);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, inRequestMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(getReturnValue(response), value);
    }

    @Test
    public void testGetXmlPayload() {
        BStruct inRequest = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inReqStruct);
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        BStruct mediaType = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, mediaTypeStruct);

        String payload = "<name>ballerina</name>";
        MimeUtil.setContentType(mediaType, entity, APPLICATION_XML);
        entity.setRefField(XML_DATA_INDEX, new BXMLItem(payload));
        inRequest.addNativeData(MESSAGE_ENTITY, entity);
        inRequest.addNativeData(IS_ENTITY_BODY_PRESENT, true);

        BValue[] inputArg = {inRequest};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetXmlPayload", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(((BXMLItem) returnVals[0]).getTextValue().stringValue(), "ballerina");
    }

    @Test(description = "Test GetXmlPayload function within a service")
    public void testServiceGetXmlPayload() {
        String value = "ballerina";
        String path = "/hello/GetXmlPayload";
        String bxmlItemString = "<name>ballerina</name>";
        List<Header> headers = new ArrayList<>();
        headers.add(new Header("Content-Type", APPLICATION_XML));
        HTTPTestRequest inRequestMsg = MessageUtils
                .generateHTTPMessage(path, Constants.HTTP_METHOD_POST, headers, bxmlItemString);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, inRequestMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(getReturnValue(response), value);
    }

    @Test
    public void testGetMethod() {
        String path = "/hello/11";
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, Constants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, inRequestMsg);

        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(
                StringUtils.getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream()),
                Constants.HTTP_METHOD_GET);
    }

    @Test
    public void testGetRequestURL() {
        String path = "/hello/12";
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, Constants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, inRequestMsg);

        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(
                StringUtils.getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream()), path);
    }

    /**
     * Get the response value from input stream.
     *
     * @param response carbon response
     * @return return value from  input stream as a string
     */
    private String getReturnValue(HTTPCarbonMessage response) {
        Reader reader;
        final int bufferSize = 1024;
        final char[] buffer = new char[bufferSize];
        final StringBuilder out = new StringBuilder();
        try {
            reader = new InputStreamReader(new HttpMessageDataStreamer(response).getInputStream(), UTF_8);
            while (true) {
                int size = reader.read(buffer, 0, buffer.length);
                if (size < 0) {
                    break;
                }
                out.append(buffer, 0, size);
            }
        } catch (IOException e) {
            LOG.error("Error occured while reading the response value in getReturnValue", e.getMessage());
        }
        return out.toString();
    }
}
