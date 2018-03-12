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
package org.ballerinalang.test.services.nativeimpl.inbound.response;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.BServiceUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.mime.util.EntityBodyHandler;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.test.services.testutils.HTTPTestRequest;
import org.ballerinalang.test.services.testutils.MessageUtils;
import org.ballerinalang.test.services.testutils.Services;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import static org.ballerinalang.mime.util.Constants.APPLICATION_FORM;
import static org.ballerinalang.mime.util.Constants.APPLICATION_JSON;
import static org.ballerinalang.mime.util.Constants.APPLICATION_XML;
import static org.ballerinalang.mime.util.Constants.ENTITY_BYTE_CHANNEL;
import static org.ballerinalang.mime.util.Constants.IS_BODY_BYTE_CHANNEL_ALREADY_SET;
import static org.ballerinalang.mime.util.Constants.MEDIA_TYPE;
import static org.ballerinalang.mime.util.Constants.MESSAGE_ENTITY;
import static org.ballerinalang.mime.util.Constants.OCTET_STREAM;
import static org.ballerinalang.mime.util.Constants.PROTOCOL_PACKAGE_MIME;
import static org.ballerinalang.mime.util.Constants.TEXT_PLAIN;

/**
 * Test cases for ballerina.net.http inbound inResponse success native functions.
 */
public class InResponseNativeFunctionSuccessTest {

    private CompileResult result, serviceResult;
    private final String inResStruct = HttpConstants.IN_RESPONSE;
    private final String entityStruct = HttpConstants.ENTITY;
    private final String mediaTypeStruct = MEDIA_TYPE;
    private final String protocolPackageHttp = HttpConstants.PROTOCOL_PACKAGE_HTTP;
    private final String protocolPackageMime = PROTOCOL_PACKAGE_MIME;

    @BeforeClass
    public void setup() {
        String sourceFilePath =
                "test-src/statements/services/nativeimpl/inbound/response/in-response-native-function.bal";
        result = BCompileUtil.compile(sourceFilePath);
        serviceResult = BServiceUtil.setupProgramFile(this, sourceFilePath);
    }

    @Test
    public void testGetBinaryPayloadMethod() {
        BStruct inResponse = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inResStruct);
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        BStruct mediaType = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, mediaTypeStruct);

        String payload = "ballerina";
        MimeUtil.setContentType(mediaType, entity, OCTET_STREAM);
        entity.addNativeData(ENTITY_BYTE_CHANNEL, EntityBodyHandler.getEntityWrapper(payload));
        inResponse.addNativeData(MESSAGE_ENTITY, entity);
        inResponse.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, true);

        BValue[] inputArg = {inResponse};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetBinaryPayload", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(returnVals[0].stringValue(), payload);
    }

    @Test
    public void testGetContentLength() {
        BStruct inResponse = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inResStruct);
        HTTPCarbonMessage inResponseMsg = HttpUtil.createHttpCarbonMessage(false);

        String payload = "ballerina";
        inResponseMsg.setHeader(HttpHeaderNames.CONTENT_LENGTH.toString(), String.valueOf(payload.length()));
        inResponseMsg.setProperty(HttpConstants.HTTP_STATUS_CODE, 200);
        HttpUtil.addCarbonMsg(inResponse, inResponseMsg);

        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        BStruct mediaType = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, mediaTypeStruct);
        HttpUtil.populateInboundResponse(inResponse, entity, mediaType, inResponseMsg);

        BValue[] inputArg = {inResponse};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetContentLength", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(payload.length(), ((BInteger) returnVals[0]).intValue());
    }

    @Test
    public void testGetHeader() {
        BStruct inResponse = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inResStruct);
        HTTPCarbonMessage inResponseMsg = HttpUtil.createHttpCarbonMessage(false);
        inResponseMsg.setHeader(HttpHeaderNames.CONTENT_TYPE.toString(), APPLICATION_FORM);
        inResponseMsg.setProperty(HttpConstants.HTTP_STATUS_CODE, 200);
        HttpUtil.addCarbonMsg(inResponse, inResponseMsg);

        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        BStruct mediaType = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, mediaTypeStruct);
        HttpUtil.populateInboundResponse(inResponse, entity, mediaType, inResponseMsg);

        BString key = new BString(HttpHeaderNames.CONTENT_TYPE.toString());
        BValue[] inputArg = {inResponse, key};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetHeader", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(returnVals[0].stringValue(), APPLICATION_FORM);
    }

    @Test(description = "Test GetHeaders function within a function")
    public void testGetHeaders() {
        BStruct inResponse = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inResStruct);
        HTTPCarbonMessage inResponseMsg = HttpUtil.createHttpCarbonMessage(false);
        HttpHeaders headers = inResponseMsg.getHeaders();
        headers.set("test-header", APPLICATION_FORM);
        headers.add("test-header", TEXT_PLAIN);

        inResponseMsg.setProperty(HttpConstants.HTTP_STATUS_CODE, 200);
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        BStruct mediaType = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, mediaTypeStruct);
        HttpUtil.populateInboundResponse(inResponse, entity, mediaType, inResponseMsg);

        BString key = new BString("test-header");
        BValue[] inputArg = {inResponse, key};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetHeaders", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(((BStringArray) returnVals[0]).get(0), APPLICATION_FORM);
        Assert.assertEquals(((BStringArray) returnVals[0]).get(1), TEXT_PLAIN);
    }

    @Test
    public void testGetJsonPayload() {
        BStruct inResponse = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inResStruct);
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        BStruct mediaType = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, mediaTypeStruct);

        String payload = "{'code':'123'}";
        MimeUtil.setContentType(mediaType, entity, APPLICATION_JSON);
        entity.addNativeData(ENTITY_BYTE_CHANNEL, EntityBodyHandler.getEntityWrapper(payload));
        inResponse.addNativeData(MESSAGE_ENTITY, entity);
        inResponse.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, true);
        BValue[] inputArg = {inResponse};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetJsonPayload", inputArg);

        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(((BJSON) returnVals[0]).value().get("code").asText(), "123");
    }

    @Test
    public void testGetProperty() {
        BStruct inResponse = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inResStruct);
        HTTPCarbonMessage inResponseMsg = HttpUtil.createHttpCarbonMessage(false);
        String propertyName = "wso2";
        String propertyValue = "Ballerina";
        inResponseMsg.setProperty(propertyName, propertyValue);
        HttpUtil.addCarbonMsg(inResponse, inResponseMsg);
        BString name = new BString(propertyName);
        BValue[] inputArg = {inResponse, name};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetProperty", inputArg);

        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(returnVals[0].stringValue(), propertyValue);
    }

    @Test
    public void testGetStringPayload() {
        BStruct inResponse = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inResStruct);
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        BStruct mediaType = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, mediaTypeStruct);

        String payload = "ballerina";
        MimeUtil.setContentType(mediaType, entity, TEXT_PLAIN);
        entity.addNativeData(ENTITY_BYTE_CHANNEL, EntityBodyHandler.getEntityWrapper(payload));
        inResponse.addNativeData(MESSAGE_ENTITY, entity);
        inResponse.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, true);
        BValue[] inputArg = {inResponse};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetStringPayload", inputArg);

        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(returnVals[0].stringValue(), payload);
    }

    @Test
    public void testGetXmlPayload() {
        BStruct inResponse = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inResStruct);
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        BStruct mediaType = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, mediaTypeStruct);

        String payload = "<name>ballerina</name>";
        MimeUtil.setContentType(mediaType, entity, APPLICATION_XML);
        entity.addNativeData(ENTITY_BYTE_CHANNEL, EntityBodyHandler.getEntityWrapper(payload));
        inResponse.addNativeData(MESSAGE_ENTITY, entity);
        inResponse.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, true);
        BValue[] inputArg = {inResponse};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetXmlPayload", inputArg);

        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(((BXML) returnVals[0]).getTextValue().stringValue(), "ballerina");
    }

    @Test
    public void testForwardMethod() {
        String path = "/hello/11";
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, inRequestMsg);
        Assert.assertNotNull(response, "Response message not found");
    }

    @Test(description = "Test getStringPayload method with JSON payload")
    public void testGetStringPayloadMethodWithJsonPayload() {
        BStruct inResponse = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp,
                HttpConstants.IN_RESPONSE);
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        BStruct mediaType = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, mediaTypeStruct);

        String payload = "{\"code\":\"123\"}";
        MimeUtil.setContentType(mediaType, entity, APPLICATION_JSON);
        entity.addNativeData(ENTITY_BYTE_CHANNEL, EntityBodyHandler.getEntityWrapper(payload));
        inResponse.addNativeData(MESSAGE_ENTITY, entity);
        inResponse.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, true);

        BValue[] inputArg = {inResponse};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetStringPayload", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(returnVals[0].stringValue(), payload);
    }
}
