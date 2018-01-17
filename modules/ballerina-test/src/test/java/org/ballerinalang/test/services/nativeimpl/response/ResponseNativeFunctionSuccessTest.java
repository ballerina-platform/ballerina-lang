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
package org.ballerinalang.test.services.nativeimpl.response;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.BServiceUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.model.util.StringUtils;
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
import org.ballerinalang.test.services.testutils.HTTPTestRequest;
import org.ballerinalang.test.services.testutils.MessageUtils;
import org.ballerinalang.test.services.testutils.Services;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;

import static org.ballerinalang.mime.util.Constants.APPLICATION_FORM;
import static org.ballerinalang.mime.util.Constants.APPLICATION_JSON;
import static org.ballerinalang.mime.util.Constants.APPLICATION_XML;
import static org.ballerinalang.mime.util.Constants.BYTE_DATA_INDEX;
import static org.ballerinalang.mime.util.Constants.CONTENT_TYPE;
import static org.ballerinalang.mime.util.Constants.ENTITY_HEADERS_INDEX;
import static org.ballerinalang.mime.util.Constants.HEADER_VALUE_STRUCT;
import static org.ballerinalang.mime.util.Constants.IS_ENTITY_BODY_PRESENT;
import static org.ballerinalang.mime.util.Constants.IS_IN_MEMORY_INDEX;
import static org.ballerinalang.mime.util.Constants.JSON_DATA_INDEX;
import static org.ballerinalang.mime.util.Constants.MEDIA_TYPE;
import static org.ballerinalang.mime.util.Constants.MESSAGE_ENTITY;
import static org.ballerinalang.mime.util.Constants.OCTET_STREAM;
import static org.ballerinalang.mime.util.Constants.PROTOCOL_PACKAGE_MIME;
import static org.ballerinalang.mime.util.Constants.TEXT_DATA_INDEX;
import static org.ballerinalang.mime.util.Constants.TEXT_PLAIN;
import static org.ballerinalang.mime.util.Constants.XML_DATA_INDEX;

/**
 * Test cases for ballerina.net.http.response success native functions.
 */
public class ResponseNativeFunctionSuccessTest {

    private CompileResult result, serviceResult;
    private final String inResponseStruct = Constants.IN_RESPONSE;
    private final String entityStruct = Constants.ENTITY;
    private final String mediaTypeStruct = MEDIA_TYPE;
    private final String headerStruct = HEADER_VALUE_STRUCT;
    private final String protocolPackageHttp = Constants.PROTOCOL_PACKAGE_HTTP;
    private final String protocolPackageMime = PROTOCOL_PACKAGE_MIME;
    private String sourceFilePath = "test-src/statements/services/nativeimpl/response/response-native-function.bal";

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile(sourceFilePath);
        serviceResult = BServiceUtil.setupProgramFile(this, sourceFilePath);
    }

    @Test
    public void testAddHeader() {
        BStruct response = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inResponseStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(false);

        HttpUtil.addCarbonMsg(response, cMsg);

        String headerName = "header1";
        String headerValue = "headerValue";
        BString key = new BString(headerName);
        BString value = new BString(headerValue);
        BValue[] inputArg = { response, key, value };
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
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get(key).asText(), value);
    }

    @Test(description = "Test res struct add Header function")
    public void testStructAddHeader() {
        String value = "ballerina";
        String path = "/hello/addResHeader";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, Constants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("headerValue").asText(), value);
        Assert.assertEquals(bJson.value().get("paramValue").asText(), String.valueOf(6));
    }

    @Test(description = "Test res struct add Header function without params")
    public void testStructAddHeaderWithNoParam() {
        String value = "ballerina";
        String path = "/hello/addResHeaderWithoutParam";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, Constants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("headerValue").asText(), value);
        Assert.assertEquals(bJson.value().get("paramValue").asText(), "param is null");
    }

    @Test(description = "Test req struct add Header function")
    public void testAddHeaderViaBalFunction() {
        String path = "/hello/addResHeaderFunc";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, Constants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("headerValue").asText(), "chamil");
        Assert.assertEquals(bJson.value().get("size").asText(), String.valueOf(3));
    }

    @Test
    public void testGetBinaryPayloadMethod() {
        BStruct response = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inResponseStruct);
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        BStruct mediaType = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, mediaTypeStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(true);

        String payload = "ballerina";
        String contentType = OCTET_STREAM;
        MimeUtil.setContentType(mediaType, entity, contentType);
        entity.setBlobField(BYTE_DATA_INDEX, payload.getBytes());
        entity.setBooleanField(IS_IN_MEMORY_INDEX, 1);
        response.addNativeData(MESSAGE_ENTITY, entity);
        response.addNativeData(IS_ENTITY_BODY_PRESENT, true);

        HttpUtil.addCarbonMsg(response, cMsg);
        BValue[] inputArg = { response };
        BValue[] returnVals = BRunUtil.invoke(result, "testGetBinaryPayload", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(returnVals[0].stringValue(), payload);
    }

    @Test
    public void testGetContentLength() {
        BStruct response = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inResponseStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(false);

        String payload = "ballerina";
        cMsg.setHeader(Constants.HTTP_CONTENT_LENGTH, String.valueOf(payload.length()));
        cMsg.setProperty(Constants.HTTP_STATUS_CODE, 200);
        HttpUtil.addCarbonMsg(response, cMsg);

        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        BStruct mediaType = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, mediaTypeStruct);
        HttpUtil.setHeaderValueStructType(
                BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, headerStruct));
        HttpUtil.populateInboundResponse(response, entity, mediaType, cMsg);

        BValue[] inputArg = { response };
        BValue[] returnVals = BRunUtil.invoke(result, "testGetContentLength", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(payload.length(), ((BInteger) returnVals[0]).intValue());
    }

    @Test(description = "Test GetContentLength function within a service")
    public void testServiceGetContentLength() {
        String header = "Content-Length";
        String length = "10";
        String path = "/hello/getContentLength/" + header + "/" + length;
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, Constants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("value").asText(), length);
    }

    @Test
    public void testGetHeader() {
        BStruct response = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inResponseStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(false);
        cMsg.setHeader(CONTENT_TYPE, APPLICATION_FORM);
        cMsg.setProperty(Constants.HTTP_STATUS_CODE, 200);
        HttpUtil.addCarbonMsg(response, cMsg);
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        BStruct mediaType = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, mediaTypeStruct);
        HttpUtil.setHeaderValueStructType(
                BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, headerStruct));
        HttpUtil.populateInboundResponse(response, entity, mediaType, cMsg);

        BString key = new BString(CONTENT_TYPE);

        BValue[] inputArg = { response, key };
        BValue[] returnVals = BRunUtil.invoke(result, "testGetHeader", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(returnVals[0].stringValue(), APPLICATION_FORM);
    }

    @Test(description = "Test GetHeader function within a service")
    public void testServiceGetHeader() {
        String value = "x-www-form-urlencoded";
        String path = "/hello/getHeader/" + CONTENT_TYPE + "/" + value;
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, Constants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("value").asText(), value);
    }

    @Test(description = "Test struct Get Header operation")
    public void testStructGetHeader() {
        String path = "/hello/getResHeader";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, Constants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("value").asText(), "ballerina");
    }

    @Test(description = "Test GetHeaders function within a function")
    public void testGetHeaders() {
        BStruct response = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inResponseStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(false);
        cMsg.setHeader("test-header", APPLICATION_FORM + "," + TEXT_PLAIN + ";b=5");
        cMsg.setProperty(Constants.HTTP_STATUS_CODE, 200);
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        BStruct mediaType = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, mediaTypeStruct);
        HttpUtil.setHeaderValueStructType(
                BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, headerStruct));
        HttpUtil.populateInboundResponse(response, entity, mediaType, cMsg);

        BString key = new BString("test-header");
        BValue[] inputArg = { response, key };
        BValue[] returnVals = BRunUtil.invoke(result, "testGetHeaders", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(returnVals[0].stringValue(), TEXT_PLAIN);
    }

    @Test(description = "Test GetHeaders function within a service")
    public void testServiceGetHeaders() {
        String path = "/hello/getHeaders";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, Constants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("value").asText(), "transport");
        Assert.assertEquals(bJson.value().get("paramValue").asText(), String.valueOf(6));
    }

    @Test
    public void testGetJsonPayload() {
        BStruct response = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inResponseStruct);
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        BStruct mediaType = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, mediaTypeStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(false);
        String payload = "{'code':'123'}";
        cMsg.setHeader(CONTENT_TYPE, APPLICATION_JSON);
        HttpUtil.addCarbonMsg(response, cMsg);
        MimeUtil.setContentType(mediaType, entity, APPLICATION_JSON);
        entity.setRefField(JSON_DATA_INDEX, new BJSON(payload));
        entity.setBooleanField(IS_IN_MEMORY_INDEX, 1);
        response.addNativeData(MESSAGE_ENTITY, entity);
        response.addNativeData(IS_ENTITY_BODY_PRESENT, true);
        BValue[] inputArg = { response };
        BValue[] returnVals = BRunUtil.invoke(result, "testGetJsonPayload", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(((BJSON) returnVals[0]).value().get("code").asText(), "123");
    }

    @Test(description = "Test GetJsonPayload function within a service")
    public void testServiceGetJsonPayload() {
        String value = "ballerina";
        String path = "/hello/getJsonPayload/" + value;
        HTTPTestRequest requestMsg = MessageUtils.generateHTTPMessage(path, Constants.HTTP_METHOD_GET);
        HTTPCarbonMessage responseMsg = Services.invokeNew(serviceResult, requestMsg);

        Assert.assertNotNull(responseMsg, "Response message not found");
        Assert.assertEquals(new BJSON(new HttpMessageDataStreamer(responseMsg).getInputStream()).stringValue(), value);
    }

    @Test
    public void testGetProperty() {
        BStruct response = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inResponseStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(false);
        String propertyName = "wso2";
        String propertyValue = "Ballerina";
        cMsg.setProperty(propertyName, propertyValue);
        HttpUtil.addCarbonMsg(response, cMsg);
        BString name = new BString(propertyName);
        BValue[] inputArg = { response, name };
        BValue[] returnVals = BRunUtil.invoke(result, "testGetProperty", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(returnVals[0].stringValue(), propertyValue);
    }

    @Test(description = "Test GetProperty function within a service")
    public void testServiceGetProperty() {
        String propertyName = "wso2";
        String propertyValue = "Ballerina";
        String path = "/hello/GetProperty/" + propertyName + "/" + propertyValue;
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, Constants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("value").asText(), propertyValue);
    }

    @Test
    public void testGetStringPayload() {
        BStruct response = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inResponseStruct);
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        BStruct mediaType = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, mediaTypeStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(false);
        String payload = "ballerina";
        HttpUtil.addCarbonMsg(response, cMsg);
        String contentType = TEXT_PLAIN;
        MimeUtil.setContentType(mediaType, entity, contentType);
        entity.setStringField(TEXT_DATA_INDEX, payload);
        entity.setBooleanField(IS_IN_MEMORY_INDEX, 1);
        response.addNativeData(MESSAGE_ENTITY, entity);
        response.addNativeData(IS_ENTITY_BODY_PRESENT, true);
        BValue[] inputArg = { response };
        BValue[] returnVals = BRunUtil.invoke(result, "testGetStringPayload", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(returnVals[0].stringValue(), payload);
    }

    @Test(description = "Test GetStringPayload function within a service")
    public void testServiceGetStringPayload() {
        String value = "ballerina";
        String path = "/hello/GetStringPayload/" + value;
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, Constants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(
                StringUtils.getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream()), value);
    }

    @Test
    public void testGetXmlPayload() {
        BStruct response = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inResponseStruct);
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        BStruct mediaType = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, mediaTypeStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(false);
        String payload = "<name>ballerina</name>";
        HttpUtil.addCarbonMsg(response, cMsg);
        String contentType = APPLICATION_XML;
        MimeUtil.setContentType(mediaType, entity, contentType);
        entity.setRefField(XML_DATA_INDEX, new BXMLItem(payload));
        entity.setBooleanField(IS_IN_MEMORY_INDEX, 1);
        response.addNativeData(MESSAGE_ENTITY, entity);
        response.addNativeData(IS_ENTITY_BODY_PRESENT, true);
        BValue[] inputArg = { response };
        BValue[] returnVals = BRunUtil.invoke(result, "testGetXmlPayload", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(((BXMLItem) returnVals[0]).getTextValue().stringValue(), "ballerina");
    }

    @Test(description = "Test GetXmlPayload function within a service")
    public void testServiceGetXmlPayload() {
        String value = "ballerina";
        String path = "/hello/GetXmlPayload";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, Constants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(
                StringUtils.getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream()), value);
    }

    @Test
    public void testRemoveHeader() {
        BStruct response = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inResponseStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(false);
        String expect = "Expect";
        cMsg.setHeader(expect, "100-continue");
        HttpUtil.addCarbonMsg(response, cMsg);
        BString key = new BString(expect);
        BValue[] inputArg = { response, key };
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
        String key = CONTENT_TYPE;
        String value = "x-www-form-urlencoded";
        String path = "/hello/RemoveHeader/" + key + "/" + value;
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, Constants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("value").asText(), "value is null");
    }

    @Test
    public void testRemoveAllHeaders() {
        BStruct response = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inResponseStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(false);
        String expect = "Expect";
        String range = "Range";
        cMsg.setHeader(expect, "100-continue");
        cMsg.setHeader(range, "bytes=500-999");
        HttpUtil.addCarbonMsg(response, cMsg);
        BValue[] inputArg = { response };
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
        String path = "/hello/RemoveAllHeaders";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, Constants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("value").asText(), "value is null");
    }

    @Test
    public void testSendMethod() {
        String path = "/hello/11";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, Constants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, cMsg);

        Assert.assertNotNull(response, "Response message not found");
    }

    @Test
    public void testSetHeader() {
        BStruct response = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inResponseStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(false);
        HttpUtil.addCarbonMsg(response, cMsg);
        String range = "Range";
        String rangeValue = "bytes=500-999";
        BString key = new BString(range);
        BString value = new BString(rangeValue);
        BValue[] inputArg = { response, key, value };
        BValue[] returnVals = BRunUtil.invoke(result, "testSetHeader", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BStruct);
        BStruct entityStruct = (BStruct) ((BStruct) returnVals[0]).getNativeData(MESSAGE_ENTITY);
        BMap map = (BMap) entityStruct.getRefField(ENTITY_HEADERS_INDEX);
        BRefValueArray array = (BRefValueArray) map.get(range);
        Assert.assertEquals(((BStruct) array.get(0)).getStringField(0), rangeValue);
    }

    @Test
    public void testSetHeaderStruct() {
        BStruct response = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inResponseStruct);
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("", Constants.HTTP_METHOD_GET);
        cMsg.setProperty(Constants.HTTP_STATUS_CODE, 200);
        HttpUtil.addCarbonMsg(response, cMsg);
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        BStruct mediaType = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, mediaTypeStruct);
        HttpUtil.setHeaderValueStructType(
                BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, headerStruct));
        HttpUtil.populateInboundResponse(response, entity, mediaType, cMsg);

        String range = "Range";
        String rangeValue = "bytes=500-999";
        BString key = new BString(range);
        BString value = new BString(rangeValue);
        BValue[] inputArg = { response, key, value };
        BValue[] returnVals = BRunUtil.invoke(result, "testSetHeaderStruct", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BStruct);
        BStruct entityStruct = (BStruct) ((BStruct) returnVals[0]).getNativeData(MESSAGE_ENTITY);
        BMap map = (BMap) entityStruct.getRefField(ENTITY_HEADERS_INDEX);
        BRefValueArray array = (BRefValueArray) map.get(range);
        Assert.assertEquals(((BStruct) array.get(0)).getStringField(0), rangeValue);
    }

    @Test
    public void testSetJsonPayload() {
        BStruct response = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inResponseStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(false);
        HttpUtil.addCarbonMsg(response, cMsg);

        BJSON value = new BJSON("{'name':'wso2'}");
        BValue[] inputArg = { response, value };
        BValue[] returnVals = BRunUtil.invoke(result, "testSetJsonPayload", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BStruct);

        BStruct entity = (BStruct) ((BStruct) returnVals[0]).getNativeData(MESSAGE_ENTITY);
        BJSON bJson = (BJSON) entity.getRefField(JSON_DATA_INDEX);
        Assert.assertEquals(bJson.value().get("name").asText(), "wso2", "Payload is not set properly");
    }

    @Test
    public void testSetProperty() {
        BStruct response = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inResponseStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(false);
        HttpUtil.addCarbonMsg(response, cMsg);
        String propertyName = "wso2";
        String propertyValue = "Ballerina";
        BString name = new BString(propertyName);
        BString value = new BString(propertyValue);
        BValue[] inputArg = { response, name, value };
        BValue[] returnVals = BRunUtil.invoke(result, "testSetProperty", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BStruct);
        HTTPCarbonMessage responseMsg = HttpUtil.getCarbonMsg((BStruct) returnVals[0], null);
        Assert.assertEquals(responseMsg.getProperty(propertyName), propertyValue);
    }

    @Test
    public void testSetReasonPhase() {
        String phase = "ballerina";
        String path = "/hello/12/" + phase;
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, Constants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(response.getProperty(Constants.HTTP_REASON_PHRASE), phase);
    }

    @Test
    public void testSetStatusCode() {
        String path = "/hello/13";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, Constants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(response.getProperty(Constants.HTTP_STATUS_CODE), 203);
    }

    @Test
    public void testSetStringPayload() {
        BStruct response = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inResponseStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(false);
        HttpUtil.addCarbonMsg(response, cMsg);

        BString value = new BString("Ballerina");
        BValue[] inputArg = { response, value };
        BValue[] returnVals = BRunUtil.invoke(result, "testSetStringPayload", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BStruct);

        BStruct entity = (BStruct) ((BStruct) returnVals[0]).getNativeData(MESSAGE_ENTITY);
        String stringValue = entity.getStringField(TEXT_DATA_INDEX);
        Assert.assertEquals(stringValue, "Ballerina", "Payload is not set properly");
    }

    @Test
    public void testSetXmlPayload() {
        BStruct response = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inResponseStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(false);
        HttpUtil.addCarbonMsg(response, cMsg);

        BXMLItem value = new BXMLItem("<name>Ballerina</name>");
        BValue[] inputArg = { response, value };
        BValue[] returnVals = BRunUtil.invoke(result, "testSetXmlPayload", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BStruct);

        BStruct entity = (BStruct) ((BStruct) returnVals[0]).getNativeData(MESSAGE_ENTITY);
        BXMLItem xmlValue = (BXMLItem) entity.getRefField(XML_DATA_INDEX);
        Assert.assertEquals(xmlValue.getTextValue().stringValue(), "Ballerina", "Payload is not set properly");
    }
}
