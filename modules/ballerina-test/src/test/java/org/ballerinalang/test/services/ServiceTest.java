/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.services;

import org.ballerinalang.launcher.util.BServiceUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.util.StringUtils;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.net.http.Constants;
import org.ballerinalang.runtime.message.StringDataSource;
import org.ballerinalang.test.services.testutils.HTTPTestRequest;
import org.ballerinalang.test.services.testutils.MessageUtils;
import org.ballerinalang.test.services.testutils.Services;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;

import java.nio.ByteBuffer;

/**
 * Service/Resource dispatchers test class.
 */
public class ServiceTest {

    CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BServiceUtil.setupProgramFile(this, "test-src/services/echo-service.bal");
    }

    @Test
    public void testServiceDispatching() {
        HTTPTestRequest requestMsg = MessageUtils.generateHTTPMessage("/echo/message", "GET");
        HTTPCarbonMessage responseMsg = Services.invokeNew(compileResult, requestMsg);
        Assert.assertNotNull(responseMsg);
        // TODO: Improve with more assets
    }
    
    @Test
    public void testServiceDispatchingWithWorker() {
        HTTPTestRequest requestMsg = MessageUtils.generateHTTPMessage("/echo/message_worker", "GET");
        HTTPCarbonMessage responseMsg = Services.invokeNew(compileResult, requestMsg);
        Assert.assertNotNull(responseMsg);
    }

    @Test(description = "Test for service availability check")
    public void testServiceAvailabilityCheck() {
        HTTPTestRequest requestMsg = MessageUtils.generateHTTPMessage("/foo/message", "GET");
        HTTPCarbonMessage responseMsg = Services.invokeNew(compileResult, requestMsg);
        String responseMsgPayload = StringUtils.getStringFromInputStream(new HttpMessageDataStreamer(responseMsg)
                .getInputStream());
        Assert.assertEquals(responseMsgPayload, "no matching service found for path : /foo/message");
    }

    @Test(description = "Test for resource availability check")
    public void testResourceAvailabilityCheck() {
        HTTPTestRequest requestMsg = MessageUtils.generateHTTPMessage("/echo/bar", "GET");
        HTTPCarbonMessage responseMsg = Services.invokeNew(compileResult, requestMsg);
        String responseMsgPayload = StringUtils.getStringFromInputStream(new HttpMessageDataStreamer(responseMsg)
                .getInputStream());
        Assert.assertEquals(responseMsgPayload,
                "no matching resource found for path : /echo/bar , method : GET");
    }

    @Test
    public void testSetString() {
        HTTPTestRequest requestMsg = MessageUtils.generateHTTPMessage("/echo/setString", "POST");
        requestMsg.waitAndReleaseAllEntities();
        requestMsg.addMessageBody(ByteBuffer.wrap("hello".getBytes()));
        requestMsg.setEndOfMsgAdded(true);
        HTTPCarbonMessage responseMsg = Services.invokeNew(compileResult, requestMsg);

        Assert.assertNotNull(responseMsg);
    }

    @Test(dependsOnMethods = "testSetString")
    public void testGetString() {
        HTTPTestRequest requestMsg = MessageUtils.generateHTTPMessage("/echo/getString", "GET");
        HTTPCarbonMessage responseMsg = Services.invokeNew(compileResult, requestMsg);
        Assert.assertNotNull(responseMsg);
        String responseMsgPayload = StringUtils.getStringFromInputStream(new HttpMessageDataStreamer(responseMsg)
                .getInputStream());
        Assert.assertNotNull(responseMsgPayload);
        Assert.assertEquals(responseMsgPayload, "hello");
    }

    @Test(description = "Test accessing service level variable in resource")
    public void testGetServiceLevelString() {
        HTTPTestRequest requestMsg = MessageUtils.generateHTTPMessage("/echo/getServiceLevelString", "GET");
        HTTPCarbonMessage responseMsg = Services.invokeNew(compileResult, requestMsg);
        Assert.assertNotNull(responseMsg);

        String responseMsgPayload = StringUtils.getStringFromInputStream(new HttpMessageDataStreamer(responseMsg)
                .getInputStream());
        StringDataSource stringDataSource = new StringDataSource(responseMsgPayload);
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), "sample value");
    }

    @Test(description = "Test using constant as annotation attribute value")
    public void testConstantValueAsAnnAttributeVal() {
        HTTPTestRequest requestMsg = MessageUtils.generateHTTPMessage("/echo/constantPath", "GET");
        HTTPCarbonMessage responseMsg = Services.invokeNew(compileResult, requestMsg);
        Assert.assertNotNull(responseMsg);

        String responseMsgPayload = StringUtils.getStringFromInputStream(new HttpMessageDataStreamer(responseMsg)
                .getInputStream());
        StringDataSource stringDataSource = new StringDataSource(responseMsgPayload);
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), "constant path test");
    }

    @Test(description = "Test getString after setting string")
    public void testGetStringAfterSetString() {
        HTTPTestRequest setStringrequestMsg = MessageUtils.generateHTTPMessage("/echo/setString", "POST");
        String stringresponseMsgPayload = "hello";
        setStringrequestMsg.waitAndReleaseAllEntities();
        setStringrequestMsg.addMessageBody(ByteBuffer.wrap(stringresponseMsgPayload.getBytes()));
        setStringrequestMsg.setEndOfMsgAdded(true);
        Services.invokeNew(compileResult, setStringrequestMsg);

        HTTPTestRequest requestMsg = MessageUtils.generateHTTPMessage("/echo/getString", "GET");
        HTTPCarbonMessage responseMsg = Services.invokeNew(compileResult, requestMsg);
        Assert.assertNotNull(responseMsg);

        String responseMsgPayload = StringUtils.getStringFromInputStream(new HttpMessageDataStreamer(responseMsg)
                .getInputStream());
        Assert.assertNotNull(responseMsgPayload);
        Assert.assertEquals(responseMsgPayload, stringresponseMsgPayload);
    }

    @Test(description = "Test remove headers native function")
    public void testRemoveHeadersNativeFunction() {
        HTTPTestRequest requestMsg = MessageUtils.generateHTTPMessage("/echo/removeHeaders", "GET");
        requestMsg.setHeader("header1", "wso2");
        requestMsg.setHeader("header2", "ballerina");
        requestMsg.setHeader("header3", "hello");
        HTTPCarbonMessage responseMsg = Services.invokeNew(compileResult, requestMsg);
        Assert.assertNotNull(responseMsg);

        Assert.assertNull(responseMsg.getHeader("header1"));
        Assert.assertNull(responseMsg.getHeader("header2"));
        Assert.assertNull(responseMsg.getHeader("header3"));
    }

    @Test(description = "Test GetFormParams Native Function")
    public void testGetFormParamsNativeFunction() {
        String path = "/echo/getFormParams";
        HTTPTestRequest requestMsg = MessageUtils
                .generateHTTPMessage(path, "POST", "firstName=WSO2&team=BalDance");
        requestMsg.setHeader(Constants.CONTENT_TYPE, Constants.APPLICATION_FORM);
        HTTPCarbonMessage responseMsg = Services.invokeNew(compileResult, requestMsg);

        Assert.assertNotNull(responseMsg, "responseMsg message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(responseMsg).getInputStream());
        Assert.assertEquals(bJson.value().get("Name").asText(), "WSO2"
                , "Name variable not set properly.");
        Assert.assertEquals(bJson.value().get("Team").asText(), "BalDance"
                , "Team variable not set properly.");
    }

    @Test(description = "Test GetFormParams with undefined key")
    public void testGetFormParamsForUndefinedKey() {
        String path = "/echo/getFormParams";
        HTTPTestRequest requestMsg = MessageUtils
                .generateHTTPMessage(path, "POST", "firstName=WSO2&company=BalDance");
        requestMsg.setHeader(Constants.CONTENT_TYPE, Constants.APPLICATION_FORM);
        HTTPCarbonMessage responseMsg = Services.invokeNew(compileResult, requestMsg);

        Assert.assertNotNull(responseMsg, "responseMsg message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(responseMsg).getInputStream());
        Assert.assertTrue(bJson.value().get("Team").isNull(), "Team variable not set properly.");
    }

    @Test(description = "Test GetFormParams empty responseMsgPayloads")
    public void testGetFormParamsEmptyresponseMsgPayload() {
        String path = "/echo/getFormParams";
        HTTPTestRequest requestMsg = MessageUtils.generateHTTPMessage(path, "POST", "");
        requestMsg.setHeader(Constants.CONTENT_TYPE, Constants.APPLICATION_FORM);
        HTTPCarbonMessage responseMsg = Services.invokeNew(compileResult, requestMsg);

        Assert.assertNotNull(responseMsg, "responseMsg message not found");
        String responseMsgPayload = StringUtils.getStringFromInputStream(new HttpMessageDataStreamer(responseMsg)
                .getInputStream());
        StringDataSource stringDataSource = new StringDataSource(responseMsgPayload);
        Assert.assertNotNull(stringDataSource);
        Assert.assertTrue(stringDataSource.getValue().contains("empty message payload"));
    }

    @Test(description = "Test GetFormParams with unsupported media type")
    public void testGetFormParamsWithUnsupportedMediaType() {
        String path = "/echo/getFormParams";
        HTTPTestRequest requestMsg = MessageUtils
                .generateHTTPMessage(path, "POST", "firstName=WSO2&company=BalDance");
        HTTPCarbonMessage responseMsg = Services.invokeNew(compileResult, requestMsg);

        Assert.assertNotNull(responseMsg, "responseMsg message not found");
        String responseMsgPayload = StringUtils.getStringFromInputStream(new HttpMessageDataStreamer(responseMsg)
                .getInputStream());
        StringDataSource stringDataSource = new StringDataSource(responseMsgPayload);
        Assert.assertNotNull(stringDataSource);
        Assert.assertTrue(stringDataSource.getValue().contains("unsupported media type"));
    }

    @Test(description = "Test Http PATCH verb dispatching with a responseMsgPayload")
    public void testPATCHMethodWithBody() {
        String path = "/echo/modify";
        HTTPTestRequest requestMsg = MessageUtils.generateHTTPMessage(path, "PATCH", "WSO2");
        HTTPCarbonMessage responseMsg = Services.invokeNew(compileResult, requestMsg);

        Assert.assertNotNull(responseMsg, "responseMsg message not found");
        Assert.assertEquals(responseMsg.getProperty(Constants.HTTP_STATUS_CODE), 204);
    }

    @Test(description = "Test Http PATCH verb dispatching without a responseMsgPayload")
    public void testPATCHMethodWithoutBody() {
        String path = "/echo/modify";
        HTTPTestRequest requestMsg = MessageUtils.generateHTTPMessage(path, "PATCH");
        HTTPCarbonMessage responseMsg = Services.invokeNew(compileResult, requestMsg);

        Assert.assertNotNull(responseMsg, "responseMsg message not found");
        Assert.assertEquals(responseMsg.getProperty(Constants.HTTP_STATUS_CODE), 204);
    }

    //TODO: add more test cases

}
