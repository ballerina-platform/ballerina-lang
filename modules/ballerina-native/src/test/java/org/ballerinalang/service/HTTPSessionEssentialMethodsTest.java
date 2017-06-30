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

package org.ballerinalang.service;

import org.ballerinalang.runtime.message.StringDataSource;
import org.ballerinalang.testutils.EnvironmentInitializer;
import org.ballerinalang.testutils.MessageUtils;
import org.ballerinalang.testutils.Services;
import org.ballerinalang.util.codegen.ProgramFile;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.messaging.CarbonMessage;

import static org.ballerinalang.services.dispatchers.http.Constants.COOKIE_HEADER;
import static org.ballerinalang.services.dispatchers.http.Constants.RESPONSE_COOKIE_HEADER;
import static org.ballerinalang.services.dispatchers.http.Constants.SESSION_ID;

/**
 * HTTP session Essential Methods Test Class
 */
public class HTTPSessionEssentialMethodsTest {

    ProgramFile programFile;

    @BeforeClass
    public void setup() {
        programFile = EnvironmentInitializer.setupProgramFile("lang/service/httpSessionTest.bal");
    }

    @Test(description = "Test for getting a session at first time")
    public void testGetSessionWithoutSessionCookie() {
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/sample/test1", "GET");
        CarbonMessage response = Services.invoke(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), "session created");
    }

    @Test(description = "Test for getting a session without Id at first time")
    public void testGetSessionWithoutSessionIDCheck() {
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/sample/test2", "GET");
        CarbonMessage response = Services.invoke(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), "no session id available");
    }

    @Test(description = "Test for not getting a session with at first time")
    public void testGetSessionMethod() {
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/sample/test3", "GET");
        CarbonMessage response = Services.invoke(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), "session is not created");
    }

    @Test(description = "Test for create two sessions ")
    public void testCreateTwoSessionsMethod() {
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/sample/test6", "GET");
        CarbonMessage response = Services.invoke(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), "wso2");
    }

    @Test(description = "Test getting a session with at first time")
    public void testGetSessionHappyPathMethod() {
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/sample/test1", "GET");
        CarbonMessage response = Services.invoke(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), "session created");

        String cookie = response.getHeader(RESPONSE_COOKIE_HEADER);
        String sessionId = cookie.substring(SESSION_ID.length(), cookie.length() - 15);

        cMsg = MessageUtils.generateHTTPMessage("/sample/test2", "GET");
        cMsg.setHeader(COOKIE_HEADER, SESSION_ID + sessionId);
        response = Services.invoke(cMsg);
        Assert.assertNotNull(response);

        stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), "session is returned");
    }


    @Test(description = "Test for Cookie Session Header availability check")
    public void testCookieSessionHeader() {
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/sample/test1", "GET");
        CarbonMessage response = Services.invoke(cMsg);
        Assert.assertNotNull(response);

        String cookie = response.getHeader(RESPONSE_COOKIE_HEADER);
        Assert.assertNotNull(cookie);
        String sessionHeader = cookie.substring(0, SESSION_ID.length());
        Assert.assertEquals(sessionHeader, SESSION_ID);

    }

    @Test(description = "Test for Cookie Session path availability check")
    public void testCookiePath() {
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/sample/test1", "GET");
        CarbonMessage response = Services.invoke(cMsg);
        Assert.assertNotNull(response);

        String cookie = response.getHeader(RESPONSE_COOKIE_HEADER);
        Assert.assertNotNull(cookie);
        String path = cookie.substring(SESSION_ID.length() + 40, cookie.length() - 1);
        Assert.assertEquals(path, "sample");

    }

    @Test(description = "Test for Get Attribute Function")
    public void testGetAttributeFunction() {
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/sample/test4", "GET");
        CarbonMessage response = Services.invoke(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), "attribute not available");

    }

    @Test(description = "Test for sample Session functionality")
    public void testCounterFunctionality() {
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/counter/echo", "GET");
        CarbonMessage response = Services.invoke(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), "1");

        String cookie = response.getHeader(RESPONSE_COOKIE_HEADER);
        String sessionId = cookie.substring(SESSION_ID.length(), cookie.length() - 16);

        cMsg = MessageUtils.generateHTTPMessage("/counter/echo", "GET");
        cMsg.setHeader(COOKIE_HEADER, SESSION_ID + sessionId);
        response = Services.invoke(cMsg);
        Assert.assertNotNull(response);

        stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), "2");
    }

    @Test(description = "Test for path limitation per service")
    public void testCheckPathValidityPerService() {
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/sample2/names", "GET");
        CarbonMessage response = Services.invoke(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), "arraysize:2");

        String cookie = response.getHeader(RESPONSE_COOKIE_HEADER);
        String sessionId = cookie.substring(SESSION_ID.length(), cookie.length() - 16);

        cMsg = MessageUtils.generateHTTPMessage("/sample2/names3", "GET");
        cMsg.setHeader(COOKIE_HEADER, SESSION_ID + sessionId);
        response = Services.invoke(cMsg);
        Assert.assertNotNull(response);

        stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), "4");
    }

    @Test(description = "Test for path limitation")
    public void testCheckPathValidity() {
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/counter/echo", "GET");
        CarbonMessage response = Services.invoke(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), "1");

        String cookie = response.getHeader(RESPONSE_COOKIE_HEADER);
        String sessionId = cookie.substring(SESSION_ID.length(), cookie.length() - 16);

        cMsg = MessageUtils.generateHTTPMessage("/sample2/echoName", "GET");
        cMsg.setHeader(COOKIE_HEADER, SESSION_ID + sessionId);
        response = Services.invoke(cMsg);
        Assert.assertNotNull(response);

        stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        String error = stringDataSource.getValue().substring(0, 92);
        Assert.assertEquals(error, "ballerina.lang.errors:Error, message: failed to get session: " +
                "/sample2 is not an allowed path");
    }

    @Test(description = "Test for path limitation with getSession")
    public void testCheckPathValiditygetSession() {
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/sample2/echoName", "GET");
        CarbonMessage response = Services.invoke(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), "wso2");

        String cookie = response.getHeader(RESPONSE_COOKIE_HEADER);
        String sessionId = cookie.substring(SESSION_ID.length(), cookie.length() - 16);

        cMsg = MessageUtils.generateHTTPMessage("/counter/echo2", "GET");
        cMsg.setHeader(COOKIE_HEADER, SESSION_ID + sessionId);
        response = Services.invoke(cMsg);
        Assert.assertNotNull(response);

        stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        String error = stringDataSource.getValue().substring(0, 92);
        Assert.assertEquals(error, "ballerina.lang.errors:Error, message: failed to get session: " +
                "/counter is not an allowed path");
    }

    @Test(description = "Test for incorrect Cookie")
    public void testCounterFunctionalityIncorrectCookie() {
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/counter/echo", "GET");
        CarbonMessage response = Services.invoke(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), "1");

        String sessionId = "FF97F7F8F7F87FA9FGS";

        cMsg = MessageUtils.generateHTTPMessage("/counter/echo", "GET");
        cMsg.setHeader(COOKIE_HEADER, SESSION_ID + sessionId);
        response = Services.invoke(cMsg);
        Assert.assertNotNull(response);

        stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), "1");
    }

    @Test(description = "Test for string attribute")
    public void testHelloFunctionalityForStringOutput() {
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/sample2/echoName", "GET");
        CarbonMessage response = Services.invoke(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), "wso2");

        String cookie = response.getHeader(RESPONSE_COOKIE_HEADER);
        String sessionId = cookie.substring(SESSION_ID.length(), cookie.length() - 14);

        cMsg = MessageUtils.generateHTTPMessage("/sample2/echoName", "GET");
        cMsg.setHeader(COOKIE_HEADER, SESSION_ID + sessionId);
        response = Services.invoke(cMsg);
        Assert.assertNotNull(response);

        stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), "chamil");
    }

    @Test(description = "Test for string attribute")
    public void testSessionForStructAttribute() {
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/sample2/myStruct", "POST", "wso2");
        CarbonMessage response = Services.invoke(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), "wso2");

        String cookie = response.getHeader(RESPONSE_COOKIE_HEADER);
        String sessionId = cookie.substring(SESSION_ID.length(), cookie.length() - 14);

        cMsg = MessageUtils.generateHTTPMessage("/sample2/myStruct", "POST", "chamil");
        cMsg.setHeader(COOKIE_HEADER, SESSION_ID + sessionId);
        response = Services.invoke(cMsg);
        Assert.assertNotNull(response);

        stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), "wso2");
    }

    @Test(description = "Test for POST method string attribute")
    public void testPOSTForStringOutput() {
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/sample/hello", "POST", "chamil");
        CarbonMessage response = Services.invoke(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), "chamil");

        String cookie = response.getHeader(RESPONSE_COOKIE_HEADER);
        String sessionId = cookie.substring(SESSION_ID.length(), cookie.length() - 14);

        cMsg = MessageUtils.generateHTTPMessage("/sample/hello", "POST", "wso2");
        cMsg.setHeader(COOKIE_HEADER, SESSION_ID + sessionId);
        response = Services.invoke(cMsg);
        Assert.assertNotNull(response);

        stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), "chamil");
    }

    @Test(description = "Test for getAttributeNames function")
    public void testGetAttributeNamesFunction() {
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/sample2/names", "GET");
        CarbonMessage response = Services.invoke(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), "arraysize:2");
    }

    @Test(description = "Test for array elements get from getAttributeNames function")
    public void testGetAttributeNamesFunctionArrayelements() {
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/sample2/names2", "GET");
        CarbonMessage response = Services.invoke(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), "Counter");
    }

    @Test(description = "Test for null attributes from getAttributeNames function")
    public void testNullGetAttributeNamesFunction() {
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/sample2/names5", "GET");
        CarbonMessage response = Services.invoke(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), "0");
    }

    @Test(description = "Test for removeAttribute function")
    public void testRemoveAttributeFunction() {
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/sample2/names3", "GET");
        CarbonMessage response = Services.invoke(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), "3");
    }

    @Test(description = "Test for removeAttribute function for unavailable attributes")
    public void testRemoveAttributeFunctionForUnavailableAttribute() {
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/sample2/names6", "GET");
        CarbonMessage response = Services.invoke(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), "1");
    }

    @Test(description = "Test for invalidate function")
    public void testInvalidateFunction() {
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/sample2/names4", "GET");
        CarbonMessage response = Services.invoke(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        String error = stringDataSource.getValue().substring(38, 96);
        Assert.assertEquals(error, "failed to get attribute names: No such session in progress");
    }

    @Test(description = "Test for null session with getsession")
    public void testNullSessionWithGetSession() {
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/sample/test5", "GET");
        CarbonMessage response = Services.invoke(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        String error = stringDataSource.getValue().substring(0, 56);
        Assert.assertEquals(error, "ballerina.lang.errors:Error, message: argument 0 is null");
    }

    @AfterClass
    public void tearDown() {
        EnvironmentInitializer.cleanup(programFile);
    }
}
