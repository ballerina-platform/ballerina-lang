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
package org.ballerinalang.test.services.nativeimpl.request;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
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
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.model.values.BXMLItem;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.net.http.caching.RequestCacheControlStruct;
import org.ballerinalang.runtime.message.MessageDataSource;
import org.ballerinalang.runtime.message.StringDataSource;
import org.ballerinalang.test.services.testutils.HTTPTestRequest;
import org.ballerinalang.test.services.testutils.MessageUtils;
import org.ballerinalang.test.services.testutils.Services;
import org.ballerinalang.test.utils.ResponseReader;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.mime.util.Constants.APPLICATION_FORM;
import static org.ballerinalang.mime.util.Constants.APPLICATION_JSON;
import static org.ballerinalang.mime.util.Constants.APPLICATION_XML;
import static org.ballerinalang.mime.util.Constants.ENTITY_BYTE_CHANNEL;
import static org.ballerinalang.mime.util.Constants.ENTITY_HEADERS;
import static org.ballerinalang.mime.util.Constants.IS_BODY_BYTE_CHANNEL_ALREADY_SET;
import static org.ballerinalang.mime.util.Constants.MEDIA_TYPE;
import static org.ballerinalang.mime.util.Constants.MESSAGE_ENTITY;
import static org.ballerinalang.mime.util.Constants.OCTET_STREAM;
import static org.ballerinalang.mime.util.Constants.PROTOCOL_PACKAGE_MIME;
import static org.ballerinalang.mime.util.Constants.TEXT_PLAIN;
import static org.ballerinalang.net.http.HttpConstants.REQUEST_CACHE_CONTROL;

/**
 * Test cases for ballerina/http request success native functions.
 */
public class RequestNativeFunctionSuccessTest {

    private CompileResult result, serviceResult;
    private final String reqStruct = HttpConstants.REQUEST;
    private final String protocolPackageHttp = HttpConstants.PROTOCOL_PACKAGE_HTTP;
    private final String protocolPackageMime = PROTOCOL_PACKAGE_MIME;
    private final String entityStruct = HttpConstants.ENTITY;
    private final String mediaTypeStruct = MEDIA_TYPE;
    private final String reqCacheControlStruct = REQUEST_CACHE_CONTROL;
    private static final String MOCK_ENDPOINT_NAME = "mockEP";

    @BeforeClass
    public void setup() {
        String resourceRoot = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        Path sourceRoot = Paths.get(resourceRoot, "test-src", "statements", "services", "nativeimpl",
                "request");
        result = BCompileUtil.compile(sourceRoot.resolve("in-request-native-function.bal").toString());
        serviceResult = BServiceUtil.setupProgramFile(this, sourceRoot.resolve("in-request-native-function.bal")
                .toString());
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
        HttpHeaders httpHeaders = (HttpHeaders) entityStruct.getNativeData(ENTITY_HEADERS);
        Assert.assertEquals(httpHeaders.getAll(headerName).get(0), "1stHeader");
        Assert.assertEquals(httpHeaders.getAll(headerName).get(1), headerValue);
    }

    @Test(description = "Test addHeader function within a service")
    public void testServiceAddHeader() {
        String key = "lang";
        String value = "ballerina";
        String path = "/hello/addheader/" + key + "/" + value;
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, MOCK_ENDPOINT_NAME, inRequestMsg);
        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(ResponseReader.getReturnValue(response));
        Assert.assertEquals(bJson.value().get(key).asText(), value);
    }

    @Test(description = "Test getBinaryPayload method of the request")
    public void testGetBinaryPayloadMethod() {
        BStruct inRequest = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, reqStruct);
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        BStruct mediaType = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, mediaTypeStruct);

        String payload = "ballerina";
        MimeUtil.setContentType(mediaType, entity, OCTET_STREAM);
        entity.addNativeData(ENTITY_BYTE_CHANNEL, EntityBodyHandler.getEntityWrapper(payload));
        inRequest.addNativeData(MESSAGE_ENTITY, entity);
        inRequest.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, true);

        BValue[] inputArg = {inRequest};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetBinaryPayload", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(returnVals[0].stringValue(), payload);
    }

    @Test(description = "Enable this once the getContentLength() is added back in http package", enabled = false)
    public void testGetContentLength() {
        BStruct inRequest = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, reqStruct);
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        String payload = "ballerina";
        HttpHeaders httpHeaders = new DefaultHttpHeaders();
        httpHeaders.add(HttpHeaderNames.CONTENT_LENGTH.toString(), payload.length());
        entity.addNativeData(ENTITY_HEADERS, httpHeaders);
        inRequest.addNativeData(MESSAGE_ENTITY, entity);

        BValue[] inputArg = {inRequest};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetContentLength", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(payload.length(), ((BInteger) returnVals[0]).intValue());
    }

    @Test(description = "Test GetContentLength function within a service. Enable this once this method is back in " +
            "http package", enabled = false)
    public void testServiceGetContentLength() {
        String key = "lang";
        String value = "ballerina";
        String path = "/hello/getContentLength";
        String jsonString = "{\"" + key + "\":\"" + value + "\"}";
        int length = jsonString.length();
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_POST,
                jsonString);
        inRequestMsg.setHeader(HttpHeaderNames.CONTENT_LENGTH.toString(), String.valueOf(length));

        HTTPCarbonMessage response = Services.invokeNew(serviceResult, MOCK_ENDPOINT_NAME, inRequestMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("value").asText(), String.valueOf(length));
    }

    @Test
    public void testGetHeader() {
        BStruct inRequest = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, reqStruct);
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage("", HttpConstants.HTTP_METHOD_GET);
        inRequestMsg.setHeader(HttpHeaderNames.CONTENT_TYPE.toString(), APPLICATION_FORM);

        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        BStruct mediaType = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, mediaTypeStruct);
        BStruct cacheControl = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp,
                                                               reqCacheControlStruct);
        RequestCacheControlStruct cacheControlStruct = new RequestCacheControlStruct(cacheControl);
        HttpUtil.populateInboundRequest(inRequest, entity, mediaType, inRequestMsg, cacheControlStruct);

        BString key = new BString(HttpHeaderNames.CONTENT_TYPE.toString());
        BValue[] inputArg = {inRequest, key};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetHeader", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(returnVals[0].stringValue(), APPLICATION_FORM);
    }

    @Test(description = "Test GetHeader function within a service")
    public void testServiceGetHeader() {
        String path = "/hello/getHeader";
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        inRequestMsg.setHeader(HttpHeaderNames.CONTENT_TYPE.toString(), APPLICATION_FORM);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, MOCK_ENDPOINT_NAME, inRequestMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("value").asText(), APPLICATION_FORM);
    }

    @Test(description = "Test GetHeaders function within a function")
    public void testGetHeaders() {
        BStruct inRequest = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, reqStruct);
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage("", HttpConstants.HTTP_METHOD_GET);
        HttpHeaders headers = inRequestMsg.getHeaders();
        headers.set("test-header", APPLICATION_FORM);
        headers.add("test-header", TEXT_PLAIN);

        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        BStruct mediaType = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, mediaTypeStruct);
        BStruct cacheControl = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp,
                                                               reqCacheControlStruct);
        RequestCacheControlStruct cacheControlStruct = new RequestCacheControlStruct(cacheControl);
        HttpUtil.populateInboundRequest(inRequest, entity, mediaType, inRequestMsg, cacheControlStruct);

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
        BStruct inRequest = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, reqStruct);
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        BStruct mediaType = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, mediaTypeStruct);

        String payload = "{'code':'123'}";
        MimeUtil.setContentType(mediaType, entity, APPLICATION_JSON);
        entity.addNativeData(ENTITY_BYTE_CHANNEL, EntityBodyHandler.getEntityWrapper(payload));
        inRequest.addNativeData(MESSAGE_ENTITY, entity);
        inRequest.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, true);
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
        headers.add(new Header(HttpHeaderNames.CONTENT_TYPE.toString(), APPLICATION_JSON));
        HTTPTestRequest inRequestMsg = MessageUtils
                .generateHTTPMessage(path, HttpConstants.HTTP_METHOD_POST, headers, jsonString);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, MOCK_ENDPOINT_NAME, inRequestMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(new BJSON(ResponseReader.getReturnValue(response)).value().stringValue(), value);
    }

    @Test
    public void testGetStringPayload() {
        BStruct inRequest = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, reqStruct);
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        BStruct mediaType = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, mediaTypeStruct);

        String payload = "ballerina";
        MimeUtil.setContentType(mediaType, entity, TEXT_PLAIN);
        entity.addNativeData(ENTITY_BYTE_CHANNEL, EntityBodyHandler.getEntityWrapper(payload));
        inRequest.addNativeData(MESSAGE_ENTITY, entity);
        inRequest.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, true);

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
        headers.add(new Header(HttpHeaderNames.CONTENT_TYPE.toString(), TEXT_PLAIN));
        HTTPTestRequest inRequestMsg = MessageUtils
                .generateHTTPMessage(path, HttpConstants.HTTP_METHOD_POST, headers, value);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, MOCK_ENDPOINT_NAME, inRequestMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(ResponseReader.getReturnValue(response), value);
    }

    @Test
    public void testGetXmlPayload() {
        BStruct inRequest = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, reqStruct);
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        BStruct mediaType = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, mediaTypeStruct);

        String payload = "<name>ballerina</name>";
        MimeUtil.setContentType(mediaType, entity, APPLICATION_XML);
        entity.addNativeData(ENTITY_BYTE_CHANNEL, EntityBodyHandler.getEntityWrapper(payload));
        inRequest.addNativeData(MESSAGE_ENTITY, entity);
        inRequest.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, true);

        BValue[] inputArg = {inRequest};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetXmlPayload", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(((BXML) returnVals[0]).getTextValue().stringValue(), "ballerina");
    }

    @Test(description = "Test GetXmlPayload function within a service")
    public void testServiceGetXmlPayload() {
        String value = "ballerina";
        String path = "/hello/GetXmlPayload";
        String bxmlItemString = "<name>ballerina</name>";
        List<Header> headers = new ArrayList<>();
        headers.add(new Header(HttpHeaderNames.CONTENT_TYPE.toString(), APPLICATION_XML));
        HTTPTestRequest inRequestMsg = MessageUtils
                .generateHTTPMessage(path, HttpConstants.HTTP_METHOD_POST, headers, bxmlItemString);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, MOCK_ENDPOINT_NAME, inRequestMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(ResponseReader.getReturnValue(response), value);
    }

    @Test
    public void testGetMethod() {
        String path = "/hello/11";
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, MOCK_ENDPOINT_NAME, inRequestMsg);

        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(
                StringUtils.getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream()),
                HttpConstants.HTTP_METHOD_GET);
    }

    @Test
    public void testGetRequestURL() {
        String path = "/hello/12";
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, MOCK_ENDPOINT_NAME, inRequestMsg);

        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(
                StringUtils.getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream()), path);
    }

    @Test(description = "Test getStringPayload method with JSON payload")
    public void testGetStringPayloadMethodWithJsonPayload() {
        BStruct inRequest = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, reqStruct);
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        BStruct mediaType = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, mediaTypeStruct);

        String payload = "{\"code\":\"123\"}";
        MimeUtil.setContentType(mediaType, entity, APPLICATION_JSON);
        entity.addNativeData(ENTITY_BYTE_CHANNEL, EntityBodyHandler.getEntityWrapper(payload));
        inRequest.addNativeData(MESSAGE_ENTITY, entity);
        inRequest.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, true);

        BValue[] inputArg = {inRequest};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetStringPayload", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(returnVals[0].stringValue(), payload);
    }

    @Test(description = "Test GetByteChannel function within a service. Send a json content as a request " +
            "and then get a byte channel from the Request and set that ByteChannel as the response content")
    public void testServiceGetByteChannel() {
        String key = "lang";
        String value = "ballerina";
        String path = "/hello/GetByteChannel";
        String jsonString = "{\"" + key + "\":\"" + value + "\"}";
        List<Header> headers = new ArrayList<>();
        headers.add(new Header("Content-Type", APPLICATION_JSON));
        HTTPTestRequest inRequestMsg = MessageUtils
                .generateHTTPMessage(path, HttpConstants.HTTP_METHOD_POST, headers, jsonString);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, MOCK_ENDPOINT_NAME, inRequestMsg);
        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get(key).asText(), value);
    }

    @Test(description = "Test RemoveAllHeaders function within a service")
    public void testServiceRemoveAllHeaders() {
        String path = "/hello/RemoveAllHeaders";
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, MOCK_ENDPOINT_NAME, inRequestMsg);

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
        HttpHeaders httpHeaders = (HttpHeaders) entityStruct.getNativeData(ENTITY_HEADERS);
        Assert.assertEquals(httpHeaders.get(headerName), headerValue);
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
        HttpHeaders httpHeaders = (HttpHeaders) entityStruct.getNativeData(ENTITY_HEADERS);
        Assert.assertEquals(httpHeaders.get(headerName), headerValue);
    }

    @Test(description = "Test SetHeader function within a service")
    public void testServiceSetHeader() {
        String key = "lang";
        String value = "ballerina";
        String path = "/hello/setHeader/" + key + "/" + value;
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, MOCK_ENDPOINT_NAME, inRequestMsg);

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
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, MOCK_ENDPOINT_NAME, inRequestMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("lang").asText(), value);
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
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, MOCK_ENDPOINT_NAME, inRequestMsg);

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
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, MOCK_ENDPOINT_NAME, inRequestMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("lang").asText(), value);
    }

    @Test(description = "Test setBinaryPayload() function within a service")
    public void testServiceSetBinaryPayload() {
        String value = "Ballerina";
        String path = "/hello/SetBinaryPayload/";
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, MOCK_ENDPOINT_NAME, inRequestMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("lang").asText(), value);
    }

    @Test(description = "Test getBinaryPayload() function within a service")
    public void testServiceGetBinaryPayload() {
        String payload = "ballerina";
        String path = "/hello/GetBinaryPayload";
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_POST, payload);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, MOCK_ENDPOINT_NAME, inRequestMsg);

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
    public void testSetEntityBody() throws IOException {
        File file = File.createTempFile("test", ".json");
        file.deleteOnExit();
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
        bufferedWriter.write("{'name':'wso2'}");
        bufferedWriter.close();

        BValue[] inputArg = {new BString(file.getAbsolutePath()), new BString(APPLICATION_JSON)};
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

    }
}
