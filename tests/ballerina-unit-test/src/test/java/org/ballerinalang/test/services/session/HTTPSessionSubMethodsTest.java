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

package org.ballerinalang.test.services.session;

import org.ballerinalang.launcher.util.BServiceUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.util.StringUtils;
import org.ballerinalang.test.services.testutils.CookieUtils;
import org.ballerinalang.test.services.testutils.HTTPTestRequest;
import org.ballerinalang.test.services.testutils.MessageUtils;
import org.ballerinalang.test.services.testutils.Services;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;

import static org.ballerinalang.net.http.HttpConstants.COOKIE_HEADER;
import static org.ballerinalang.net.http.HttpConstants.RESPONSE_COOKIE_HEADER;
import static org.ballerinalang.net.http.HttpConstants.SESSION_ID;

/**
 * HTTP session sub Methods Test Class.
 */
public class HTTPSessionSubMethodsTest {

    private static final String TEST_ENDPOINT_NAME = "sessionEP";
    CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BServiceUtil.setupProgramFile(this, "test-src/services/session/http-session-test.bal");
    }

    @Test(description = "Test for GetId Function")
    public void testGetIdFunction() {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/sample2/id1", "GET");
        HTTPCarbonMessage response = Services.invokeNew(compileResult, TEST_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response);

        String cookie = response.getHeader(RESPONSE_COOKIE_HEADER);
        String sessionId = CookieUtils.getCookie(cookie).value;

        String responseMsgPayload = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertNotNull(responseMsgPayload);
        Assert.assertEquals(responseMsgPayload, sessionId);
    }

    @Test(description = "Test for null session GetId Function")
    public void testNullSessionGetIdFunction() {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/sample2/id2", "GET");
        HTTPCarbonMessage response = Services.invokeNew(compileResult, TEST_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response);

        String responseMsgPayload = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertNotNull(responseMsgPayload);
        Assert.assertEquals(responseMsgPayload, "");
    }

    @Test(description = "Test for isNew Function")
    public void testIsNewFunction() {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/sample2/new1", "GET");
        HTTPCarbonMessage response = Services.invokeNew(compileResult, TEST_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response);

        String responseMsgPayload = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertNotNull(responseMsgPayload);
        Assert.assertEquals(responseMsgPayload, "true");

    }

    @Test(description = "Test for isNew Function two attempts")
    public void testIsNewFunctiontwoAttempts() {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/sample2/new1", "GET");
        HTTPCarbonMessage response = Services.invokeNew(compileResult, TEST_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response);

        String responseMsgPayload = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertNotNull(responseMsgPayload);
        Assert.assertEquals(responseMsgPayload, "true");

        String cookie = response.getHeader(RESPONSE_COOKIE_HEADER);
        String sessionId = CookieUtils.getCookie(cookie).value;

        cMsg = MessageUtils.generateHTTPMessage("/sample2/new1", "GET");
        cMsg.setHeader(COOKIE_HEADER, SESSION_ID + sessionId);
        response = Services.invokeNew(compileResult, TEST_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response);

        responseMsgPayload = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertNotNull(responseMsgPayload);
        Assert.assertEquals(responseMsgPayload, "false");
    }

    @Test(description = "Test for GetCreateTime Function")
    public void testGetCreateTimeFunction() {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/sample2/new2", "GET");
        HTTPCarbonMessage response = Services.invokeNew(compileResult, TEST_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response);

        String responseMsgPayload = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        long firstAccess = Long.parseLong(responseMsgPayload.toString());


        String cookie = response.getHeader(RESPONSE_COOKIE_HEADER);
        String sessionId = CookieUtils.getCookie(cookie).value;

        cMsg = MessageUtils.generateHTTPMessage("/sample2/new2", "GET");
        cMsg.setHeader(COOKIE_HEADER, SESSION_ID + sessionId);
        response = Services.invokeNew(compileResult, TEST_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response);

        responseMsgPayload = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertNotNull(responseMsgPayload);
        long secondAccess = Long.parseLong(responseMsgPayload);
        Assert.assertTrue(firstAccess == secondAccess);
    }

    @Test(description = "Test for GetCreateTime Function error")
    public void testGetCreateTimeFunctionError() {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/sample2/new3", "GET");
        HTTPCarbonMessage response = Services.invokeNew(compileResult, TEST_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response);

        String responseMsgPayload = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertNotNull(responseMsgPayload);
        Assert.assertTrue(responseMsgPayload.contains("No such session in progress"));
    }

    @Test(description = "Test for GetLastAccessedTime Function")
    public void testGetLastAccessedTimeFunction() {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/sample2/new4", "GET");
        HTTPCarbonMessage response = Services.invokeNew(compileResult, TEST_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response);

        String responseMsgPayload = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertNotNull(responseMsgPayload);
        long firstAccess = Long.parseLong(responseMsgPayload.toString());


        String cookie = response.getHeader(RESPONSE_COOKIE_HEADER);
        String sessionId = CookieUtils.getCookie(cookie).value;

        cMsg = MessageUtils.generateHTTPMessage("/sample2/new4", "GET");
        cMsg.setHeader(COOKIE_HEADER, SESSION_ID + sessionId);
        response = Services.invokeNew(compileResult, TEST_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response);

        responseMsgPayload = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertNotNull(responseMsgPayload);
        long secondAccess = Long.parseLong(responseMsgPayload);
        Assert.assertTrue(firstAccess == secondAccess);

        cMsg = MessageUtils.generateHTTPMessage("/sample2/new4", "GET");
        cMsg.setHeader(COOKIE_HEADER, SESSION_ID + sessionId);
        response = Services.invokeNew(compileResult, TEST_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response);

        responseMsgPayload = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertNotNull(responseMsgPayload);
        long thirdAccess = Long.parseLong(responseMsgPayload);
        Assert.assertTrue(firstAccess <= thirdAccess);
    }

    @Test(description = "Test for GetLastAccessed Function error")
    public void testGetLastAccessedTimeFunctionError() {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/sample2/new5", "GET");
        HTTPCarbonMessage response = Services.invokeNew(compileResult, TEST_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response);

        String responseMsgPayload = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertNotNull(responseMsgPayload);
        Assert.assertTrue(responseMsgPayload.contains("No such session in progress"));
    }

    @Test(description = "Test for setMaxInactiveInterval and getMaxInactiveInterval Function")
    public void testSetMaxInactiveIntervalFunction() {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/sample2/new6", "GET");
        HTTPCarbonMessage response = Services.invokeNew(compileResult, TEST_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response);

        String responseMsgPayload = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertNotNull(responseMsgPayload);
        long timeInterval = Long.parseLong(responseMsgPayload.toString());
        Assert.assertEquals(timeInterval, 900);

        String cookie = response.getHeader(RESPONSE_COOKIE_HEADER);
        String sessionId = CookieUtils.getCookie(cookie).value;

        cMsg = MessageUtils.generateHTTPMessage("/sample2/new6", "GET");
        cMsg.setHeader(COOKIE_HEADER, SESSION_ID + sessionId);
        response = Services.invokeNew(compileResult, TEST_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response);

        responseMsgPayload = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertNotNull(responseMsgPayload);
        long timeInterval2 = Long.parseLong(responseMsgPayload);
        Assert.assertEquals(timeInterval2, 60);
    }

    @Test(description = "Test for SetMaxInactiveInterval Function error")
    public void testSetMaxInactiveIntervalFunctionError() {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/sample2/new7", "GET");
        HTTPCarbonMessage response = Services.invokeNew(compileResult, TEST_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response);

        String responseMsgPayload = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertNotNull(responseMsgPayload);
        Assert.assertTrue(responseMsgPayload.contains("No such session in progress"));
    }

    @Test(description = "Test for negative timeout setMaxInactiveInterval")
    public void testSetMaxInactiveIntervalNegativeTimeoutFunction() {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/sample2/new8", "GET");
        HTTPCarbonMessage response = Services.invokeNew(compileResult, TEST_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response);

        String responseMsgPayload = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertNotNull(responseMsgPayload);
        long timeInterval = Long.parseLong(responseMsgPayload.toString());
        Assert.assertEquals(timeInterval, 900);

        String cookie = response.getHeader(RESPONSE_COOKIE_HEADER);
        String sessionId = CookieUtils.getCookie(cookie).value;

        cMsg = MessageUtils.generateHTTPMessage("/sample2/new8", "GET");
        cMsg.setHeader(COOKIE_HEADER, SESSION_ID + sessionId);
        response = Services.invokeNew(compileResult, TEST_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response);

        responseMsgPayload = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertNotNull(responseMsgPayload);
        long timeInterval2 = Long.parseLong(responseMsgPayload.toString());
        Assert.assertEquals(timeInterval2, -1);
    }
}
