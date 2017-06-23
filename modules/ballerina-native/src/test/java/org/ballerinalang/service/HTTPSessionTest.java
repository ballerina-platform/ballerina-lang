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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.messaging.CarbonMessage;

import static org.ballerinalang.services.dispatchers.http.Constants.COOKIE_HEADER;
import static org.ballerinalang.services.dispatchers.http.Constants.RESPONSE_COOKIE_HEADER;
import static org.ballerinalang.services.dispatchers.http.Constants.SESSION_ID;

/**
 * HTTP session Test Class
 */
public class HTTPSessionTest {

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

    @Test(description = "Test for getting a session with param at first time")
    public void testGetSessionWithParamMethodCheck() {
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/sample/test2", "GET");
        CarbonMessage response = Services.invoke(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), "session created");
    }

    @Test(description = "Test for not getting a session with at first time")
    public void testGetSessionWithParamMethod() {
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/sample/test3", "GET");
        CarbonMessage response = Services.invoke(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), "session is not created");
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
        String error = stringDataSource.getValue().substring(0, 69);
        Assert.assertEquals(error, "ballerina.lang.errors:Error, message: /sample2 is not an allowed path");
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
}
