/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.service.http.sample;

import io.netty.handler.codec.http.HttpHeaderNames;
import org.ballerinalang.test.service.http.HttpBaseTest;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.ballerinalang.test.util.TestConstant;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Test HTTP client actions with direct payload.
 */
@Test(groups = "http-test")
public class HTTPClientActionsTestCase extends HttpBaseTest {
    private static final String RESPONSE_CODE_MISMATCHED = "Response code mismatched";
    private static final String MESSAGE_CONTENT_MISMATCHED = "Message content mismatched";
    private static final String SERVICE_URL_PART = "test2/";
    private final int servicePort = 9098;

    @Test
    public void testGetAction() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(servicePort,
                                                                                         SERVICE_URL_PART +
                                                                                                 "clientGet"));
        Assert.assertEquals(response.getResponseCode(), 200, RESPONSE_CODE_MISMATCHED);
        Assert.assertEquals(response.getHeaders().get(HttpHeaderNames.CONTENT_TYPE.toString()),
                TestConstant.CONTENT_TYPE_TEXT_PLAIN, "Content-Type mismatched");
        Assert.assertEquals(response.getData(), "HelloHelloHello", MESSAGE_CONTENT_MISMATCHED);
    }

    @Test
    public void testPostAction() throws IOException {
        sendAndAssert("clientPostWithoutBody",
                      "No payload");
    }

    @Test
    public void testPostActionWithBody() throws IOException {
        sendAndAssert("clientPostWithBody",
                      "Sample TextSample Xml{\"name\":\"apple\", \"color\":\"red\"}");
    }

    @Test
    public void testPostWithBlob() throws IOException {
        sendAndAssert("handleBinary", "Sample Text");
    }

    @Test
    public void testPostWithByteChannel() throws IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_TEXT_PLAIN);
        HttpResponse response = HttpClientRequest.doPost(serverInstance.getServiceURLHttp(servicePort,
                                                                                          SERVICE_URL_PART +
                                                                                                  "handleByteChannel"),
                                                         "Sample Text", headers);
        Assert.assertEquals(response.getResponseCode(), 200, RESPONSE_CODE_MISMATCHED);
        Assert.assertEquals(response.getData(), "Sample Text", MESSAGE_CONTENT_MISMATCHED);
    }

    @Test
    public void testPostWithBodyParts() throws IOException {
        sendAndAssert("handleMultiparts", "{\"name\":\"wso2\"}Hello");
    }

    @Test(description = "Tests when the call to setJsonPayload is made with a string having a new line")
    public void testPostWithStringJson() throws IOException {
        sendAndAssert("handleStringJson", "\"a\\nb\\n\"");
    }

    @Test(description = "Tests when a call to setTextPayload is made with a string having a new line while setting " +
            "the contentType header to application/json")
    public void testPostWithTextAndJsonContent() throws IOException {
        // The new line is removed by the test parser
        sendAndAssert("handleTextAndJsonContent", "ab");
    }

    @Test(description = "Call setTextPayload with text/xml contentType for invalid xml")
    public void testPostWithTextAndXmlContent() throws IOException {
        sendAndAssert("handleTextAndXmlContent", "ab");
    }

    @Test(description = "Tests setTextPayload call followed by setJsonPayload call for payload string having new lines")
    public void testPostWithTextAndJsonAlternateContent() throws IOException {
        sendAndAssert("handleTextAndJsonAlternateContent", "\"a\\nb\\n\"");
    }

    @Test(description = "Call setJsonPayload followed by setTextPayload for payload string with new lines")
    public void testPostWithStringJsonAlternate() throws IOException {
        sendAndAssert("handleStringJsonAlternate", "ab");
    }

    @Test(description = "Test client path with whitespaces")
    public void testClientPathWithWhitespaces() throws IOException {
        sendAndAssert("literal", "dispatched to white_spaced literal");
        sendAndAssert("expression", "dispatched to white_spaced expression");
    }

    private void sendAndAssert(String path, String expectedResponse) throws IOException {
        HttpResponse response = HttpClientRequest.doGet(
                serverInstance.getServiceURLHttp(servicePort, SERVICE_URL_PART + path));
        Assert.assertEquals(response.getResponseCode(), 200, RESPONSE_CODE_MISMATCHED);
        Assert.assertEquals(response.getData(), expectedResponse, MESSAGE_CONTENT_MISMATCHED);
    }
}
