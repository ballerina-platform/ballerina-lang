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

import io.netty.handler.codec.http.HttpHeaderNames;
import org.ballerinalang.core.model.util.JsonParser;
import org.ballerinalang.core.model.util.StringUtils;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.stdlib.utils.HTTPTestRequest;
import org.ballerinalang.stdlib.utils.MessageUtils;
import org.ballerinalang.stdlib.utils.Services;
import org.ballerinalang.test.util.BCompileUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;

/**
 * Test class for Uri Template based resource dispatchers.
 * Ex: /products/{productId}?regID={regID}
 */
public class UriTemplateDispatcherTest {

    private static final int TEST_EP_PORT = 9090;

    @BeforeClass()
    public void setup() {
        BCompileUtil.compile("test-src/services/dispatching/uri-template.bal");
    }

    @Test(description = "Test accessing the variables parsed with URL. /products/{productId}/{regId}",
            dataProvider = "validUrl")
    public void testValidUrlTemplateDispatching(String path) {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        final String xOrderIdHeadeName = "X-ORDER-ID";
        final String xOrderIdHeadeValue = "ORD12345";
        cMsg.setHeader(xOrderIdHeadeName, xOrderIdHeadeValue);
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        //Expected Json message : {"X-ORDER-ID":"ORD12345","ProductID":"PID123","RegID":"RID123"}
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get(xOrderIdHeadeName).stringValue(), xOrderIdHeadeValue
                , "Header value mismatched");
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("ProductID").stringValue(), "PID123"
                , "ProductID variable not set properly.");
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("RegID").stringValue(), "RID123"
                , "RegID variable not set properly.");
    }

    @Test(description = "Test resource dispatchers with invalid URL. /products/{productId}/{regId}",
            dataProvider = "inValidUrl")
    public void testInValidUrlTemplateDispatching(String path) {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        final String xOrderIdHeadeName = "X-ORDER-ID";
        final String xOrderIdHeadeValue = "ORD12345";
        cMsg.setHeader(xOrderIdHeadeName, xOrderIdHeadeValue);
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);
        Assert.assertEquals((int) response.getHttpStatusCode(), 404, "Response code mismatch");
        //checking the exception message
        String errorMessage = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertNotNull(errorMessage, "Message body null");
        Assert.assertTrue(errorMessage.contains("no matching resource found for path"),
                "Expected error not found.");
    }

    @Test(description = "Test accessing the variables parsed with URL. /products/{productId}",
            dataProvider = "validUrlWithQueryParam")
    public void testValidUrlTemplateWithQueryParamDispatching(String path) {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        //Expected Json message : {"X-ORDER-ID":"ORD12345","ProductID":"PID123","RegID":"RID123"}
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("Template").stringValue(), "T4"
                , "Resource dispatched to wrong template");
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("ProductID").stringValue(), "PID123"
                , "ProductID variable not set properly.");
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("RegID").stringValue(), "RID123"
                , "RegID variable not set properly.");
    }

    @Test(description = "Test accessing the variables parsed with URL. /products2/{productId}/{regId}/item")
    public void testValidUrlTemplate2Dispatching() {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(
                "/ecommerceservice/products2/PID125/RID125/item", "GET");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        //Expected Json message : {"Template":"T2","ProductID":"PID125","RegID":"RID125"}
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("Template").stringValue(), "T2"
                , "Resource dispatched to wrong template");
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("ProductID").stringValue(), "PID125"
                , "ProductID variable not set properly.");
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("RegID").stringValue(), "RID125"
                , "RegID variable not set properly.");
    }

    @Test(description = "Test accessing the variables parsed with URL. /products3/{productId}/{regId}/*")
    public void testValidUrlTemplate3Dispatching() {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(
                "/ecommerceservice/products3/PID125/RID125/xyz?para1=value1", "GET");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        //Expected Json message : {"Template":"T3","ProductID":"PID125","RegID":"RID125"}
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("Template").stringValue(), "T3"
                , "Resource dispatched to wrong template");
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("ProductID").stringValue(), "PID125"
                , "ProductID variable not set properly.");
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("RegID").stringValue(), "RID125"
                , "RegID variable not set properly.");
    }

    @Test(description = "Test accessing the variables parsed with URL. /products5/{productId}/reg")
    public void testValidUrlTemplate5Dispatching() {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(
                "/ecommerceservice/products5/PID125/reg?regID=RID125&para1=value1", "GET");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        //Expected Json message : {"Template":"T5","ProductID":"PID125","RegID":"RID125"}
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("Template").stringValue(), "T5"
                , "Resource dispatched to wrong template");
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("ProductID").stringValue(), "PID125"
                , "ProductID variable not set properly.");
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("RegID").stringValue(), "RID125"
                , "RegID variable not set properly.");
    }

    @Test(description = "Test dispatching with URL. /products")
    public void testUrlTemplateWithMultipleQueryParamDispatching() {
        String path = "/ecommerceservice/products?prodID=PID123&regID=RID123";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        //Expected Json message : {"X-ORDER-ID":"ORD12345","ProductID":"PID123","RegID":"RID123"}
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("Template").stringValue(), "T6"
                , "Resource dispatched to wrong template");
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("ProductID").stringValue(), "PID123"
                , "ProductID variable not set properly.");
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("RegID").stringValue(), "RID123"
                , "RegID variable not set properly.");
    }

    @Test(description = "Test dispatching with URL. /products?productId={productId}&regID={regID} "
            + "Ex: products?productID=PID%20123&regID=RID%201123")
    public void testUrlTemplateWithMultipleQueryParamWithURIEncodeCharacterDispatching() {
        String path = "/ecommerceservice/products?prodID=PID%20123&regID=RID%20123";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        //Expected Json message : {"X-ORDER-ID":"ORD12345","ProductID":"PID123","RegID":"RID123"}
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("Template").stringValue(), "T6"
                , "Resource dispatched to wrong template");
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("ProductID").stringValue(), "PID 123"
                , "ProductID variable not set properly.");
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("RegID").stringValue(), "RID 123"
                , "RegID variable not set properly.");
    }


    @DataProvider(name = "validUrl")
    public static Object[][] validUrl() {
        return new Object[][]{
                {"/ecommerceservice/products/PID123/RID123"}
                , {"/ecommerceservice/products/PID123/RID123/"}
        };
    }

    @DataProvider(name = "inValidUrl")
    public static Object[][] inValidUrl() {
        return new Object[][]{
                 {"/ecommerceservice/prod/PID123/RID123"}
                , {"/ecommerceservice/products/PID123/RID123/ID"}
                , {"/ecommerceservice/products/PID123/RID123/ID?param=value"}
                , {"/ecommerceservice/products/PID123/RID123/ID?param1=value1&param2=value2"}
        };
    }

    @DataProvider(name = "validUrlWithQueryParam")
    public static Object[][] validUrlWithQueryParam() {
        return new Object[][]{
                {"/ecommerceservice/products/PID123?regID=RID123"}
        };
    }

    @Test(description = "Test empty string resource path")
    public void testEmptyStringResourcepath() {
        String path = "/ecommerceservice/echo1";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());

        Assert.assertEquals(((BMap<String, BValue>) bJson).get("echo11").stringValue(), "echo11"
                , "Resource dispatched to wrong template");
    }

    @Test(description = "Test dispatching with OPTIONS method")
    public void testOPTIONSMethods() {
        String path = "/options/hi";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "OPTIONS", "hi");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("echo").stringValue(), "wso2"
                , "Resource dispatched to wrong template");
    }

    @Test(description = "Test dispatching with OPTIONS request with GET method")
    public void testOPTIONSWithGETMethods() {
        String path = "/options/getme";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "OPTIONS", "hi");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream()), "");
        Assert.assertEquals((int) response.getHttpStatusCode(), 200, "Response code mismatch");

        String allowHeader = cMsg.getHeader(HttpHeaderNames.ALLOW.toString());
        Assert.assertEquals(allowHeader, "GET, OPTIONS");
    }

    @Test(description = "Test dispatching with OPTIONS request with POST method")
    public void testOPTIONSWithPOSTMethods() {
        String path = "/options/post";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "OPTIONS");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream()), "");
        Assert.assertEquals((int) response.getHttpStatusCode(), 200, "Response code mismatch");

        String allowHeader = cMsg.getHeader(HttpHeaderNames.ALLOW.toString());
        Assert.assertEquals(allowHeader, "POST, OPTIONS");
    }

    @Test(description = "Test dispatching with OPTIONS request with PUT method")
    public void testOPTIONSWithPUTMethods() {
        String path = "/options/put/add";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "OPTIONS");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream()), "");
        Assert.assertEquals((int) response.getHttpStatusCode(), 200, "Response code mismatch");

        String allowHeader = response.getHeader(HttpHeaderNames.ALLOW.toString());
        Assert.assertEquals(allowHeader, "PUT, OPTIONS");
    }

    @Test(description = "Test dispatching with OPTIONS request with PATH params")
    public void testOPTIONSWithPathParams() {
        String path = "/options/put/xyz";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "OPTIONS");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream()), "");
        Assert.assertEquals((int) response.getHttpStatusCode(), 200, "Response code mismatch");

        String allowHeader = response.getHeader(HttpHeaderNames.ALLOW.toString());
        Assert.assertEquals(allowHeader, "DELETE, OPTIONS");
    }

    @Test(description = "Test dispatching with OPTIONS request multiple resources")
    public void testOPTIONSWithMultiResources() {
        String path = "/options/test";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "OPTIONS");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream()), "");
        Assert.assertEquals((int) response.getHttpStatusCode(), 200, "Response code mismatch");

        String allowHeader = response.getHeader(HttpHeaderNames.ALLOW.toString());
        Assert.assertEquals(allowHeader, "POST, UPDATE, GET, PUT, OPTIONS");
    }

    @Test(description = "Test dispatching with OPTIONS request to Root")
    public void testOPTIONSAtRootPath() {
        String path = "/options";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "OPTIONS");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(0, response.getFullMessageLength());
        Assert.assertEquals((int) response.getHttpStatusCode(), 200, "Response code mismatch");

        String allowHeader = response.getHeader(HttpHeaderNames.ALLOW.toString());
        Assert.assertEquals(allowHeader, "POST, UPDATE, OPTIONS, GET, PUT, DELETE");
    }

    @Test(description = "Test dispatching with OPTIONS request wrong Root")
    public void testOPTIONSAtWrongRootPath() {
        String path = "/optionss";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "OPTIONS");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals((int) response.getHttpStatusCode(), 404, "Response code mismatch");
        Assert.assertEquals(StringUtils
                        .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream()),
                "no matching service found for path : /optionss");
    }

    @Test(description = "Test dispatching with OPTIONS request when no resources available")
    public void testOPTIONSWhenNoResourcesAvailable() {
        String path = "/noResource";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "OPTIONS");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals((int) response.getHttpStatusCode(), 404, "Response code mismatch");
        Assert.assertEquals(StringUtils
                        .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream()),
                "no matching resource found for path : /noResource , method : OPTIONS");
    }

    @Test(description = "Test dispatching with OPTIONS request with wildcard")
    public void testOPTIONSWithWildCards() {
        String path = "/options/un";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "OPTIONS", "hi");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals((int) response.getHttpStatusCode(), 404, "Response code mismatch");
        Assert.assertEquals(StringUtils
                        .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream()),
                "no matching resource found for path : /options/un , method : OPTIONS");
    }

    @Test(description = "Test dispatching with basePath ending with forward slash")
    public void testBasePathEndingWithSlash() {
        String path = "/hello/test";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("echo").stringValue(), "sanitized"
                , "Resource dispatched to wrong template");
    }

    @Test(description = "Test dispatching with OPTIONS request with wildcard")
    public void testSpecialCharacterURI() {
        String path = "/ech%5Bo/ech%5Bo/b%5Bar";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET", null);
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");

        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("echo113").stringValue(), "b[ar"
                , "Resource dispatched to wrong template");
    }

    @Test(description = "Test dispatching with OPTIONS request with wildcard")
    public void testSpecialCharacterEscapedURI() {
        String path = "/ech%5Bo14/ech%5Bo14/b%5Bar14";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET", null);
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");

        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("echo114").stringValue(), "b[ar14"
                , "Resource dispatched to wrong template");
    }

    @Test(description = "Test a listener with no service registered", dataProvider = "SomeUrlsWithCorrectHost")
    public void testListenerWithNoServiceRegistered(String path) {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET", 9095);
        cMsg.setHeader(HttpHeaderNames.HOST.toString(), "localhost:9095");
        HttpCarbonMessage response = Services.invoke(9095, cMsg);

        Assert.assertEquals((int) response.getHttpStatusCode(), 404, "Response code mismatch");
        String errorMessage = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertNotNull(errorMessage, "Message body null");
        Assert.assertEquals(errorMessage, "no service has registered for listener : /0.0.0.0:9095");
    }

    @DataProvider(name = "SomeUrlsWithCorrectHost")
    public static Object[][] someUrls() {
        return new Object[][]{
                {""}, {"/"}, {"/products"}
        };
    }
}
