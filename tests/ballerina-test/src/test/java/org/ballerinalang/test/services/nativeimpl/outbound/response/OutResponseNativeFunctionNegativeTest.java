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
import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.mime.util.EntityBodyHandler;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import static org.ballerinalang.mime.util.Constants.ENTITY_BYTE_CHANNEL;
import static org.ballerinalang.mime.util.Constants.ENTITY_HEADERS_INDEX;
import static org.ballerinalang.mime.util.Constants.IS_BODY_BYTE_CHANNEL_ALREADY_SET;
import static org.ballerinalang.mime.util.Constants.MEDIA_TYPE;
import static org.ballerinalang.mime.util.Constants.MESSAGE_ENTITY;
import static org.ballerinalang.mime.util.Constants.PROTOCOL_PACKAGE_MIME;
import static org.ballerinalang.mime.util.Constants.TEXT_PLAIN;

/**
 * Test cases for ballerina.net.http outbound response negative native functions.
 */
public class OutResponseNativeFunctionNegativeTest {

    private CompileResult result, resultNegative;
    private final String inRespStruct = HttpConstants.IN_RESPONSE;
    private final String outRespStruct = HttpConstants.OUT_RESPONSE;
    private final String entityStruct = HttpConstants.ENTITY;
    private final String mediaTypeStruct = MEDIA_TYPE;
    private final String protocolPackageHttp = HttpConstants.PROTOCOL_PACKAGE_HTTP;
    private final String protocolPackageMime = PROTOCOL_PACKAGE_MIME;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile(
                "test-src/statements/services/nativeimpl/outbound/response/out-response-native-function-negative.bal");
        resultNegative = BCompileUtil.compile(
                "test-src/statements/services/nativeimpl/outbound/response/out-response-compile-negative.bal");
    }

    @Test(description = "Test when the content length header is not set")
    public void testGetContentLength() {
        BStruct outResponse = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inRespStruct);
        BValue[] inputArg = {outResponse};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetContentLength", inputArg);
        Assert.assertEquals(Integer.parseInt(returnVals[0].stringValue()), -1);
    }

    @Test
    public void testGetHeader() {
        BStruct outResponse = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inRespStruct);
        BString key = new BString(HttpHeaderNames.CONTENT_TYPE.toString());
        BValue[] inputArg = {outResponse, key};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetHeader", inputArg);
        Assert.assertNull(((BString) returnVals[0]).value());
    }

    @Test(description = "Test method without json payload")
    public void testGetJsonPayloadWithoutPayload() {
        BStruct outResponse = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inRespStruct);
        BValue[] inputArg = {outResponse};
        String error = null;
        BValue[] returnVals = BRunUtil.invoke(result, "testGetJsonPayload", inputArg);
        Assert.assertNull(returnVals[0]);
    }

    @Test(description = "Test getEntity method on a response without a entity")
    public void testGetEntityNegative() {
        BStruct outResponse = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inRespStruct);
        BValue[] inputArg = {outResponse};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetEntity", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertNotNull(returnVals[0]);
    }

    @Test(description = "Test method with string payload")
    public void testGetJsonPayloadWithStringPayload() {
        BStruct outResponse = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inRespStruct);
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        BStruct mediaType = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, mediaTypeStruct);

        String payload = "ballerina";
        MimeUtil.setContentType(mediaType, entity, TEXT_PLAIN);
        entity.addNativeData(ENTITY_BYTE_CHANNEL, EntityBodyHandler.getEntityWrapper(payload));
        outResponse.addNativeData(MESSAGE_ENTITY, entity);
        outResponse.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, true);

        BValue[] inputArg = {outResponse};
        String error = null;
        try {
            BRunUtil.invoke(result, "testGetJsonPayload", inputArg);
        } catch (Throwable e) {
            error = e.getMessage();
        }
        Assert.assertTrue(error.contains("error occurred while extracting json data from entity"));
    }

    @Test
    public void testGetProperty() {
        BStruct outResponse = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inRespStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(true);
        HttpUtil.addCarbonMsg(outResponse, cMsg);

        BString propertyName = new BString("wso2");
        BValue[] inputArg = {outResponse, propertyName};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetProperty", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertNull(returnVals[0].stringValue());
    }

    @Test(description = "Test getStringPayload method without a paylaod")
    public void testGetStringPayloadNegative() {
        BStruct outResponse = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inRespStruct);
        BValue[] inputArg = {outResponse};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetStringPayload", inputArg);
        Assert.assertNull(returnVals[0].stringValue());
    }

    @Test
    public void testGetXmlPayloadNegative() {
        BStruct outResponse = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inRespStruct);
        BValue[] inputArg = {outResponse};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetXmlPayload", inputArg);
        Assert.assertNull(returnVals[0]);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRemoveHeaderNegative() {
        BStruct outResponse = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, outRespStruct);
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        String range = "Range";
        BMap<String, BStringArray> headersMap = new BMap<>();
        headersMap.put("Expect", new BStringArray(new String[]{"100-continue"}));
        entity.setRefField(ENTITY_HEADERS_INDEX, headersMap);
        outResponse.addNativeData(MESSAGE_ENTITY, entity);
        BString key = new BString(range);
        BValue[] inputArg = {outResponse, key};
        BValue[] returnVals = BRunUtil.invoke(result, "testRemoveHeader", inputArg);

        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BStruct);
        BStruct entityStruct = (BStruct) ((BStruct) returnVals[0]).getNativeData(MESSAGE_ENTITY);
        BMap<String, BStringArray> map = (BMap<String, BStringArray>) entityStruct.getRefField(ENTITY_HEADERS_INDEX);
        Assert.assertNull(map.get("range"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRemoveAllHeadersNegative() {
        BStruct outResponse = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inRespStruct);
        BValue[] inputArg = {outResponse};
        BValue[] returnVals = BRunUtil.invoke(result, "testRemoveAllHeaders", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BStruct);
        BStruct entityStruct = (BStruct) ((BStruct) returnVals[0]).getNativeData(MESSAGE_ENTITY);
        BMap<String, BStringArray> map = (BMap<String, BStringArray>) entityStruct.getRefField(ENTITY_HEADERS_INDEX);
        Assert.assertNull(map.get("Expect"));
    }

    @Test
    public void testCompilationErrorTestCases() {
        Assert.assertEquals(resultNegative.getErrorCount(), 2);
        //testResponseSetStatusCodeWithString
        BAssertUtil.validateError(resultNegative, 0,
                "incompatible types: expected 'int', found 'string'", 4, 22);
        //testResponseGetMethod
        BAssertUtil.validateError(resultNegative, 1,
                "undefined field 'method' in struct 'ballerina.net.http:InResponse'",
                9, 21);
    }
}
