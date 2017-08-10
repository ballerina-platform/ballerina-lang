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

import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.testutils.EnvironmentInitializer;
import org.ballerinalang.testutils.MessageUtils;
import org.ballerinalang.testutils.Services;
import org.ballerinalang.util.codegen.ProgramFile;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.messaging.CarbonMessage;

import static org.ballerinalang.services.dispatchers.http.Constants.AC_ALLOW_CREDENTIALS;
import static org.ballerinalang.services.dispatchers.http.Constants.AC_ALLOW_ORIGIN;
import static org.ballerinalang.services.dispatchers.http.Constants.AC_EXPOSE_HEADERS;

/**
 * Test cases related to HTTP CORS
 */
public class HTTPCorsTest {

    ProgramFile programFile;

    @BeforeClass
    public void setup() {
        programFile = EnvironmentInitializer.setupProgramFile("lang/service/corsTest.bal");
    }

    @Test(description = "Test for CORS override at two levels")
    public void testSimpleReqServiceResourceCorsOverride() {
        String path = "/hello1/test1";
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage(path, "POST", "Hello there");
        cMsg.setHeader("Origin", "http://www.wso2.com");
        CarbonMessage response = Services.invoke(cMsg);

        Assert.assertNotNull(response);
        BJSON bJson = ((BJSON) response.getMessageDataSource());
        Assert.assertEquals(bJson.value().get("echo").asText(), "resCors"
                , "Resource dispatched to wrong template");
        String origin = response.getHeader(AC_ALLOW_ORIGIN);
        String cred = response.getHeader(AC_ALLOW_CREDENTIALS);
        Assert.assertEquals(origin, "http://www.wso2.com");
        Assert.assertEquals(cred, "true");
    }

    @Test(description = "Test for service CORS")
    public void testSimpleReqServiceCors() {
        String path = "/hello1/test2";
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        cMsg.setHeader("Origin", "http://www.hello.com");
        CarbonMessage response = Services.invoke(cMsg);

        Assert.assertNotNull(response);
        BJSON bJson = ((BJSON) response.getMessageDataSource());
        Assert.assertEquals(bJson.value().get("echo").asText(), "serCors"
                , "Resource dispatched to wrong template");
        String origin = response.getHeader(AC_ALLOW_ORIGIN);
        String cred = response.getHeader(AC_ALLOW_CREDENTIALS);
        Assert.assertEquals(origin, "http://www.hello.com");
        Assert.assertEquals(cred, "true");
    }

    @Test(description = "Test for resource only CORS")
    public void testSimpleReqResourceOnlyCors() {
        String path = "/hello2/test1";
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage(path, "POST", "hello");
        cMsg.setHeader("Origin", "http://www.hello.com");
        CarbonMessage response = Services.invoke(cMsg);

        Assert.assertNotNull(response);
        BJSON bJson = ((BJSON) response.getMessageDataSource());
        Assert.assertEquals(bJson.value().get("echo").asText(), "resOnlyCors"
                , "Resource dispatched to wrong template");
        String origin = response.getHeader(AC_ALLOW_ORIGIN);
        String cred = response.getHeader(AC_ALLOW_CREDENTIALS);
        String expoHead = response.getHeader(AC_EXPOSE_HEADERS);
        Assert.assertEquals(origin, "*");
        Assert.assertEquals(cred, "false");
        Assert.assertEquals(expoHead, "X-Content-Type-Options, X-PINGARUNER");
    }

    @Test(description = "Test for simple request with multiple origins")
    public void testSimpleReqMultipleOrigins() {
        String path = "/hello1/test3";
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage(path, "POST", "Hello there");
        cMsg.setHeader("Origin", "http://www.wso2.com http://www.amazon.com");
        CarbonMessage response = Services.invoke(cMsg);

        Assert.assertNotNull(response);
        BJSON bJson = ((BJSON) response.getMessageDataSource());
        Assert.assertEquals(bJson.value().get("echo").asText(), "moreOrigins"
                , "Resource dispatched to wrong template");
        String origin = response.getHeader(AC_ALLOW_ORIGIN);
        String cred = response.getHeader(AC_ALLOW_CREDENTIALS);
        Assert.assertEquals(origin, "http://www.wso2.com http://www.amazon.com");
        Assert.assertEquals(cred, "true");
    }

    @Test(description = "Test for simple request invalid origins")
    public void testSimpleReqInvalidOrigin() {
        String path = "/hello1/test1";
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage(path, "POST", "Hello there");
        cMsg.setHeader("Origin", "www.wso2.com");
        CarbonMessage response = Services.invoke(cMsg);

        Assert.assertNotNull(response);
        BJSON bJson = ((BJSON) response.getMessageDataSource());
        Assert.assertEquals(bJson.value().get("echo").asText(), "resCors"
                , "Resource dispatched to wrong template");
        String origin = response.getHeader(AC_ALLOW_ORIGIN);
        String cred = response.getHeader(AC_ALLOW_CREDENTIALS);
        Assert.assertNull(origin);
        Assert.assertNull(cred);
    }

    @Test(description = "Test for values with extra white spaces")
    public void testSimpleReqwithExtraWS() {
        String path = "/hello2/test1";
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage(path, "POST", "hello");
        cMsg.setHeader("Origin", "http://www.facebook.com");
        CarbonMessage response = Services.invoke(cMsg);

        Assert.assertNotNull(response);
        BJSON bJson = ((BJSON) response.getMessageDataSource());
        Assert.assertEquals(bJson.value().get("echo").asText(), "resOnlyCors"
                , "Resource dispatched to wrong template");
        String origin = response.getHeader(AC_ALLOW_ORIGIN);
        Assert.assertEquals(origin, "*");
    }

    @AfterClass
    public void tearDown() {
        EnvironmentInitializer.cleanup(programFile);
    }

}
