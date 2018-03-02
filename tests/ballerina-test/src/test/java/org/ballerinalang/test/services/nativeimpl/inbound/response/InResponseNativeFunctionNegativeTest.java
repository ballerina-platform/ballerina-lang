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

import static org.ballerinalang.mime.util.Constants.BYTE_CHANNEL_STRUCT;
import static org.ballerinalang.mime.util.Constants.IS_BODY_BYTE_CHANNEL_ALREADY_SET;
import static org.ballerinalang.mime.util.Constants.MEDIA_TYPE;
import static org.ballerinalang.mime.util.Constants.MESSAGE_ENTITY;
import static org.ballerinalang.mime.util.Constants.PROTOCOL_PACKAGE_MIME;
import static org.ballerinalang.mime.util.Constants.TEXT_PLAIN;

/**
 * Test cases for ballerina.net.http inbound response negative native functions.
 */
public class InResponseNativeFunctionNegativeTest {

    private CompileResult result, resultNegative;
    private final String inRespStruct = HttpConstants.IN_RESPONSE;
    private final String entityStruct = HttpConstants.ENTITY;
    private final String mediaTypeStruct = MEDIA_TYPE;
    private final String protocolPackageHttp = HttpConstants.PROTOCOL_PACKAGE_HTTP;
    private final String protocolPackageMime = PROTOCOL_PACKAGE_MIME;

    @BeforeClass
    public void setup() {
        String basePath = "test-src/statements/services/nativeimpl/inbound/response/";
        result = BCompileUtil.compile(basePath + "in-response-native-function-negative.bal");
        resultNegative = BCompileUtil.compile(basePath + "in-response-compile-negative.bal");
    }

    @Test(description = "Test when the content length header is not set")
    public void testGetContentLength() {
        BStruct inResponse = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inRespStruct);
        BValue[] inputArg = {inResponse};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetContentLength", inputArg);
        Assert.assertEquals(Integer.parseInt(returnVals[0].stringValue()), -1);
    }

    @Test
    public void testGetHeader() {
        BStruct inResponse = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inRespStruct);
        BString key = new BString(HttpHeaderNames.CONTENT_TYPE.toString());
        BValue[] inputArg = {inResponse, key};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetHeader", inputArg);
        Assert.assertNull(((BString) returnVals[0]).value());
    }

    @Test(description = "Test method without json payload")
    public void testGetJsonPayloadWithoutPayload() {
        BStruct inResponse = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inRespStruct);
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        inResponse.addNativeData(MESSAGE_ENTITY, entity);
        BValue[] inputArg = {inResponse};
        String error = null;
        try {
            BRunUtil.invoke(result, "testGetJsonPayload", inputArg);
        } catch (Throwable e) {
            error = e.getMessage();
        }
        Assert.assertTrue(error.contains("empty JSON document"));
    }

    @Test(description = "Test getEntity method on a response without a entity")
    public void testGetEntityNegative() {
        BStruct inResponse = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inRespStruct);
        BValue[] inputArg = {inResponse};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetEntity", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertNotNull(returnVals[0]);
    }

    @Test(description = "Test method with string payload")
    public void testGetJsonPayloadWithStringPayload() {
        BStruct inResponse = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inRespStruct);
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        BStruct mediaType = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, mediaTypeStruct);

        String payload = "ballerina";
        MimeUtil.setContentType(mediaType, entity, TEXT_PLAIN);
        entity.addNativeData(BYTE_CHANNEL_STRUCT, EntityBodyHandler.getEntityWrapper(payload));
        inResponse.addNativeData(MESSAGE_ENTITY, entity);
        inResponse.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, true);
        BValue[] inputArg = {inResponse};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetJsonPayload", inputArg);
        Assert.assertNull(returnVals[0]);
    }

    @Test
    public void testGetProperty() {
        BStruct inResponse = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inRespStruct);
        HTTPCarbonMessage inResponseMsg = HttpUtil.createHttpCarbonMessage(true);
        HttpUtil.addCarbonMsg(inResponse, inResponseMsg);

        BString propertyName = new BString("wso2");
        BValue[] inputArg = {inResponse, propertyName};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetProperty", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertNull(returnVals[0].stringValue());
    }

    @Test(description = "Test getStringPayload method without a paylaod")
    public void testGetStringPayloadNegative() {
        BStruct inResponse = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inRespStruct);
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        inResponse.addNativeData(MESSAGE_ENTITY, entity);
        BValue[] inputArg = {inResponse};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetStringPayload", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(returnVals[0].stringValue(), "");
    }

    @Test
    public void testGetXmlPayloadNegative() {
        BStruct inResponse = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inRespStruct);
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        inResponse.addNativeData(MESSAGE_ENTITY, entity);
        BValue[] inputArg = {inResponse};
        String error = null;
        try {
            BRunUtil.invoke(result, "testGetXmlPayload", inputArg);
        } catch (Throwable e) {
            error = e.getMessage();
        }
        Assert.assertTrue(error.contains("error occurred while retrieving xml data from entity : " +
                "Unexpected EOF in prolog"));
    }

    @Test
    public void testCompilationErrorTestCases() {
        Assert.assertEquals(resultNegative.getErrorCount(), 10);
        //testInResponseSetStatusCodeWithString
        BAssertUtil.validateError(resultNegative, 0,
                "incompatible types: expected 'int', found 'string'", 4, 22);
        //testInResponseGetMethod
        BAssertUtil.validateError(resultNegative, 1,
                "undefined field 'method' in struct 'ballerina.net.http:InResponse'",
                9, 21);
        //testInResponseAddHeader
        BAssertUtil.validateError(resultNegative, 2,
                "undefined function 'addHeader' in struct 'ballerina.net.http:InResponse'",
                14, 5);
        //testInResponseRemoveHeader
        BAssertUtil.validateError(resultNegative, 3,
                "undefined function 'removeHeader' in struct 'ballerina.net.http:InResponse'",
                19, 5);
        //testInResponseRemoveAllHeaders
        BAssertUtil.validateError(resultNegative, 4,
                "undefined function 'removeAllHeaders' in struct 'ballerina.net.http:InResponse'",
                24, 5);
        //testInResponseSetHeader
        BAssertUtil.validateError(resultNegative, 5,
                "undefined function 'setHeader' in struct 'ballerina.net.http:InResponse'",
                29, 5);
        //testInResponseSetJsonPayload
        BAssertUtil.validateError(resultNegative, 6,
                "undefined function 'setJsonPayload' in struct 'ballerina.net.http:InResponse'",
                34, 5);
        //testInResponseSetProperty
        BAssertUtil.validateError(resultNegative, 7,
                "undefined function 'setProperty' in struct 'ballerina.net.http:InResponse'",
                39, 5);
        //testInResponseSetStringPayload
        BAssertUtil.validateError(resultNegative, 8,
                "undefined function 'setStringPayload' in struct 'ballerina.net.http:InResponse'",
                44, 5);
        //testInResponseSetXmlPayload
        BAssertUtil.validateError(resultNegative, 9,
                "undefined function 'setXmlPayload' in struct 'ballerina.net.http:InResponse'",
                49, 5);
    }
}
