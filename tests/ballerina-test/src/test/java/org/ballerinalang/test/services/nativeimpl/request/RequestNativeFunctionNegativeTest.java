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
import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.mime.util.EntityBodyHandler;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import static org.ballerinalang.mime.util.Constants.ENTITY_BYTE_CHANNEL;
import static org.ballerinalang.mime.util.Constants.ENTITY_HEADERS;
import static org.ballerinalang.mime.util.Constants.IS_BODY_BYTE_CHANNEL_ALREADY_SET;
import static org.ballerinalang.mime.util.Constants.MEDIA_TYPE;
import static org.ballerinalang.mime.util.Constants.MESSAGE_ENTITY;
import static org.ballerinalang.mime.util.Constants.PROTOCOL_PACKAGE_MIME;
import static org.ballerinalang.mime.util.Constants.TEXT_PLAIN;

/**
 * Test cases for ballerina/http request negative native functions.
 */
public class RequestNativeFunctionNegativeTest {

    private CompileResult result, resultNegative;
    private final String reqStruct = HttpConstants.REQUEST;
    private final String entityStruct = HttpConstants.ENTITY;
    private final String mediaTypeStruct = MEDIA_TYPE;
    private final String protocolPackageHttp = HttpConstants.PROTOCOL_PACKAGE_HTTP;
    private final String protocolPackageMime = PROTOCOL_PACKAGE_MIME;

    @BeforeClass
    public void setup() {
        String basePath = "test-src/statements/services/nativeimpl/request/";
        result = BCompileUtil.compile(basePath + "in-request-native-function-negative.bal");
        resultNegative = BCompileUtil.compile(basePath + "in-request-compile-negative.bal");
    }

    @Test(description = "Test when the content length header is not set")
    public void testGetContentLength() {
        BStruct inRequest = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, reqStruct);
        BValue[] inputArg = {inRequest};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetContentLength", inputArg);
        Assert.assertEquals(returnVals[0].stringValue(), "Content-length is not found");
    }

    @Test
    public void testGetHeader() {
        BStruct inRequest = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, reqStruct);
        BString key = new BString(HttpHeaderNames.CONTENT_TYPE.toString());
        BValue[] inputArg = {inRequest, key};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetHeader", inputArg);
        Assert.assertNotNull(returnVals[0]);
        Assert.assertEquals(((BString) returnVals[0]).value(), "Header not found!");
    }

    @Test(description = "Test method without json payload")
    public void testGetJsonPayloadWithoutPayload() {
        BStruct inRequest = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, reqStruct);
        BValue[] inputArg = {inRequest};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetJsonPayload", inputArg);
        Assert.assertNull(returnVals[0]);
    }

    @Test(description = "Test method with string payload")
    public void testGetJsonPayloadWithStringPayload() {
        BStruct inRequest = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, reqStruct);
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        BStruct mediaType = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, mediaTypeStruct);

        String payload = "ballerina";
        MimeUtil.setContentType(mediaType, entity, TEXT_PLAIN);
        entity.addNativeData(ENTITY_BYTE_CHANNEL, EntityBodyHandler.getEntityWrapper(payload));
        inRequest.addNativeData(MESSAGE_ENTITY, entity);
        inRequest.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, true);

        BValue[] inputArg = {inRequest};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetJsonPayload", inputArg);
        Assert.assertNotNull(returnVals[0]);
        Assert.assertTrue(((BStruct) returnVals[0]).getStringField(0).contains("Error occurred while" +
                " extracting json data from entity: failed to create json: unrecognized token 'ballerina'"));
    }

    @Test(description = "Test getEntity method on a outRequest without a entity")
    public void testGetEntityNegative() {
        BStruct outRequest = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, reqStruct);
        BValue[] inputArg = {outRequest};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetEntity", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertNotNull(returnVals[0]);
    }

    @Test(description = "Test getStringPayload method without a paylaod")
    public void testGetStringPayloadNegative() {
        BStruct inRequest = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, reqStruct);
        BValue[] inputArg = {inRequest};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetStringPayload", inputArg);
        Assert.assertTrue(returnVals[0].stringValue().contains("Error occurred while retrieving text data " +
                "from entity : Payload is null"));
    }

    @Test
    public void testGetXmlPayloadNegative() {
        BStruct inRequest = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, reqStruct);
        BValue[] inputArg = {inRequest};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetXmlPayload", inputArg);
        Assert.assertTrue(returnVals[0].stringValue().contains("{message:\"Error occurred while retrieving xml data " +
                "from entity : Empty xml payload\", cause:null}"));
    }

    @Test
    public void testGetMethodNegative() {
        BStruct inRequest = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, reqStruct);
        HTTPCarbonMessage inRequestMsg = HttpUtil.createHttpCarbonMessage(true);
        HttpUtil.addCarbonMsg(inRequest, inRequestMsg);
        BValue[] inputArg = {inRequest};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetMethod", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(returnVals[0].stringValue(), "");
    }

    @Test
    public void testGetRequestURLNegative() {
        BStruct inRequest = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, reqStruct);
        HTTPCarbonMessage inRequestMsg = HttpUtil.createHttpCarbonMessage(true);
        HttpUtil.addCarbonMsg(inRequest, inRequestMsg);
        BValue[] inputArg = {inRequest};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetRequestURL", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(returnVals[0].stringValue(), "no url");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRemoveHeaderNegative() {
        BStruct outRequest = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, reqStruct);
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        String range = "Range";
        HttpHeaders httpHeaders = new DefaultHttpHeaders();
        httpHeaders.add("Expect", "100-continue");
        entity.addNativeData(ENTITY_HEADERS, httpHeaders);
        outRequest.addNativeData(MESSAGE_ENTITY, entity);
        BString key = new BString(range);
        BValue[] inputArg = {outRequest, key};
        BValue[] returnVals = BRunUtil.invoke(result, "testRemoveHeader", inputArg);

        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BStruct);
        BStruct entityStruct = (BStruct) ((BStruct) returnVals[0]).getNativeData(MESSAGE_ENTITY);
        HttpHeaders returnHeaders = (HttpHeaders) entityStruct.getNativeData(ENTITY_HEADERS);
        Assert.assertNull(returnHeaders.get(range));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRemoveAllHeadersNegative() {
        BStruct outRequest = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, reqStruct);
        BValue[] inputArg = {outRequest};
        BValue[] returnVals = BRunUtil.invoke(result, "testRemoveAllHeaders", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BStruct);
        BStruct entityStruct = (BStruct) ((BStruct) returnVals[0]).getNativeData(MESSAGE_ENTITY);
        HttpHeaders returnHeaders = (HttpHeaders) entityStruct.getNativeData(ENTITY_HEADERS);
        Assert.assertNull(returnHeaders.get("Expect"));
    }

    @Test
    public void testCompilationErrorTestCases() {
        Assert.assertEquals(resultNegative.getErrorCount(), 2);
        //testRequestSetStatusCode
        BAssertUtil.validateError(resultNegative, 0,
                "undefined function 'setStatusCode' in object 'ballerina.http:Request'", 4, 5);
        BAssertUtil.validateError(resultNegative, 1,
                "undefined field 'statusCode' in object 'ballerina.http:Request'", 5, 5);
    }
}
