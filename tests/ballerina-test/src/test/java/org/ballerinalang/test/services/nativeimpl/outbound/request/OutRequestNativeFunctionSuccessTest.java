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
package org.ballerinalang.test.services.nativeimpl.outbound.request;

import io.netty.handler.codec.http.HttpHeaderNames;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.BServiceUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.mime.util.EntityBodyHandler;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.model.util.StringUtils;
import org.ballerinalang.model.values.BBlob;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.model.values.BXMLItem;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.runtime.message.MessageDataSource;
import org.ballerinalang.runtime.message.StringDataSource;
import org.ballerinalang.test.services.testutils.HTTPTestRequest;
import org.ballerinalang.test.services.testutils.MessageUtils;
import org.ballerinalang.test.services.testutils.Services;
import org.ballerinalang.test.utils.ResponseReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.ballerinalang.mime.util.Constants.APPLICATION_FORM;
import static org.ballerinalang.mime.util.Constants.APPLICATION_JSON;
import static org.ballerinalang.mime.util.Constants.APPLICATION_XML;
import static org.ballerinalang.mime.util.Constants.ENTITY_BYTE_CHANNEL;
import static org.ballerinalang.mime.util.Constants.ENTITY_HEADERS_INDEX;
import static org.ballerinalang.mime.util.Constants.FILE;
import static org.ballerinalang.mime.util.Constants.IS_BODY_BYTE_CHANNEL_ALREADY_SET;
import static org.ballerinalang.mime.util.Constants.MEDIA_TYPE;
import static org.ballerinalang.mime.util.Constants.MESSAGE_ENTITY;
import static org.ballerinalang.mime.util.Constants.OCTET_STREAM;
import static org.ballerinalang.mime.util.Constants.PROTOCOL_PACKAGE_FILE;
import static org.ballerinalang.mime.util.Constants.PROTOCOL_PACKAGE_MIME;
import static org.ballerinalang.mime.util.Constants.TEXT_PLAIN;

/**
 * Test cases for ballerina.net.http outbound outRequest success native functions.
 */
public class OutRequestNativeFunctionSuccessTest {
    private static final Logger LOG = LoggerFactory.getLogger(OutRequestNativeFunctionSuccessTest.class);

    private CompileResult result, serviceResult;
    private final String outReqStruct = HttpConstants.OUT_REQUEST;
    private final String protocolPackageHttp = HttpConstants.PROTOCOL_PACKAGE_HTTP;
    private final String protocolPackageMime = PROTOCOL_PACKAGE_MIME;
    private final String entityStruct = HttpConstants.ENTITY;
    private final String mediaTypeStruct = MEDIA_TYPE;

    @BeforeClass
    public void setup() {
        String sourceFilePath =
                "test-src/statements/services/nativeimpl/outbound/request/out-request-native-function.bal";
        result = BCompileUtil.compile(sourceFilePath);
        serviceResult = BServiceUtil.setupProgramFile(this, sourceFilePath);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAddHeader() {
        String headerName = "header1";
        String headerValue = "abc, xyz";
        BString key = new BString(headerName);
        BString value = new BString(headerValue);
        BValue[] inputArg = {key, value};
        BValue[] returnVals = BRunUtil.invoke(result, "testAddHeader", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BStruct);
        BStruct entityStruct = (BStruct) ((BStruct) returnVals[0]).getNativeData(MESSAGE_ENTITY);
        BMap<String, BStringArray> map = (BMap<String, BStringArray>) entityStruct.getRefField(ENTITY_HEADERS_INDEX);
        Assert.assertEquals(map.get(headerName).get(1), headerValue);
    }

    @Test(description = "Test addHeader function within a service")
    public void testServiceAddHeader() {
        String key = "lang";
        String value = "ballerina";
        String path = "/hello/addheader/" + key + "/" + value;
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, inRequestMsg);
        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(ResponseReader.getReturnValue(response));
        Assert.assertEquals(bJson.value().get(key).asText(), value);
    }

    @Test(description = "Test getBinaryPayload method of the request")
    public void testGetBinaryPayloadMethod() {
        BStruct outRequest = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, outReqStruct);
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        BStruct mediaType = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, mediaTypeStruct);
        HTTPCarbonMessage outRequestMsg = HttpUtil.createHttpCarbonMessage(true);

        String payload = "ballerina";
        MimeUtil.setContentType(mediaType, entity, OCTET_STREAM);
        entity.addNativeData(ENTITY_BYTE_CHANNEL, EntityBodyHandler.getEntityWrapper(payload));
        outRequest.addNativeData(MESSAGE_ENTITY, entity);
        outRequest.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, true);

        HttpUtil.addCarbonMsg(outRequest, outRequestMsg);
        BValue[] inputArg = {outRequest};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetBinaryPayload", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(returnVals[0].stringValue(), payload);
    }

    @Test
    public void testGetContentLength() {
        String payload = "ballerina";
        BStruct outRequest = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, outReqStruct);
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);

        BMap<String, BStringArray> headersMap = new BMap<>();
        headersMap.put(HttpHeaderNames.CONTENT_LENGTH.toString(),
                       new BStringArray(new String[]{String.valueOf(payload.length())}));
        entity.setRefField(ENTITY_HEADERS_INDEX, headersMap);
        outRequest.addNativeData(MESSAGE_ENTITY, entity);

        BValue[] inputArg = {outRequest};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetContentLength", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(payload.length(), ((BInteger) returnVals[0]).intValue());
    }

    @Test(description = "Test GetContentLength function within a service")
    public void testServiceGetContentLength() {
        String path = "/hello/getContentLength";
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, inRequestMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("value").asText(), String.valueOf(15));
    }

    @Test
    public void testGetHeader() {
        BStruct outRequest = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, outReqStruct);
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);

        BMap<String, BStringArray> headersMap = new BMap<>();
        headersMap.put(HttpHeaderNames.CONTENT_TYPE.toString(),
                       new BStringArray(new String[]{APPLICATION_FORM + ";a=2"}));
        entity.setRefField(ENTITY_HEADERS_INDEX, headersMap);
        outRequest.addNativeData(MESSAGE_ENTITY, entity);

        BString key = new BString(HttpHeaderNames.CONTENT_TYPE.toString());
        BValue[] inputArg = {outRequest, key};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetHeader", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(returnVals[0].stringValue(), APPLICATION_FORM + ";a=2");
    }

    @Test(description = "Test GetHeader function within a service")
    public void testServiceGetHeader() {
        String path = "/hello/getHeader";
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, inRequestMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("value").asText(), APPLICATION_FORM);
    }

    @Test(description = "Test GetHeaders function within a function")
    public void testGetHeaders() {
        String headerValue1 = APPLICATION_FORM;
        String headerValue2 = TEXT_PLAIN + ";b=5";
        BStruct outRequest = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, outReqStruct);
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);

        BMap<String, BStringArray> headersMap = new BMap<>();
        headersMap.put("test-header", new BStringArray(new String[]{headerValue1, headerValue2}));
        entity.setRefField(ENTITY_HEADERS_INDEX, headersMap);
        outRequest.addNativeData(MESSAGE_ENTITY, entity);

        BString key = new BString("test-header");
        BValue[] inputArg = {outRequest, key};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetHeaders", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(((BStringArray) returnVals[0]).get(0), headerValue1);
        Assert.assertEquals(((BStringArray) returnVals[0]).get(1), headerValue2);
    }

    @Test
    public void testGetJsonPayload() {
        BStruct outRequest = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, outReqStruct);
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        BStruct mediaType = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, mediaTypeStruct);

        String payload = "{'code':'123'}";
        MimeUtil.setContentType(mediaType, entity, APPLICATION_JSON);
        entity.addNativeData(ENTITY_BYTE_CHANNEL, EntityBodyHandler.getEntityWrapper(payload));
        outRequest.addNativeData(MESSAGE_ENTITY, entity);
        outRequest.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, true);

        BValue[] inputArg = {outRequest};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetJsonPayload", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(((BJSON) returnVals[0]).value().get("code").asText(), "123");
    }

    @Test(description = "Test GetJsonPayload function within a service")
    public void testServiceGetJsonPayload() {
        String value = "ballerina";
        String path = "/hello/getJsonPayload";
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, inRequestMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(new BJSON(ResponseReader.getReturnValue(response)).value().stringValue(), value);
    }

    @Test
    public void testGetProperty() {
        BStruct outRequest = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, outReqStruct);
        HTTPCarbonMessage outRequestMsg = HttpUtil.createHttpCarbonMessage(true);
        String propertyName = "wso2";
        String propertyValue = "Ballerina";
        outRequestMsg.setProperty(propertyName, propertyValue);
        HttpUtil.addCarbonMsg(outRequest, outRequestMsg);
        BString name = new BString(propertyName);

        BValue[] inputArg = {outRequest, name};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetProperty", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(returnVals[0].stringValue(), propertyValue);
    }

    @Test(description = "Test GetProperty function within a service")
    public void testServiceGetProperty() {
        String propertyValue = "Ballerina";
        String path = "/hello/GetProperty";
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, inRequestMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("value").asText(), propertyValue);
    }

    @Test(description = "Test GetStringPayload within a function")
    public void testGetStringPayload() {
        BStruct outRequest = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, outReqStruct);
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        BStruct mediaType = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, mediaTypeStruct);

        String payload = "ballerina";
        MimeUtil.setContentType(mediaType, entity, TEXT_PLAIN);
        entity.addNativeData(ENTITY_BYTE_CHANNEL, EntityBodyHandler.getEntityWrapper(payload));
        outRequest.addNativeData(MESSAGE_ENTITY, entity);
        outRequest.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, true);

        BValue[] inputArg = {outRequest};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetStringPayload", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(returnVals[0].stringValue(), payload);
    }

    @Test(description = "Test GetStringPayload function within a service")
    public void testServiceGetStringPayload() {
        String value = "ballerina";
        String path = "/hello/GetStringPayload";
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, inRequestMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(
                StringUtils.getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream()), value);
    }

    @Test(description = "Test GetXmlPayload within a function")
    public void testGetXmlPayload() {
        BStruct outRequest = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, outReqStruct);
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        BStruct mediaType = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, mediaTypeStruct);

        String payload = "<name>ballerina</name>";
        MimeUtil.setContentType(mediaType, entity, APPLICATION_XML);
        entity.addNativeData(ENTITY_BYTE_CHANNEL, EntityBodyHandler.getEntityWrapper(payload));
        outRequest.addNativeData(MESSAGE_ENTITY, entity);
        outRequest.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, true);

        BValue[] inputArg = {outRequest};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetXmlPayload", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(((BXML) returnVals[0]).getTextValue().stringValue(), "ballerina");
    }

    @Test(description = "Test GetXmlPayload function within a service")
    public void testServiceGetXmlPayload() {
        String path = "/hello/GetXmlPayload";
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, inRequestMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(ResponseReader.getReturnValue(response), "ballerina");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRemoveHeader() {
        BStruct outRequest = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, outReqStruct);
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        String expect = "Expect";
        String headerValue = "100-continue";
        BString key = new BString(expect);

        BMap<String, BStringArray> headersMap = new BMap<>();
        headersMap.put(expect, new BStringArray(new String[]{headerValue}));
        entity.setRefField(ENTITY_HEADERS_INDEX, headersMap);
        outRequest.addNativeData(MESSAGE_ENTITY, entity);

        BValue[] inputArg = {outRequest, key};
        BValue[] returnVals = BRunUtil.invoke(result, "testRemoveHeader", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BStruct);
        BStruct entityStruct = (BStruct) ((BStruct) returnVals[0]).getNativeData(MESSAGE_ENTITY);
        BMap<String, BStringArray> map = (BMap<String, BStringArray>) entityStruct.getRefField(ENTITY_HEADERS_INDEX);
        Assert.assertNull(map.get(headerValue));
    }

    @Test(description = "Test RemoveHeader function within a service")
    public void testServiceRemoveHeader() {
        String path = "/hello/RemoveHeader";
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, inRequestMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("value").asText(), "value is null");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRemoveAllHeaders() {
        BStruct outRequest = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, outReqStruct);
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        String expect = "Expect";
        String range = "Range";

        BMap<String, BStringArray> headersMap = new BMap<>();
        headersMap.put(expect, new BStringArray(new String[]{"100-continue"}));
        headersMap.put(range, new BStringArray(new String[]{"bytes=500-999"}));
        entity.setRefField(ENTITY_HEADERS_INDEX, headersMap);
        outRequest.addNativeData(MESSAGE_ENTITY, entity);
        BValue[] inputArg = {outRequest};

        BValue[] returnVals = BRunUtil.invoke(result, "testRemoveAllHeaders", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BStruct);
        BStruct entityStruct = (BStruct) ((BStruct) returnVals[0]).getNativeData(MESSAGE_ENTITY);
        BMap<String, BStringArray> map = (BMap<String, BStringArray>) entityStruct.getRefField(ENTITY_HEADERS_INDEX);
        Assert.assertNull(map.get(expect));
        Assert.assertNull(map.get(range));
    }

    @Test(description = "Test RemoveAllHeaders function within a service")
    public void testServiceRemoveAllHeaders() {
        String path = "/hello/RemoveAllHeaders";
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, inRequestMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("value").asText(), "value is null");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSetHeader() {
        String headerName = "lang";
        String headerValue = "ballerina; a=6";
        BString key = new BString(headerName);
        BString value = new BString(headerValue);
        BValue[] inputArg = {key, value};
        BValue[] returnVals = BRunUtil.invoke(result, "testSetHeader", inputArg);

        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BStruct);
        BStruct entityStruct = (BStruct) ((BStruct) returnVals[0]).getNativeData(MESSAGE_ENTITY);
        BMap<String, BStringArray> map = (BMap<String, BStringArray>) entityStruct.getRefField(ENTITY_HEADERS_INDEX);
        Assert.assertEquals(map.get(headerName).get(0), headerValue);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSetMultipleHeader() {
        String headerName = "team";
        String headerValue = "lang, composer";
        BString key = new BString(headerName);
        BString value = new BString(headerValue);
        BValue[] inputArg = {key, value};
        BValue[] returnVals = BRunUtil.invoke(result, "testSetHeader", inputArg);

        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BStruct);
        BStruct entityStruct = (BStruct) ((BStruct) returnVals[0]).getNativeData(MESSAGE_ENTITY);
        BMap<String, BStringArray> map = (BMap<String, BStringArray>) entityStruct.getRefField(ENTITY_HEADERS_INDEX);
        Assert.assertEquals(map.get(headerName).get(0), headerValue);
    }

    @Test(description = "Test SetHeader function within a service")
    public void testServiceSetHeader() {
        String key = "lang";
        String value = "ballerina";
        String path = "/hello/setHeader/" + key + "/" + value;
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, inRequestMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("value").asText(), value);
    }

    @Test
    public void testSetJsonPayload() {
        BJSON value = new BJSON("{'name':'wso2'}");
        BValue[] inputArg = {value};
        BValue[] returnVals = BRunUtil.invoke(result, "testSetJsonPayload", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BStruct);
        BStruct entity = (BStruct) ((BStruct) returnVals[0]).getNativeData(MESSAGE_ENTITY);
        BJSON bJson = (BJSON) EntityBodyHandler.getMessageDataSource(entity);
        Assert.assertEquals(bJson.value().get("name").asText(), "wso2", "Payload is not set properly");
    }

    @Test(description = "Test SetJsonPayload function within a service")
    public void testServiceSetJsonPayload() {
        String value = "ballerina";
        String path = "/hello/SetJsonPayload/" + value;
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, inRequestMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("lang").asText(), value);
    }

    @Test
    public void testSetProperty() {
        String propertyName = "wso2";
        String propertyValue = "Ballerina";
        BString name = new BString(propertyName);
        BString value = new BString(propertyValue);
        BValue[] inputArg = {name, value};
        BValue[] returnVals = BRunUtil.invoke(result, "testSetProperty", inputArg);

        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BStruct);
        HTTPCarbonMessage response = HttpUtil.getCarbonMsg((BStruct) returnVals[0], null);
        Assert.assertEquals(response.getProperty(propertyName), propertyValue);
    }

    @Test(description = "Test SetProperty function within a service")
    public void testServiceSetProperty() {
        String key = "lang";
        String value = "ballerina";
        String path = "/hello/SetProperty/" + key + "/" + value;
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, inRequestMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("value").asText(), value);
    }

    @Test
    public void testSetStringPayload() {
        BString value = new BString("Ballerina");
        BValue[] inputArg = {value};
        BValue[] returnVals = BRunUtil.invoke(result, "testSetStringPayload", inputArg);

        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BStruct);
        BStruct entity = (BStruct) ((BStruct) returnVals[0]).getNativeData(MESSAGE_ENTITY);
        StringDataSource stringValue = (StringDataSource) EntityBodyHandler.getMessageDataSource(entity);
        Assert.assertEquals(stringValue.getMessageAsString(), "Ballerina", "Payload is not set properly");
    }

    @Test(description = "Test SetStringPayload function within a service")
    public void testServiceSetStringPayload() {
        String value = "ballerina";
        String path = "/hello/SetStringPayload/" + value;
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, inRequestMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("lang").asText(), value);
    }

    @Test
    public void testSetXmlPayload() {
        BXMLItem value = new BXMLItem("<name>Ballerina</name>");
        BValue[] inputArg = {value};
        BValue[] returnVals = BRunUtil.invoke(result, "testSetXmlPayload", inputArg);

        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BStruct);
        BStruct entity = (BStruct) ((BStruct) returnVals[0]).getNativeData(MESSAGE_ENTITY);
        //   BXMLItem xmlValue = (BXMLItem) entity.getRefField(XML_DATA_INDEX);
        BXML xmlValue = (BXML) EntityBodyHandler.getMessageDataSource(entity);
        Assert.assertEquals(xmlValue.getTextValue().stringValue(), "Ballerina", "Payload is not set properly");
    }

    @Test(description = "Test SetXmlPayload function within a service")
    public void testServiceSetXmlPayload() {
        String value = "Ballerina";
        String path = "/hello/SetXmlPayload/";
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, inRequestMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("lang").asText(), value);
    }

    @Test(description = "Test setBinaryPayload() function within a service")
    public void testServiceSetBinaryPayload() {
        String value = "Ballerina";
        String path = "/hello/SetBinaryPayload/";
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, inRequestMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("lang").asText(), value);
    }

    @Test(description = "Test getBinaryPayload() function within a service")
    public void testServiceGetBinaryPayload() {
        String payload = "ballerina";
        String path = "/hello/GetBinaryPayload";
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_POST, payload);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, inRequestMsg);

        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(
                StringUtils.getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream()), payload);
    }

    @Test(description = "Test setBinaryPayload() function")
    public void testSetBinaryPayload() {
        BBlob value = new BBlob("Ballerina".getBytes());
        BValue[] inputArg = {value};
        BValue[] returnVals = BRunUtil.invoke(result, "testSetBinaryPayload", inputArg);

        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BStruct);
        BStruct entity = (BStruct) ((BStruct) returnVals[0]).getNativeData(MESSAGE_ENTITY);
        MessageDataSource messageDataSource = EntityBodyHandler.getMessageDataSource(entity);
        Assert.assertEquals(messageDataSource.getMessageAsString(), "Ballerina",
                "Payload is not set properly");
    }

    @Test(description = "Test setEntityBody() function")
    public void testSetEntityBody() {
        try {
            File file = File.createTempFile("test", ".json");
            file.deleteOnExit();
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            bufferedWriter.write("{'name':'wso2'}");
            bufferedWriter.close();

            BStruct fileStruct = BCompileUtil.createAndGetStruct(result.getProgFile(), PROTOCOL_PACKAGE_FILE, FILE);
            fileStruct.setStringField(0, file.getAbsolutePath());
            BValue[] inputArg = {fileStruct, new BString(APPLICATION_JSON)};
            BValue[] returnVals = BRunUtil.invoke(result, "testSetEntityBody", inputArg);
            Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                    "Invalid Return Values.");
            Assert.assertTrue(returnVals[0] instanceof BStruct);
            BStruct entity = (BStruct) ((BStruct) returnVals[0]).getNativeData(MESSAGE_ENTITY);
           /* BStruct returnFileStruct = (BStruct) entity.getRefField(OVERFLOW_DATA_INDEX);

            String returnJsonValue = new String(Files.readAllBytes(Paths.get(returnFileStruct.getStringField(0))),
                    UTF_8);*/
            BJSON bJson = EntityBodyHandler.constructJsonDataSource(entity);

            Assert.assertEquals(bJson.value().get("name").asText(), "wso2", "Payload is not set properly");
        } catch (IOException e) {
            LOG.error("Error occured while creating a temporary file in testSetEntityBody", e.getMessage());
        }
    }

    @Test(description = "Test getStringPayload method with JSON payload")
    public void testGetStringPayloadMethodWithJsonPayload() {
        BStruct outRequest = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, outReqStruct);
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        BStruct mediaType = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, mediaTypeStruct);

        String payload = "{\"code\":\"123\"}";
        MimeUtil.setContentType(mediaType, entity, APPLICATION_JSON);
        entity.addNativeData(ENTITY_BYTE_CHANNEL, EntityBodyHandler.getEntityWrapper(payload));
        outRequest.addNativeData(MESSAGE_ENTITY, entity);
        outRequest.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, true);

        BValue[] inputArg = {outRequest};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetStringPayload", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(returnVals[0].stringValue(), payload);
    }
}
