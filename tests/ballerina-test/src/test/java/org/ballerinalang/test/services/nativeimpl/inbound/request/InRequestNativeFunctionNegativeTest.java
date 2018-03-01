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
package org.ballerinalang.test.services.nativeimpl.inbound.request;

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

import static org.ballerinalang.mime.util.Constants.ENTITY_BYTE_CHANNEL;
import static org.ballerinalang.mime.util.Constants.IS_BODY_BYTE_CHANNEL_ALREADY_SET;
import static org.ballerinalang.mime.util.Constants.MEDIA_TYPE;
import static org.ballerinalang.mime.util.Constants.MESSAGE_ENTITY;
import static org.ballerinalang.mime.util.Constants.PROTOCOL_PACKAGE_MIME;
import static org.ballerinalang.mime.util.Constants.TEXT_PLAIN;

/**
 * Test cases for ballerina.net.http inbound inRequest negative native functions.
 */
public class InRequestNativeFunctionNegativeTest {

    private CompileResult result, resultNegative;
    private final String inReqStruct = HttpConstants.IN_REQUEST;
    private final String entityStruct = HttpConstants.ENTITY;
    private final String mediaTypeStruct = MEDIA_TYPE;
    private final String protocolPackageHttp = HttpConstants.PROTOCOL_PACKAGE_HTTP;
    private final String protocolPackageMime = PROTOCOL_PACKAGE_MIME;

    @BeforeClass
    public void setup() {
        String basePath = "test-src/statements/services/nativeimpl/inbound/request/";
        result = BCompileUtil.compile(basePath + "in-request-native-function-negative.bal");
        resultNegative = BCompileUtil.compile(basePath + "in-request-compile-negative.bal");
    }

    @Test(description = "Test when the content length header is not set")
    public void testGetContentLength() {
        BStruct inRequest = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inReqStruct);
        BValue[] inputArg = {inRequest};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetContentLength", inputArg);
        Assert.assertEquals(Integer.parseInt(returnVals[0].stringValue()), -1);
    }

    @Test
    public void testGetHeader() {
        BStruct inRequest = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inReqStruct);
        BString key = new BString(HttpHeaderNames.CONTENT_TYPE.toString());
        BValue[] inputArg = {inRequest, key};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetHeader", inputArg);
        Assert.assertNotNull(returnVals[0]);
        Assert.assertNull(((BString) returnVals[0]).value());
    }

    @Test(description = "Test method without json payload")
    public void testGetJsonPayloadWithoutPayload() {
        BStruct inRequest = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inReqStruct);
        BValue[] inputArg = {inRequest};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetJsonPayload", inputArg);
        Assert.assertNull(returnVals[0]);
    }

    @Test(description = "Test method with string payload")
    public void testGetJsonPayloadWithStringPayload() {
        BStruct inRequest = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inReqStruct);
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        BStruct mediaType = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, mediaTypeStruct);

        String payload = "ballerina";
        MimeUtil.setContentType(mediaType, entity, TEXT_PLAIN);
        entity.addNativeData(ENTITY_BYTE_CHANNEL, EntityBodyHandler.getEntityWrapper(payload));
        inRequest.addNativeData(MESSAGE_ENTITY, entity);
        inRequest.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, true);

        BValue[] inputArg = {inRequest};
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
        BStruct inRequest = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inReqStruct);
        BString propertyName = new BString("wso2");
        BValue[] inputArg = {inRequest, propertyName};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetProperty", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertNull(returnVals[0].stringValue());
    }

    @Test(description = "Test getEntity method on a inRequest without a entity")
    public void testGetEntityNegative() {
        BStruct inRequest = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inReqStruct);
        BValue[] inputArg = {inRequest};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetEntity", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertNotNull(returnVals[0]);
    }

    @Test(description = "Test getStringPayload method without a paylaod")
    public void testGetStringPayloadNegative() {
        BStruct inRequest = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inReqStruct);
        BValue[] inputArg = {inRequest};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetStringPayload", inputArg);
        Assert.assertNull(returnVals[0].stringValue());
    }

    @Test
    public void testGetXmlPayloadNegative() {
        BStruct inRequest = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inReqStruct);
        BValue[] inputArg = {inRequest};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetXmlPayload", inputArg);
        Assert.assertNull(returnVals[0]);
    }

    @Test
    public void testGetMethodNegative() {
        BStruct inRequest = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inReqStruct);
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
        BStruct inRequest = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inReqStruct);
        HTTPCarbonMessage inRequestMsg = HttpUtil.createHttpCarbonMessage(true);
        HttpUtil.addCarbonMsg(inRequest, inRequestMsg);
        BValue[] inputArg = {inRequest};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetRequestURL", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(returnVals[0].stringValue(), "no url");
    }

    @Test
    public void testCompilationErrorTestCases() {
        Assert.assertEquals(resultNegative.getErrorCount(), 2);
        //testRequestSetStatusCode
        BAssertUtil.validateError(resultNegative, 0,
                "undefined function 'setStatusCode' in struct 'ballerina.net.http:InRequest'", 4, 5);
        BAssertUtil.validateError(resultNegative, 1,
                "undefined field 'statusCode' in struct 'ballerina.net.http:InRequest'", 5, 5);
    }
}
