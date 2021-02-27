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

package org.ballerinalang.stdlib.services.dispatching;

import io.netty.handler.codec.http.HttpHeaderNames;
import org.ballerinalang.core.model.util.JsonParser;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BValue;
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
 * Test class for @Produces @Consumes annotation tests.
 */
public class ProducesConsumesAnnotationTest {

    private static final int TEST_EP_PORT = 9090;

    @BeforeClass()
    public void setup() {
        BCompileUtil.compile("test-src/services/dispatching/produces-consumes-test.bal");
    }

    @Test(description = "Test Consumes annotation with URL. /echo66/test1 ")
    public void testConsumesAnnotation() {
        String path = "/echo66/test1";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "POST", "Test");
        cMsg.setHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "application/xml; charset=ISO-8859-4");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("msg").stringValue(), "wso2", "Content type matched");
    }

    @Test(description = "Test incorrect Consumes annotation with URL. /echo66/test1 ")
    public void testIncorrectConsumesAnnotation() {
        String path = "/echo66/test1";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "POST", "Test");
        cMsg.setHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "compileResult/json");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        int trueResponse = response.getHttpStatusCode();
        Assert.assertEquals(trueResponse, 415, "Unsupported media type");
    }

    @Test(description = "Test bogus Consumes annotation with URL. /echo66/test1 ")
    public void testBogusConsumesAnnotation() {
        String path = "/echo66/test1";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "POST", "Test");
        cMsg.setHeader(HttpHeaderNames.CONTENT_TYPE.toString(), ",:vhjv");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        int trueResponse = response.getHttpStatusCode();
        Assert.assertEquals(trueResponse, 415, "Unsupported media type");
    }

    @Test(description = "Test Produces annotation with URL. /echo66/test2 ")
    public void testProducesAnnotation() {
        String path = "/echo66/test2";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        cMsg.setHeader(HttpHeaderNames.ACCEPT.toString(), "text/xml;q=0.3, multipart/*;Level=1;q=0.7");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("msg").stringValue(), "wso22", "media type matched");
    }

    @Test(description = "Test Produces with no Accept header with URL. /echo66/test2 ")
    public void testProducesAnnotationWithNoHeaders() {
        String path = "/echo66/test2";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("msg").stringValue(), "wso22", "media type matched");
    }

    @Test(description = "Test Produces with wildcard header with URL. /echo66/test2 ")
    public void testProducesAnnotationWithWildCard() {
        String path = "/echo66/test2";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        cMsg.setHeader(HttpHeaderNames.ACCEPT.toString(), "*/*, text/html;Level=1;q=0.7");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("msg").stringValue(), "wso22", "media type matched");
    }

    @Test(description = "Test Produces with sub type wildcard header with URL. /echo66/test2 ")
    public void testProducesAnnotationWithSubTypeWildCard() {
        String path = "/echo66/test2";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        cMsg.setHeader(HttpHeaderNames.ACCEPT.toString(), "text/*;q=0.3, text/html;Level=1;q=0.7");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("msg").stringValue(), "wso22", "media type matched");
    }

    @Test(description = "Test incorrect Produces annotation with URL. /echo66/test2 ")
    public void testIncorrectProducesAnnotation() {
        String path = "/echo66/test2";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        cMsg.setHeader(HttpHeaderNames.ACCEPT.toString(), "multipart/*;q=0.3, text/html;Level=1;q=0.7");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        int trueResponse = response.getHttpStatusCode();
        Assert.assertEquals(trueResponse, 406, "Not acceptable");
    }

    @Test(description = "Test bogus Produces annotation with URL. /echo66/test2 ")
    public void testBogusProducesAnnotation() {
        String path = "/echo66/test2";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        cMsg.setHeader(HttpHeaderNames.ACCEPT.toString(), ":,;,v567br");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        int trueResponse = response.getHttpStatusCode();
        Assert.assertEquals(trueResponse, 406, "Not acceptable");
    }

    @Test(description = "Test Produces and Consumes with URL. /echo66/test3 ")
    public void testProducesConsumeAnnotation() {
        String path = "/echo66/test3";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "POST", "Test");
        cMsg.setHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "text/plain; charset=ISO-8859-4");
        cMsg.setHeader(HttpHeaderNames.ACCEPT.toString(), "text/*;q=0.3, text/html;Level=1;q=0.7");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("msg").stringValue(), "wso222", "media types matched");
    }

    @Test(description = "Test Incorrect Produces and Consumes with URL. /echo66/test3 ")
    public void testIncorrectProducesConsumeAnnotation() {
        String path = "/echo66/test3";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "POST", "Test");
        cMsg.setHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "text/plain ; charset=ISO-8859-4");
        cMsg.setHeader(HttpHeaderNames.ACCEPT.toString(), "compileResult/xml, text/html");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        int trueResponse = response.getHttpStatusCode();
        Assert.assertEquals(trueResponse, 406, "Not acceptable");
    }

    @Test(description = "Test without Pro-Con annotation with URL. /echo67/echo1 ")
    public void testWithoutProducesConsumeAnnotation() {
        String path = "/echo67/echo1";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        cMsg.setHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "text/plain; charset=ISO-8859-4");
        cMsg.setHeader(HttpHeaderNames.ACCEPT.toString(), "text/*;q=0.3, text/html;Level=1;q=0.7");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("echo33").stringValue(), "echo1", "No media types");
    }

    @Test(description = "Test case insensitivity of produces and consumes annotation values")
    public void testCaseInSensitivityOfProduceAndConsume() {
        String path = "/echo66/test4";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "POST", "<test>TestVal</test>");
        cMsg.setHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "application/xml; charset=ISO-8859-4");
        cMsg.setHeader(HttpHeaderNames.ACCEPT.toString(), "application/json");
        HttpCarbonMessage response = Services.invoke(TEST_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("msg").stringValue(), "wso222", "media types matched");
    }
}
