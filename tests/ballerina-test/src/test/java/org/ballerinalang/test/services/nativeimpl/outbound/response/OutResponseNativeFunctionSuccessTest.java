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
package org.ballerinalang.test.services.nativeimpl.outbound.response;

import io.netty.handler.codec.http.HttpHeaderNames;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.BServiceUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.mime.util.EntityBodyHandler;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.model.util.StringUtils;
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
import static org.ballerinalang.mime.util.Constants.ENTITY_BYTE_CHANNEL;
import static org.ballerinalang.mime.util.Constants.ENTITY_HEADERS_INDEX;
import static org.ballerinalang.mime.util.Constants.IS_BODY_BYTE_CHANNEL_ALREADY_SET;
import static org.ballerinalang.mime.util.Constants.MEDIA_TYPE;
import static org.ballerinalang.mime.util.Constants.MESSAGE_ENTITY;
import static org.ballerinalang.mime.util.Constants.OCTET_STREAM;
import static org.ballerinalang.mime.util.Constants.PROTOCOL_PACKAGE_MIME;
import static org.ballerinalang.mime.util.Constants.TEXT_PLAIN;

/**
 * Test cases for ballerina.net.http outbound outResponse success native functions.
 */
public class OutResponseNativeFunctionSuccessTest {

    private CompileResult result, serviceResult;
    private final String outRespStruct = HttpConstants.OUT_RESPONSE;
    private final String entityStruct = HttpConstants.ENTITY;
    private final String mediaTypeStruct = MEDIA_TYPE;
    private final String protocolPackageHttp = HttpConstants.PROTOCOL_PACKAGE_HTTP;
    private final String protocolPackageMime = PROTOCOL_PACKAGE_MIME;

    @BeforeClass
    public void setup() {
        String sourceFilePath =
                "test-src/statements/services/nativeimpl/outbound/response/out-response-native-function.bal";
        result = BCompileUtil.compile(sourceFilePath);
        serviceResult = BServiceUtil.setupProgramFile(this, sourceFilePath);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAddHeader() {
        BStruct outResponse = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, outRespStruct);
        String headerName = "header1";
        String headerValue = "abc, xyz";
        BString key = new BString(headerName);
        BString value = new BString(headerValue);
        BValue[] inputArg = {outResponse, key, value};
        BValue[] returnVals = BRunUtil.invoke(result, "testAddHeader", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BStruct);
        BStruct entityStruct = (BStruct) ((BStruct) returnVals[0]).getNativeData(MESSAGE_ENTITY);
        BMap<String, BStringArray> map = (BMap<String, BStringArray>) entityStruct.getRefField(ENTITY_HEADERS_INDEX);
        Assert.assertEquals(map.get(headerName).get(0), headerValue);
    }

    @Test(description = "Test addHeader function within a service")
    public void testServiceAddHeader() {
        String key = "lang";
        String value = "ballerina";
        String path = "/hello/addheader/" + key + "/" + value;
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, inRequestMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get(key).asText(), value);
    }

    @Test
    public void testGetBinaryPayloadMethod() {
        BStruct outResponse = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, outRespStruct);
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        BStruct mediaType = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, mediaTypeStruct);

        String payload = "ballerina";
        MimeUtil.setContentType(mediaType, entity, OCTET_STREAM);
        entity.addNativeData(ENTITY_BYTE_CHANNEL, EntityBodyHandler.getEntityWrapper(payload));
        outResponse.addNativeData(MESSAGE_ENTITY, entity);
        outResponse.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, true);

        BValue[] inputArg = {outResponse};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetBinaryPayload", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(returnVals[0].stringValue(), payload);
    }

    @Test
    public void testGetContentLength() {
        String payload = "ballerina";
        BStruct outResponse = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, outRespStruct);
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);

        BMap<String, BStringArray> headersMap = new BMap<>();
        headersMap.put(HttpHeaderNames.CONTENT_LENGTH.toString(),
                       new BStringArray(new String[]{String.valueOf(payload.length())}));
        entity.setRefField(ENTITY_HEADERS_INDEX, headersMap);
        outResponse.addNativeData(MESSAGE_ENTITY, entity);
        BValue[] inputArg = {outResponse};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetContentLength", inputArg);

        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(payload.length(), ((BInteger) returnVals[0]).intValue());
    }

    @Test
    public void testGetHeader() {
        BStruct outResponse = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, outRespStruct);
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);

        BMap<String, BStringArray> headersMap = new BMap<>();
        headersMap.put(HttpHeaderNames.CONTENT_TYPE.toString(),
                       new BStringArray(new String[]{APPLICATION_FORM + "; q=0.7"}));
        entity.setRefField(ENTITY_HEADERS_INDEX, headersMap);
        outResponse.addNativeData(MESSAGE_ENTITY, entity);

        BString key = new BString(HttpHeaderNames.CONTENT_TYPE.toString());
        BValue[] inputArg = {outResponse, key};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetHeader", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(returnVals[0].stringValue(), APPLICATION_FORM + "; q=0.7");
    }

    @Test(description = "Test GetHeader function within a service")
    public void testServiceGetHeader() {
        String value = "x-www-form-urlencoded";
        String path = "/hello/getHeader/" + HttpHeaderNames.CONTENT_TYPE.toString() + "/" + value;
        HTTPTestRequest inRequest = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, inRequest);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("value").asText(), value);
    }

    @Test(description = "Test GetHeaders function within a function")
    public void testGetHeaders() {
        String headerValue1 = APPLICATION_FORM;
        String headerValue2 = TEXT_PLAIN + ";b=5";
        BStruct outResponse = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, outRespStruct);
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);

        BMap<String, BStringArray> headersMap = new BMap<>();
        headersMap.put("test-header", new BStringArray(new String[]{headerValue1, headerValue2}));
        entity.setRefField(ENTITY_HEADERS_INDEX, headersMap);
        outResponse.addNativeData(MESSAGE_ENTITY, entity);

        BString key = new BString("test-header");
        BValue[] inputArg = {outResponse, key};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetHeaders", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(((BStringArray) returnVals[0]).get(0), headerValue1);
        Assert.assertEquals(((BStringArray) returnVals[0]).get(1), headerValue2);
    }

    @Test
    public void testGetJsonPayload() {
        BStruct outResponse = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, outRespStruct);
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        BStruct mediaType = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, mediaTypeStruct);

        String payload = "{'code':'123'}";
        MimeUtil.setContentType(mediaType, entity, APPLICATION_JSON);
        entity.addNativeData(ENTITY_BYTE_CHANNEL, EntityBodyHandler.getEntityWrapper(payload));
        outResponse.addNativeData(MESSAGE_ENTITY, entity);
        outResponse.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, true);

        BValue[] inputArg = {outResponse};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetJsonPayload", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(((BJSON) returnVals[0]).value().get("code").asText(), "123");
    }

    @Test(description = "Test GetJsonPayload function within a service")
    public void testServiceGetJsonPayload() {
        String value = "ballerina";
        String path = "/hello/getJsonPayload/" + value;
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HTTPCarbonMessage responseMsg = Services.invokeNew(serviceResult, inRequestMsg);

        Assert.assertNotNull(responseMsg, "Response message not found");
        Assert.assertEquals(new BJSON(new HttpMessageDataStreamer(responseMsg).getInputStream()).stringValue(),
                value);
    }

    @Test
    public void testGetProperty() {
        BStruct outResponse = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, outRespStruct);
        HTTPCarbonMessage outResponseMsg = HttpUtil.createHttpCarbonMessage(false);
        String propertyName = "wso2";
        String propertyValue = "Ballerina";
        outResponseMsg.setProperty(propertyName, propertyValue);
        HttpUtil.addCarbonMsg(outResponse, outResponseMsg);

        BString name = new BString(propertyName);
        BValue[] inputArg = {outResponse, name};
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
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, inRequestMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("value").asText(), propertyValue);
    }

    @Test
    public void testGetStringPayload() {
        BStruct outResponse = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, outRespStruct);
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        BStruct mediaType = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, mediaTypeStruct);

        String payload = "ballerina";
        MimeUtil.setContentType(mediaType, entity, TEXT_PLAIN);
        entity.addNativeData(ENTITY_BYTE_CHANNEL, EntityBodyHandler.getEntityWrapper(payload));
        outResponse.addNativeData(MESSAGE_ENTITY, entity);
        outResponse.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, true);

        BValue[] inputArg = {outResponse};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetStringPayload", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(returnVals[0].stringValue(), payload);
    }

    @Test(description = "Test GetStringPayload function within a service")
    public void testServiceGetStringPayload() {
        String value = "ballerina";
        String path = "/hello/GetStringPayload/" + value;
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, inRequestMsg);

        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(
                StringUtils.getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream()), value);
    }

    @Test
    public void testGetXmlPayload() {
        BStruct outResponse = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, outRespStruct);
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        BStruct mediaType = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, mediaTypeStruct);

        String payload = "<name>ballerina</name>";
        MimeUtil.setContentType(mediaType, entity, APPLICATION_XML);
        entity.addNativeData(ENTITY_BYTE_CHANNEL, EntityBodyHandler.getEntityWrapper(payload));
        outResponse.addNativeData(MESSAGE_ENTITY, entity);
        outResponse.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, true);

        BValue[] inputArg = {outResponse};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetXmlPayload", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(((BXML) returnVals[0]).getTextValue().stringValue(), "ballerina");
    }

    @Test(description = "Test GetXmlPayload function within a service")
    public void testServiceGetXmlPayload() {
        String value = "ballerina";
        String path = "/hello/GetXmlPayload";
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, inRequestMsg);

        Assert.assertNotNull(response, "Response message not found");
        String returnvalue = StringUtils.getStringFromInputStream(new HttpMessageDataStreamer(response).
                getInputStream());
        Assert.assertEquals(returnvalue, value);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRemoveHeader() {
        BStruct outResponse = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, outRespStruct);
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        String expect = "Expect";
        String headerValue = "100-continue";
        BString key = new BString(expect);

        BMap<String, BStringArray> headersMap = new BMap<>();
        headersMap.put(expect, new BStringArray(new String[]{headerValue}));
        entity.setRefField(ENTITY_HEADERS_INDEX, headersMap);
        outResponse.addNativeData(MESSAGE_ENTITY, entity);

        BValue[] inputArg = {outResponse, key};
        BValue[] returnVals = BRunUtil.invoke(result, "testRemoveHeader", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BStruct);
        BStruct entityStruct = (BStruct) ((BStruct) returnVals[0]).getNativeData(MESSAGE_ENTITY);
        BMap<String, BStringArray> map = (BMap<String, BStringArray>) entityStruct.getRefField(ENTITY_HEADERS_INDEX);
        Assert.assertNull(map.get("100-continue"));
    }

    @Test(description = "Test RemoveHeader function within a service")
    public void testServiceRemoveHeader() {
        String value = "x-www-form-urlencoded";
        String path = "/hello/RemoveHeader/Content-Type/" + value;
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, inRequestMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("value").asText(), "value is null");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRemoveAllHeaders() {
        BStruct outResponse = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, outRespStruct);
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        String expect = "Expect";
        String range = "Range";

        BMap<String, BStringArray> headersMap = new BMap<>();
        headersMap.put(expect, new BStringArray(new String[]{"100-continue"}));
        headersMap.put(range, new BStringArray(new String[]{"bytes=500-999"}));
        entity.setRefField(ENTITY_HEADERS_INDEX, headersMap);
        outResponse.addNativeData(MESSAGE_ENTITY, entity);
        BValue[] inputArg = {outResponse};

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
    public void testRespondMethod() {
        String path = "/hello/11";
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, inRequestMsg);
        Assert.assertNotNull(response, "Response message not found");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSetHeader() {
        String range = "Range";
        String rangeValue = "bytes=500-999; a=4";
        BString key = new BString(range);
        BString value = new BString(rangeValue);
        BValue[] inputArg = {key, value};
        BValue[] returnVals = BRunUtil.invoke(result, "testSetHeader", inputArg);

        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BStruct);
        BStruct entityStruct = (BStruct) ((BStruct) returnVals[0]).getNativeData(MESSAGE_ENTITY);
        BMap<String, BStringArray> map = (BMap<String, BStringArray>) entityStruct.getRefField(ENTITY_HEADERS_INDEX);
        Assert.assertEquals(map.get(range).get(0), rangeValue);
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
        HTTPCarbonMessage responseMsg = HttpUtil.getCarbonMsg((BStruct) returnVals[0], null);
        Assert.assertEquals(responseMsg.getProperty(propertyName), propertyValue);
    }

    @Test
    public void testSetReasonPhase() {
        String phase = "ballerina";
        String path = "/hello/12/" + phase;
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, inRequestMsg);

        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(response.getProperty(HttpConstants.HTTP_REASON_PHRASE), phase);
    }

    @Test
    public void testSetStatusCode() {
        String path = "/hello/13";
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, inRequestMsg);

        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(response.getProperty(HttpConstants.HTTP_STATUS_CODE), 203);
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
        String stringValue = EntityBodyHandler.getMessageDataSource(entity).getMessageAsString();
        Assert.assertEquals(stringValue, "Ballerina", "Payload is not set properly");
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
        BXML xmlValue = (BXML) EntityBodyHandler.getMessageDataSource(entity);
        Assert.assertEquals(xmlValue.getTextValue().stringValue(), "Ballerina",
                "Payload is not set properly");
    }

    @Test(description = "Test getStringPayload method with JSON payload")
    public void testGetStringPayloadMethodWithJsonPayload() {
        BStruct outResponse = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp,
                HttpConstants.IN_RESPONSE);
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        BStruct mediaType = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, mediaTypeStruct);

        String payload = "{\"code\":\"123\"}";
        MimeUtil.setContentType(mediaType, entity, APPLICATION_JSON);
        entity.addNativeData(ENTITY_BYTE_CHANNEL, EntityBodyHandler.getEntityWrapper(payload));
        outResponse.addNativeData(MESSAGE_ENTITY, entity);
        outResponse.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, true);

        BValue[] inputArg = {outResponse};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetStringPayload", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(returnVals[0].stringValue(), payload);
    }
}
