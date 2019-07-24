/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.service.http.configuration;

import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.ballerinalang.test.service.http.HttpBaseTest;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.ballerinalang.test.util.TestConstant;
import org.ballerinalang.test.util.client.HttpClient;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Test case for services with requestLimit configurations.
 *
 * @since 0.990.1
 */
@Test(groups = "http-test")
public class RequestLimitsConfigurationTest extends HttpBaseTest {

    private static final String TEST_HOST = "localhost";

    @Test(description = "Tests the behaviour when url length is less than the configured threshold")
    public void testValidUrlLength() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(9234,
                        "requestUriLimit/validUrl"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getHeaders().get(HttpHeaderNames.CONTENT_TYPE.toString())
                , TestConstant.CONTENT_TYPE_TEXT_PLAIN, "Content-Type mismatched");
        Assert.assertEquals(response.getData(), "Hello World!!!", "Message content mismatched");
    }

    @Test(description = "Tests the behaviour when header size is less than the configured threshold")
    public void testValidHeaderLength() {
        String expectedMessage = "Hello World!!!";
        HttpClient httpClient = new HttpClient(TEST_HOST, 9237);
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET,
                                        "/requestHeaderLimit/validHeaderSize");
        FullHttpResponse response = httpClient.sendRequest(request);
        Assert.assertNotNull(response, "Response is empty.");
        Assert.assertEquals(response.status(), HttpResponseStatus.OK, "Response status does not match.");
        Assert.assertEquals(getEntityBodyFrom(response), expectedMessage, "Response status does not match.");
    }

    @Test(description = "Tests the behaviour when url length is greater than the configured threshold")
    public void testInvalidUrlLength() {
        String expected = "414 Request-URI Too Long";
        HttpClient httpClient = new HttpClient(TEST_HOST, 9235);
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET,
                                    "/lowRequestUriLimit/invalidUrl");
        FullHttpResponse response = httpClient.sendRequest(request);
        Assert.assertNotNull(response, "Response is empty.");
        Assert.assertEquals(response.status().toString(), expected, "Response status does not match.");
    }

    @Test(description = "Tests the behaviour when header size is greater than the configured threshold")
    public void testInvalidHeaderLength() {
        String expectedMessage = "413 Request Entity Too Large";
        HttpClient httpClient = new HttpClient(TEST_HOST, 9236);
        FullHttpRequest httpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET,
                                            "/lowRequestHeaderLimit/invalidHeaderSize");
        httpRequest.headers().set("X-Test", getLargeHeader());
        FullHttpResponse httpResponse = httpClient.sendRequest(httpRequest);
        Assert.assertNotNull(httpResponse, "Response is empty.");
        Assert.assertEquals(httpResponse.status().toString(), expectedMessage, "Response status does not match.");
    }

    private String getLargeHeader() {
        StringBuilder header = new StringBuilder("x");
        for (int i = 0; i < 9000; i++) {
            header.append("x");
        }
        return header.toString();
    }

    private  String getEntityBodyFrom(FullHttpResponse httpResponse) {
        ByteBuffer content = httpResponse.content().nioBuffer();
        StringBuilder stringContent = new StringBuilder();
        while (content.hasRemaining()) {
            stringContent.append((char) content.get());
        }
        return stringContent.toString();
    }
}
