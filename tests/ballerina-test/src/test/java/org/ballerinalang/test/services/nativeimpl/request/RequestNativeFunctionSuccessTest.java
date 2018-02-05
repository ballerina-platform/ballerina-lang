/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.services.nativeimpl.request;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.BServiceUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.model.util.StringUtils;
import org.ballerinalang.model.values.BBlob;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXMLItem;
import org.ballerinalang.net.http.Constants;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.runtime.message.BlobDataSource;
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.mime.util.Constants.APPLICATION_FORM;
import static org.ballerinalang.mime.util.Constants.APPLICATION_JSON;
import static org.ballerinalang.mime.util.Constants.APPLICATION_XML;
import static org.ballerinalang.mime.util.Constants.BYTE_DATA_INDEX;
import static org.ballerinalang.mime.util.Constants.CONTENT_TYPE;
import static org.ballerinalang.mime.util.Constants.ENTITY_HEADERS_INDEX;
import static org.ballerinalang.mime.util.Constants.FILE;
import static org.ballerinalang.mime.util.Constants.HEADER_VALUE_STRUCT;
import static org.ballerinalang.mime.util.Constants.IS_ENTITY_BODY_PRESENT;
import static org.ballerinalang.mime.util.Constants.JSON_DATA_INDEX;
import static org.ballerinalang.mime.util.Constants.MEDIA_TYPE;
import static org.ballerinalang.mime.util.Constants.MESSAGE_ENTITY;
import static org.ballerinalang.mime.util.Constants.OCTET_STREAM;
import static org.ballerinalang.mime.util.Constants.OVERFLOW_DATA_INDEX;
import static org.ballerinalang.mime.util.Constants.PROTOCOL_PACKAGE_FILE;
import static org.ballerinalang.mime.util.Constants.PROTOCOL_PACKAGE_MIME;
import static org.ballerinalang.mime.util.Constants.TEXT_DATA_INDEX;
import static org.ballerinalang.mime.util.Constants.TEXT_PLAIN;
import static org.ballerinalang.mime.util.Constants.UTF_8;
import static org.ballerinalang.mime.util.Constants.XML_DATA_INDEX;

/**
 * Test cases for ballerina.net.http.request success native functions.
 */
public class RequestNativeFunctionSuccessTest {
    private static final Logger LOG = LoggerFactory.getLogger(RequestNativeFunctionSuccessTest.class);

    private CompileResult result, serviceResult;
    private final String inRequestStruct = Constants.IN_REQUEST;
    private final String outRequestStruct = Constants.OUT_REQUEST;
    private final String headerStruct = HEADER_VALUE_STRUCT;
    private final String protocolPackageHttp = Constants.PROTOCOL_PACKAGE_HTTP;
    private final String protocolPackageMime = PROTOCOL_PACKAGE_MIME;
    private final String protocolPackageFile = PROTOCOL_PACKAGE_FILE;
    private final String entityStruct = Constants.ENTITY;
    private final String mediaTypeStruct = MEDIA_TYPE;
    private String sourceFilePath = "test-src/statements/services/nativeimpl/request/request-native-function.bal";

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile(sourceFilePath);
        serviceResult = BServiceUtil.setupProgramFile(this, sourceFilePath);
    }

    @Test
    public void testAddHeader() {
        BStruct request = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, outRequestStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(true);

        HttpUtil.addCarbonMsg(request, cMsg);

        String headerName = "header1";
        String headerValue = "headerValue";
        BString key = new BString(headerName);
        BString value = new BString(headerValue);
        BValue[] inputArg = { request, key, value };
        BValue[] returnVals = BRunUtil.invoke(result, "testAddHeader", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BStruct);
        BStruct entityStruct = (BStruct) ((BStruct) returnVals[0]).getNativeData(MESSAGE_ENTITY);
        BMap map = (BMap) entityStruct.getRefField(ENTITY_HEADERS_INDEX);
        BRefValueArray array = (BRefValueArray) map.get(headerName);
        Assert.assertEquals(((BStruct) array.get(0)).getStringField(0), headerValue);
    }

    @Test(description = "Test addHeader function within a service")
    public void testServiceAddHeader() {
        String key = "lang";
        String value = "ballerina";
        String path = "/hello/addheader/" + key + "/" + value;
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, Constants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(getReturnValue(response));
        Assert.assertEquals(bJson.value().get(key).asText(), value);
    }

    @Test(description = "Test req struct add Header function")
    public void testStructAddHeader() {
        String value = "ballerina";
        String path = "/hello/addReqHeader";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, Constants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(getReturnValue(response));
        Assert.assertEquals(bJson.value().get("headerValue").asText(), value);
        Assert.assertEquals(bJson.value().get("paramValue").asText(), String.valueOf(6));

    }

    @Test(description = "Test req struct add Header function without params")
    public void testStructAddHeaderWithNoParam() {
        String value = "ballerina";
        String path = "/hello/addReqHeaderWithoutParam";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, Constants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(getReturnValue(response));
        Assert.assertEquals(bJson.value().get("headerValue").asText(), value);
        Assert.assertEquals(bJson.value().get("paramValue").asText(), "param is null");
    }

    @Test(description = "Test req struct add Header function")
    public void testAddHeaderViaBalFunction() {
        String path = "/hello/addReqHeaderFunc";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, Constants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(getReturnValue(response));
        Assert.assertEquals(bJson.value().get("headerValue").asText(), "chamil");
        Assert.assertEquals(bJson.value().get("size").asText(), String.valueOf(3));
    }

    @Test(description = "Test getBinaryPayload method of the request")
    public void testGetBinaryPayloadMethod() {
        BStruct request = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inRequestStruct);
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        BStruct mediaType = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, mediaTypeStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(true);

        String payload = "ballerina";
        MimeUtil.setContentType(mediaType, entity, OCTET_STREAM);
        entity.setBlobField(BYTE_DATA_INDEX, payload.getBytes());
        request.addNativeData(MESSAGE_ENTITY, entity);
        request.addNativeData(IS_ENTITY_BODY_PRESENT, true);

        HttpUtil.addCarbonMsg(request, cMsg);
        BValue[] inputArg = { request };
        BValue[] returnVals = BRunUtil.invoke(result, "testGetBinaryPayload", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(returnVals[0].stringValue(), payload);
    }

    @Test
    public void testGetContentLength() {
        BStruct request = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inRequestStruct);
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("", Constants.HTTP_METHOD_GET);

        String payload = "ballerina";
        cMsg.setHeader(Constants.HTTP_CONTENT_LENGTH, String.valueOf(payload.length()));
        HttpUtil.addCarbonMsg(request, cMsg);

        HttpUtil.setHeaderValueStructType(
                BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, headerStruct));
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        BStruct mediaType = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, mediaTypeStruct);
        HttpUtil.populateInboundRequest(request, entity, mediaType, cMsg);

        BValue[] inputArg = { request };
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
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, Constants.HTTP_METHOD_POST, jsonString);
        cMsg.setHeader(Constants.HTTP_CONTENT_LENGTH, String.valueOf(length));

        HTTPCarbonMessage response = Services.invokeNew(serviceResult, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("value").asText(), String.valueOf(length));
    }

    @Test
    public void testGetHeader() {
        BStruct request = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inRequestStruct);
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("", Constants.HTTP_METHOD_GET);
        cMsg.setHeader(CONTENT_TYPE, APPLICATION_FORM);

        HttpUtil.setHeaderValueStructType(
                BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, headerStruct));
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        BStruct mediaType = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, mediaTypeStruct);
        HttpUtil.populateInboundRequest(request, entity, mediaType, cMsg);

        BString key = new BString(CONTENT_TYPE);
        BValue[] inputArg = { request, key };
        BValue[] returnVals = BRunUtil.invoke(result, "testGetHeader", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(returnVals[0].stringValue(), APPLICATION_FORM);
    }

    @Test(description = "Test GetHeader function within a service")
    public void testServiceGetHeader() {
        String path = "/hello/getHeader";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, Constants.HTTP_METHOD_GET);
        cMsg.setHeader(CONTENT_TYPE, APPLICATION_FORM);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("value").asText(), APPLICATION_FORM);
    }

    @Test(description = "Test struct Get Header operation")
    public void testStructGetHeader() {
        String path = "/hello/getReqHeader";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, Constants.HTTP_METHOD_GET);
        cMsg.setHeader("test-header", APPLICATION_FORM);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(getReturnValue(response));
        Assert.assertEquals(bJson.value().get("value").asText(), APPLICATION_FORM);
    }

    @Test(description = "Test GetHeaders function within a function")
    public void testGetHeaders() {
        BStruct request = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inRequestStruct);
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("", Constants.HTTP_METHOD_GET);
        cMsg.setHeader("test-header", APPLICATION_FORM + "," + TEXT_PLAIN + ";b=5");

        HttpUtil.setHeaderValueStructType(
                BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, headerStruct));
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        BStruct mediaType = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, mediaTypeStruct);
        HttpUtil.populateInboundRequest(request, entity, mediaType, cMsg);

        BString key = new BString("test-header");
        BValue[] inputArg = { request, key };
        BValue[] returnVals = BRunUtil.invoke(result, "testGetHeaders", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(returnVals[0].stringValue(), TEXT_PLAIN);
    }

    @Test(description = "Test GetHeaders function within a service")
    public void testServiceGetHeaders() {
        String path = "/hello/getHeaders";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, Constants.HTTP_METHOD_GET);
        cMsg.setHeader("test-header", APPLICATION_FORM + "," + TEXT_PLAIN + ";b=5");
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(getReturnValue(response));
        Assert.assertEquals(bJson.value().get("value").asText(), TEXT_PLAIN);
        Assert.assertEquals(bJson.value().get("paramValue").asText(), String.valueOf(5));
    }

    @Test(description = "Test GetHeaders function with values of struct")
    public void testStructGetHeaders() {
        String path = "/hello/getReqHeaders";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, Constants.HTTP_METHOD_GET);
        cMsg.setHeader("test-header", APPLICATION_FORM);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(getReturnValue(response));
        Assert.assertEquals(bJson.value().get("value").asText(), "transport");
    }

    @Test
    public void testGetJsonPayload() {
        BStruct request = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inRequestStruct);
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        BStruct mediaType = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, mediaTypeStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(true);

        String payload = "{'code':'123'}";
        cMsg.setHeader(CONTENT_TYPE, APPLICATION_JSON);
        HttpUtil.addCarbonMsg(request, cMsg);
        MimeUtil.setContentType(mediaType, entity, APPLICATION_JSON);
        entity.setRefField(JSON_DATA_INDEX, new BJSON(payload));
        request.addNativeData(MESSAGE_ENTITY, entity);
        request.addNativeData(IS_ENTITY_BODY_PRESENT, true);
        BValue[] inputArg = { request };
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
        List<Header> headers = new ArrayList<Header>();
        headers.add(new Header("Content-Type", APPLICATION_JSON));
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, Constants.HTTP_METHOD_POST, headers, jsonString);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(new BJSON(getReturnValue(response)).value().stringValue(), value);
    }

    @Test
    public void testGetProperty() {
        BStruct request = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inRequestStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(true);
        String propertyName = "wso2";
        String propertyValue = "Ballerina";
        cMsg.setProperty(propertyName, propertyValue);
        HttpUtil.addCarbonMsg(request, cMsg);
        BString name = new BString(propertyName);
        BValue[] inputArg = { request, name };
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
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, Constants.HTTP_METHOD_GET);
        cMsg.setProperty(propertyName, propertyValue);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("value").asText(), propertyValue);
    }

    @Test
    public void testGetStringPayload() {
        BStruct request = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inRequestStruct);
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        BStruct mediaType = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, mediaTypeStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(true);

        String payload = "ballerina";
        MimeUtil.setContentType(mediaType, entity, TEXT_PLAIN);
        entity.setStringField(TEXT_DATA_INDEX, payload);
        request.addNativeData(MESSAGE_ENTITY, entity);
        request.addNativeData(IS_ENTITY_BODY_PRESENT, true);

        HttpUtil.addCarbonMsg(request, cMsg);
        BValue[] inputArg = { request };
        BValue[] returnVals = BRunUtil.invoke(result, "testGetStringPayload", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(returnVals[0].stringValue(), payload);
    }

    @Test(description = "Test GetStringPayload function within a service")
    public void testServiceGetStringPayload() {
        String value = "ballerina";
        String path = "/hello/GetStringPayload";
        List<Header> headers = new ArrayList<Header>();
        headers.add(new Header("Content-Type", TEXT_PLAIN));
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, Constants.HTTP_METHOD_POST, headers, value);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(getReturnValue(response), value);
    }

    @Test
    public void testGetXmlPayload() {
        BStruct request = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inRequestStruct);
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        BStruct mediaType = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, mediaTypeStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(true);

        String payload = "<name>ballerina</name>";
        MimeUtil.setContentType(mediaType, entity, APPLICATION_XML);
        entity.setRefField(XML_DATA_INDEX, new BXMLItem(payload));
        request.addNativeData(MESSAGE_ENTITY, entity);
        request.addNativeData(IS_ENTITY_BODY_PRESENT, true);

        HttpUtil.addCarbonMsg(request, cMsg);
        BValue[] inputArg = { request };
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
        List<Header> headers = new ArrayList<Header>();
        headers.add(new Header("Content-Type", APPLICATION_XML));
        HTTPTestRequest cMsg = MessageUtils
                .generateHTTPMessage(path, Constants.HTTP_METHOD_POST, headers, bxmlItemString);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(getReturnValue(response), value);
    }

    @Test
    public void testRemoveHeader() {
        BStruct request = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, outRequestStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(true);
        String expect = "Expect";
        cMsg.setHeader(expect, "100-continue");
        HttpUtil.addCarbonMsg(request, cMsg);
        BString key = new BString(expect);
        BValue[] inputArg = { request, key };
        BValue[] returnVals = BRunUtil.invoke(result, "testRemoveHeader", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BStruct);
        BStruct entityStruct = (BStruct) ((BStruct) returnVals[0]).getNativeData(MESSAGE_ENTITY);
        BMap map = (BMap) entityStruct.getRefField(ENTITY_HEADERS_INDEX);
        Assert.assertNull(map.get("100-continue"));
    }

    @Test(description = "Test RemoveHeader function within a service")
    public void testServiceRemoveHeader() {
        String path = "/hello/RemoveHeader";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, Constants.HTTP_METHOD_GET);
        cMsg.setHeader(CONTENT_TYPE, APPLICATION_FORM);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("value").asText(), "value is null");
    }

    @Test
    public void testRemoveAllHeaders() {
        BStruct request = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, outRequestStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(true);
        String expect = "Expect";
        String range = "Range";
        cMsg.setHeader(expect, "100-continue");
        cMsg.setHeader(range, "bytes=500-999");
        HttpUtil.addCarbonMsg(request, cMsg);
        BValue[] inputArg = { request };
        BValue[] returnVals = BRunUtil.invoke(result, "testRemoveAllHeaders", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BStruct);
        BStruct entityStruct = (BStruct) ((BStruct) returnVals[0]).getNativeData(MESSAGE_ENTITY);
        BMap map = (BMap) entityStruct.getRefField(ENTITY_HEADERS_INDEX);
        Assert.assertNull(map.get(expect));
        Assert.assertNull(map.get(range));
    }

    @Test(description = "Test RemoveAllHeaders function within a service")
    public void testServiceRemoveAllHeaders() {
        String expect = "Expect";
        String range = "Range";
        String path = "/hello/RemoveAllHeaders";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, Constants.HTTP_METHOD_GET);
        cMsg.setHeader(expect, "100-continue");
        cMsg.setHeader(range, "bytes=500-999");
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("value").asText(), "value is null");
    }

    @Test
    public void testSetHeader() {
        BStruct request = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, outRequestStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(true);
        HttpUtil.addCarbonMsg(request, cMsg);
        String range = "Range";
        String rangeValue = "bytes=500-999";
        BString key = new BString(range);
        BString value = new BString(rangeValue);
        BValue[] inputArg = { request, key, value };
        BValue[] returnVals = BRunUtil.invoke(result, "testSetHeader", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BStruct);
        BStruct entityStruct = (BStruct) ((BStruct) returnVals[0]).getNativeData(MESSAGE_ENTITY);
        BMap map = (BMap) entityStruct.getRefField(ENTITY_HEADERS_INDEX);
        BRefValueArray array = (BRefValueArray) map.get(range);
        Assert.assertEquals(((BStruct) array.get(0)).getStringField(0), rangeValue);
    }

    //TODO There is a problem with this test case. Please refactor it.
    //TODO Passing the InRequest as an argument to a function which expect OutRequest. 
//    @Test
    public void testSetHeaderStruct() {
        BStruct request = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inRequestStruct);
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("", Constants.HTTP_METHOD_GET);
        HttpUtil.addCarbonMsg(request, cMsg);
        HttpUtil.setHeaderValueStructType(
                BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct));
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        BStruct mediaType = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, mediaTypeStruct);
        HttpUtil.populateInboundRequest(request, entity, mediaType, cMsg);
        String range = "Range";
        String rangeValue = "bytes=500-999";
        BString key = new BString(range);
        BString value = new BString(rangeValue);
        BValue[] inputArg = { request, key, value };
        BValue[] returnVals = BRunUtil.invoke(result, "testSetHeaderStruct", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BStruct);
        BStruct entityStruct = (BStruct) ((BStruct) returnVals[0]).getNativeData(MESSAGE_ENTITY);
        BMap map = (BMap) entityStruct.getRefField(ENTITY_HEADERS_INDEX);
        BRefValueArray array = (BRefValueArray) map.get(range);
        Assert.assertEquals(((BStruct) array.get(0)).getStringField(0), rangeValue);
    }

    @Test(description = "Test SetHeader function within a service")
    public void testServiceSetHeader() {
        String key = "lang";
        String value = "ballerina";
        String path = "/hello/setHeader/" + key + "/" + value;
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, Constants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("value").asText(), value);
    }

    @Test(description = "Test Setting Header in struct within a service")
    public void testServiceSetHeaderStruct() {
        String key = "lang";
        String value = "ballerina";
        String path = "/hello/setHeaderStruct/" + key + "/" + value;
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, Constants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("value").asText(), value);
    }

    @Test
    public void testSetJsonPayload() {
        BStruct request = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, outRequestStruct);
        HTTPCarbonMessage requestMsg = HttpUtil.createHttpCarbonMessage(true);
        HttpUtil.addCarbonMsg(request, requestMsg);

        BJSON value = new BJSON("{'name':'wso2'}");
        BValue[] inputArg = { request, value };
        BValue[] returnVals = BRunUtil.invoke(result, "testSetJsonPayload", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BStruct);
        BStruct entity = (BStruct) ((BStruct) returnVals[0]).getNativeData(MESSAGE_ENTITY);
        BJSON bJson = (BJSON) entity.getRefField(JSON_DATA_INDEX);
        Assert.assertEquals(bJson.value().get("name").asText(), "wso2", "Payload is not set properly");
    }

    @Test(description = "Test SetJsonPayload function within a service")
    public void testServiceSetJsonPayload() {
        String value = "ballerina";
        String path = "/hello/SetJsonPayload/" + value;
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, Constants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("lang").asText(), value);
    }

    @Test
    public void testSetProperty() {
        BStruct request = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, outRequestStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(true);
        HttpUtil.addCarbonMsg(request, cMsg);
        String propertyName = "wso2";
        String propertyValue = "Ballerina";
        BString name = new BString(propertyName);
        BString value = new BString(propertyValue);
        BValue[] inputArg = { request, name, value };
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
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, Constants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("value").asText(), value);
    }

    @Test
    public void testSetStringPayload() {
        BStruct request = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, outRequestStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(true);
        HttpUtil.addCarbonMsg(request, cMsg);

        BString value = new BString("Ballerina");
        BValue[] inputArg = { request, value };
        BValue[] returnVals = BRunUtil.invoke(result, "testSetStringPayload", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BStruct);
        BStruct entity = (BStruct) ((BStruct) returnVals[0]).getNativeData(MESSAGE_ENTITY);
        String stringValue = entity.getStringField(TEXT_DATA_INDEX);
        Assert.assertEquals(stringValue, "Ballerina", "Payload is not set properly");
    }

    @Test(description = "Test SetStringPayload function within a service")
    public void testServiceSetStringPayload() {
        String value = "ballerina";
        String path = "/hello/SetStringPayload/" + value;
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, Constants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("lang").asText(), value);
    }

    @Test
    public void testSetXmlPayload() {
        BStruct request = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, outRequestStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(true);
        HttpUtil.addCarbonMsg(request, cMsg);

        BXMLItem value = new BXMLItem("<name>Ballerina</name>");
        BValue[] inputArg = { request, value };
        BValue[] returnVals = BRunUtil.invoke(result, "testSetXmlPayload", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BStruct);
        BStruct entity = (BStruct) ((BStruct) returnVals[0]).getNativeData(MESSAGE_ENTITY);
        BXMLItem xmlValue = (BXMLItem) entity.getRefField(XML_DATA_INDEX);
        Assert.assertEquals(xmlValue.getTextValue().stringValue(), "Ballerina", "Payload is not set properly");
    }

    @Test(description = "Test SetXmlPayload function within a service")
    public void testServiceSetXmlPayload() {
        String value = "Ballerina";
        String path = "/hello/SetXmlPayload/";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, Constants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("lang").asText(), value);
    }

    @Test
    public void testGetMethod() {
        String path = "/hello/11";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, Constants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(
                StringUtils.getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream()),
                Constants.HTTP_METHOD_GET);
    }

    @Test
    public void testGetRequestURL() {
        String path = "/hello/12";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, Constants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(
                StringUtils.getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream()), path);
    }

    @Test(description = "Test setBinaryPayload() function within a service")
    public void testServiceSetBinaryPayload() {
        String value = "Ballerina";
        String path = "/hello/SetBinaryPayload/";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, Constants.HTTP_METHOD_GET);

        HTTPCarbonMessage response = Services.invokeNew(serviceResult, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("lang").asText(), value);
    }

    @Test(description = "Test getBinaryPayload() function within a service")
    public void testServiceGetBinaryPayload() {
        String payload = "ballerina";
        String path = "/hello/GetBinaryPayload";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, Constants.HTTP_METHOD_POST, payload);

        HTTPCarbonMessage response = Services.invokeNew(serviceResult, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(
                StringUtils.getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream()), payload);
    }

    @Test(description = "Test setBinaryPayload() function")
    public void testSetBinaryPayload() {
        BStruct request = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, outRequestStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(true);
        HttpUtil.addCarbonMsg(request, cMsg);

        BBlob value = new BBlob("Ballerina".getBytes());
        BValue[] inputArg = { request, value };

        BValue[] returnVals = BRunUtil.invoke(result, "testSetBinaryPayload", inputArg);

        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BStruct);
        BStruct entity = (BStruct) ((BStruct) returnVals[0]).getNativeData(MESSAGE_ENTITY);
        BlobDataSource blobDataSource = new BlobDataSource(entity.getBlobField(BYTE_DATA_INDEX));
        Assert.assertEquals(blobDataSource.getMessageAsString(), "Ballerina", "Payload is not set properly");
    }

    @Test (description = "Test setEntityBody() function")
    public void testSetEntityBody() {
        BStruct request = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, outRequestStruct);
        HTTPCarbonMessage requestMsg = HttpUtil.createHttpCarbonMessage(true);
        HttpUtil.addCarbonMsg(request, requestMsg);

        try {
            File file = File.createTempFile("test", ".json");
            file.deleteOnExit();
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            bufferedWriter.write("{'name':'wso2'}");
            bufferedWriter.close();

            BStruct fileStruct = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageFile, FILE);
            fileStruct.setStringField(0, file.getAbsolutePath());
            BValue[] inputArg = { request, fileStruct, new BString(APPLICATION_JSON) };
            BValue[] returnVals = BRunUtil.invoke(result, "testSetEntityBody", inputArg);
            Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                    "Invalid Return Values.");
            Assert.assertTrue(returnVals[0] instanceof BStruct);
            BStruct entity = (BStruct) ((BStruct) returnVals[0]).getNativeData(MESSAGE_ENTITY);
            BStruct returnFileStruct = (BStruct) entity.getRefField(OVERFLOW_DATA_INDEX);

            String returnJsonValue = new String (Files.readAllBytes(Paths.get(returnFileStruct.getStringField(0))),
                    UTF_8);
            BJSON bJson = new BJSON(returnJsonValue);
            Assert.assertEquals(bJson.value().get("name").asText(), "wso2", "Payload is not set properly");
        } catch (IOException e) {
            LOG.error("Error occured while creating a temporary file in testSetEntityBody", e.getMessage());
        }
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
