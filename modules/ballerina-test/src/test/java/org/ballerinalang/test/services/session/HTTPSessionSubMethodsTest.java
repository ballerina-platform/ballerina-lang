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
 * HTTP session sub Methods Test Class.
 */
public class HTTPSessionSubMethodsTest {

    CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = EnvironmentInitializer
                .setupProgramFile("test-src/services/session/httpSessionTest.bal");
    }

    @Test(description = "Test for GetId Function")
    public void testGetIdFunction() {
        HTTPCarbonMessage cMsg = MessageUtils.generateHTTPMessage("/sample2/id1", "GET");
        HTTPCarbonMessage response = Services.invokeNew(cMsg);
        Assert.assertNotNull(response);

        String cookie = response.getHeader(RESPONSE_COOKIE_HEADER);
        String sessionId = cookie.substring(SESSION_ID.length(), cookie.length() - 16);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), sessionId);
    }

    @Test(description = "Test for null session GetId Function")
    public void testNullSessionGetIdFunction() {
        HTTPCarbonMessage cMsg = MessageUtils.generateHTTPMessage("/sample2/id2", "GET");
        HTTPCarbonMessage response = Services.invokeNew(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        String error = stringDataSource.getValue().substring(0, 56);
        Assert.assertTrue(error.contains("message: argument 0 is null"));
    }

    @Test(description = "Test for isNew Function")
    public void testIsNewFunction() {
        HTTPCarbonMessage cMsg = MessageUtils.generateHTTPMessage("/sample2/new1", "GET");
        HTTPCarbonMessage response = Services.invokeNew(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), "true");

    }

    @Test(description = "Test for isNew Function two attempts")
    public void testIsNewFunctiontwoAttempts() {
        HTTPCarbonMessage cMsg = MessageUtils.generateHTTPMessage("/sample2/new1", "GET");
        HTTPCarbonMessage response = Services.invokeNew(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), "true");

        String cookie = response.getHeader(RESPONSE_COOKIE_HEADER);
        String sessionId = cookie.substring(SESSION_ID.length(), cookie.length() - 14);

        cMsg = MessageUtils.generateHTTPMessage("/sample2/new1", "GET");
        cMsg.setHeader(COOKIE_HEADER, SESSION_ID + sessionId);
        response = Services.invokeNew(cMsg);
        Assert.assertNotNull(response);

        stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), "false");
    }

    @Test(description = "Test for GetCreateTime Function")
    public void testGetCreateTimeFunction() {
        HTTPCarbonMessage cMsg = MessageUtils.generateHTTPMessage("/sample2/new2", "GET");
        HTTPCarbonMessage response = Services.invokeNew(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        long firstAccess = Long.parseLong(stringDataSource.getValue().toString());


        String cookie = response.getHeader(RESPONSE_COOKIE_HEADER);
        String sessionId = cookie.substring(SESSION_ID.length(), cookie.length() - 14);

        cMsg = MessageUtils.generateHTTPMessage("/sample2/new2", "GET");
        cMsg.setHeader(COOKIE_HEADER, SESSION_ID + sessionId);
        response = Services.invokeNew(cMsg);
        Assert.assertNotNull(response);

        stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        long secondAccess = Long.parseLong(stringDataSource.getValue());
        Assert.assertTrue(firstAccess == secondAccess);
    }

    @Test(description = "Test for GetCreateTime Function error")
    public void testGetCreateTimeFunctionError() {
        HTTPCarbonMessage cMsg = MessageUtils.generateHTTPMessage("/sample2/new3", "GET");
        HTTPCarbonMessage response = Services.invokeNew(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        String error = stringDataSource.getValue().substring(38, 94);
        Assert.assertTrue(error.contains("No such session in progress"));
    }

    @Test(description = "Test for GetLastAccessedTime Function")
    public void testGetLastAccessedTimeFunction() {
        HTTPCarbonMessage cMsg = MessageUtils.generateHTTPMessage("/sample2/new4", "GET");
        HTTPCarbonMessage response = Services.invokeNew(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        long firstAccess = Long.parseLong(stringDataSource.getValue().toString());


        String cookie = response.getHeader(RESPONSE_COOKIE_HEADER);
        String sessionId = cookie.substring(SESSION_ID.length(), cookie.length() - 14);

        cMsg = MessageUtils.generateHTTPMessage("/sample2/new4", "GET");
        cMsg.setHeader(COOKIE_HEADER, SESSION_ID + sessionId);
        response = Services.invokeNew(cMsg);
        Assert.assertNotNull(response);

        stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        long secondAccess = Long.parseLong(stringDataSource.getValue());
        Assert.assertTrue(firstAccess <= secondAccess);
    }

    @Test(description = "Test for GetLastAccessed Function error")
    public void testGetLastAccessedTimeFunctionError() {
        HTTPCarbonMessage cMsg = MessageUtils.generateHTTPMessage("/sample2/new5", "GET");
        HTTPCarbonMessage response = Services.invokeNew(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        String error = stringDataSource.getValue().substring(38, 99);
        Assert.assertTrue(error.contains("No such session in progress"));
    }

    @Test(description = "Test for setMaxInactiveInterval and getMaxInactiveInterval Function")
    public void testSetMaxInactiveIntervalFunction() {
        HTTPCarbonMessage cMsg = MessageUtils.generateHTTPMessage("/sample2/new6", "GET");
        HTTPCarbonMessage response = Services.invokeNew(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        long timeInterval = Long.parseLong(stringDataSource.getValue().toString());
        Assert.assertEquals(timeInterval, 900);

        String cookie = response.getHeader(RESPONSE_COOKIE_HEADER);
        String sessionId = cookie.substring(SESSION_ID.length(), cookie.length() - 14);

        cMsg = MessageUtils.generateHTTPMessage("/sample2/new6", "GET");
        cMsg.setHeader(COOKIE_HEADER, SESSION_ID + sessionId);
        response = Services.invokeNew(cMsg);
        Assert.assertNotNull(response);

        stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        long timeInterval2 = Long.parseLong(stringDataSource.getValue());
        Assert.assertEquals(timeInterval2, 60);
    }

    @Test(description = "Test for SetMaxInactiveInterval Function error")
    public void testSetMaxInactiveIntervalFunctionError() {
        HTTPCarbonMessage cMsg = MessageUtils.generateHTTPMessage("/sample2/new7", "GET");
        HTTPCarbonMessage response = Services.invokeNew(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        String error = stringDataSource.getValue().substring(38, 98);
        Assert.assertTrue(error.contains("No such session in progress"));
    }

    @Test(description = "Test for negative timeout setMaxInactiveInterval")
    public void testSetMaxInactiveIntervalNegativeTimeoutFunction() {
        HTTPCarbonMessage cMsg = MessageUtils.generateHTTPMessage("/sample2/new8", "GET");
        HTTPCarbonMessage response = Services.invokeNew(cMsg);
        Assert.assertNotNull(response);

        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        long timeInterval = Long.parseLong(stringDataSource.getValue().toString());
        Assert.assertEquals(timeInterval, 900);

        String cookie = response.getHeader(RESPONSE_COOKIE_HEADER);
        String sessionId = cookie.substring(SESSION_ID.length(), cookie.length() - 14);

        cMsg = MessageUtils.generateHTTPMessage("/sample2/new8", "GET");
        cMsg.setHeader(COOKIE_HEADER, SESSION_ID + sessionId);
        response = Services.invokeNew(cMsg);
        Assert.assertNotNull(response);

        stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        long timeInterval2 = Long.parseLong(stringDataSource.getValue().toString());
        Assert.assertEquals(timeInterval2, -1);
    }

    @AfterClass
    public void tearDown() {
        EnvironmentInitializer.cleanup(compileResult);
    }
}
