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
package org.ballerinalang.stdlib.services.dispatching;

import io.ballerina.runtime.JSONParser;
import io.ballerina.runtime.api.BStringUtils;
import io.ballerina.runtime.api.values.BString;
import org.ballerinalang.model.util.JsonParser;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.stdlib.utils.HTTPTestRequest;
import org.ballerinalang.stdlib.utils.MessageUtils;
import org.ballerinalang.stdlib.utils.ResponseReader;
import org.ballerinalang.stdlib.utils.Services;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;

/**
 * Test class for Uri Template based resource dispatchers.
 * Ex: /products/{productId}?regID={regID}
 */
@SuppressWarnings("unchecked")
public class UriTemplateBestMatchTest {

    private static final int TEST_EP_PORT = 9090;

    @BeforeClass()
    public void setup() {
        CompileResult compileResult = BCompileUtil.compile("test-src/services/dispatching/uri-template-matching.bal");
    }

    @Test(description = "Test dispatching with URL. /hello/world/echo2?regid=abc")
    public void testMostSpecificMatchWithQueryParam() {
        String path = "/hello/world/echo2?regid=abc";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        Object bJson = JSONParser.parse(new HttpMessageDataStreamer(response).getInputStream());

        Assert.assertEquals(((BMap<BString, Object>) bJson).get(BStringUtils.fromString("echo1")).toString(),
                            "echo1", "Resource dispatched to wrong template");

    }

    @Test(description = "Test dispatching with URL. /hello/world/echo2/bar")
    public void testMostSpecificMatchWithWildCard() {
        String path = "/hello/world/echo2/bar";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        Object bJson = JSONParser.parse(new HttpMessageDataStreamer(response).getInputStream());

        Assert.assertEquals(((BMap<BString, Object>) bJson).get(BStringUtils.fromString("echo2")).toString(),
                            "echo2", "Resource dispatched to wrong template");
    }

    @Test(description = "Test dispatching with URL. /hello/world/echo2/foo/bar")
    public void testMostSpecificMatch() {
        String path = "/hello/world/echo2/foo/bar";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        Object bJson = JSONParser.parse(new HttpMessageDataStreamer(response).getInputStream());

        Assert.assertEquals(((BMap<BString, Object>) bJson).get(BStringUtils.fromString("echo3")).toString(),
                            "echo3", "Resource dispatched to wrong template");
    }

    @Test(description = "Test dispatching with URL. /hello/echo2?regid=abc")
    public void testMostSpecificServiceDispatch() {
        String path = "/hello/echo2?regid=abc";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        Object bJson = JSONParser.parse(new HttpMessageDataStreamer(response).getInputStream());

        Assert.assertEquals(((BMap<BString, Object>) bJson).get(BStringUtils.fromString("echo5")).toString(),
                            "echo5"
                , "Resource dispatched to wrong template");
    }

    @Test(description = "Test dispatching with URL. /hello/echo2?regid=abc")
    public void testSubPathEndsWithPathParam() {
        String path = "/hello/echo2/shafreen";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        Object bJson = JSONParser.parse(new HttpMessageDataStreamer(response).getInputStream());

        Assert.assertEquals(((BMap<BString, Object>) bJson).get(BStringUtils.fromString("echo3")).toString(),
                            "shafreen", "Resource dispatched to wrong template");
    }

    @Test(description = "Test dispatching with URL. /hello/echo2/shafreen-anfar & /hello/echo2/shafreen+anfar")
    public void testMostSpecificWithPathParam() {
        String path = "/hello/echo2/shafreen-anfar";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        Object bJson = JSONParser.parse(new HttpMessageDataStreamer(response).getInputStream());

        Assert.assertEquals(((BMap<BString, Object>) bJson).get(BStringUtils.fromString("echo3")).toString(),
                            "shafreen-anfar", "Resource dispatched to wrong template");

        path = "/hello/echo2/shafreen+anfar";
        cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        response = Services.invoke(TEST_EP_PORT, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        bJson = JSONParser.parse(new HttpMessageDataStreamer(response).getInputStream());

        Assert.assertEquals(((BMap<BString, Object>) bJson).get(BStringUtils.fromString("echo3")).toString(),
                            "shafreen anfar", "Resource dispatched to wrong template");
    }

    @Test(description = "Test dispatching with URL. /hello/echo2/shafreen+anfar/bar")
    public void testSubPathEndsWithBar() {
        String path = "/hello/echo2/shafreen+anfar/bar";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        Object bJson = JSONParser.parse(new HttpMessageDataStreamer(response).getInputStream());

        Assert.assertEquals(((BMap<BString, Object>) bJson).get(BStringUtils.fromString("first")).toString(),
                            "shafreen anfar", "Resource dispatched to wrong template");

        Assert.assertEquals(((BMap<BString, Object>) bJson).get(BStringUtils.fromString("echo4")).toString(),
                            "echo4", "Resource dispatched to wrong template");
    }


    @Test(description = "Test dispatching with URL. /hello/echo2/shafreen+anfar/foo/bar")
    public void testLeastSpecificURITemplate() {
        String path = "/hello/echo2/shafreen+anfar/foo/bar";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        Object bJson = JSONParser.parse(new HttpMessageDataStreamer(response).getInputStream());

        Assert.assertEquals(((BMap<BString, Object>) bJson).get(BStringUtils.fromString("echo5")).toString(),
                            "any"
                , "Resource dispatched to wrong template");
    }

    @Test(description = "Test dispatching with URL. /hello/echo2/shafreen+anfar/bar")
    public void testParamDefaultValues() {
        String path = "/hello/echo3/shafreen+anfar?foo=bar";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());

        Assert.assertEquals(((BMap<String, BValue>) bJson).get("first").stringValue(), "shafreen anfar"
                , "Resource dispatched to wrong template");

        Assert.assertEquals(((BMap<String, BValue>) bJson).get("second").stringValue(), "bar"
                , "Resource dispatched to wrong template");

        Assert.assertEquals(((BMap<String, BValue>) bJson).get("echo9").stringValue(), "echo9"
                , "Resource dispatched to wrong template");
    }

    @Test(description = "Test dispatching with URL. /hello/echo2/suffix.id")
    public void testPathParamWithSuffix() {
        String path = "/hello/echo2/suffix.id";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());

        Assert.assertEquals(((BMap<String, BValue>) bJson).get("echo6").stringValue(), "suffix"
                , "Resource dispatched to wrong template");
    }

    @Test(description = "Test dispatching with URL. /hello/echo2/literal.id")
    public void testBestMatchWhenPathLiteralHasSameSuffix() {
        String path = "/hello/echo2/literal.id";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());

        Assert.assertEquals(((BMap<String, BValue>) bJson).get("echo6").stringValue(), "literal invoked"
                , "Resource dispatched to wrong template");
    }

    @Test(description = "Test dispatching with URL. /hello/echo2/ballerina.id/foo")
    public void testSpecificMatchForPathParamWithSuffix() {
        String path = "/hello/echo2/ballerina.id/foo";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());

        Assert.assertEquals(((BMap<String, BValue>) bJson).get("echo6").stringValue(), "specific path invoked"
                , "Resource dispatched to wrong template");
    }

    @Test(description = "Test dispatching with URL. /hello/echo2/suffix.hello")
    public void testPathParamWithInvalidSuffix() {
        String path = "/hello/echo2/suffix.hello";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());

        Assert.assertEquals(((BMap<String, BValue>) bJson).get("echo3").stringValue(), "suffix.hello"
                , "Resource dispatched to wrong template");
    }

    @Test(description = "Test dispatching with URL. /hello/echo2/rs.654.58.id")
    public void testPathSegmentContainsBothLeadingDotsAndSuffix() {
        String path = "/hello/echo2/Rs.654.58.id";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());

        Assert.assertEquals(((BMap<String, BValue>) bJson).get("echo6").stringValue(), "Rs.654.58"
                , "Resource dispatched to wrong template");
    }

    @Test(description = "Test dispatching with URL. /hello/echo2/hello.identity")
    public void testSpecificPathParamSuffix() {
        String path = "/hello/echo2/hello.identity";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());

        Assert.assertEquals(((BMap<String, BValue>) bJson).get("echo6").stringValue(), "identity"
                , "Resource dispatched to wrong template");
    }

    @Test(description = "Test dispatching with URL. /hello")
    public void testRootPathDefaultValues() {
        String path = "/hello?foo=zzz";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());

        Assert.assertEquals(((BMap<String, BValue>) bJson).get("third").stringValue(), "zzz"
                , "Resource dispatched to wrong template");

        Assert.assertEquals(((BMap<String, BValue>) bJson).get("echo10").stringValue(), "echo10"
                , "Resource dispatched to wrong template");
    }

    @Test(description = "Test dispatching with URL. /hello")
    public void testDefaultPathDefaultValues() {
        String path = "/hello/echo11?foo=zzz";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());

        Assert.assertEquals(((BMap<String, BValue>) bJson).get("third").stringValue(), "zzz"
                , "Resource dispatched to wrong template");

        Assert.assertEquals(((BMap<String, BValue>) bJson).get("echo11").stringValue(), "echo11"
                , "Resource dispatched to wrong template");
    }

    @Test(description = "Test dispatching with URL. /hello")
    public void testServiceRoot() {
        String path = "/echo1?foo=zzz";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());

        Assert.assertEquals(((BMap<String, BValue>) bJson).get("third").stringValue(), "zzz"
                , "Resource dispatched to wrong template");

        Assert.assertEquals(((BMap<String, BValue>) bJson).get("echo33").stringValue(), "echo1"
                , "Resource dispatched to wrong template");
    }

    @Test(description = "Test dispatching with all default values")
    public void testAllDefaultValues() {
        String path = "/echo44/echo1?foo=zzz";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());

        Assert.assertEquals(((BMap<String, BValue>) bJson).get("first").stringValue(), "zzz"
                , "Resource dispatched to wrong template");

        Assert.assertEquals(((BMap<String, BValue>) bJson).get("echo44").stringValue(), "echo1"
               , "Resource dispatched to wrong template");
    }

    @Test(description = "Test suitable method with URL. /hello/so2 ")
    public void testWrongGETMethod() {
        String path = "/hello/so2";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        int trueResponse = response.getHttpStatusCode();
        Assert.assertEquals(trueResponse, 405, "Method not found");
    }

    @Test(description = "Test suitable method with URL. /hello/echo2 ")
    public void testWrongPOSTMethod() {
        String path = "/hello/echo2";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "POST");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        int trueResponse = response.getHttpStatusCode();
        Assert.assertEquals(trueResponse, 405, "Method not found");
    }

    @Test(description = "Test suitable method with URL. /echo12/bar/bar ")
    public void testValueWithNextSegmentStartCharacter() {
        String path = "/hello/echo12/bar/bar";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("echo12").stringValue(), "bar"
                , "Resource dispatched to wrong template");
    }

    @Test(description = "Test suitable method with URL. /echo125?foo=hello ")
    public void testStringQueryParam() {
        String path = "/hello/echo125?foo=hello";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("echo125").stringValue(), "hello"
                , "Resource dispatched to wrong template");

        path = "/hello/echo125?foo=";
        cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("echo125").stringValue(), ""
                , "Resource dispatched to wrong template");
    }

    @Test(description = "Test GetQueryParam method when params are not set with URL. /paramNeg")
    public void testGetQueryParamNegative() {
        String path = "/hello/paramNeg";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("echo125").stringValue(), "", 
                "param value is not null");
    }

    @Test(description = "Test suitable method with URL. /echo13?foo=1 ")
    public void testIntegerQueryParam() {
        String path = "/hello/echo13?foo=1";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("echo13").stringValue(), "1"
                , "Resource dispatched to wrong template");

        path = "/hello/echo13?foo=";
        cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("echo13").stringValue(), "0"
                , "Resource dispatched to wrong template");
    }

    @Test(description = "Test suitable method with URL. /echo14?foo=1.11 ")
    public void testFloatQueryParam() {
        String path = "/hello/echo14?foo=1.11";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("echo14").stringValue(), "1.11"
                , "Resource dispatched to wrong template");

        path = "/hello/echo14?foo=";
        cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("echo14").stringValue(), "0.0"
                , "Resource dispatched to wrong template");
    }

    @Test(description = "Test suitable method with URL. /echo15?foo=1.11 ")
    public void testBooleanQueryParam() {
        String path = "/hello/echo15?foo=true";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("echo15").stringValue(), "true"
                , "Resource dispatched to wrong template");

        path = "/hello/echo15?foo=";
        cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("echo15").stringValue(), "false"
                , "Resource dispatched to wrong template");
    }

    @Test(description = "Test suitable method with URL. /echo155?foo=a,b&bar=c&foo=d")
    public void testSameNameQueryParam() {
        String path = "/hello/echo155?foo=a,b&bar=c&foo=d";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(ResponseReader.getReturnValue(response),
                            "{\"name1\":\"a\", \"name2\":\"b\", \"name3\":\"c\", \"name4\":\"d\"}");

        path = "/hello/echo155?foo=a,b,c";
        cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(ResponseReader.getReturnValue(response),
                            "{\"name1\":\"a\", \"name2\":\"b\", \"name3\":null, \"name4\":\"c\"}");
    }

    @Test(description = "Test suitable method with URL.")
    public void testQueryParamWithSpecialChars() {
        String path = "/hello/echo125?foo=%25aaa";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("echo125").stringValue(), "%aaa"
                , "Resource dispatched to wrong template");

        path = "/hello/echo125?foo=abc%21";
        cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("echo125").stringValue(), "abc!"
                , "Resource dispatched to wrong template");

        path = "/hello/echo125?foo=Owner%20IN%20%28%27owner1%27%2C%27owner2%27%29,Owner%20OUT";
        cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("echo125").stringValue(), "Owner IN ('owner1','owner2')"
                , "Resource dispatched to wrong template");
    }

    @Test(description = "Test GetQueryParamValue method when params are not set with URL. /paramNeg")
    public void testGetQueryParamValueNegative() {
        String path = "/hello?bar=";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("third").stringValue(), "go",
                            "param value is not null");
    }

    @Test(description = "Test GetQueryParamValues method when params are not set with URL. /paramNeg")
    public void testGetQueryParamValuesNegative() {
        String path = "/hello/paramNeg?bar=xxx,zzz";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("echo125").stringValue(), "",
                            "param value is not null");
    }

    @Test(description = "Test suitable method with URL.")
    public void testAllInOneQueryParamAPIs() {
        String path = "/hello/echo156/bar?foo=a,b&bar=c&bar=d";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(ResponseReader.getReturnValue(response),
                            "{\"map\":\"c\", \"array\":\"c\", \"value\":\"c\", \"map_\":\"a\", \"array_\":\"d\"}");

        path = "/hello/echo156/zzz?zzz=x,X&bar=x&foo=";
        cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(ResponseReader.getReturnValue(response),
                            "{\"map\":\"x\", \"array\":\"x\", \"value\":\"x\", \"map_\":\"\", \"array_\":\"X\"}");
    }

        @Test(description = "Test dispatching without verbs")
    public void testResourceWithoutMethod() {
        String path = "/echo44/echo2";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "POST");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("first").stringValue(), "zzz"
                , "Resource dispatched to wrong template");

        cMsg = MessageUtils.generateHTTPMessage(path, "HEAD");
        response = Services.invoke(TEST_EP_PORT, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("first").stringValue(), "zzz"
                , "Resource dispatched to wrong template");

        cMsg = MessageUtils.generateHTTPMessage(path, "PUT");
        response = Services.invoke(TEST_EP_PORT, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("first").stringValue(), "zzz"
                , "Resource dispatched to wrong template");

        cMsg = MessageUtils.generateHTTPMessage(path, "DELETE");
        response = Services.invoke(TEST_EP_PORT, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("first").stringValue(), "zzz"
                , "Resource dispatched to wrong template");

        cMsg = MessageUtils.generateHTTPMessage(path, "OPTIONS");
        response = Services.invoke(TEST_EP_PORT, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("first").stringValue(), "zzz"
                , "Resource dispatched to wrong template");
    }

    @Test(description = "Test dispatching for the best match")
    public void testBestMatchingResource() {
        String path = "/echo44/echo2";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("first").stringValue(), "bar"
                , "Resource dispatched to wrong template");
    }

    @Test(description = "Test default resource support")
    public void testDefaultResourceSupport() {
        String path = "/echo55/hello";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "POST", "Test");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("echo55").stringValue(), "default"
                , "Resource dispatched to wrong template");

        path = "/echo55/foo";
        cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        response = Services.invoke(TEST_EP_PORT, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("echo55").stringValue(), "default"
                , "Resource dispatched to wrong template");

        path = "/echo55/foo/";
        cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        response = Services.invoke(TEST_EP_PORT, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("echo55").stringValue(), "default"
                , "Resource dispatched to wrong template");

        path = "/echo55/foo/abc";
        cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        response = Services.invoke(TEST_EP_PORT, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("echo55").stringValue(), "/foo/*"
                , "Resource dispatched to wrong template");
    }

    @Test(description = "Test rest uri post fix. /echo66/a/b/c")
    public void testRestUriPostFix() {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/echo66/a/b/c", "GET");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());

        Assert.assertEquals(((BMap<String, BValue>) bJson).get("echo66").stringValue(), "/b/c"
                , "Wrong rest uri post fix value");

        cMsg = MessageUtils.generateHTTPMessage("/echo66/a/c", "GET");
        response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());

        Assert.assertEquals(((BMap<String, BValue>) bJson).get("echo66").stringValue(), "/c"
                , "Wrong rest uri post fix value");

        cMsg = MessageUtils.generateHTTPMessage("/echo66/a", "GET");
        response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());

        Assert.assertEquals(((BMap<String, BValue>) bJson).get("echo66").stringValue(), "empty"
                , "Wrong rest uri post fix value");
    }

    @Test(description = "Test whether requests get dispatch to the correct resource.")
    public void testMatchWithWildCard() {
        String path = "/uri/123";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found.");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());

        Assert.assertEquals(((BMap<String, BValue>) bJson).get("message").stringValue(),
                "Wildcard Params Resource is invoked.", "Request dispatched to wrong resource");
    }

    @Test(description = "Test whether requests get dispatch to the best matching resource.")
    public void testBestMatchWithWildCard() {
        String path = "/uri/123";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "POST");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found.");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());

        Assert.assertEquals(((BMap<String, BValue>) bJson).get("message").stringValue(),
                "Path Params Resource is invoked.", "Request dispatched to wrong resource");
    }

    @Test(description = "Test best match with path param different lengths.")
    public void testDifferentLengthPathParams() {
        String path = "/uri/go/wso2/ballerina/http";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found.");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());

        Assert.assertEquals(((BMap<String, BValue>) bJson).get("aaa").stringValue(), "wso2", "wrong param value");
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("bbb").stringValue(), "ballerina", "wrong param value");
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("ccc").stringValue(), "http", "wrong param value");

        path =  "/uri/go/123/456";
        cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());

        Assert.assertEquals(((BMap<String, BValue>) bJson).get("xxx").stringValue(), "123", "wrong param value");
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("yyy").stringValue(), "456", "wrong param value");
    }

    @Test(description = "Test whether requests get dispatched to the capitalized resource path.")
    public void testBestMatchWithCapitalizedPathSegments() {
        String path = "/uri/Go";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "POST");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found.");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());

        Assert.assertEquals(((BMap<String, BValue>) bJson).get("value").stringValue(), "capitalized"
                , "Request dispatched to wrong resource");
    }

    @Test()
    public void testTwistedPathSegmentsInTheSignature() {
        String path = "/uri/twisted/20/john";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found.");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());

        Assert.assertEquals(((BMap<String, BValue>) bJson).get("Name").stringValue(), "john"
                , "Request dispatched to wrong resource");
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("Age").stringValue(), "20"
                , "Request dispatched to wrong resource");
    }

    @Test()
    public void testMultiTypePathSegmentsInTheSignature() {
        String path = "/uri/type/20/ballerina/true/15.6";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found.");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());

        Assert.assertEquals(((BMap<String, BValue>) bJson).get("Name").stringValue(), "ballerina"
                , "Request dispatched to wrong resource");
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("Age").stringValue(), "21"
                , "Request dispatched to wrong resource");
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("Weight").stringValue(), "18.55"
                , "Request dispatched to wrong resource");
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("Status").stringValue(), "true"
                , "Request dispatched to wrong resource");
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("Lang").stringValue(), "ballerina"
                , "Request dispatched to wrong resource");

        path = "/uri/type/120/hello/false/15.9";
        cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found.");
        bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());

        Assert.assertEquals(((BMap<String, BValue>) bJson).get("Name").stringValue(), "hello"
                , "Request dispatched to wrong resource");
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("Age").stringValue(), "121"
                , "Request dispatched to wrong resource");
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("Weight").stringValue(), "18.85"
                , "Request dispatched to wrong resource");
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("Status").stringValue(), "false"
                , "Request dispatched to wrong resource");
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("Lang").stringValue(), "hello false"
                , "Request dispatched to wrong resource");

    }

    @Test(description = "Test best match with encoded path params.")
    public void testEncodedPathParams() {
        String path = "/uri/go/1%2F1/ballerina/1%2F3";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found.");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());

        Assert.assertEquals(((BMap<String, BValue>) bJson).get("aaa").stringValue(), "1/1", "wrong param value");
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("bbb").stringValue(), "ballerina", "wrong param value");
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("ccc").stringValue(), "1/3", "wrong param value");

        path = "/uri/go/123/456";
        cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());

        Assert.assertEquals(((BMap<String, BValue>) bJson).get("xxx").stringValue(), "123", "wrong param value");
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("yyy").stringValue(), "456", "wrong param value");
    }
}
