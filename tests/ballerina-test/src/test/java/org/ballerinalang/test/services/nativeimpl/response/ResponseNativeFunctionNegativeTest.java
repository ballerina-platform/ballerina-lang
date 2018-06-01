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
package org.ballerinalang.test.services.nativeimpl.response;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.test.services.testutils.TestEntityUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.ballerinalang.mime.util.Constants.APPLICATION_JSON;
import static org.ballerinalang.mime.util.Constants.APPLICATION_XML;
import static org.ballerinalang.mime.util.Constants.ENTITY_HEADERS;
import static org.ballerinalang.mime.util.Constants.IS_BODY_BYTE_CHANNEL_ALREADY_SET;
import static org.ballerinalang.mime.util.Constants.MEDIA_TYPE;
import static org.ballerinalang.mime.util.Constants.PROTOCOL_PACKAGE_MIME;
import static org.ballerinalang.mime.util.Constants.RESPONSE_ENTITY_INDEX;
import static org.ballerinalang.mime.util.Constants.TEXT_PLAIN;

/**
 * Test cases for ballerina/http inbound response negative native functions.
 */
public class ResponseNativeFunctionNegativeTest {

    private CompileResult result, resultNegative;
    private final String inRespStruct = HttpConstants.RESPONSE;
    private final String entityStruct = HttpConstants.ENTITY;
    private final String mediaTypeStruct = MEDIA_TYPE;
    private final String protocolPackageHttp = HttpConstants.PROTOCOL_PACKAGE_HTTP;
    private final String protocolPackageMime = PROTOCOL_PACKAGE_MIME;
    private static final String CONTENT_TYPE = "Content-Type";

    @BeforeClass
    public void setup() {
        String basePath = "test-src/statements/services/nativeimpl/response/";
        result = BCompileUtil.compile(basePath + "in-response-native-function-negative.bal");
        resultNegative = BCompileUtil.compile(basePath + "in-response-compile-negative.bal");
    }

    @Test
    public void testGetHeader() {
        try {
            BStruct inResponse = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp,
                                                                 inRespStruct);
            BString key = new BString(HttpHeaderNames.CONTENT_TYPE.toString());
            BValue[] inputArg = {inResponse, key};
            BValue[] returnVals = BRunUtil.invoke(result, "testGetHeader", inputArg);
            Assert.assertNull(returnVals[0]);
        } catch (Exception exception) {
            String errorMessage = exception.getMessage();
            Assert.assertTrue(errorMessage.contains(" message: http Header does not exist!"));
        }
    }

    @Test(description = "Test method without json payload")
    public void testGetJsonPayloadWithoutPayload() {
        BStruct inResponse = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inRespStruct);
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        TestEntityUtils.enrichTestEntityHeaders(entity, APPLICATION_JSON);
        inResponse.setRefField(RESPONSE_ENTITY_INDEX, entity);
        BValue[] inputArg = {inResponse};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetJsonPayload", inputArg);
        Assert.assertNull(returnVals[0]);
    }

    @Test(description = "Test method with string payload")
    public void testGetJsonPayloadWithStringPayload() {
        BStruct inResponse = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inRespStruct);
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);

        String payload = "ballerina";
        TestEntityUtils.enrichTestEntity(entity, TEXT_PLAIN, payload);
        inResponse.setRefField(RESPONSE_ENTITY_INDEX, entity);
        inResponse.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, true);
        BValue[] inputArg = {inResponse};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetJsonPayload", inputArg);
        Assert.assertNotNull(returnVals[0]);
        Assert.assertTrue((returnVals[0].stringValue().contains("{message:\"Entity body is not json compatible " +
                "since the received content-type is : text/plain\", cause:null}")));
    }

    @Test(description = "Test getTextPayload method without a paylaod")
    public void testGetTextPayloadNegative() {
        BStruct inResponse = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inRespStruct);
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        TestEntityUtils.enrichTestEntityHeaders(entity, TEXT_PLAIN);
        inResponse.setRefField(RESPONSE_ENTITY_INDEX, entity);
        BValue[] inputArg = {inResponse};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetTextPayload", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0].stringValue().contains("Error occurred while retrieving text" +
                " data from entity : String payload is null"));
    }

    @Test
    public void testGetXmlPayloadNegative() {
        BStruct inResponse = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inRespStruct);
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        TestEntityUtils.enrichTestEntityHeaders(entity, APPLICATION_XML);
        inResponse.setRefField(RESPONSE_ENTITY_INDEX, entity);
        BValue[] inputArg = {inResponse};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetXmlPayload", inputArg);
        Assert.assertTrue(((BStruct) returnVals[0]).getStringField(0).contains("Error occurred while " +
                "retrieving xml data from entity : Empty xml payload"));
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

    @Test
    @SuppressWarnings("unchecked")
    public void testRemoveHeaderNegative() {
        BStruct outResponse = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inRespStruct);
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        String range = "Range";
        HttpHeaders httpHeaders = new DefaultHttpHeaders();
        httpHeaders.add("Expect", "100-continue");
        entity.addNativeData(ENTITY_HEADERS, httpHeaders);
        outResponse.setRefField(RESPONSE_ENTITY_INDEX, entity);
        BString key = new BString(range);
        BValue[] inputArg = {outResponse, key};
        BValue[] returnVals = BRunUtil.invoke(result, "testRemoveHeader", inputArg);

        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BStruct);
        BStruct entityStruct = (BStruct) ((BStruct) returnVals[0]).getRefField(RESPONSE_ENTITY_INDEX);
        HttpHeaders returnHeaders = (HttpHeaders) entityStruct.getNativeData(ENTITY_HEADERS);
        Assert.assertNull(returnHeaders.get("range"));
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
        BStruct entityStruct = (BStruct) ((BStruct) returnVals[0]).getRefField(RESPONSE_ENTITY_INDEX);
        HttpHeaders returnHeaders = (HttpHeaders) entityStruct.getNativeData(ENTITY_HEADERS);
        Assert.assertNull(returnHeaders.get("Expect"));
    }

    @Test
    public void testCompilationErrorTestCases() {
        Assert.assertEquals(resultNegative.getErrorCount(), 2);
        //testInResponseSetStatusCodeWithString
        BAssertUtil.validateError(resultNegative, 0,
                "incompatible types: expected 'int', found 'string'", 4, 22);
        //testInResponseGetMethod
        BAssertUtil.validateError(resultNegative, 1,
                "undefined field 'method' in struct 'ballerina.http:Response'",
                9, 21);
    }
}
