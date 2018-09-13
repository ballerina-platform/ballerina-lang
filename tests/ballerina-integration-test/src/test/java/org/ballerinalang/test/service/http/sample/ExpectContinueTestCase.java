/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.service.http.sample;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.ballerinalang.test.service.http.HttpBaseTest;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.ballerinalang.test.util.TestUtils;
import org.ballerinalang.test.util.client.HttpClient;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test case for verifying the server-side 100-continue behaviour.
 */
@Test(groups = "http-test")
public class ExpectContinueTestCase extends HttpBaseTest {

    private final int servicePort = 9090;

    @Test
    public void test100Continue() {
        HttpClient httpClient = new HttpClient("localhost", servicePort);

        DefaultHttpRequest reqHeaders = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/continue");
        DefaultLastHttpContent reqPayload = new DefaultLastHttpContent(
                Unpooled.wrappedBuffer(TestUtils.LARGE_ENTITY.getBytes()));

        reqHeaders.headers().set(HttpHeaderNames.CONTENT_LENGTH, TestUtils.LARGE_ENTITY.getBytes().length);
        reqHeaders.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN);

        List<FullHttpResponse> responses = httpClient.sendExpectContinueRequest(reqHeaders, reqPayload);

        Assert.assertFalse(httpClient.waitForChannelClose());

        // 100-continue response
        Assert.assertEquals(responses.get(0).status(), HttpResponseStatus.CONTINUE);
        Assert.assertEquals(Integer.parseInt(responses.get(0).headers().get(HttpHeaderNames.CONTENT_LENGTH)), 0);

        // Actual response
        String responsePayload = TestUtils.getEntityBodyFrom(responses.get(1));
        Assert.assertEquals(responses.get(1).status(), HttpResponseStatus.OK);
        Assert.assertEquals(responsePayload, TestUtils.LARGE_ENTITY);
        Assert.assertEquals(responsePayload.getBytes().length, TestUtils.LARGE_ENTITY.getBytes().length);
        Assert.assertEquals(Integer.parseInt(responses.get(1).headers().get(HttpHeaderNames.CONTENT_LENGTH)),
                TestUtils.LARGE_ENTITY.getBytes().length);
    }

    @Test(description = "Test multipart form data request with expect:100-continue header")
    public void testMultipartWith100ContinueHeader() throws IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.EXPECT.toString(), HttpHeaderValues.CONTINUE.toString());

        Map<String, String> formData = new HashMap<>();
        formData.put("person", "engineer");
        formData.put("team", "ballerina");

        HttpResponse response = HttpClientRequest.doMultipartFormData(
                serverInstance.getServiceURLHttp(servicePort, "continue/outOfOrder"), headers, formData);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(), "Result = Key:person Value: engineer Key:team Value: ballerina");
    }
}
