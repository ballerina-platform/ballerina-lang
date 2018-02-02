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

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.http.Constants;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.runtime.message.BallerinaMessageDataSource;
import org.ballerinalang.runtime.message.StringDataSource;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import static org.ballerinalang.mime.util.Constants.APPLICATION_JSON;
import static org.ballerinalang.mime.util.Constants.CONTENT_TYPE;
import static org.ballerinalang.mime.util.Constants.IS_ENTITY_BODY_PRESENT;
import static org.ballerinalang.mime.util.Constants.JSON_DATA_INDEX;
import static org.ballerinalang.mime.util.Constants.MEDIA_TYPE;
import static org.ballerinalang.mime.util.Constants.MESSAGE_ENTITY;
import static org.ballerinalang.mime.util.Constants.PROTOCOL_PACKAGE_MIME;

/**
 * Test cases for ballerina.net.http.request negative native functions.
 */
public class RequestNativeFunctionNegativeTest {

    private CompileResult result, resultNegative;
    private final String inRequestStruct = Constants.IN_REQUEST;
    private final String entityStruct = Constants.ENTITY;
    private final String mediaTypeStruct = MEDIA_TYPE;
    private final String protocolPackageHttp = Constants.PROTOCOL_PACKAGE_HTTP;
    private final String protocolPackageMime = PROTOCOL_PACKAGE_MIME;
    private String filePath = "test-src/statements/services/nativeimpl/request/request-native-function-negative.bal";
    private String filePathNeg = "test-src/statements/services/nativeimpl/request/request-compile-negative.bal";

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile(filePath);
        resultNegative = BCompileUtil.compile(filePathNeg);
    }

    @Test(description = "Test when the content length header is not set")
    public void testGetContentLength() {
        BStruct request = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inRequestStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(true);
        HttpUtil.addCarbonMsg(request, cMsg);
        BValue[] inputArg = { request };
        BValue[] returnVals = BRunUtil.invoke(result, "testGetContentLength", inputArg);
        Assert.assertEquals(Integer.parseInt(returnVals[0].stringValue()), -1);
    }

    @Test
    public void testGetHeader() {
        BStruct request = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inRequestStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(true);
        HttpUtil.addCarbonMsg(request, cMsg);
        BString key = new BString(CONTENT_TYPE);
        BValue[] inputArg = { request, key };
        BValue[] returnVals = BRunUtil.invoke(result, "testGetHeader", inputArg);
        Assert.assertNotNull(returnVals[0]);
        Assert.assertNull(((BString) returnVals[0]).value());
    }

    @Test(description = "Test method without json payload")
    public void testGetJsonPayloadWithoutPayload() {
        BStruct request = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inRequestStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(true);
        HttpUtil.addCarbonMsg(request, cMsg);
        BValue[] inputArg = { request };
        BValue[] returnVals = BRunUtil.invoke(result, "testGetJsonPayload", inputArg);
        Assert.assertNull(returnVals[0]);
    }

    @Test(description = "Test method with string payload")
    public void testGetJsonPayloadWithStringPayload() {
        BStruct request = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inRequestStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(true);
        String payload = "ballerina";
        BallerinaMessageDataSource dataSource = new StringDataSource(payload);
        HttpUtil.addCarbonMsg(request, cMsg);
        BValue[] inputArg = { request };
        BValue[] returnVals = BRunUtil.invoke(result, "testGetJsonPayload", inputArg);
        Assert.assertNull(returnVals[0]);
    }

    @Test
    public void testGetProperty() {
        BStruct request = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inRequestStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(true);
        HttpUtil.addCarbonMsg(request, cMsg);
        BString propertyName = new BString("wso2");
        BValue[] inputArg = { request, propertyName };
        BValue[] returnVals = BRunUtil.invoke(result, "testGetProperty", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertNull(returnVals[0].stringValue());
    }

    @Test(description = "Test getEntity method on a request without a entity")
    public void testGetEntityNegative() {
        BStruct request = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inRequestStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(true);
        HttpUtil.addCarbonMsg(request, cMsg);
        BValue[] inputArg = { request };
        BValue[] returnVals = BRunUtil.invoke(result, "testGetEntity", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertNotNull(returnVals[0]);
    }

    @Test(description = "Test getStringPayload method without a paylaod")
    public void testGetStringPayloadNegative() {
        BStruct request = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inRequestStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(true);
        HttpUtil.addCarbonMsg(request, cMsg);
        BValue[] inputArg = { request };
        BValue[] returnVals = BRunUtil.invoke(result, "testGetStringPayload", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertNull(returnVals[0].stringValue());
    }

    @Test(description = "Test getStringPayload method with JSON payload")
    public void testGetStringPayloadMethodWithJsonPayload() {
        BStruct request = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inRequestStruct);
        BStruct entity = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
        BStruct mediaType = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, mediaTypeStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(true);

        String payload = "{\"code\":\"123\"}";
        MimeUtil.setContentType(mediaType, entity, APPLICATION_JSON);
        entity.setRefField(JSON_DATA_INDEX, new BJSON(payload));
        request.addNativeData(MESSAGE_ENTITY, entity);
        request.addNativeData(IS_ENTITY_BODY_PRESENT, true);

        HttpUtil.addCarbonMsg(request, cMsg);
        BValue[] inputArg = { request };
        BValue[] returnVals = BRunUtil.invoke(result, "testGetStringPayload", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertNull(returnVals[0].stringValue());
    }

    @Test
    public void testGetXmlPayloadNegative() {
        BStruct request = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inRequestStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(true);
        HttpUtil.addCarbonMsg(request, cMsg);
        BValue[] inputArg = { request };
        BValue[] returnVals = BRunUtil.invoke(result, "testGetXmlPayload", inputArg);
        Assert.assertNull(returnVals[0]);
    }

    @Test
    public void testRemoveHeaderNegative() {
        BStruct request = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inRequestStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(true);
        String expect = "Expect";
        cMsg.setHeader(expect, "100-continue");
        HttpUtil.addCarbonMsg(request, cMsg);
        BString key = new BString("Range");
        BValue[] inputArg = { request, key };
        BValue[] returnVals = BRunUtil.invoke(result, "testRemoveHeader", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BStruct);
        HTTPCarbonMessage response = HttpUtil.getCarbonMsg((BStruct) returnVals[0], null);
        Assert.assertNotNull(response.getHeader(expect));
    }

    @Test
    public void testRemoveAllHeadersNegative() {
        BStruct request = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inRequestStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(true);
        HttpUtil.addCarbonMsg(request, cMsg);
        BValue[] inputArg = { request };
        BValue[] returnVals = BRunUtil.invoke(result, "testRemoveAllHeaders", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BStruct);
        HTTPCarbonMessage response = HttpUtil.getCarbonMsg((BStruct) returnVals[0], null);
        Assert.assertNull(response.getHeader("Expect"));
    }

    @Test
    public void testGetMethodNegative() {
        BStruct request = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inRequestStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(true);
        HttpUtil.addCarbonMsg(request, cMsg);
        BValue[] inputArg = { request };
        BValue[] returnVals = BRunUtil.invoke(result, "testGetMethod", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(returnVals[0].stringValue(), "");
    }

    @Test
    public void testGetRequestURLNegative() {
        BStruct request = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, inRequestStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(true);
        HttpUtil.addCarbonMsg(request, cMsg);

        BValue[] inputArg = { request };
        BValue[] returnVals = BRunUtil.invoke(result, "testGetRequestURL", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(returnVals[0].stringValue(), "no url");
    }

    @Test
    public void testGetBinaryPayloadMethod() {
        //TODO Test this with multipart support, not needed for now
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
