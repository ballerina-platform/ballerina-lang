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

import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.http.Constants;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.runtime.message.BallerinaMessageDataSource;
import org.ballerinalang.runtime.message.StringDataSource;
import org.ballerinalang.test.utils.BTestUtils;
import org.ballerinalang.test.utils.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.carbon.transport.http.netty.message.HttpMessageDataStreamer;

/**
 * Test cases for ballerina.net.http.request negative native functions.
 */
public class RequestNativeFunctionNegativeTest {

    private CompileResult result, resultNegative;
    private final String requestStruct = Constants.REQUEST;
    private final String protocolPackageHttp = Constants.PROTOCOL_PACKAGE_HTTP;
    private String filePath = "test-src/statements/services/nativeimpl/request/requestNativeFunctionNegative.bal";
    private String filePathNeg = "test-src/statements/services/nativeimpl/request/requestCompileNegative.bal";

    @BeforeClass
    public void setup() {
        result = BTestUtils.compile(filePath);
        resultNegative = BTestUtils.compile(filePathNeg);
    }

    @Test(description = "Test when the content length header is not set")
    public void testGetContentLength() {
        BStruct request = BTestUtils.createAndGetStruct(result.getProgFile(), protocolPackageHttp, requestStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(true);
        HttpUtil.addCarbonMsg(request, cMsg);
        BValue[] inputArg = {request};
        String error = null;
        try {
            BTestUtils.invoke(result, "testGetContentLength", inputArg);
        } catch (Throwable e) {
            error = e.getMessage();
        }
        Assert.assertEquals(error.substring(23, 45), "invalid content length");
    }

    @Test
    public void testGetHeader() {
        BStruct request = BTestUtils.createAndGetStruct(result.getProgFile(), protocolPackageHttp, requestStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(true);
        HttpUtil.addCarbonMsg(request, cMsg);
        BString key = new BString(Constants.CONTENT_TYPE);
        BValue[] inputArg = {request, key};
        BValue[] returnVals = BTestUtils.invoke(result, "testGetHeader", inputArg);
        Assert.assertNull(((BString) returnVals[0]).value());
    }

    @Test(description = "Test method without json payload")
    public void testGetJsonPayloadWithoutPayload() {
        BStruct request = BTestUtils.createAndGetStruct(result.getProgFile(), protocolPackageHttp, requestStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(true);
        HttpUtil.addCarbonMsg(request, cMsg);
        BValue[] inputArg = {request};
        String error = null;
        try {
            BTestUtils.invoke(result, "testGetJsonPayload", inputArg);
        } catch (Throwable e) {
            error = e.getMessage();
        }
        Assert.assertEquals(error.substring(23, 133), "error while retrieving json payload from message: " +
                "failed to create json: No content to map due to end-of-input");
    }

    @Test(description = "Test method with string payload")
    public void testGetJsonPayloadWithStringPayload() {
        BStruct request = BTestUtils.createAndGetStruct(result.getProgFile(), protocolPackageHttp, requestStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(true);
        String payload = "ballerina";
        BallerinaMessageDataSource dataSource = new StringDataSource(payload);
        dataSource.setOutputStream(new HttpMessageDataStreamer(cMsg).getOutputStream());
        cMsg.setMessageDataSource(dataSource);
        cMsg.setAlreadyRead(true);
        HttpUtil.addCarbonMsg(request, cMsg);
        BValue[] inputArg = {request};
        String error = null;
        try {
            BTestUtils.invoke(result, "testGetJsonPayload", inputArg);
        } catch (Throwable e) {
            error = e.getMessage();
        }
        Assert.assertEquals(error.substring(23, 118), "error while retrieving json payload from message: " +
                "Unrecognized token 'ballerina': was expecting");
    }

    @Test
    public void testGetProperty() {
        BStruct request = BTestUtils.createAndGetStruct(result.getProgFile(), protocolPackageHttp, requestStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(true);
        HttpUtil.addCarbonMsg(request, cMsg);
        BString propertyName = new BString("wso2");
        BValue[] inputArg = {request, propertyName};
        BValue[] returnVals = BTestUtils.invoke(result, "testGetProperty", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(((BString) returnVals[0]).value(), "");
    }

    @Test(description = "Test getStringPayload method without a paylaod")
    public void testGetStringPayloadNegative() {
        BStruct request = BTestUtils.createAndGetStruct(result.getProgFile(), protocolPackageHttp, requestStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(true);
        HttpUtil.addCarbonMsg(request, cMsg);
        BValue[] inputArg = {request};
        BValue[] returnVals = BTestUtils.invoke(result, "testGetStringPayload", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(returnVals[0].stringValue(), "");
    }

    @Test(description = "Test getStringPayload method with JSON payload")
    public void testGetStringPayloadMethodWithJsonPayload() {
        BStruct request = BTestUtils.createAndGetStruct(result.getProgFile(), protocolPackageHttp, requestStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(true);
        String payload = "{\"code\":\"123\"}";
        BallerinaMessageDataSource dataSource = new BJSON(payload);
        dataSource.setOutputStream(new HttpMessageDataStreamer(cMsg).getOutputStream());
        cMsg.setMessageDataSource(dataSource);
        cMsg.setAlreadyRead(true);
        HttpUtil.addCarbonMsg(request, cMsg);
        BValue[] inputArg = {request};
        BValue[] returnVals = BTestUtils.invoke(result, "testGetStringPayload", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(returnVals[0].stringValue(), payload);
    }

    @Test
    public void testGetXmlPayloadNegative() {
        BStruct request = BTestUtils.createAndGetStruct(result.getProgFile(), protocolPackageHttp, requestStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(true);
        HttpUtil.addCarbonMsg(request, cMsg);
        BValue[] inputArg = {request};
        String error = null;
        try {
            BTestUtils.invoke(result, "testGetXmlPayload", inputArg);
        } catch (Throwable e) {
            error = e.getMessage();
        }
        Assert.assertEquals(error.substring(23, 85)
                , "error while retrieving XML payload from message: empty content");
    }

    @Test
    public void testRemoveHeaderNegative() {
        BStruct request = BTestUtils.createAndGetStruct(result.getProgFile(), protocolPackageHttp, requestStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(true);
        String expect = "Expect";
        cMsg.setHeader(expect, "100-continue");
        HttpUtil.addCarbonMsg(request, cMsg);
        BString key = new BString("Range");
        BValue[] inputArg = {request, key};
        BValue[] returnVals = BTestUtils.invoke(result, "testRemoveHeader", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BStruct);
        HTTPCarbonMessage response = HttpUtil.getCarbonMsg((BStruct) returnVals[0], null);
        Assert.assertNotNull(response.getHeader(expect));
    }

    @Test
    public void testRemoveAllHeadersNegative() {
        BStruct request = BTestUtils.createAndGetStruct(result.getProgFile(), protocolPackageHttp, requestStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(true);
        HttpUtil.addCarbonMsg(request, cMsg);
        BValue[] inputArg = {request};
        BValue[] returnVals = BTestUtils.invoke(result, "testRemoveAllHeaders", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BStruct);
        HTTPCarbonMessage response = HttpUtil.getCarbonMsg((BStruct) returnVals[0], null);
        Assert.assertNull(response.getHeader("Expect"));
    }

    @Test
    public void testGetMethodNegative() {
        BStruct request = BTestUtils.createAndGetStruct(result.getProgFile(), protocolPackageHttp, requestStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(true);
        HttpUtil.addCarbonMsg(request, cMsg);
        BValue[] inputArg = {request};
        BValue[] returnVals = BTestUtils.invoke(result, "testGetMethod", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(returnVals[0].stringValue(), "");
    }

    @Test
    public void testGetRequestURL() {
        BStruct request = BTestUtils.createAndGetStruct(result.getProgFile(), protocolPackageHttp, requestStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(true);
        HttpUtil.addCarbonMsg(request, cMsg);

        BValue[] inputArg = {request};
        BValue[] returnVals = BTestUtils.invoke(result, "testGetRequestURL", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(returnVals[0].stringValue(), "no url");
    }

    @Test
    public void testGetBinaryPayloadMethod() {
        //TODO
    }

    @Test
    public void testCompilationErrorTestCases() {
        Assert.assertEquals(resultNegative.getErrorCount(), 2);
        //testRequestSetStatusCode
        BTestUtils.validateError(resultNegative, 0, "undefined function 'setStatusCode'", 5, 5);
        //testRequestGetContentLengthWithString
        BTestUtils.validateError(resultNegative, 1, "incompatible types: expected 'int', found 'string'", 10, 35);
    }

}
