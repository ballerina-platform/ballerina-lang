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

import io.netty.handler.codec.http.HttpHeaderNames;
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
import org.wso2.carbon.messaging.Header;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;

import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.mime.util.Constants.TEXT_PLAIN;
import static org.ballerinalang.net.http.HttpConstants.COOKIE_HEADER;
import static org.ballerinalang.net.http.HttpConstants.RESPONSE_COOKIE_HEADER;
import static org.ballerinalang.net.http.HttpConstants.SESSION_ID;

/**
 * HTTP session Essential Methods Test Class.
 */
public class HTTPSessionEssentialMethodsTest {

    private static final String TEST_ENDPOINT_NAME = "sessionEP";
    CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BServiceUtil
                .setupProgramFile(this, "test-src/services/session/http-session-test.bal");
    }

    @Test(description = "Test for getting a session at first time")
    public void testGetSessionWithoutSessionCookie() {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/sample/test1", "GET");
        HTTPCarbonMessage responseMsg = Services.invokeNew(compileResult, TEST_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(responseMsg);

        String responseMsgPayload = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(responseMsg).getInputStream());
        Assert.assertNotNull(responseMsgPayload);
        Assert.assertEquals(responseMsgPayload, "session created");
        Assert.assertTrue(responseMsg.getHeader(RESPONSE_COOKIE_HEADER) != null);
    }

    @Test(description = "Test createSessionIfAbsent with invalid session cookie")
    public void testCreateSessionIfAbsentWithInvalidSessionCookie() {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/sample/test1", "GET");
        cMsg.setHeader(COOKIE_HEADER, SESSION_ID + "A4673299S549242");
        HTTPCarbonMessage responseMsg = Services.invokeNew(compileResult, TEST_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(responseMsg);

        String responseMsgPayload = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(responseMsg).getInputStream());
        Assert.assertNotNull(responseMsgPayload);
        Assert.assertEquals(responseMsgPayload, "session created");
        Assert.assertTrue(responseMsg.getHeader(RESPONSE_COOKIE_HEADER) != null);
    }

    @Test(description = "Test for getting a session without Id at first time")
    public void testGetSessionWithoutSessionIDCheck() {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/sample/test2", "GET");
        HTTPCarbonMessage responseMsg = Services.invokeNew(compileResult, TEST_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(responseMsg);

        String responseMsgPayload = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(responseMsg).getInputStream());
        Assert.assertNotNull(responseMsgPayload);
        Assert.assertEquals(responseMsgPayload, "no session id available");
    }

    @Test(description = "Test getSession with invalid session cookie")
    public void testGetSessionWithInvalidSessionCookie() {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/sample/test2", "GET");
        cMsg.setHeader(COOKIE_HEADER, SESSION_ID + "A4673299S549242");
        HTTPCarbonMessage responseMsg = Services.invokeNew(compileResult, TEST_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(responseMsg);

        String responseMsgPayload = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(responseMsg).getInputStream());
        Assert.assertNotNull(responseMsgPayload);
        Assert.assertEquals(responseMsgPayload, "no session id available");
        Assert.assertTrue(responseMsg.getHeader(RESPONSE_COOKIE_HEADER) == null);
    }

    @Test(description = "Test for not getting a session with at first time")
    public void testGetSessionMethod() {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/sample/test3", "GET");
        HTTPCarbonMessage response = Services.invokeNew(compileResult, TEST_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response);

        String responseMsgPayload = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertNotNull(responseMsgPayload);
        Assert.assertEquals(responseMsgPayload, "session is not created");
    }

    @Test(description = "Test for create two sessions ")
    public void testCreateTwoSessionsMethod() {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/sample/test6", "GET");
        HTTPCarbonMessage response = Services.invokeNew(compileResult, TEST_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response);

        String responseMsgPayload = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());;
        Assert.assertNotNull(responseMsgPayload);
        Assert.assertEquals(responseMsgPayload, "wso2");
    }

    @Test(description = "Test getting a session with at first time")
    public void testGetSessionHappyPathMethod() {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/sample/test1", "GET");
        HTTPCarbonMessage response = Services.invokeNew(compileResult, TEST_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response);

        String responseMsgPayload = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertNotNull(responseMsgPayload);
        Assert.assertEquals(responseMsgPayload, "session created");

        String cookie = response.getHeader(RESPONSE_COOKIE_HEADER);
        String sessionId = CookieUtils.getCookie(cookie).value;

        cMsg = MessageUtils.generateHTTPMessage("/sample/test2", "GET");
        cMsg.setHeader(COOKIE_HEADER, SESSION_ID + sessionId);
        response = Services.invokeNew(compileResult, TEST_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response);

        responseMsgPayload = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertNotNull(responseMsgPayload);
        Assert.assertEquals(responseMsgPayload, "session is returned");
    }

    @Test(description = "Test create session if absent with multiple cookies")
    public void testCreateSessionIfAbsentWithMultipleCookies() {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/sample2/new9", "GET");
        HTTPCarbonMessage response = Services.invokeNew(compileResult, TEST_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response);

        String responseMsgPayload = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertNotNull(responseMsgPayload);
        Assert.assertEquals(responseMsgPayload, "FirstName");

        String cookie = response.getHeader(RESPONSE_COOKIE_HEADER);
        String sessionId = CookieUtils.getCookie(cookie).value;

        cMsg = MessageUtils.generateHTTPMessage("/sample2/new9", "GET");
        cMsg.setHeader(COOKIE_HEADER, "A=5454252;  " + SESSION_ID + sessionId + "; JSESSIONID=5454252");
        response = Services.invokeNew(compileResult, TEST_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response);

        responseMsgPayload = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertNotNull(responseMsgPayload);
        Assert.assertEquals(responseMsgPayload, "SecondName");
    }

    @Test(description = "Test getting a session with multiple cookies")
    public void testGetSessionWithMultipleCookies() {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/sample/test1", "GET");
        HTTPCarbonMessage response = Services.invokeNew(compileResult, TEST_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response);

        String responseMsgPayload = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertNotNull(responseMsgPayload);
        Assert.assertEquals(responseMsgPayload, "session created");

        String cookie = response.getHeader(RESPONSE_COOKIE_HEADER);
        String sessionId = CookieUtils.getCookie(cookie).value;

        cMsg = MessageUtils.generateHTTPMessage("/sample/test2", "GET");
        cMsg.setHeader(COOKIE_HEADER, "A=5454252;  " + SESSION_ID + sessionId + "; JSESSIONID=5454252");
        response = Services.invokeNew(compileResult, TEST_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response);

        responseMsgPayload = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertNotNull(responseMsgPayload);
        Assert.assertEquals(responseMsgPayload, "session is returned");
    }


    @Test(description = "Test for Cookie Session Header availability check")
    public void testCookieSessionHeader() {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/sample/test1", "GET");
        HTTPCarbonMessage response = Services.invokeNew(compileResult, TEST_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response);

        String cookie = response.getHeader(RESPONSE_COOKIE_HEADER);
        Assert.assertNotNull(cookie);
        String sessionHeader = cookie.substring(0, SESSION_ID.length());
        Assert.assertEquals(sessionHeader, SESSION_ID);

    }

    @Test(description = "Test for Cookie Session path availability check")
    public void testCookiePath() {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/sample/test1", "GET");
        HTTPCarbonMessage response = Services.invokeNew(compileResult, TEST_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response);

        String cookie = response.getHeader(RESPONSE_COOKIE_HEADER);
        Assert.assertNotNull(cookie);
        Assert.assertEquals(CookieUtils.getCookie(cookie).path, "/sample");

    }

    @Test(description = "Test for HttpOnly flag in session cookie")
    public void testHttpOnlyFlagInSessionCookie() {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/sample/test1", "GET");
        HTTPCarbonMessage responseMsg = Services.invokeNew(compileResult, TEST_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(responseMsg);

        String cookie = responseMsg.getHeader(RESPONSE_COOKIE_HEADER);
        Assert.assertNotNull(cookie);
        Assert.assertTrue(CookieUtils.getCookie(cookie).httpOnly);
    }

    @Test(description = "Test for Get Attribute Function")
    public void testGetAttributeFunction() {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/sample/test4", "GET");
        HTTPCarbonMessage response = Services.invokeNew(compileResult, TEST_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response);

        String responseMsgPayload = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());;
        Assert.assertNotNull(responseMsgPayload);
        Assert.assertEquals(responseMsgPayload, "attribute not available");

    }

    @Test(description = "Test for sample Session functionality")
    public void testCounterFunctionality() {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/counter/echo", "GET");
        HTTPCarbonMessage response = Services.invokeNew(compileResult, TEST_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response);

        String responseMsgPayload = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());;
        Assert.assertNotNull(responseMsgPayload);
        Assert.assertEquals(responseMsgPayload, "1");

        String cookie = response.getHeader(RESPONSE_COOKIE_HEADER);
        String sessionId = CookieUtils.getCookie(cookie).value;

        cMsg = MessageUtils.generateHTTPMessage("/counter/echo", "GET");
        cMsg.setHeader(COOKIE_HEADER, SESSION_ID + sessionId);
        response = Services.invokeNew(compileResult, TEST_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response);

        responseMsgPayload = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertNotNull(responseMsgPayload);
        Assert.assertEquals(responseMsgPayload, "2");
    }

    @Test(description = "Test for path limitation per service")
    public void testCheckPathValidityPerService() {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/sample2/names", "GET");
        HTTPCarbonMessage response = Services.invokeNew(compileResult, TEST_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response);

        String responseMsgPayload = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());;
        Assert.assertNotNull(responseMsgPayload);
        Assert.assertEquals(responseMsgPayload, "arraysize:2");

        String cookie = response.getHeader(RESPONSE_COOKIE_HEADER);
        String sessionId = CookieUtils.getCookie(cookie).value;

        cMsg = MessageUtils.generateHTTPMessage("/sample2/names3", "GET");
        cMsg.setHeader(COOKIE_HEADER, SESSION_ID + sessionId);
        response = Services.invokeNew(compileResult, TEST_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response);

        responseMsgPayload = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertNotNull(responseMsgPayload);
        Assert.assertEquals(responseMsgPayload, "4");
    }

    @Test(description = "Test for path limitation")
    public void testCheckPathValidity() {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/counter/echo", "GET");
        HTTPCarbonMessage response = Services.invokeNew(compileResult, TEST_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response);

        String responseMsgPayload = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());;
        Assert.assertNotNull(responseMsgPayload);
        Assert.assertEquals(responseMsgPayload, "1");

        String cookie = response.getHeader(RESPONSE_COOKIE_HEADER);
        String sessionId = CookieUtils.getCookie(cookie).value;

        cMsg = MessageUtils.generateHTTPMessage("/sample2/echoName", "GET");
        cMsg.setHeader(COOKIE_HEADER, SESSION_ID + sessionId);
        response = Services.invokeNew(compileResult, TEST_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response);

        responseMsgPayload = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertNotNull(responseMsgPayload);
        Assert.assertTrue(responseMsgPayload.contains("failed to get session: /sample2 is not an allowed path"));
    }

    @Test(description = "Test for path limitation with getSession")
    public void testCheckPathValiditygetSession() {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/sample2/echoName", "GET");
        HTTPCarbonMessage response = Services.invokeNew(compileResult, TEST_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response);

        String responseMsgPayload = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());;
        Assert.assertNotNull(responseMsgPayload);
        Assert.assertEquals(responseMsgPayload, "wso2");

        String cookie = response.getHeader(RESPONSE_COOKIE_HEADER);
        String sessionId = CookieUtils.getCookie(cookie).value;

        cMsg = MessageUtils.generateHTTPMessage("/counter/echo2", "GET");
        cMsg.setHeader(COOKIE_HEADER, SESSION_ID + sessionId);
        response = Services.invokeNew(compileResult, TEST_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response);

        responseMsgPayload = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertNotNull(responseMsgPayload);
        Assert.assertTrue(responseMsgPayload.contains("failed to get session: " +
                "/counter is not an allowed path"));
    }

    @Test(description = "Test for incorrect Cookie")
    public void testCounterFunctionalityIncorrectCookie() {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/counter/echo", "GET");
        HTTPCarbonMessage response = Services.invokeNew(compileResult, TEST_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response);

        String responseMsgPayload = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertNotNull(responseMsgPayload);
        Assert.assertEquals(responseMsgPayload, "1");

        String sessionId = "FF97F7F8F7F87FA9FGS";

        cMsg = MessageUtils.generateHTTPMessage("/counter/echo", "GET");
        cMsg.setHeader(COOKIE_HEADER, SESSION_ID + sessionId);
        response = Services.invokeNew(compileResult, TEST_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response);

        responseMsgPayload = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertNotNull(responseMsgPayload);
        Assert.assertEquals(responseMsgPayload, "1");
    }

    @Test(description = "Test for string attribute")
    public void testHelloFunctionalityForStringOutput() {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/sample2/echoName", "GET");
        HTTPCarbonMessage response = Services.invokeNew(compileResult, TEST_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response);

        String responseMsgPayload = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertNotNull(responseMsgPayload);
        Assert.assertEquals(responseMsgPayload, "wso2");

        String cookie = response.getHeader(RESPONSE_COOKIE_HEADER);
        String sessionId = CookieUtils.getCookie(cookie).value;

        cMsg = MessageUtils.generateHTTPMessage("/sample2/echoName", "GET");
        cMsg.setHeader(COOKIE_HEADER, SESSION_ID + sessionId);
        response = Services.invokeNew(compileResult, TEST_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response);

        responseMsgPayload = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertNotNull(responseMsgPayload);
        Assert.assertEquals(responseMsgPayload, "chamil");
    }

    @Test(description = "Test for struct attribute")
    public void testSessionForStructAttribute() {
        List<Header> headers = new ArrayList<Header>();
        headers.add(new Header(HttpHeaderNames.CONTENT_TYPE.toString(), TEXT_PLAIN));
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/sample2/myStruct", "POST", headers, "wso2");
        HTTPCarbonMessage response = Services.invokeNew(compileResult, TEST_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response);

        String responseMsgPayload = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertNotNull(responseMsgPayload);
        Assert.assertEquals(responseMsgPayload, "wso2");

        String cookie = response.getHeader(RESPONSE_COOKIE_HEADER);
        String sessionId = CookieUtils.getCookie(cookie).value;

        cMsg = MessageUtils.generateHTTPMessage("/sample2/myStruct", "POST", headers, "chamil");
        cMsg.setHeader(COOKIE_HEADER, SESSION_ID + sessionId);
        response = Services.invokeNew(compileResult, TEST_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response);

        responseMsgPayload = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertNotNull(responseMsgPayload);
        Assert.assertEquals(responseMsgPayload, "wso2");
    }

    @Test(description = "Test for POST method string attribute")
    public void testPOSTForStringOutput() {
        List<Header> headers = new ArrayList<Header>();
        headers.add(new Header(HttpHeaderNames.CONTENT_TYPE.toString(), TEXT_PLAIN));
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/sample/hello", "POST", headers, "chamil");
        HTTPCarbonMessage response = Services.invokeNew(compileResult, TEST_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response);

        String responseMsgPayload = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertNotNull(responseMsgPayload);
        Assert.assertEquals(responseMsgPayload, "chamil");

        String cookie = response.getHeader(RESPONSE_COOKIE_HEADER);
        String sessionId = CookieUtils.getCookie(cookie).value;

        cMsg = MessageUtils.generateHTTPMessage("/sample/hello", "POST", headers, "wso2");
        cMsg.setHeader(COOKIE_HEADER, SESSION_ID + sessionId);
        response = Services.invokeNew(compileResult, TEST_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response);

        responseMsgPayload = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertNotNull(responseMsgPayload);
        Assert.assertEquals(responseMsgPayload, "chamil");
    }

    @Test(description = "Test for getAttributeNames function")
    public void testGetAttributeNamesFunction() {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/sample2/names", "GET");
        HTTPCarbonMessage response = Services.invokeNew(compileResult, TEST_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response);

        String responseMsgPayload = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertNotNull(responseMsgPayload);
        Assert.assertEquals(responseMsgPayload, "arraysize:2");
    }

    @Test(description = "Test for array elements get from getAttributeNames function")
    public void testGetAttributeNamesFunctionArrayelements() {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/sample2/names2", "GET");
        HTTPCarbonMessage response = Services.invokeNew(compileResult, TEST_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response);

        String responseMsgPayload = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertNotNull(responseMsgPayload);
        Assert.assertEquals(responseMsgPayload, "Counter");
    }

    @Test(description = "Test for null attributes from getAttributeNames function")
    public void testNullGetAttributeNamesFunction() {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/sample2/names5", "GET");
        HTTPCarbonMessage response = Services.invokeNew(compileResult, TEST_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response);

        String responseMsgPayload = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertNotNull(responseMsgPayload);
        Assert.assertEquals(responseMsgPayload, "0");
    }

    @Test(description = "Test for getAttributes function")
    public void testGetAttributesFunction() {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/sample2/map", "GET");
        HTTPCarbonMessage response = Services.invokeNew(compileResult, TEST_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response);

        String responseMsgPayload = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertNotNull(responseMsgPayload);
        Assert.assertEquals(responseMsgPayload, "Name:chamil");

        String cookie = response.getHeader(RESPONSE_COOKIE_HEADER);
        String sessionId = CookieUtils.getCookie(cookie).value;

        cMsg = MessageUtils.generateHTTPMessage("/sample2/map", "GET");
        cMsg.setHeader(COOKIE_HEADER, SESSION_ID + sessionId);
        cMsg.setHeader("counter", "1");
        response = Services.invokeNew(compileResult, TEST_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response);

        responseMsgPayload = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertNotNull(responseMsgPayload);
        Assert.assertEquals(responseMsgPayload, "Lang:ballerina");
    }

    @Test(description = "Test for null attribute map from getAttributes function")
    public void testNullGetAttributesFunction() {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/sample2/map2", "GET");
        HTTPCarbonMessage response = Services.invokeNew(compileResult, TEST_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response);

        String responseMsgPayload = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertNotNull(responseMsgPayload);
        Assert.assertEquals(responseMsgPayload, "value:map not present");
    }

    @Test(description = "Test for removeAttribute function")
    public void testRemoveAttributeFunction() {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/sample2/names3", "GET");
        HTTPCarbonMessage response = Services.invokeNew(compileResult, TEST_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response);

        String responseMsgPayload = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertNotNull(responseMsgPayload);
        Assert.assertEquals(responseMsgPayload, "3");
    }

    @Test(description = "Test for removeAttribute function for unavailable attributes")
    public void testRemoveAttributeFunctionForUnavailableAttribute() {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/sample2/names6", "GET");
        HTTPCarbonMessage response = Services.invokeNew(compileResult, TEST_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response);

        String responseMsgPayload = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertNotNull(responseMsgPayload);
        Assert.assertEquals(responseMsgPayload, "1");
    }

    @Test(description = "Test for invalidate function")
    public void testInvalidateFunction() {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/sample2/names4", "GET");
        HTTPCarbonMessage response = Services.invokeNew(compileResult, TEST_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response);

        String responseMsgPayload = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertNotNull(responseMsgPayload);
        Assert.assertTrue(responseMsgPayload.contains("No such session in progress"));
    }

    @Test(description = "Test for null session with getsession")
    public void testNullSessionWithGetSession() {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/sample/test5", "GET");
        HTTPCarbonMessage response = Services.invokeNew(compileResult, TEST_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response);

        String responseMsgPayload = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertNotNull(responseMsgPayload);
        Assert.assertEquals(responseMsgPayload, "testValue");
    }
}
