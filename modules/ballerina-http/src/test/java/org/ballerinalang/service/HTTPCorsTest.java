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
import org.ballerinalang.services.dispatchers.http.Constants;
import org.ballerinalang.testutils.EnvironmentInitializer;
import org.ballerinalang.testutils.MessageUtils;
import org.ballerinalang.testutils.Services;
import org.ballerinalang.util.codegen.ProgramFile;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.messaging.CarbonMessage;

/**
 * Test cases related to HTTP CORS
 */
public class HTTPCorsTest {

    ProgramFile programFile;

    @BeforeClass
    public void setup() {
        programFile = EnvironmentInitializer.setupProgramFile("lang/service/corsTest.bal");
    }

    public void assertEqualsCorsResponse(CarbonMessage response, int statusCode, String origin, String credentials
            , String headers, String methods, String maxAge) {
        Assert.assertEquals(response.getProperty(Constants.HTTP_STATUS_CODE), statusCode);
        Assert.assertEquals(response.getHeader(Constants.AC_ALLOW_ORIGIN), origin);
        Assert.assertEquals(response.getHeader(Constants.AC_ALLOW_CREDENTIALS), credentials);
        Assert.assertEquals(response.getHeader(Constants.AC_ALLOW_HEADERS), headers);
        Assert.assertEquals(response.getHeader(Constants.AC_ALLOW_METHODS), methods);
        Assert.assertEquals(response.getHeader(Constants.AC_MAX_AGE), maxAge);
    }

    @Test(description = "Test for CORS override at two levels for simple requests")
    public void testSimpleReqServiceResourceCorsOverride() {
        String path = "/hello1/test1";
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage(path, "POST", "Hello there");
        cMsg.setHeader(Constants.ORIGIN, "http://www.wso2.com");
        CarbonMessage response = Services.invoke(cMsg);

        Assert.assertNotNull(response);
        BJSON bJson = ((BJSON) response.getMessageDataSource());
        Assert.assertEquals(bJson.value().get("echo").asText(), "resCors");
        Assert.assertEquals("http://www.wso2.com", response.getHeader(Constants.AC_ALLOW_ORIGIN));
        Assert.assertEquals("true", response.getHeader(Constants.AC_ALLOW_CREDENTIALS));
    }

    @Test(description = "Test for simple request service CORS")
    public void testSimpleReqServiceCors() {
        String path = "/hello1/test2";
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        cMsg.setHeader(Constants.ORIGIN, "http://www.hello.com");
        CarbonMessage response = Services.invoke(cMsg);

        Assert.assertNotNull(response);
        BJSON bJson = ((BJSON) response.getMessageDataSource());
        Assert.assertEquals(bJson.value().get("echo").asText(), "serCors");
        Assert.assertEquals("http://www.hello.com", response.getHeader(Constants.AC_ALLOW_ORIGIN));
        Assert.assertEquals("true", response.getHeader(Constants.AC_ALLOW_CREDENTIALS));
    }

    @Test(description = "Test for resource only CORS declaration")
    public void testSimpleReqResourceOnlyCors() {
        String path = "/hello2/test1";
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage(path, "POST", "hello");
        cMsg.setHeader(Constants.ORIGIN, "http://www.hello.com");
        CarbonMessage response = Services.invoke(cMsg);

        Assert.assertNotNull(response);
        BJSON bJson = ((BJSON) response.getMessageDataSource());
        Assert.assertEquals(bJson.value().get("echo").asText(), "resOnlyCors");
        Assert.assertEquals("http://www.hello.com", response.getHeader(Constants.AC_ALLOW_ORIGIN));
        Assert.assertEquals(null, response.getHeader(Constants.AC_ALLOW_CREDENTIALS));
        Assert.assertEquals("X-Content-Type-Options, X-PINGOTHER", response.getHeader(Constants.AC_EXPOSE_HEADERS));
    }

    @Test(description = "Test simple request with multiple origins")
    public void testSimpleReqMultipleOrigins() {
        String path = "/hello1/test3";
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage(path, "POST", "Hello there");
        cMsg.setHeader(Constants.ORIGIN, "http://www.wso2.com http://www.amazon.com");
        CarbonMessage response = Services.invoke(cMsg);

        Assert.assertNotNull(response);
        BJSON bJson = ((BJSON) response.getMessageDataSource());
        Assert.assertEquals(bJson.value().get("echo").asText(), "moreOrigins");
        Assert.assertEquals("http://www.wso2.com http://www.amazon.com", response.getHeader(Constants.AC_ALLOW_ORIGIN));
        Assert.assertEquals("true", response.getHeader(Constants.AC_ALLOW_CREDENTIALS));
    }

    @Test(description = "Test simple request for invalid origins")
    public void testSimpleReqInvalidOrigin() {
        String path = "/hello1/test1";
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage(path, "POST", "Hello there");
        cMsg.setHeader(Constants.ORIGIN, "www.wso2.com");
        CarbonMessage response = Services.invoke(cMsg);

        Assert.assertNotNull(response);
        BJSON bJson = ((BJSON) response.getMessageDataSource());
        Assert.assertEquals(bJson.value().get("echo").asText(), "resCors");
        Assert.assertEquals(null, response.getHeader(Constants.AC_ALLOW_ORIGIN));
        Assert.assertNull(null, response.getHeader(Constants.AC_ALLOW_CREDENTIALS));
    }

    @Test(description = "Test simple request for null origins")
    public void testSimpleReqWithNullOrigin() {
        String path = "/hello1/test1";
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage(path, "POST", "Hello there");
        cMsg.setHeader(Constants.ORIGIN, null);
        CarbonMessage response = Services.invoke(cMsg);

        Assert.assertNotNull(response);
        BJSON bJson = ((BJSON) response.getMessageDataSource());
        Assert.assertEquals(bJson.value().get("echo").asText(), "resCors");
        Assert.assertEquals(null, response.getHeader(Constants.AC_ALLOW_ORIGIN));
        Assert.assertNull(null, response.getHeader(Constants.AC_ALLOW_CREDENTIALS));
    }

    @Test(description = "Test for values with extra white spaces")
    public void testSimpleReqwithExtraWS() {
        String path = "/hello2/test1";
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage(path, "POST", "hello");
        cMsg.setHeader(Constants.ORIGIN, "http://www.facebook.com");
        CarbonMessage response = Services.invoke(cMsg);

        Assert.assertNotNull(response);
        BJSON bJson = ((BJSON) response.getMessageDataSource());
        Assert.assertEquals(bJson.value().get("echo").asText(), "resOnlyCors");
        Assert.assertEquals("http://www.facebook.com", response.getHeader(Constants.AC_ALLOW_ORIGIN));
    }

    @Test(description = "Test for CORS override at two levels with preflight")
    public void testPreFlightReqServiceResourceCorsOverride() {
        String path = "/hello1/test1";
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage(path, "OPTIONS", "Hello there");
        cMsg.setHeader(Constants.ORIGIN, "http://www.wso2.com");
        cMsg.setHeader(Constants.AC_REQUEST_METHOD, Constants.HTTP_METHOD_POST);
        cMsg.setHeader(Constants.AC_REQUEST_HEADERS, "X-PINGOTHER");
        CarbonMessage response = Services.invoke(cMsg);

        Assert.assertNotNull(response);
        assertEqualsCorsResponse(response, 200, "http://www.wso2.com", "true"
                , "X-PINGOTHER", "POST", "-1");
    }

    @Test(description = "Test preflight without origin header")
    public void testPreFlightReqwithNoOrigin() {
        String path = "/hello1/test1";
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage(path, "OPTIONS", "Hello there");
        cMsg.setHeader(Constants.AC_REQUEST_METHOD, Constants.HTTP_METHOD_POST);
        cMsg.setHeader(Constants.AC_REQUEST_HEADERS, "X-PINGOTHER");
        CarbonMessage response = Services.invoke(cMsg);

        Assert.assertNotNull(response);
        assertEqualsCorsResponse(response, 200, null, null
                , null, null, null);
    }

    @Test(description = "Test preflight without Request Method header")
    public void testPreFlightReqwithNoMethod() {
        String path = "/hello1/test1";
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage(path, "OPTIONS", "Hello there");
        cMsg.setHeader(Constants.ORIGIN, "http://www.wso2.com");
        cMsg.setHeader(Constants.AC_REQUEST_HEADERS, "X-PINGOTHER");
        CarbonMessage response = Services.invoke(cMsg);

        Assert.assertNotNull(response);
        assertEqualsCorsResponse(response, 200, null, null
                , null, null, null);
    }

    @Test(description = "Test preflight with unavailable HTTP methods")
    public void testPreFlightReqwithUnavailableMethod() {
        String path = "/hello1/test1";
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage(path, "OPTIONS", "Hello there");
        cMsg.setHeader(Constants.ORIGIN, "http://www.wso2.com");
        cMsg.setHeader(Constants.AC_REQUEST_METHOD, Constants.HTTP_METHOD_PUT);
        cMsg.setHeader(Constants.AC_REQUEST_HEADERS, "X-PINGOTHER");
        CarbonMessage response = Services.invoke(cMsg);

        Assert.assertNotNull(response);
        assertEqualsCorsResponse(response, 200, null, null
                , null, null, null);
    }

    @Test(description = "Test for preflight with Head as request method to a GET method annotated resource")
    public void testPreFlightReqwithHeadMethod() {
        String path = "/hello1/test2";
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage(path, "OPTIONS", "Hello there");
        cMsg.setHeader(Constants.ORIGIN, "http://www.m3.com");
        cMsg.setHeader(Constants.AC_REQUEST_METHOD, Constants.HTTP_METHOD_HEAD);
        cMsg.setHeader(Constants.AC_REQUEST_HEADERS, "CORELATION_ID");
        CarbonMessage response = Services.invoke(cMsg);

        Assert.assertNotNull(response);
        assertEqualsCorsResponse(response, 200, "http://www.m3.com", "true"
                , "CORELATION_ID", Constants.HTTP_METHOD_HEAD, "1");
    }

    @Test(description = "Test preflight for invalid headers")
    public void testPreFlightReqwithInvalidHeaders() {
        String path = "/hello1/test1";
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage(path, "OPTIONS", "Hello there");
        cMsg.setHeader(Constants.ORIGIN, "http://www.wso2.com");
        cMsg.setHeader(Constants.AC_REQUEST_METHOD, Constants.HTTP_METHOD_POST);
        cMsg.setHeader(Constants.AC_REQUEST_HEADERS, "WSO2");
        CarbonMessage response = Services.invoke(cMsg);

        Assert.assertNotNull(response);
        assertEqualsCorsResponse(response, 200, null, null
                , null, null, null);
    }

    @Test(description = "Test preflight without headers")
    public void testPreFlightReqwithNoHeaders() {
        String path = "/hello1/test1";
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage(path, "OPTIONS", "Hello there");
        cMsg.setHeader(Constants.ORIGIN, "http://www.wso2.com");
        cMsg.setHeader(Constants.AC_REQUEST_METHOD, Constants.HTTP_METHOD_POST);
        CarbonMessage response = Services.invoke(cMsg);

        Assert.assertNotNull(response);
        assertEqualsCorsResponse(response, 200, "http://www.wso2.com", "true"
                , null, Constants.HTTP_METHOD_POST, "-1");
    }

    @Test(description = "Test preflight with method restriction at service level")
    public void testPreFlightReqwithRestrictedMethodsServiceLevel() {
        String path = "/hello3/info1";
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage(path, "OPTIONS", "Hello there");
        cMsg.setHeader(Constants.ORIGIN, "http://www.m3.com");
        cMsg.setHeader(Constants.AC_REQUEST_METHOD, Constants.HTTP_METHOD_POST);
        cMsg.setHeader(Constants.AC_REQUEST_HEADERS, "X-PINGOTHER");
        CarbonMessage response = Services.invoke(cMsg);

        Assert.assertNotNull(response);
        assertEqualsCorsResponse(response, 200, null, null
                , null, null, null);
    }

    @Test(description = "Test preflight with method restriction at resource level")
    public void testPreFlightReqwithRestrictedMethodsResourceLevel() {
        String path = "/hello2/test2";
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage(path, "OPTIONS", "Hello there");
        cMsg.setHeader(Constants.ORIGIN, "http://www.bbc.com");
        cMsg.setHeader(Constants.AC_REQUEST_METHOD, Constants.HTTP_METHOD_DELETE);
        cMsg.setHeader(Constants.AC_REQUEST_HEADERS, "X-PINGOTHER");
        CarbonMessage response = Services.invoke(cMsg);

        Assert.assertNotNull(response);
        assertEqualsCorsResponse(response, 200, null, null
                , null, null, null);
    }

    @Test(description = "Test preflight with allowed method at service level")
    public void testPreFlightReqwithAllowedMethod() {
        String path = "/hello3/info1";
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage(path, "OPTIONS", "Hello there");
        cMsg.setHeader(Constants.ORIGIN, "http://www.m3.com");
        cMsg.setHeader(Constants.AC_REQUEST_METHOD, Constants.HTTP_METHOD_PUT);
        cMsg.setHeader(Constants.AC_REQUEST_HEADERS, "X-PINGOTHER");
        CarbonMessage response = Services.invoke(cMsg);

        Assert.assertNotNull(response);
        assertEqualsCorsResponse(response, 200, "http://www.m3.com", "true"
                , "X-PINGOTHER", "PUT", "1");
    }

    @Test(description = "Test preflight with missing headers at resource level")
    public void testPreFlightReqwithMissingHeadersAtResourceLevel() {
        String path = "/hello2/test2";
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage(path, "OPTIONS", "Hello there");
        cMsg.setHeader(Constants.ORIGIN, "http://www.bbc.com");
        cMsg.setHeader(Constants.AC_REQUEST_METHOD, Constants.HTTP_METHOD_PUT);
        cMsg.setHeader(Constants.AC_REQUEST_HEADERS, "X-PINGOTHER");
        CarbonMessage response = Services.invoke(cMsg);

        Assert.assertNotNull(response);
        assertEqualsCorsResponse(response, 200, "http://www.bbc.com", null
                , "X-PINGOTHER", "PUT", "-1");
    }

    @Test(description = "Test preflight without CORS headers")
    public void testPreFlightReqNoCorsResource() {
        String path = "/echo4/info1";
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage(path, "OPTIONS", "Hello there");
        cMsg.setHeader(Constants.ORIGIN, "http://www.wso2.com");
        cMsg.setHeader(Constants.AC_REQUEST_METHOD, Constants.HTTP_METHOD_POST);
        CarbonMessage response = Services.invoke(cMsg);

        Assert.assertNotNull(response);
        assertEqualsCorsResponse(response, 200, null, null
                , null, null, null);
        Assert.assertEquals("POST, OPTIONS", response.getHeader(Constants.ALLOW));
    }

    @Test(description = "Test for simple OPTIONS request")
    public void testSimpleOPTIONSReq() {
        String path = "/echo4/info2";
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage(path, "OPTIONS", "Hello there");
        cMsg.setHeader(Constants.ORIGIN, "http://www.wso2.com");
        CarbonMessage response = Services.invoke(cMsg);

        Assert.assertNotNull(response);
        BJSON bJson = ((BJSON) response.getMessageDataSource());
        Assert.assertEquals(bJson.value().get("echo").asText(), "noCorsOPTIONS");
    }

    @Test(description = "Test for case insensitive origin")
    public void testPreFlightReqwithCaseInsensitiveOrigin() {
        String path = "/hello1/test1";
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage(path, "OPTIONS", "Hello there");
        cMsg.setHeader(Constants.ORIGIN, "http://www.Wso2.com");
        cMsg.setHeader(Constants.AC_REQUEST_METHOD, Constants.HTTP_METHOD_POST);
        CarbonMessage response = Services.invoke(cMsg);

        Assert.assertNotNull(response);
        assertEqualsCorsResponse(response, 200, null, null
                , null, null, null);
    }

    @Test(description = "Test for case insensitive header")
    public void testPreFlightReqwithCaseInsensitiveHeader() {
        String path = "/hello1/test1";
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage(path, "OPTIONS", "Hello there");
        cMsg.setHeader(Constants.ORIGIN, "http://www.wso2.com");
        cMsg.setHeader(Constants.AC_REQUEST_METHOD, Constants.HTTP_METHOD_POST);
        cMsg.setHeader(Constants.AC_REQUEST_HEADERS, "X-PINGOTHER");
        CarbonMessage response = Services.invoke(cMsg);

        Assert.assertNotNull(response);
        assertEqualsCorsResponse(response, 200, "http://www.wso2.com", "true"
                , "X-PINGOTHER", "POST", "-1");
    }

    @Test(description = "Test simple request with worker")
    public void testSimpleReqwithWorker() {
        String path = "/hello3/info2";
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage(path, "GET", "Hello there");
        cMsg.setHeader(Constants.ORIGIN, "http://www.m3.com");
        CarbonMessage response = Services.invoke(cMsg);

        Assert.assertNotNull(response);
        BJSON bJson = ((BJSON) response.getMessageDataSource());
        Assert.assertEquals(bJson.value().get("echo").asText(), "worker");
        Assert.assertEquals("http://www.m3.com", response.getHeader(Constants.AC_ALLOW_ORIGIN));
        Assert.assertEquals("true", response.getHeader(Constants.AC_ALLOW_CREDENTIALS));
    }

    @Test(description = "Test preflight with worker")
    public void testPreFlightReqwithWorker() {
        String path = "/hello3/info2";
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage(path, "OPTIONS", "Hello there");
        cMsg.setHeader(Constants.ORIGIN, "http://www.m3.com");
        cMsg.setHeader(Constants.AC_REQUEST_METHOD, Constants.HTTP_METHOD_GET);
        cMsg.setHeader(Constants.AC_REQUEST_HEADERS, "X-PINGOTHER");
        CarbonMessage response = Services.invoke(cMsg);

        Assert.assertNotNull(response);
        assertEqualsCorsResponse(response, 200, "http://www.m3.com", "true"
                , "X-PINGOTHER", "GET", "1");
    }

    @AfterClass
    public void tearDown() {
        EnvironmentInitializer.cleanup(programFile);
    }

}
