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

package org.ballerinalang.stdlib.services.cors;

import io.netty.handler.codec.http.HttpHeaderNames;
import org.ballerinalang.core.model.util.JsonParser;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.stdlib.utils.HTTPTestRequest;
import org.ballerinalang.stdlib.utils.MessageUtils;
import org.ballerinalang.stdlib.utils.Services;
import org.ballerinalang.test.util.BCompileUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;

/**
 * Test cases related to HTTP CORS.
 */
public class HTTPCorsTest {

    private static final int TEST_EP_PORT = 9090;

    @BeforeClass
    public void setup() {
        BCompileUtil.compile("test-src/services/cors/cors-test.bal");
    }

    public void assertEqualsCorsResponse(HttpCarbonMessage response, int statusCode, String origin, String credentials
            , String headers, String methods, String maxAge) {
        Assert.assertEquals((int) response.getHttpStatusCode(), statusCode);
        Assert.assertEquals(response.getHeader(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN.toString()), origin);
        Assert.assertEquals(response.getHeader(HttpHeaderNames.ACCESS_CONTROL_ALLOW_CREDENTIALS.toString()),
                credentials);
        Assert.assertEquals(response.getHeader(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS.toString()), headers);
        Assert.assertEquals(response.getHeader(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS.toString()), methods);
        Assert.assertEquals(response.getHeader(HttpHeaderNames.ACCESS_CONTROL_MAX_AGE.toString()), maxAge);
    }

    @Test(description = "Test for CORS override at two levels for simple requests")
    public void testSimpleReqServiceResourceCorsOverride() {
        String path = "/hello1/test1";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "POST", "Hello there");
        cMsg.setHeader(HttpHeaderNames.ORIGIN.toString(), "http://www.wso2.com");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response);
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("echo").stringValue(), "resCors");
        Assert.assertEquals("http://www.wso2.com",
                response.getHeader(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN.toString()));
        Assert.assertEquals("true", response.getHeader(HttpHeaderNames.ACCESS_CONTROL_ALLOW_CREDENTIALS.toString()));
    }

    @Test(description = "Test for simple request service CORS")
    public void testSimpleReqServiceCors() {
        String path = "/hello1/test2";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        cMsg.setHeader(HttpHeaderNames.ORIGIN.toString(), "http://www.hello.com");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response);
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("echo").stringValue(), "serCors");
        Assert.assertEquals(response.getHeader(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN.toString()),
                "http://www.hello.com");
        Assert.assertEquals(response.getHeader(HttpHeaderNames.ACCESS_CONTROL_ALLOW_CREDENTIALS.toString()), "true");
    }

    @Test(description = "Test for resource only CORS declaration")
    public void testSimpleReqResourceOnlyCors() {
        String path = "/hello2/test1";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "POST", "hello");
        cMsg.setHeader(HttpHeaderNames.ORIGIN.toString(), "http://www.hello.com");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response);
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("echo").stringValue(), "resOnlyCors");
        Assert.assertEquals("http://www.hello.com",
                response.getHeader(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN.toString()));
        Assert.assertEquals(null, response.getHeader(HttpHeaderNames.ACCESS_CONTROL_ALLOW_CREDENTIALS.toString()));
        Assert.assertEquals("X-Content-Type-Options, X-PINGOTHER",
                response.getHeader(HttpHeaderNames.ACCESS_CONTROL_EXPOSE_HEADERS.toString()));
    }

    @Test(description = "Test simple request with multiple origins")
    public void testSimpleReqMultipleOrigins() {
        String path = "/hello1/test3";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "POST", "Hello there");
        cMsg.setHeader(HttpHeaderNames.ORIGIN.toString(), "http://www.wso2.com http://www.amazon.com");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response);
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("echo").stringValue(), "moreOrigins");
        Assert.assertEquals("http://www.wso2.com http://www.amazon.com",
                response.getHeader(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN.toString()));
        Assert.assertEquals("true", response.getHeader(HttpHeaderNames.ACCESS_CONTROL_ALLOW_CREDENTIALS.toString()));
    }

    @Test(description = "Test simple request for invalid origins")
    public void testSimpleReqInvalidOrigin() {
        String path = "/hello1/test1";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "POST", "Hello there");
        cMsg.setHeader(HttpHeaderNames.ORIGIN.toString(), "www.wso2.com");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response);
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("echo").stringValue(), "resCors");
        Assert.assertEquals(null, response.getHeader(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN.toString()));
        Assert.assertNull(null, response.getHeader(HttpHeaderNames.ACCESS_CONTROL_ALLOW_CREDENTIALS.toString()));
    }

    @Test(description = "Test simple request for null origins")
    public void testSimpleReqWithNullOrigin() {
        String path = "/hello1/test1";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "POST", "Hello there");
        cMsg.setHeader(HttpHeaderNames.ORIGIN.toString(), "");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response);
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("echo").stringValue(), "resCors");
        Assert.assertEquals(null, response.getHeader(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN.toString()));
        Assert.assertNull(null, response.getHeader(HttpHeaderNames.ACCESS_CONTROL_ALLOW_CREDENTIALS.toString()));
    }

    @Test(description = "Test for values with extra white spaces")
    public void testSimpleReqwithExtraWS() {
        String path = "/hello2/test1";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "POST", "hello");
        cMsg.setHeader(HttpHeaderNames.ORIGIN.toString(), "http://www.facebook.com");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response);
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("echo").stringValue(), "resOnlyCors");
        Assert.assertEquals("http://www.facebook.com",
                response.getHeader(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN.toString()));
    }

    @Test(description = "Test for CORS override at two levels with preflight")
    public void testPreFlightReqServiceResourceCorsOverride() {
        String path = "/hello1/test1";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "OPTIONS", "Hello there");
        cMsg.setHeader(HttpHeaderNames.ORIGIN.toString(), "http://www.wso2.com");
        cMsg.setHeader(HttpHeaderNames.ACCESS_CONTROL_REQUEST_METHOD.toString(), HttpConstants.HTTP_METHOD_POST);
        cMsg.setHeader(HttpHeaderNames.ACCESS_CONTROL_REQUEST_HEADERS.toString(), "X-PINGOTHER");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response);
        assertEqualsCorsResponse(response, 200, "http://www.wso2.com", "true"
                , "X-PINGOTHER", "POST", "-1");
    }

    @Test(description = "Test preflight without origin header")
    public void testPreFlightReqwithNoOrigin() {
        String path = "/hello1/test1";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "OPTIONS", "Hello there");
        cMsg.setHeader(HttpHeaderNames.ACCESS_CONTROL_REQUEST_METHOD.toString(), HttpConstants.HTTP_METHOD_POST);
        cMsg.setHeader(HttpHeaderNames.ACCESS_CONTROL_REQUEST_HEADERS.toString(), "X-PINGOTHER");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response);
        assertEqualsCorsResponse(response, 200, null, null
                , null, null, null);
    }

    @Test(description = "Test preflight without Request Method header")
    public void testPreFlightReqwithNoMethod() {
        String path = "/hello1/test1";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "OPTIONS", "Hello there");
        cMsg.setHeader(HttpHeaderNames.ORIGIN.toString(), "http://www.wso2.com");
        cMsg.setHeader(HttpHeaderNames.ACCESS_CONTROL_REQUEST_HEADERS.toString(), "X-PINGOTHER");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response);
        assertEqualsCorsResponse(response, 200, null, null
                , null, null, null);
    }

    @Test(description = "Test preflight with unavailable HTTP methods")
    public void testPreFlightReqwithUnavailableMethod() {
        String path = "/hello1/test1";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "OPTIONS", "Hello there");
        cMsg.setHeader(HttpHeaderNames.ORIGIN.toString(), "http://www.wso2.com");
        cMsg.setHeader(HttpHeaderNames.ACCESS_CONTROL_REQUEST_METHOD.toString(), HttpConstants.HTTP_METHOD_PUT);
        cMsg.setHeader(HttpHeaderNames.ACCESS_CONTROL_REQUEST_HEADERS.toString(), "X-PINGOTHER");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response);
        assertEqualsCorsResponse(response, 200, null, null
                , null, null, null);
    }

    @Test(description = "Test for preflight with Head as request method to a GET method annotated resource")
    public void testPreFlightReqwithHeadMethod() {
        String path = "/hello1/test2";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "OPTIONS", "Hello there");
        cMsg.setHeader(HttpHeaderNames.ORIGIN.toString(), "http://www.m3.com");
        cMsg.setHeader(HttpHeaderNames.ACCESS_CONTROL_REQUEST_METHOD.toString(), HttpConstants.HTTP_METHOD_HEAD);
        cMsg.setHeader(HttpHeaderNames.ACCESS_CONTROL_REQUEST_HEADERS.toString(), "CORELATION_ID");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response);
        assertEqualsCorsResponse(response, 200, "http://www.m3.com", "true"
                , "CORELATION_ID", HttpConstants.HTTP_METHOD_HEAD, "1");
    }

    @Test(description = "Test preflight for invalid headers")
    public void testPreFlightReqwithInvalidHeaders() {
        String path = "/hello1/test1";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "OPTIONS", "Hello there");
        cMsg.setHeader(HttpHeaderNames.ORIGIN.toString(), "http://www.wso2.com");
        cMsg.setHeader(HttpHeaderNames.ACCESS_CONTROL_REQUEST_METHOD.toString(), HttpConstants.HTTP_METHOD_POST);
        cMsg.setHeader(HttpHeaderNames.ACCESS_CONTROL_REQUEST_HEADERS.toString(), "WSO2");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response);
        assertEqualsCorsResponse(response, 200, null, null
                , null, null, null);
    }

    @Test(description = "Test preflight without headers")
    public void testPreFlightReqwithNoHeaders() {
        String path = "/hello1/test1";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "OPTIONS", "Hello there");
        cMsg.setHeader(HttpHeaderNames.ORIGIN.toString(), "http://www.wso2.com");
        cMsg.setHeader(HttpHeaderNames.ACCESS_CONTROL_REQUEST_METHOD.toString(), HttpConstants.HTTP_METHOD_POST);
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response);
        assertEqualsCorsResponse(response, 200, "http://www.wso2.com", "true"
                , null, HttpConstants.HTTP_METHOD_POST, "-1");
    }

    @Test(description = "Test preflight with method restriction at service level")
    public void testPreFlightReqwithRestrictedMethodsServiceLevel() {
        String path = "/hello3/info1";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "OPTIONS", "Hello there");
        cMsg.setHeader(HttpHeaderNames.ORIGIN.toString(), "http://www.m3.com");
        cMsg.setHeader(HttpHeaderNames.ACCESS_CONTROL_REQUEST_METHOD.toString(), HttpConstants.HTTP_METHOD_POST);
        cMsg.setHeader(HttpHeaderNames.ACCESS_CONTROL_REQUEST_HEADERS.toString(), "X-PINGOTHER");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response);
        assertEqualsCorsResponse(response, 200, null, null
                , null, null, null);
    }

    @Test(description = "Test preflight with method restriction at resource level")
    public void testPreFlightReqwithRestrictedMethodsResourceLevel() {
        String path = "/hello2/test2";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "OPTIONS", "Hello there");
        cMsg.setHeader(HttpHeaderNames.ORIGIN.toString(), "http://www.bbc.com");
        cMsg.setHeader(HttpHeaderNames.ACCESS_CONTROL_REQUEST_METHOD.toString(), HttpConstants.HTTP_METHOD_DELETE);
        cMsg.setHeader(HttpHeaderNames.ACCESS_CONTROL_REQUEST_HEADERS.toString(), "X-PINGOTHER");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response);
        assertEqualsCorsResponse(response, 200, null, null
                , null, null, null);
    }

    @Test(description = "Test preflight with allowed method at service level")
    public void testPreFlightReqwithAllowedMethod() {
        String path = "/hello3/info1";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "OPTIONS", "Hello there");
        cMsg.setHeader(HttpHeaderNames.ORIGIN.toString(), "http://www.m3.com");
        cMsg.setHeader(HttpHeaderNames.ACCESS_CONTROL_REQUEST_METHOD.toString(), HttpConstants.HTTP_METHOD_PUT);
        cMsg.setHeader(HttpHeaderNames.ACCESS_CONTROL_REQUEST_HEADERS.toString(), "X-PINGOTHER");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response);
        assertEqualsCorsResponse(response, 200, "http://www.m3.com", "true"
                , "X-PINGOTHER", "PUT", "1");
    }

    @Test(description = "Test preflight with missing headers at resource level")
    public void testPreFlightReqwithMissingHeadersAtResourceLevel() {
        String path = "/hello2/test2";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "OPTIONS", "Hello there");
        cMsg.setHeader(HttpHeaderNames.ORIGIN.toString(), "http://www.bbc.com");
        cMsg.setHeader(HttpHeaderNames.ACCESS_CONTROL_REQUEST_METHOD.toString(), HttpConstants.HTTP_METHOD_PUT);
        cMsg.setHeader(HttpHeaderNames.ACCESS_CONTROL_REQUEST_HEADERS.toString(), "X-PINGOTHER");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response);
        assertEqualsCorsResponse(response, 200, "http://www.bbc.com", null
                , "X-PINGOTHER", "PUT", "-1");
    }

    @Test(description = "Test preflight without CORS headers")
    public void testPreFlightReqNoCorsResource() {
        String path = "/echo4/info1";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "OPTIONS", "Hello there");
        cMsg.setHeader(HttpHeaderNames.ORIGIN.toString(), "http://www.wso2.com");
        cMsg.setHeader(HttpHeaderNames.ACCESS_CONTROL_REQUEST_METHOD.toString(), HttpConstants.HTTP_METHOD_POST);
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response);
        assertEqualsCorsResponse(response, 200, null, null
                , null, null, null);
        Assert.assertEquals(response.getHeader(HttpHeaderNames.ALLOW.toString()), "POST, OPTIONS");
    }

    @Test(description = "Test for simple OPTIONS request")
    public void testSimpleOPTIONSReq() {
        String path = "/echo4/info2";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "OPTIONS", "Hello there");
        cMsg.setHeader(HttpHeaderNames.ORIGIN.toString(), "http://www.wso2.com");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response);
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("echo").stringValue(), "noCorsOPTIONS");
    }

    @Test(description = "Test for case insensitive origin")
    public void testPreFlightReqwithCaseInsensitiveOrigin() {
        String path = "/hello1/test1";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "OPTIONS", "Hello there");
        cMsg.setHeader(HttpHeaderNames.ORIGIN.toString(), "http://www.Wso2.com");
        cMsg.setHeader(HttpHeaderNames.ACCESS_CONTROL_REQUEST_METHOD.toString(), HttpConstants.HTTP_METHOD_POST);
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response);
        assertEqualsCorsResponse(response, 200, null, null
                , null, null, null);
    }

    @Test(description = "Test for case insensitive header")
    public void testPreFlightReqwithCaseInsensitiveHeader() {
        String path = "/hello1/test1";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "OPTIONS", "Hello there");
        cMsg.setHeader(HttpHeaderNames.ORIGIN.toString(), "http://www.wso2.com");
        cMsg.setHeader(HttpHeaderNames.ACCESS_CONTROL_REQUEST_METHOD.toString(), HttpConstants.HTTP_METHOD_POST);
        cMsg.setHeader(HttpHeaderNames.ACCESS_CONTROL_REQUEST_HEADERS.toString(), "X-pingOTHER");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response);
        assertEqualsCorsResponse(response, 200, "http://www.wso2.com", "true"
                , "X-pingOTHER", "POST", "-1");
    }

    @Test(description = "Test for serviceLevel wildcard/default CORS configs")
    public void testPreFlightReqwithWildCardServiceConfigs() {
        String path = "/hello5/info1";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "OPTIONS", "Hello there");
        cMsg.setHeader(HttpHeaderNames.ORIGIN.toString(), "http://www.wso2Ballerina.com");
        cMsg.setHeader(HttpHeaderNames.ACCESS_CONTROL_REQUEST_METHOD.toString(), "POST");
        cMsg.setHeader(HttpHeaderNames.ACCESS_CONTROL_REQUEST_HEADERS.toString(), "X-PINGOTHER");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response);
        assertEqualsCorsResponse(response, 200, "http://www.wso2Ballerina.com", null
                , "X-PINGOTHER", "POST", "-1");
    }

    @Test(description = "Test for resource Level wildcard/default CORS configs")
    public void testPreFlightReqwithWildCardResourceConfigs() {
        String path = "/echo4/info3";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "OPTIONS", "Hello there");
        cMsg.setHeader(HttpHeaderNames.ORIGIN.toString(), "http://www.wso2Ballerina456.com");
        cMsg.setHeader(HttpHeaderNames.ACCESS_CONTROL_REQUEST_METHOD.toString(), "POST");
        cMsg.setHeader(HttpHeaderNames.ACCESS_CONTROL_REQUEST_HEADERS.toString(), "X-PINGOTHER");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response);
        assertEqualsCorsResponse(response, 200, "http://www.wso2Ballerina456.com", "true"
                , "X-PINGOTHER", "POST", "-1");
    }

    @Test(description = "Test for resource Level wildcard/default CORS configs override")
    public void testPreFlightReqwithWildCardResourceConfigsOverride() {
        String path = "/hello1/test4";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "OPTIONS", "Hello there");
        cMsg.setHeader(HttpHeaderNames.ORIGIN.toString(), "http://www.wso2Ballerina123.com");
        cMsg.setHeader(HttpHeaderNames.ACCESS_CONTROL_REQUEST_METHOD.toString(), "PUT");
        cMsg.setHeader(HttpHeaderNames.ACCESS_CONTROL_REQUEST_HEADERS.toString(), "X-PONGOTHER");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response);
        assertEqualsCorsResponse(response, 200, "http://www.wso2Ballerina123.com", "true"
                , "X-PONGOTHER", "PUT", "-1");
    }
}
