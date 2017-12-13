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
import org.ballerinalang.model.util.StringUtils;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXMLItem;
import org.ballerinalang.net.http.Constants;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.runtime.message.BallerinaMessageDataSource;
import org.ballerinalang.runtime.message.StringDataSource;
import org.ballerinalang.test.services.testutils.HTTPTestRequest;
import org.ballerinalang.test.services.testutils.MessageUtils;
import org.ballerinalang.test.services.testutils.Services;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;

/**
 * Test cases for ballerina.net.http.response success native functions.
 */
public class ResponseNativeFunctionSuccessTest {

    private CompileResult result, serviceResult;
    private final String responseStruct = Constants.RESPONSE;
    private final String protocolPackageHttp = Constants.PROTOCOL_PACKAGE_HTTP;
    private String sourceFilePath = "test-src/statements/services/nativeimpl/response/response-native-function.bal";

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile(sourceFilePath);
        serviceResult = BServiceUtil.setupProgramFile(this, sourceFilePath);
    }

    @Test
    public void testAddHeader() {
        BStruct response = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, responseStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(false);

        HttpUtil.addCarbonMsg(response, cMsg);

        String headerName = "header1";
        String headerValue = "headerValue";
        BString key = new BString(headerName);
        BString value = new BString(headerValue);
        BValue[] inputArg = {response, key, value};
        BValue[] returnVals = BRunUtil.invoke(result, "testAddHeader", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BStruct);
        HTTPCarbonMessage responseMsg = HttpUtil.getCarbonMsg((BStruct) returnVals[0], null);
        Assert.assertEquals(responseMsg.getHeader(headerName), headerValue);
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

    //TODO Test this with multipart support, not needed for now
    @Test(enabled = false)
    public void testGetBinaryPayloadMethod() {
        BStruct response = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, responseStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(false);

        String payload = "ballerina";
        BallerinaMessageDataSource dataSource = new StringDataSource(payload);
        dataSource.setOutputStream(new HttpMessageDataStreamer(cMsg).getOutputStream());
        HttpUtil.addMessageDataSource(response, dataSource);

        HttpUtil.addCarbonMsg(response, cMsg);
        BValue[] inputArg = {response};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetBinaryPayload", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(returnVals[0].stringValue(), payload);
    }

    @Test
    public void testGetContentLength() {
        BStruct response = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, responseStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(false);

        String payload = "ballerina";
        BallerinaMessageDataSource dataSource = new StringDataSource(payload);
        dataSource.setOutputStream(new HttpMessageDataStreamer(cMsg).getOutputStream());
        HttpUtil.addMessageDataSource(response, dataSource);

        cMsg.setHeader(Constants.HTTP_CONTENT_LENGTH, String.valueOf(payload.length()));
        HttpUtil.addCarbonMsg(response, cMsg);
        BValue[] inputArg = {response};
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
        BStruct response = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, responseStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(false);
        cMsg.setHeader(Constants.CONTENT_TYPE, Constants.APPLICATION_FORM);
        HttpUtil.addCarbonMsg(response, cMsg);
        BString key = new BString(Constants.CONTENT_TYPE);

        BValue[] inputArg = {response, key};
        BValue[] returnVals = BRunUtil.invoke(result, "testGetHeader", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(returnVals[0].stringValue(), Constants.APPLICATION_FORM);
    }

    @Test(description = "Test GetHeader function within a service")
    public void testServiceGetHeader() {
        String header = Constants.CONTENT_TYPE;
        String value = "x-www-form-urlencoded";
        String path = "/hello/getHeader/" + header + "/" + value;
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, Constants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("value").asText(), value);
    }

    @Test
    public void testGetJsonPayload() {
        BStruct response = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, responseStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(false);

        String payload = "{'code':'123'}";
        BallerinaMessageDataSource dataSource = new BJSON(payload);
        dataSource.setOutputStream(new HttpMessageDataStreamer(cMsg).getOutputStream());
        HttpUtil.addMessageDataSource(response, dataSource);

        cMsg.setHeader(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
        HttpUtil.addCarbonMsg(response, cMsg);
        BValue[] inputArg = {response};
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
        Assert.assertEquals(new BJSON(new HttpMessageDataStreamer(responseMsg).getInputStream())
                .stringValue(), value);
    }

    @Test
    public void testGetProperty() {
        BStruct response = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, responseStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(false);
        String propertyName = "wso2";
        String propertyValue = "Ballerina";
        cMsg.setProperty(propertyName, propertyValue);
        HttpUtil.addCarbonMsg(response, cMsg);
        BString name = new BString(propertyName);
        BValue[] inputArg = {response, name};
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
        BStruct response = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, responseStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(false);
        String payload = "ballerina";

        BallerinaMessageDataSource dataSource = new StringDataSource(payload);
        dataSource.setOutputStream(new HttpMessageDataStreamer(cMsg).getOutputStream());
        HttpUtil.addMessageDataSource(response, dataSource);

        HttpUtil.addCarbonMsg(response, cMsg);
        BValue[] inputArg = {response};
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
        Assert.assertEquals(StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream()), value);
    }

    @Test
    public void testGetXmlPayload() {
        BStruct response = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, responseStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(false);

        String payload = "<name>ballerina</name>";
        BallerinaMessageDataSource dataSource = new BXMLItem(payload);
        dataSource.setOutputStream(new HttpMessageDataStreamer(cMsg).getOutputStream());
        HttpUtil.addMessageDataSource(response, dataSource);

        HttpUtil.addCarbonMsg(response, cMsg);
        BValue[] inputArg = {response};
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
        Assert.assertEquals(StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream()), value);
    }

    @Test
    public void testRemoveHeader() {
        BStruct response = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, responseStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(false);
        String expect = "Expect";
        cMsg.setHeader(expect, "100-continue");
        HttpUtil.addCarbonMsg(response, cMsg);
        BString key = new BString(expect);
        BValue[] inputArg = {response, key};
        BValue[] returnVals = BRunUtil.invoke(result, "testRemoveHeader", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BStruct);
        HTTPCarbonMessage responseMsg = HttpUtil.getCarbonMsg((BStruct) returnVals[0], null);
        Assert.assertNull(responseMsg.getHeader(expect));
    }

    @Test(description = "Test RemoveHeader function within a service")
    public void testServiceRemoveHeader() {
        String key = Constants.CONTENT_TYPE;
        String value = "x-www-form-urlencoded";
        String path = "/hello/RemoveHeader/" + key + "/" + value;
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, Constants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertNull(bJson.value().get("value").stringValue());
    }

    @Test
    public void testRemoveAllHeaders() {
        BStruct response = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, responseStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(false);
        String expect = "Expect";
        String range = "Range";
        cMsg.setHeader(expect, "100-continue");
        cMsg.setHeader(range, "bytes=500-999");
        HttpUtil.addCarbonMsg(response, cMsg);
        BValue[] inputArg = {response};
        BValue[] returnVals = BRunUtil.invoke(result, "testRemoveAllHeaders", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BStruct);
        HTTPCarbonMessage responseMsg = HttpUtil.getCarbonMsg((BStruct) returnVals[0], null);
        Assert.assertNull(responseMsg.getHeader(expect));
        Assert.assertNull(responseMsg.getHeader(range));
    }

    @Test(description = "Test RemoveAllHeaders function within a service")
    public void testServiceRemoveAllHeaders() {
        String path = "/hello/RemoveAllHeaders";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, Constants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertNull(bJson.value().get("value").stringValue());
    }

    @Test
    public void testSendMethod() {
        String path = "/hello/11";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, Constants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, cMsg);

        Assert.assertNotNull(response, "Response message not found");
    }

    @Test
    public void testSetContentLength() {
        BStruct response = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, responseStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(false);
        HttpUtil.addCarbonMsg(response, cMsg);
        BInteger length = new BInteger(10);
        BValue[] inputArg = {response, length};
        BValue[] returnVals = BRunUtil.invoke(result, "testSetContentLength", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BStruct);
        HTTPCarbonMessage responseMsg = HttpUtil.getCarbonMsg((BStruct) returnVals[0], null);
        Assert.assertEquals(responseMsg.getHeader(Constants.HTTP_CONTENT_LENGTH), "10");
    }

    @Test(description = "Test SetContentLength function within a service")
    public void testServiceSetContentLength() {
        String path = "/hello/setContentLength";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, Constants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("value").asText(), String.valueOf(100));
    }

    @Test
    public void testSetHeader() {
        BStruct response = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, responseStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(false);
        HttpUtil.addCarbonMsg(response, cMsg);
        String range = "Range";
        String rangeValue = "bytes=500-999";
        BString key = new BString(range);
        BString value = new BString(rangeValue);
        BValue[] inputArg = {response, key, value};
        BValue[] returnVals = BRunUtil.invoke(result, "testSetHeader", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BStruct);
        HTTPCarbonMessage responseMsg = HttpUtil.getCarbonMsg((BStruct) returnVals[0], null);
        Assert.assertEquals(responseMsg.getHeader(range), rangeValue);
    }

    @Test
    public void testSetJsonPayload() {
        BStruct response = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, responseStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(false);
        HttpUtil.addCarbonMsg(response, cMsg);

        BJSON value = new BJSON("{'name':'wso2'}");
        BValue[] inputArg = {response, value};
        BValue[] returnVals = BRunUtil.invoke(result, "testSetJsonPayload", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BStruct);

        BJSON bJson = (BJSON) ((BStruct) returnVals[0]).getNativeData(Constants.MESSAGE_DATA_SOURCE);
        Assert.assertEquals(bJson.value().get("name").asText(), "wso2", "Payload is not set properly");
    }

    @Test
    public void testSetProperty() {
        BStruct response = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, responseStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(false);
        HttpUtil.addCarbonMsg(response, cMsg);
        String propertyName = "wso2";
        String propertyValue = "Ballerina";
        BString name = new BString(propertyName);
        BString value = new BString(propertyValue);
        BValue[] inputArg = {response, name, value};
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
        BStruct response = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, responseStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(false);
        HttpUtil.addCarbonMsg(response, cMsg);

        BString value = new BString("Ballerina");
        BValue[] inputArg = {response, value};
        BValue[] returnVals = BRunUtil.invoke(result, "testSetStringPayload", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BStruct);

        StringDataSource stringDataSource = (StringDataSource)
                ((BStruct) returnVals[0]).getNativeData(Constants.MESSAGE_DATA_SOURCE);
        Assert.assertEquals(stringDataSource.getMessageAsString(), "Ballerina"
                , "Payload is not set properly");
    }

    @Test
    public void testSetXmlPayload() {
        BStruct response = BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, responseStruct);
        HTTPCarbonMessage cMsg = HttpUtil.createHttpCarbonMessage(false);
        HttpUtil.addCarbonMsg(response, cMsg);

        BXMLItem value = new BXMLItem("<name>Ballerina</name>");
        BValue[] inputArg = {response, value};
        BValue[] returnVals = BRunUtil.invoke(result, "testSetXmlPayload", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BStruct);

        BXMLItem xmlValue = (BXMLItem) ((BStruct) returnVals[0]).getNativeData(Constants.MESSAGE_DATA_SOURCE);
        Assert.assertEquals(xmlValue.getTextValue().stringValue(), "Ballerina"
                , "Payload is not set properly");
    }

    @Test
    public void testForwardMethod() {
        String path = "/hello/14";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, Constants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(StringUtils
                        .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream()), "hello"
                , "Payload is not set properly");
    }
}
