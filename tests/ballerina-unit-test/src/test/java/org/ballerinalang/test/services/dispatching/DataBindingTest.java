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

package org.ballerinalang.test.services.dispatching;

import io.netty.handler.codec.http.HttpHeaderNames;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.launcher.util.BServiceUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.util.JsonParser;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.services.testutils.HTTPTestRequest;
import org.ballerinalang.test.services.testutils.MessageUtils;
import org.ballerinalang.test.services.testutils.Services;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;

import static org.ballerinalang.mime.util.MimeConstants.APPLICATION_JSON;
import static org.ballerinalang.mime.util.MimeConstants.APPLICATION_XML;
import static org.ballerinalang.mime.util.MimeConstants.OCTET_STREAM;
import static org.ballerinalang.mime.util.MimeConstants.TEXT_PLAIN;

/**
 * Data binding related test cases.
 */
public class DataBindingTest {

    private static final String TEST_EP = "testEP";
    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BServiceUtil.setupProgramFile(this, "test-src/services/dispatching/data-binding-test.bal");
    }

    @Test(description = "Test data binding with string payload")
    public void testDataBindingWithStringPayload() {
        HTTPTestRequest requestMsg = MessageUtils
                .generateHTTPMessage("/echo/body1", "POST", "WSO2");
        requestMsg.setHeader(HttpHeaderNames.CONTENT_TYPE.toString(), TEXT_PLAIN);
        HttpCarbonMessage responseMsg = Services.invokeNew(compileResult, TEST_EP, requestMsg);

        Assert.assertNotNull(responseMsg, "responseMsg message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(responseMsg).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("Person").stringValue(), "WSO2"
                , "Person variable not set properly.");
    }

    @Test(description = "Test data binding when path param exists")
    public void testDataBindingWhenPathParamExist() {
        HTTPTestRequest requestMsg = MessageUtils
                .generateHTTPMessage("/echo/body2/hello", "POST", "WSO2");
        requestMsg.setHeader(HttpHeaderNames.CONTENT_TYPE.toString(), TEXT_PLAIN);
        HttpCarbonMessage responseMsg = Services.invokeNew(compileResult, TEST_EP, requestMsg);

        Assert.assertNotNull(responseMsg, "responseMsg message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(responseMsg).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("Key").stringValue(), "hello"
                , "Key variable not set properly.");
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("Person").stringValue(), "WSO2"
                , "Person variable not set properly.");
    }

    @Test(description = "Test data binding with JSON payload")
    public void testDataBindingWithJSONPayload() {
        HTTPTestRequest requestMsg = MessageUtils
                .generateHTTPMessage("/echo/body3", "POST", "{'name':'WSO2', 'team':'ballerina'}");
        requestMsg.setHeader(HttpHeaderNames.CONTENT_TYPE.toString(), APPLICATION_JSON);
        HttpCarbonMessage responseMsg = Services.invokeNew(compileResult, TEST_EP, requestMsg);

        Assert.assertNotNull(responseMsg, "responseMsg message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(responseMsg).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("Key").stringValue(), "WSO2"
                , "Key variable not set properly.");
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("Team").stringValue(), "ballerina"
                , "Team variable not set properly.");
    }

    @Test(description = "Test data binding with XML payload")
    public void testDataBindingWithXMLPayload() {
        HTTPTestRequest requestMsg = MessageUtils
                .generateHTTPMessage("/echo/body4", "POST", "<name>WSO2</name>");
        requestMsg.setHeader(HttpHeaderNames.CONTENT_TYPE.toString(), APPLICATION_XML);
        HttpCarbonMessage responseMsg = Services.invokeNew(compileResult, TEST_EP, requestMsg);

        Assert.assertNotNull(responseMsg, "responseMsg message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(responseMsg).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("Key").stringValue(), "name"
                , "Key variable not set properly.");
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("Team").stringValue(), "WSO2"
                , "Team variable not set properly.");
    }

    @Test(description = "Test data binding with binary payload")
    public void testDataBindingWithBinaryPayload() {
        HTTPTestRequest requestMsg = MessageUtils
                .generateHTTPMessage("/echo/body5", "POST", "WSO2");
        requestMsg.setHeader(HttpHeaderNames.CONTENT_TYPE.toString(), OCTET_STREAM);
        HttpCarbonMessage responseMsg = Services.invokeNew(compileResult, TEST_EP, requestMsg);

        Assert.assertNotNull(responseMsg, "responseMsg message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(responseMsg).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("Key").stringValue(), "WSO2"
                , "Key variable not set properly.");
    }

    @Test(description = "Test data binding with global custom struct")
    public void testDataBindingWithGlobalStruct() {
        HTTPTestRequest requestMsg = MessageUtils
                .generateHTTPMessage("/echo/body6", "POST", "{'name':'wso2','age':12}");
        requestMsg.setHeader(HttpHeaderNames.CONTENT_TYPE.toString(), APPLICATION_JSON);
        HttpCarbonMessage responseMsg = Services.invokeNew(compileResult, TEST_EP, requestMsg);

        Assert.assertNotNull(responseMsg, "responseMsg message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(responseMsg).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("Key").stringValue(), "wso2"
                , "Key variable not set properly.");
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("Age").stringValue(), "12"
                , "Age variable not set properly.");
    }

    @Test(description = "Test data binding with an array of records")
    public void testDataBindingWithRecordArray() {
        HTTPTestRequest requestMsg = MessageUtils.generateHTTPMessage("/echo/body8", "POST",
                "[{'name':'wso2','age':12}, {'name':'ballerina','age':3}]");
        requestMsg.setHeader(HttpHeaderNames.CONTENT_TYPE.toString(), APPLICATION_JSON);
        HttpCarbonMessage responseMsg = Services.invokeNew(compileResult, TEST_EP, requestMsg);
        Assert.assertNotNull(responseMsg, "responseMsg message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(responseMsg).getInputStream());
        Assert.assertEquals(bJson.stringValue(), "[{\"name\":\"wso2\", \"age\":12}, " +
                "{\"name\":\"ballerina\", \"age\":3}]");
    }

    @Test(description = "Test data binding without content-type header")
    public void testDataBindingWithoutContentType() {
        HTTPTestRequest requestMsg = MessageUtils
                .generateHTTPMessage("/echo/body1", "POST", "WSO2");
        HttpCarbonMessage responseMsg = Services.invokeNew(compileResult, TEST_EP, requestMsg);

        Assert.assertNotNull(responseMsg, "responseMsg message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(responseMsg).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("Person").stringValue(), "WSO2"
                , "Person variable not set properly.");
    }

    @Test(description = "Test data binding with incompatible content-type")
    public void testDataBindingIncompatibleJSONPayloadType() {
        HTTPTestRequest requestMsg = MessageUtils
                .generateHTTPMessage("/echo/body3", "POST", "{'name':'WSO2', 'team':'EI'}");
        requestMsg.setHeader(HttpHeaderNames.CONTENT_TYPE.toString(), TEXT_PLAIN);
        HttpCarbonMessage responseMsg = Services.invokeNew(compileResult, TEST_EP, requestMsg);

        Assert.assertNotNull(responseMsg, "responseMsg message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(responseMsg).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("Key").stringValue(), "WSO2"
                , "Key variable not set properly.");
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("Team").stringValue(), "EI"
                , "Team variable not set properly.");
    }

    @Test(description = "Test data binding with compatible but type different payload")
    public void testDataBindingCompatiblePayload() {
        HTTPTestRequest requestMsg = MessageUtils
                .generateHTTPMessage("/echo/body5", "POST", "{'name':'WSO2', 'team':'ballerina'}");
        requestMsg.setHeader(HttpHeaderNames.CONTENT_TYPE.toString(), TEXT_PLAIN);
        HttpCarbonMessage responseMsg = Services.invokeNew(compileResult, TEST_EP, requestMsg);

        Assert.assertNotNull(responseMsg, "responseMsg message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(responseMsg).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("Key").stringValue(),
                "{'name':'WSO2', 'team':'ballerina'}", "Key variable not set properly.");
    }

    @Test(description = "Test data binding without a payload", expectedExceptions = BallerinaConnectorException.class,
            expectedExceptionsMessageRegExp = ".*Error in reading payload : String payload is null*")
    public void testDataBindingWithoutPayload() {
        HTTPTestRequest requestMsg = MessageUtils
                .generateHTTPMessage("/echo/body1", "GET");
        HttpCarbonMessage responseMsg = Services.invokeNew(compileResult, TEST_EP, requestMsg);

        Assert.assertNotNull(responseMsg, "responseMsg message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(responseMsg).getInputStream());
    }

    @Test(expectedExceptions = BallerinaConnectorException.class,
            expectedExceptionsMessageRegExp = ".*data binding failed: Error in reading payload.*")
    public void testDataBindingIncompatibleXMLPayload() {
        HTTPTestRequest requestMsg = MessageUtils
                .generateHTTPMessage("/echo/body4", "POST", "name':'WSO2', 'team':'ballerina");
        requestMsg.setHeader(HttpHeaderNames.CONTENT_TYPE.toString(), APPLICATION_JSON);
        Services.invokeNew(compileResult, TEST_EP, requestMsg);
    }

    @Test(expectedExceptions = BallerinaConnectorException.class,
            expectedExceptionsMessageRegExp = ".*Error in reading payload : unrecognized token 'ballerina'.*")
    public void testDataBindingIncompatibleStructPayload() {
        HTTPTestRequest requestMsg = MessageUtils
                .generateHTTPMessage("/echo/body6", "POST", "ballerina");
        requestMsg.setHeader(HttpHeaderNames.CONTENT_TYPE.toString(), TEXT_PLAIN);
        Services.invokeNew(compileResult, TEST_EP, requestMsg);
    }

    @Test
    public void testDataBindingWithEmptyJsonPayload() {
        HTTPTestRequest requestMsg = MessageUtils
                .generateHTTPMessage("/echo/body3", "GET");
        HttpCarbonMessage responseMsg = Services.invokeNew(compileResult, TEST_EP, requestMsg);
        Assert.assertNotNull(responseMsg, "responseMsg message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(responseMsg).getInputStream());
        Assert.assertNull(((BMap<String, BValue>) bJson).get("Key"), "Key variable not set properly.");
        Assert.assertNull(((BMap<String, BValue>) bJson).get("Team"), "Team variable not set properly.");
    }

    //TODO following two test cases doesn't throw error anymore. json to struct conversion doesn't do field validation.
    //It returns then required struct with default values when matching fields are not present.
    @Test(expectedExceptions = BallerinaConnectorException.class,
            expectedExceptionsMessageRegExp = ".*error while mapping 'age': no such field found in json",
            enabled = false)
    public void testDataBindingStructWithNoMatchingContent() {
        HTTPTestRequest requestMsg = MessageUtils
                .generateHTTPMessage("/echo/body6", "POST", "{'name':'WSO2', 'team':8}");
        requestMsg.setHeader(HttpHeaderNames.CONTENT_TYPE.toString(), APPLICATION_JSON);
        Services.invokeNew(compileResult, TEST_EP, requestMsg);
    }

    @Test(expectedExceptions = BallerinaConnectorException.class,
            expectedExceptionsMessageRegExp = ".* error while mapping 'message': no such field found in json",
            enabled = false)
    public void testDataBindingStructWithInvalidTypes() {
        HTTPTestRequest requestMsg = MessageUtils
                .generateHTTPMessage("/echo/body7", "POST", "{'name':'WSO2', 'team':8}");
        requestMsg.setHeader(HttpHeaderNames.CONTENT_TYPE.toString(), APPLICATION_JSON);
        Services.invokeNew(compileResult, TEST_EP, requestMsg);
    }
}
