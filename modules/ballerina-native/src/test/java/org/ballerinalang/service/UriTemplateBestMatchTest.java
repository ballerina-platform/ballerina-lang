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
 * Test class for Uri Template based resource dispatchers.
 * Ex: /products/{productId}?regID={regID}
 */
public class UriTemplateBestMatchTest {

    private ProgramFile application;

    @BeforeClass()
    public void setup() {
        application = EnvironmentInitializer.setupProgramFile("lang/service/uritemplate/uri-template-matching.bal");
    }

    @Test(description = "Test dispatching with URL. /hello/world/echo2?regid=abc")
    public void testMostSpecificMatchWithQueryParam() {
        String path = "/hello/world/echo2?regid=abc";
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        CarbonMessage response = Services.invoke(cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = ((BJSON) response.getMessageDataSource());

        Assert.assertEquals(bJson.value().get("echo1").asText(), "echo1"
                , "Resource dispatched to wrong template");
    }

    @Test(description = "Test dispatching with URL. /hello/world/echo2/bar")
    public void testMostSpecificMatchWithWildCard() {
        String path = "/hello/world/echo2/bar";
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        CarbonMessage response = Services.invoke(cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = ((BJSON) response.getMessageDataSource());

        Assert.assertEquals(bJson.value().get("echo2").asText(), "echo2"
                , "Resource dispatched to wrong template");
    }

    @Test(description = "Test dispatching with URL. /hello/world/echo2/foo/bar")
    public void testMostSpecificMatch() {
        String path = "/hello/world/echo2/foo/bar";
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        CarbonMessage response = Services.invoke(cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = ((BJSON) response.getMessageDataSource());

        Assert.assertEquals(bJson.value().get("echo3").asText(), "echo3"
                , "Resource dispatched to wrong template");
    }

    @Test(description = "Test dispatching with URL. /hello/echo2?regid=abc")
    public void testMostSpecificServiceDispatch() {
        String path = "/hello/echo2?regid=abc";
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        CarbonMessage response = Services.invoke(cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = ((BJSON) response.getMessageDataSource());

        Assert.assertEquals(bJson.value().get("echo5").asText(), "echo5"
                , "Resource dispatched to wrong template");
    }

    @Test(description = "Test dispatching with URL. /hello/echo2?regid=abc")
    public void testSubPathEndsWithPathParam() {
        String path = "/hello/echo2/shafreen";
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        CarbonMessage response = Services.invoke(cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = ((BJSON) response.getMessageDataSource());

        Assert.assertEquals(bJson.value().get("echo3").asText(), "shafreen"
                , "Resource dispatched to wrong template");
    }

    @Test(description = "Test dispatching with URL. /hello/echo2/shafreen-anfar & /hello/echo2/shafreen+anfar")
    public void testMostSpecificWithPathParam() {
        String path = "/hello/echo2/shafreen-anfar";
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        CarbonMessage response = Services.invoke(cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = ((BJSON) response.getMessageDataSource());

        Assert.assertEquals(bJson.value().get("first").asText(), "shafreen"
                , "Resource dispatched to wrong template");

        Assert.assertEquals(bJson.value().get("second").asText(), "anfar"
                , "Resource dispatched to wrong template");

        path = "/hello/echo2/shafreen+anfar";
        cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        response = Services.invoke(cMsg);
        Assert.assertNotNull(response, "Response message not found");
        bJson = ((BJSON) response.getMessageDataSource());

        Assert.assertEquals(bJson.value().get("first").asText(), "anfar"
                , "Resource dispatched to wrong template");

        Assert.assertEquals(bJson.value().get("second").asText(), "shafreen"
                , "Resource dispatched to wrong template");
    }

    @Test(description = "Test dispatching with URL. /hello/echo2/shafreen+anfar/bar")
    public void testSubPathEndsWithBar() {
        String path = "/hello/echo2/shafreen+anfar/bar";
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        CarbonMessage response = Services.invoke(cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = ((BJSON) response.getMessageDataSource());

        Assert.assertEquals(bJson.value().get("first").asText(), "shafreen"
                , "Resource dispatched to wrong template");

        Assert.assertEquals(bJson.value().get("second").asText(), "anfar"
                , "Resource dispatched to wrong template");

        Assert.assertEquals(bJson.value().get("echo4").asText(), "echo4"
                , "Resource dispatched to wrong template");
    }

    @Test(description = "Test dispatching with URL. /hello/echo2/shafreen+anfar/foo")
    public void testSubPathEndsWithFoo() {
        String path = "/hello/echo2/shafreen+anfar/foo";
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        CarbonMessage response = Services.invoke(cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = ((BJSON) response.getMessageDataSource());

        Assert.assertEquals(bJson.value().get("first").asText(), "shafreen"
                , "Resource dispatched to wrong template");

        Assert.assertEquals(bJson.value().get("second").asText(), "anfar"
                , "Resource dispatched to wrong template");

        Assert.assertEquals(bJson.value().get("echo4").asText(), "foo"
                , "Resource dispatched to wrong template");
    }

    @Test(description = "Test dispatching with URL. /hello/echo2/shafreen+anfar/foo/bar")
    public void testLeastSpecificURITemplate() {
        String path = "/hello/echo2/shafreen+anfar/foo/bar";
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        CarbonMessage response = Services.invoke(cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = ((BJSON) response.getMessageDataSource());

        Assert.assertEquals(bJson.value().get("echo5").asText(), "any"
                , "Resource dispatched to wrong template");
    }

    @Test(description = "Test dispatching with URL. /hello/echo2/shafreen+anfar/bar")
    public void testBestSpecificURITemplateWithPOST() {
        String path = "/hello/echo2/shafreen+anfar/bar";
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage(path, "POST");
        CarbonMessage response = Services.invoke(cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = ((BJSON) response.getMessageDataSource());

        Assert.assertEquals(bJson.value().get("first").asText(), "shafreen"
                , "Resource dispatched to wrong template");

        Assert.assertEquals(bJson.value().get("second").asText(), "anfar"
                , "Resource dispatched to wrong template");

        Assert.assertEquals(bJson.value().get("echo8").asText(), "echo8"
                , "Resource dispatched to wrong template");
    }

    @Test(description = "Test dispatching with URL. /hello/echo2/shafreen+anfar/bar")
    public void testParamDefaultValues() {
        String path = "/hello/echo3/shafreen+anfar?foo=bar";
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        CarbonMessage response = Services.invoke(cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = ((BJSON) response.getMessageDataSource());

        Assert.assertEquals(bJson.value().get("first").asText(), "shafreen"
                , "Resource dispatched to wrong template");

        Assert.assertEquals(bJson.value().get("second").asText(), "anfar"
                , "Resource dispatched to wrong template");

        Assert.assertEquals(bJson.value().get("third").asText(), "bar"
                , "Resource dispatched to wrong template");

        Assert.assertEquals(bJson.value().get("echo9").asText(), "echo9"
                , "Resource dispatched to wrong template");
    }

    @Test(description = "Test dispatching with URL. /hello")
    public void testRootPathDefaultValues() {
        String path = "/hello?foo=zzz";
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        CarbonMessage response = Services.invoke(cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = ((BJSON) response.getMessageDataSource());

        Assert.assertEquals(bJson.value().get("third").asText(), "zzz"
                , "Resource dispatched to wrong template");

        Assert.assertEquals(bJson.value().get("echo10").asText(), "echo10"
                , "Resource dispatched to wrong template");
    }

    @Test(description = "Test dispatching with URL. /hello")
    public void testDefaultPathDefaultValues() {
        String path = "/hello/echo11?foo=zzz";
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        CarbonMessage response = Services.invoke(cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = ((BJSON) response.getMessageDataSource());

        Assert.assertEquals(bJson.value().get("third").asText(), "zzz"
                , "Resource dispatched to wrong template");

        Assert.assertEquals(bJson.value().get("echo11").asText(), "echo11"
                , "Resource dispatched to wrong template");
    }

    @Test(description = "Test dispatching with URL. /hello")
    public void testServiceRoot() {
        String path = "/echo1?foo=zzz";
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        CarbonMessage response = Services.invoke(cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = ((BJSON) response.getMessageDataSource());

        Assert.assertEquals(bJson.value().get("third").asText(), "zzz"
                , "Resource dispatched to wrong template");

        Assert.assertEquals(bJson.value().get("echo33").asText(), "echo1"
                , "Resource dispatched to wrong template");
    }

    @Test(description = "Test dispatching with all default values")
    public void testAllDefaultValues() {
        String path = "/echo44/echo1?foo=zzz";
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        CarbonMessage response = Services.invoke(cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = ((BJSON) response.getMessageDataSource());

        Assert.assertEquals(bJson.value().get("first").asText(), "zzz"
                , "Resource dispatched to wrong template");

        Assert.assertEquals(bJson.value().get("echo44").asText(), "echo1"
               , "Resource dispatched to wrong template");
    }

    @Test(description = "Test suitable method with URL. /hello/so2 ")
    public void testWrongGETMethod() {
        String path = "/hello/so2";
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        CarbonMessage response = Services.invoke(cMsg);

        Assert.assertNotNull(response, "Response message not found");
        int trueResponse = (int) response.getProperty(Constants.HTTP_STATUS_CODE);
        Assert.assertEquals(trueResponse, 405, "Method not found");
    }

    @Test(description = "Test suitable method with URL. /hello/echo2 ")
    public void testWrongPOSTMethod() {
        String path = "/hello/echo2";
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage(path, "POST");
        CarbonMessage response = Services.invoke(cMsg);

        Assert.assertNotNull(response, "Response message not found");
        int trueResponse = (int) response.getProperty(Constants.HTTP_STATUS_CODE);
        Assert.assertEquals(trueResponse, 405, "Method not found");
    }

    @Test(description = "Test suitable method with URL. /echo12/bar/bar ")
    public void testValueWithNextSegmentStartCharacter() {
        String path = "/hello/echo12/bar/bar";
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        CarbonMessage response = Services.invoke(cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = ((BJSON) response.getMessageDataSource());
        Assert.assertEquals(bJson.value().get("echo12").asText(), "bar"
                , "Resource dispatched to wrong template");
    }

    @Test(description = "Test suitable method with URL. /echo13?foo=1 ")
    public void testIntegerQueryParam() {
        String path = "/hello/echo13?foo=1";
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        CarbonMessage response = Services.invoke(cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = ((BJSON) response.getMessageDataSource());
        Assert.assertEquals(bJson.value().get("echo13").asText(), "1"
                , "Resource dispatched to wrong template");

        path = "/hello/echo13?foo=";
        cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        response = Services.invoke(cMsg);

        Assert.assertNotNull(response, "Response message not found");
        bJson = ((BJSON) response.getMessageDataSource());
        Assert.assertEquals(bJson.value().get("echo13").asText(), "0"
                , "Resource dispatched to wrong template");

        path = "/hello/echo13";
        cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        response = Services.invoke(cMsg);

        Assert.assertNotNull(response, "Response message not found");
        bJson = ((BJSON) response.getMessageDataSource());
        Assert.assertEquals(bJson.value().get("echo13").asText(), "0"
                , "Resource dispatched to wrong template");
    }

    @Test(description = "Test suitable method with URL. /echo14?foo=1.11 ")
    public void testFloatQueryParam() {
        String path = "/hello/echo14?foo=1.11";
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        CarbonMessage response = Services.invoke(cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = ((BJSON) response.getMessageDataSource());
        Assert.assertEquals(bJson.value().get("echo14").asText(), "1.11"
                , "Resource dispatched to wrong template");

        path = "/hello/echo14?foo=";
        cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        response = Services.invoke(cMsg);

        Assert.assertNotNull(response, "Response message not found");
        bJson = ((BJSON) response.getMessageDataSource());
        Assert.assertEquals(bJson.value().get("echo14").asText(), "0.0"
                , "Resource dispatched to wrong template");

        path = "/hello/echo14";
        cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        response = Services.invoke(cMsg);

        Assert.assertNotNull(response, "Response message not found");
        bJson = ((BJSON) response.getMessageDataSource());
        Assert.assertEquals(bJson.value().get("echo14").asText(), "0.0"
                , "Resource dispatched to wrong template");
    }

    @Test(description = "Test suitable method with URL. /echo15?foo=1.11 ")
    public void testBooleanQueryParam() {
        String path = "/hello/echo15?foo=true";
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        CarbonMessage response = Services.invoke(cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = ((BJSON) response.getMessageDataSource());
        Assert.assertEquals(bJson.value().get("echo15").asText(), "true"
                , "Resource dispatched to wrong template");

        path = "/hello/echo15?foo=";
        cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        response = Services.invoke(cMsg);

        Assert.assertNotNull(response, "Response message not found");
        bJson = ((BJSON) response.getMessageDataSource());
        Assert.assertEquals(bJson.value().get("echo15").asText(), "false"
                , "Resource dispatched to wrong template");

        path = "/hello/echo15";
        cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        response = Services.invoke(cMsg);

        Assert.assertNotNull(response, "Response message not found");
        bJson = ((BJSON) response.getMessageDataSource());
        Assert.assertEquals(bJson.value().get("echo15").asText(), "false"
                , "Resource dispatched to wrong template");
    }

    @AfterClass
    public void tearDown() {
        EnvironmentInitializer.cleanup(application);
    }
}
