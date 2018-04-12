/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*  http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.test.services.dispatching;

import org.ballerinalang.launcher.util.BServiceUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.test.services.testutils.HTTPTestRequest;
import org.ballerinalang.test.services.testutils.MessageUtils;
import org.ballerinalang.test.services.testutils.Services;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;

/**
 * Test class for Uri Template based resource dispatchers.
 * Ex: /products/{productId}?regID={regID}
 */
public class UriTemplateBestMatchTest {

    private static final String TEST_EP = "testEP";
    private CompileResult application;

    @BeforeClass()
    public void setup() {
        application = BServiceUtil
                .setupProgramFile(this, "test-src/services/dispatching/uri-template-matching.bal");
    }

    @Test(description = "Test dispatching with URL. /hello/world/echo2?regid=abc")
    public void testMostSpecificMatchWithQueryParam() {
        String path = "/hello/world/echo2?regid=abc";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HTTPCarbonMessage response = Services.invokeNew(application, TEST_EP, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());

        Assert.assertEquals(bJson.value().get("echo1").asText(), "echo1"
                , "Resource dispatched to wrong template");
    }

    @Test(description = "Test dispatching with URL. /hello/world/echo2/bar")
    public void testMostSpecificMatchWithWildCard() {
        String path = "/hello/world/echo2/bar";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HTTPCarbonMessage response = Services.invokeNew(application, TEST_EP, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());

        Assert.assertEquals(bJson.value().get("echo2").asText(), "echo2"
                , "Resource dispatched to wrong template");
    }

    @Test(description = "Test dispatching with URL. /hello/world/echo2/foo/bar")
    public void testMostSpecificMatch() {
        String path = "/hello/world/echo2/foo/bar";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HTTPCarbonMessage response = Services.invokeNew(application, TEST_EP, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());

        Assert.assertEquals(bJson.value().get("echo3").asText(), "echo3"
                , "Resource dispatched to wrong template");
    }

    @Test(description = "Test dispatching with URL. /hello/echo2?regid=abc")
    public void testMostSpecificServiceDispatch() {
        String path = "/hello/echo2?regid=abc";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HTTPCarbonMessage response = Services.invokeNew(application, TEST_EP, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());

        Assert.assertEquals(bJson.value().get("echo5").asText(), "echo5"
                , "Resource dispatched to wrong template");
    }

    @Test(description = "Test dispatching with URL. /hello/echo2?regid=abc")
    public void testSubPathEndsWithPathParam() {
        String path = "/hello/echo2/shafreen";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HTTPCarbonMessage response = Services.invokeNew(application, TEST_EP, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());

        Assert.assertEquals(bJson.value().get("echo3").asText(), "shafreen"
                , "Resource dispatched to wrong template");
    }

    @Test(description = "Test dispatching with URL. /hello/echo2/shafreen-anfar & /hello/echo2/shafreen+anfar")
    public void testMostSpecificWithPathParam() {
        String path = "/hello/echo2/shafreen-anfar";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HTTPCarbonMessage response = Services.invokeNew(application, TEST_EP, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());

        Assert.assertEquals(bJson.value().get("echo3").asText(), "shafreen-anfar"
                , "Resource dispatched to wrong template");

        path = "/hello/echo2/shafreen+anfar";
        cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        response = Services.invokeNew(application, TEST_EP, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());

        Assert.assertEquals(bJson.value().get("echo3").asText(), "shafreen anfar"
                , "Resource dispatched to wrong template");
    }

    @Test(description = "Test dispatching with URL. /hello/echo2/shafreen+anfar/bar")
    public void testSubPathEndsWithBar() {
        String path = "/hello/echo2/shafreen+anfar/bar";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HTTPCarbonMessage response = Services.invokeNew(application, TEST_EP, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());

        Assert.assertEquals(bJson.value().get("first").asText(), "shafreen anfar"
                , "Resource dispatched to wrong template");

        Assert.assertEquals(bJson.value().get("echo4").asText(), "echo4"
                , "Resource dispatched to wrong template");
    }


    @Test(description = "Test dispatching with URL. /hello/echo2/shafreen+anfar/foo/bar")
    public void testLeastSpecificURITemplate() {
        String path = "/hello/echo2/shafreen+anfar/foo/bar";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HTTPCarbonMessage response = Services.invokeNew(application, TEST_EP, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());

        Assert.assertEquals(bJson.value().get("echo5").asText(), "any"
                , "Resource dispatched to wrong template");
    }

    @Test(description = "Test dispatching with URL. /hello/echo2/shafreen+anfar/bar")
    public void testParamDefaultValues() {
        String path = "/hello/echo3/shafreen+anfar?foo=bar";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HTTPCarbonMessage response = Services.invokeNew(application, TEST_EP, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());

        Assert.assertEquals(bJson.value().get("first").asText(), "shafreen anfar"
                , "Resource dispatched to wrong template");

        Assert.assertEquals(bJson.value().get("second").asText(), "bar"
                , "Resource dispatched to wrong template");

        Assert.assertEquals(bJson.value().get("echo9").asText(), "echo9"
                , "Resource dispatched to wrong template");
    }

    @Test(description = "Test dispatching with URL. /hello")
    public void testRootPathDefaultValues() {
        String path = "/hello?foo=zzz";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HTTPCarbonMessage response = Services.invokeNew(application, TEST_EP, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());

        Assert.assertEquals(bJson.value().get("third").asText(), "zzz"
                , "Resource dispatched to wrong template");

        Assert.assertEquals(bJson.value().get("echo10").asText(), "echo10"
                , "Resource dispatched to wrong template");
    }

    @Test(description = "Test dispatching with URL. /hello")
    public void testDefaultPathDefaultValues() {
        String path = "/hello/echo11?foo=zzz";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HTTPCarbonMessage response = Services.invokeNew(application, TEST_EP, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());

        Assert.assertEquals(bJson.value().get("third").asText(), "zzz"
                , "Resource dispatched to wrong template");

        Assert.assertEquals(bJson.value().get("echo11").asText(), "echo11"
                , "Resource dispatched to wrong template");
    }

    @Test(description = "Test dispatching with URL. /hello")
    public void testServiceRoot() {
        String path = "/echo1?foo=zzz";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HTTPCarbonMessage response = Services.invokeNew(application, TEST_EP, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());

        Assert.assertEquals(bJson.value().get("third").asText(), "zzz"
                , "Resource dispatched to wrong template");

        Assert.assertEquals(bJson.value().get("echo33").asText(), "echo1"
                , "Resource dispatched to wrong template");
    }

    @Test(description = "Test dispatching with all default values")
    public void testAllDefaultValues() {
        String path = "/echo44/echo1?foo=zzz";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HTTPCarbonMessage response = Services.invokeNew(application, TEST_EP, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());

        Assert.assertEquals(bJson.value().get("first").asText(), "zzz"
                , "Resource dispatched to wrong template");

        Assert.assertEquals(bJson.value().get("echo44").asText(), "echo1"
               , "Resource dispatched to wrong template");
    }

    @Test(description = "Test suitable method with URL. /hello/so2 ")
    public void testWrongGETMethod() {
        String path = "/hello/so2";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HTTPCarbonMessage response = Services.invokeNew(application, TEST_EP, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        int trueResponse = (int) response.getProperty(HttpConstants.HTTP_STATUS_CODE);
        Assert.assertEquals(trueResponse, 405, "Method not found");
    }

    @Test(description = "Test suitable method with URL. /hello/echo2 ")
    public void testWrongPOSTMethod() {
        String path = "/hello/echo2";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "POST");
        HTTPCarbonMessage response = Services.invokeNew(application, TEST_EP, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        int trueResponse = (int) response.getProperty(HttpConstants.HTTP_STATUS_CODE);
        Assert.assertEquals(trueResponse, 405, "Method not found");
    }

    @Test(description = "Test suitable method with URL. /echo12/bar/bar ")
    public void testValueWithNextSegmentStartCharacter() {
        String path = "/hello/echo12/bar/bar";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HTTPCarbonMessage response = Services.invokeNew(application, TEST_EP, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("echo12").asText(), "bar"
                , "Resource dispatched to wrong template");
    }

    @Test(description = "Test suitable method with URL. /echo125?foo=hello ")
    public void testStringQueryParam() {
        String path = "/hello/echo125?foo=hello";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HTTPCarbonMessage response = Services.invokeNew(application, TEST_EP, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("echo125").asText(), "hello"
                , "Resource dispatched to wrong template");

        path = "/hello/echo125?foo=";
        cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        response = Services.invokeNew(application, TEST_EP, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("echo125").asText(), ""
                , "Resource dispatched to wrong template");
    }

    @Test(description = "Test GetQueryParam method when params are not set with URL. /paramNeg")
    public void testGetQueryParamNegative() {
        String path = "/hello/paramNeg";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HTTPCarbonMessage response = Services.invokeNew(application, TEST_EP, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("echo125").asText(), "" , "param value is not null");
    }

    @Test(description = "Test suitable method with URL. /echo13?foo=1 ")
    public void testIntegerQueryParam() {
        String path = "/hello/echo13?foo=1";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HTTPCarbonMessage response = Services.invokeNew(application, TEST_EP, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("echo13").asText(), "1"
                , "Resource dispatched to wrong template");

        path = "/hello/echo13?foo=";
        cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        response = Services.invokeNew(application, TEST_EP, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("echo13").asText(), "0"
                , "Resource dispatched to wrong template");
    }

    @Test(description = "Test suitable method with URL. /echo14?foo=1.11 ")
    public void testFloatQueryParam() {
        String path = "/hello/echo14?foo=1.11";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HTTPCarbonMessage response = Services.invokeNew(application, TEST_EP, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("echo14").asText(), "1.11"
                , "Resource dispatched to wrong template");

        path = "/hello/echo14?foo=";
        cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        response = Services.invokeNew(application, TEST_EP, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("echo14").asText(), "0.0"
                , "Resource dispatched to wrong template");
    }

    @Test(description = "Test suitable method with URL. /echo15?foo=1.11 ")
    public void testBooleanQueryParam() {
        String path = "/hello/echo15?foo=true";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HTTPCarbonMessage response = Services.invokeNew(application, TEST_EP, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("echo15").asText(), "true"
                , "Resource dispatched to wrong template");

        path = "/hello/echo15?foo=";
        cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        response = Services.invokeNew(application, TEST_EP, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("echo15").asText(), "false"
                , "Resource dispatched to wrong template");
    }

    @Test(description = "Test dispatching without verbs")
    public void testResourceWithoutMethod() {
        String path = "/echo44/echo2";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "POST");
        HTTPCarbonMessage response = Services.invokeNew(application, TEST_EP, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("first").asText(), "zzz"
                , "Resource dispatched to wrong template");

        cMsg = MessageUtils.generateHTTPMessage(path, "HEAD");
        response = Services.invokeNew(application, TEST_EP, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("first").asText(), "zzz"
                , "Resource dispatched to wrong template");

        cMsg = MessageUtils.generateHTTPMessage(path, "PUT");
        response = Services.invokeNew(application, TEST_EP, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("first").asText(), "zzz"
                , "Resource dispatched to wrong template");

        cMsg = MessageUtils.generateHTTPMessage(path, "DELETE");
        response = Services.invokeNew(application, TEST_EP, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("first").asText(), "zzz"
                , "Resource dispatched to wrong template");

        cMsg = MessageUtils.generateHTTPMessage(path, "OPTIONS");
        response = Services.invokeNew(application, TEST_EP, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("first").asText(), "zzz"
                , "Resource dispatched to wrong template");
    }

    @Test(description = "Test dispatching for the best match")
    public void testBestMatchingResource() {
        String path = "/echo44/echo2";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HTTPCarbonMessage response = Services.invokeNew(application, TEST_EP, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("first").asText(), "bar"
                , "Resource dispatched to wrong template");
    }

    @Test(description = "Test default resource support")
    public void testDefaultResourceSupport() {
        String path = "/echo55/hello";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "POST", "Test");
        HTTPCarbonMessage response = Services.invokeNew(application, TEST_EP, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("echo55").asText(), "default"
                , "Resource dispatched to wrong template");

        path = "/echo55/foo";
        cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        response = Services.invokeNew(application, TEST_EP, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("echo55").asText(), "default"
                , "Resource dispatched to wrong template");

        path = "/echo55/foo/";
        cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        response = Services.invokeNew(application, TEST_EP, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("echo55").asText(), "default"
                , "Resource dispatched to wrong template");

        path = "/echo55/foo/abc";
        cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        response = Services.invokeNew(application, TEST_EP, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("echo55").asText(), "/foo/*"
                , "Resource dispatched to wrong template");
    }

    @Test(description = "Test rest uri post fix. /echo66/a/b/c")
    public void testRestUriPostFix() {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/echo66/a/b/c", "GET");
        HTTPCarbonMessage response = Services.invokeNew(application, TEST_EP, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());

        Assert.assertEquals(bJson.value().get("echo66").asText(), "/b/c"
                , "Wrong rest uri post fix value");

        cMsg = MessageUtils.generateHTTPMessage("/echo66/a/c", "GET");
        response = Services.invokeNew(application, TEST_EP, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());

        Assert.assertEquals(bJson.value().get("echo66").asText(), "/c"
                , "Wrong rest uri post fix value");

        cMsg = MessageUtils.generateHTTPMessage("/echo66/a", "GET");
        response = Services.invokeNew(application, TEST_EP, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());

        Assert.assertEquals(bJson.value().get("echo66").asText(), "empty"
                , "Wrong rest uri post fix value");
    }

    @Test(description = "Test whether requests get dispatch to the correct resource.")
    public void testMatchWithWildCard() {
        String path = "/uri/123";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HTTPCarbonMessage response = Services.invokeNew(application, TEST_EP, cMsg);

        Assert.assertNotNull(response, "Response message not found.");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());

        Assert.assertEquals(bJson.value().get("message").asText(), "Wildcard Params Resource is invoked."
                , "Request dispatched to wrong resource");
    }

    @Test(description = "Test whether requests get dispatch to the best matching resource.")
    public void testBestMatchWithWildCard() {
        String path = "/uri/123";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "POST");
        HTTPCarbonMessage response = Services.invokeNew(application, TEST_EP, cMsg);

        Assert.assertNotNull(response, "Response message not found.");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());

        Assert.assertEquals(bJson.value().get("message").asText(), "Path Params Resource is invoked."
                , "Request dispatched to wrong resource");
    }

    @Test(description = "Test best match with path param different lengths.")
    public void testDifferentLengthPathParams() {
        String path = "/uri/go/wso2/ballerina/http";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HTTPCarbonMessage response = Services.invokeNew(application, TEST_EP, cMsg);

        Assert.assertNotNull(response, "Response message not found.");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());

        Assert.assertEquals(bJson.value().get("aaa").asText(), "wso2", "wrong param value");
        Assert.assertEquals(bJson.value().get("bbb").asText(), "ballerina", "wrong param value");
        Assert.assertEquals(bJson.value().get("ccc").asText(), "http", "wrong param value");

        path =  "/uri/go/123/456";
        cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        response = Services.invokeNew(application, TEST_EP, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());

        Assert.assertEquals(bJson.value().get("xxx").asText(), "123", "wrong param value");
        Assert.assertEquals(bJson.value().get("yyy").asText(), "456", "wrong param value");
    }

    @Test(description = "Test whether requests get dispatched to the capitalized resource path.")
    public void testBestMatchWithCapitalizedPathSegments() {
        String path = "/uri/Go";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "POST");
        HTTPCarbonMessage response = Services.invokeNew(application, TEST_EP, cMsg);

        Assert.assertNotNull(response, "Response message not found.");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());

        Assert.assertEquals(bJson.value().get("value").asText(), "capitalized"
                , "Request dispatched to wrong resource");
    }
}
