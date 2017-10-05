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

import org.ballerinalang.runtime.message.StringDataSource;
import org.ballerinalang.test.services.testutils.EnvironmentInitializer;
import org.ballerinalang.test.services.testutils.MessageUtils;
import org.ballerinalang.test.services.testutils.Services;
import org.ballerinalang.test.utils.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;

import static org.ballerinalang.net.http.Constants.COOKIE_HEADER;
import static org.ballerinalang.net.http.Constants.RESPONSE_COOKIE_HEADER;
import static org.ballerinalang.net.http.Constants.SESSION_ID;

/**
 * HTTP session Essential Methods Test Class.
 */
public class HTTPSessionEssentialMethodsTest {

    CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = EnvironmentInitializer
                .setupProgramFile("test-src/services/session/httpSessionTest.bal");
    }

    @Test(description = "Test for getting a session at first time")
    public void testGetSessionWithoutSessionCookie() {
        HTTPCarbonMessage cMsg = MessageUtils.generateHTTPMessage("/sample/test1", "GET");
        HTTPCarbonMessage response = Services.invokeNew(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), "session created");
    }

    @Test(description = "Test for getting a session without Id at first time")
    public void testGetSessionWithoutSessionIDCheck() {
        HTTPCarbonMessage cMsg = MessageUtils.generateHTTPMessage("/sample/test2", "GET");
        HTTPCarbonMessage response = Services.invokeNew(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), "no session id available");
    }

    @Test(description = "Test for not getting a session with at first time")
    public void testGetSessionMethod() {
        HTTPCarbonMessage cMsg = MessageUtils.generateHTTPMessage("/sample/test3", "GET");
        HTTPCarbonMessage response = Services.invokeNew(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), "session is not created");
    }

    @Test(description = "Test for create two sessions ")
    public void testCreateTwoSessionsMethod() {
        HTTPCarbonMessage cMsg = MessageUtils.generateHTTPMessage("/sample/test6", "GET");
        HTTPCarbonMessage response = Services.invokeNew(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), "wso2");
    }

    @Test(description = "Test getting a session with at first time")
    public void testGetSessionHappyPathMethod() {
        HTTPCarbonMessage cMsg = MessageUtils.generateHTTPMessage("/sample/test1", "GET");
        HTTPCarbonMessage response = Services.invokeNew(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), "session created");

        String cookie = response.getHeader(RESPONSE_COOKIE_HEADER);
        String sessionId = cookie.substring(SESSION_ID.length(), cookie.length() - 15);

        cMsg = MessageUtils.generateHTTPMessage("/sample/test2", "GET");
        cMsg.setHeader(COOKIE_HEADER, SESSION_ID + sessionId);
        response = Services.invokeNew(cMsg);
        Assert.assertNotNull(response);

        stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), "session is returned");
    }


    @Test(description = "Test for Cookie Session Header availability check")
    public void testCookieSessionHeader() {
        HTTPCarbonMessage cMsg = MessageUtils.generateHTTPMessage("/sample/test1", "GET");
        HTTPCarbonMessage response = Services.invokeNew(cMsg);
        Assert.assertNotNull(response);

        String cookie = response.getHeader(RESPONSE_COOKIE_HEADER);
        Assert.assertNotNull(cookie);
        String sessionHeader = cookie.substring(0, SESSION_ID.length());
        Assert.assertEquals(sessionHeader, SESSION_ID);

    }

    @Test(description = "Test for Cookie Session path availability check")
    public void testCookiePath() {
        HTTPCarbonMessage cMsg = MessageUtils.generateHTTPMessage("/sample/test1", "GET");
        HTTPCarbonMessage response = Services.invokeNew(cMsg);
        Assert.assertNotNull(response);

        String cookie = response.getHeader(RESPONSE_COOKIE_HEADER);
        Assert.assertNotNull(cookie);
        String path = cookie.substring(SESSION_ID.length() + 40, cookie.length() - 1);
        Assert.assertEquals(path, "sample");

    }

    @Test(description = "Test for Get Attribute Function")
    public void testGetAttributeFunction() {
        HTTPCarbonMessage cMsg = MessageUtils.generateHTTPMessage("/sample/test4", "GET");
        HTTPCarbonMessage response = Services.invokeNew(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), "attribute not available");

    }

    @Test(description = "Test for sample Session functionality")
    public void testCounterFunctionality() {
        HTTPCarbonMessage cMsg = MessageUtils.generateHTTPMessage("/counter/echo", "GET");
        HTTPCarbonMessage response = Services.invokeNew(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), "1");

        String cookie = response.getHeader(RESPONSE_COOKIE_HEADER);
        String sessionId = cookie.substring(SESSION_ID.length(), cookie.length() - 16);

        cMsg = MessageUtils.generateHTTPMessage("/counter/echo", "GET");
        cMsg.setHeader(COOKIE_HEADER, SESSION_ID + sessionId);
        response = Services.invokeNew(cMsg);
        Assert.assertNotNull(response);

        stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), "2");
    }

    @Test(description = "Test for path limitation per service")
    public void testCheckPathValidityPerService() {
        HTTPCarbonMessage cMsg = MessageUtils.generateHTTPMessage("/sample2/names", "GET");
        HTTPCarbonMessage response = Services.invokeNew(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), "arraysize:2");

        String cookie = response.getHeader(RESPONSE_COOKIE_HEADER);
        String sessionId = cookie.substring(SESSION_ID.length(), cookie.length() - 16);

        cMsg = MessageUtils.generateHTTPMessage("/sample2/names3", "GET");
        cMsg.setHeader(COOKIE_HEADER, SESSION_ID + sessionId);
        response = Services.invokeNew(cMsg);
        Assert.assertNotNull(response);

        stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), "4");
    }

    @Test(description = "Test for path limitation")
    public void testCheckPathValidity() {
        HTTPCarbonMessage cMsg = MessageUtils.generateHTTPMessage("/counter/echo", "GET");
        HTTPCarbonMessage response = Services.invokeNew(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), "1");

        String cookie = response.getHeader(RESPONSE_COOKIE_HEADER);
        String sessionId = cookie.substring(SESSION_ID.length(), cookie.length() - 16);

        cMsg = MessageUtils.generateHTTPMessage("/sample2/echoName", "GET");
        cMsg.setHeader(COOKIE_HEADER, SESSION_ID + sessionId);
        response = Services.invokeNew(cMsg);
        Assert.assertNotNull(response);

        stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        String error = stringDataSource.getValue().substring(0, 92);
        Assert.assertEquals(error, "error, message: failed to get session: /sample2 is not an allowed path\n"
                + "\tat ballerina.net.htt");
    }

    @Test(description = "Test for path limitation with getSession")
    public void testCheckPathValiditygetSession() {
        HTTPCarbonMessage cMsg = MessageUtils.generateHTTPMessage("/sample2/echoName", "GET");
        HTTPCarbonMessage response = Services.invokeNew(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), "wso2");

        String cookie = response.getHeader(RESPONSE_COOKIE_HEADER);
        String sessionId = cookie.substring(SESSION_ID.length(), cookie.length() - 16);

        cMsg = MessageUtils.generateHTTPMessage("/counter/echo2", "GET");
        cMsg.setHeader(COOKIE_HEADER, SESSION_ID + sessionId);
        response = Services.invokeNew(cMsg);
        Assert.assertNotNull(response);

        stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        String error = stringDataSource.getValue().substring(0, 70);
        Assert.assertEquals(error, "error, message: failed to get session: " +
                "/counter is not an allowed path");
    }

    @Test(description = "Test for incorrect Cookie")
    public void testCounterFunctionalityIncorrectCookie() {
        HTTPCarbonMessage cMsg = MessageUtils.generateHTTPMessage("/counter/echo", "GET");
        HTTPCarbonMessage response = Services.invokeNew(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), "1");

        String sessionId = "FF97F7F8F7F87FA9FGS";

        cMsg = MessageUtils.generateHTTPMessage("/counter/echo", "GET");
        cMsg.setHeader(COOKIE_HEADER, SESSION_ID + sessionId);
        response = Services.invokeNew(cMsg);
        Assert.assertNotNull(response);

        stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), "1");
    }

    @Test(description = "Test for string attribute")
    public void testHelloFunctionalityForStringOutput() {
        HTTPCarbonMessage cMsg = MessageUtils.generateHTTPMessage("/sample2/echoName", "GET");
        HTTPCarbonMessage response = Services.invokeNew(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), "wso2");

        String cookie = response.getHeader(RESPONSE_COOKIE_HEADER);
        String sessionId = cookie.substring(SESSION_ID.length(), cookie.length() - 14);

        cMsg = MessageUtils.generateHTTPMessage("/sample2/echoName", "GET");
        cMsg.setHeader(COOKIE_HEADER, SESSION_ID + sessionId);
        response = Services.invokeNew(cMsg);
        Assert.assertNotNull(response);

        stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), "chamil");
    }

    @Test(description = "Test for string attribute", enabled = false)
    public void testSessionForStructAttribute() {
        HTTPCarbonMessage cMsg = MessageUtils.generateHTTPMessage("/sample2/myStruct", "POST", "wso2");
        HTTPCarbonMessage response = Services.invokeNew(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), "wso2");

        String cookie = response.getHeader(RESPONSE_COOKIE_HEADER);
        String sessionId = cookie.substring(SESSION_ID.length(), cookie.length() - 14);

        cMsg = MessageUtils.generateHTTPMessage("/sample2/myStruct", "POST", "chamil");
        cMsg.setHeader(COOKIE_HEADER, SESSION_ID + sessionId);
        response = Services.invokeNew(cMsg);
        Assert.assertNotNull(response);

        stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), "wso2");
    }

    @Test(description = "Test for POST method string attribute")
    public void testPOSTForStringOutput() {
        HTTPCarbonMessage cMsg = MessageUtils.generateHTTPMessage("/sample/hello", "POST", "chamil");
        HTTPCarbonMessage response = Services.invokeNew(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), "chamil");

        String cookie = response.getHeader(RESPONSE_COOKIE_HEADER);
        String sessionId = cookie.substring(SESSION_ID.length(), cookie.length() - 14);

        cMsg = MessageUtils.generateHTTPMessage("/sample/hello", "POST", "wso2");
        cMsg.setHeader(COOKIE_HEADER, SESSION_ID + sessionId);
        response = Services.invokeNew(cMsg);
        Assert.assertNotNull(response);

        stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), "chamil");
    }

    @Test(description = "Test for getAttributeNames function")
    public void testGetAttributeNamesFunction() {
        HTTPCarbonMessage cMsg = MessageUtils.generateHTTPMessage("/sample2/names", "GET");
        HTTPCarbonMessage response = Services.invokeNew(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), "arraysize:2");
    }

    @Test(description = "Test for array elements get from getAttributeNames function")
    public void testGetAttributeNamesFunctionArrayelements() {
        HTTPCarbonMessage cMsg = MessageUtils.generateHTTPMessage("/sample2/names2", "GET");
        HTTPCarbonMessage response = Services.invokeNew(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), "Counter");
    }

    @Test(description = "Test for null attributes from getAttributeNames function")
    public void testNullGetAttributeNamesFunction() {
        HTTPCarbonMessage cMsg = MessageUtils.generateHTTPMessage("/sample2/names5", "GET");
        HTTPCarbonMessage response = Services.invokeNew(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), "0");
    }

    @Test(description = "Test for removeAttribute function")
    public void testRemoveAttributeFunction() {
        HTTPCarbonMessage cMsg = MessageUtils.generateHTTPMessage("/sample2/names3", "GET");
        HTTPCarbonMessage response = Services.invokeNew(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), "3");
    }

    @Test(description = "Test for removeAttribute function for unavailable attributes")
    public void testRemoveAttributeFunctionForUnavailableAttribute() {
        HTTPCarbonMessage cMsg = MessageUtils.generateHTTPMessage("/sample2/names6", "GET");
        HTTPCarbonMessage response = Services.invokeNew(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), "1");
    }

    @Test(description = "Test for invalidate function")
    public void testInvalidateFunction() {
        HTTPCarbonMessage cMsg = MessageUtils.generateHTTPMessage("/sample2/names4", "GET");
        HTTPCarbonMessage response = Services.invokeNew(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        String error = stringDataSource.getValue().substring(47, 74);
        Assert.assertEquals(error, "No such session in progress");
    }

    @Test(description = "Test for null session with getsession")
    public void testNullSessionWithGetSession() {
        HTTPCarbonMessage cMsg = MessageUtils.generateHTTPMessage("/sample/test5", "GET");
        HTTPCarbonMessage response = Services.invokeNew(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        String error = stringDataSource.getValue().substring(0, 34);
        Assert.assertEquals(error, "error, message: argument 0 is null");
    }

    @AfterClass
    public void tearDown() {
        EnvironmentInitializer.cleanup(compileResult);
    }
}
