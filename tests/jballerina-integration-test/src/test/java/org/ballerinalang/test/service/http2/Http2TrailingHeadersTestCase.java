/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.service.http2;

import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.ballerinalang.test.util.TestUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;

/**
 * Test HTTP2 listener/client with trailing headers.
 */
@Test(groups = "http2-test")
public class Http2TrailingHeadersTestCase extends Http2BaseTest {
    private final int servicePort = 9108;

    @Test(description = "Test inbound response trailers with a payload lesser than 8K")
    public void testSmallPayloadResponseTrailers() throws IOException {
        HttpResponse response = HttpClientRequest.doPost(
                serverInstance.getServiceURLHttp(servicePort, "initiator/backend/echoResponseWithTrailer"),
                "Small payload", new HashMap<>());
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(), "{\"foo\":\"Trailer for echo payload\", \"baz\":\"The second " +
                "trailer\", \"count\":2}");
        Assert.assertEquals(response.getHeaders().get("response-trailer"), "foo, baz");
    }

    @Test(description = "Test inbound response trailers with a payload greater than 8K")
    public void testLargePayloadResponseTrailers() throws IOException {
        HttpResponse response = HttpClientRequest.doPost(
                serverInstance.getServiceURLHttp(servicePort, "initiator/backend/echoResponseWithTrailer"),
                TestUtils.LARGE_ENTITY, new HashMap<>());
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(), "{\"foo\":\"Trailer for echo payload\", \"baz\":\"The second " +
                "trailer\", \"count\":2}");
        Assert.assertEquals(response.getHeaders().get("response-trailer"), "foo, baz");
    }

    @Test(description = "Test inbound response trailers with an empty payload")
    public void testEmptyPayloadResponseTrailers() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(
                serverInstance.getServiceURLHttp(servicePort, "initiator/backend/responseEmptyPayloadWithTrailer"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(), "{\"foo\":\"Trailer for empty payload\", \"baz\":\"The second " +
                "trailer for empty payload\", \"count\":2}");
        Assert.assertEquals(response.getHeaders().get("response-trailer"), "foo, baz");
    }

    @Test(description = "Test proxy behaviour with trailers and trailer count")
    public void testProxiedTrailers() throws IOException {
        HttpResponse response = HttpClientRequest.doPost(
                serverInstance.getServiceURLHttp(servicePort, "initiator/passthroughsvc/forward"), "Small payload",
                new HashMap<>());
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(), "{\"foo\":\"Trailer for echo payload\", \"baz\":\"The second " +
                "trailer\", \"count\":2}");
        Assert.assertEquals(response.getHeaders().get("response-trailer"), "foo, baz");
    }

    @Test(description = "Test pass-through setting trailers after building payload. Behavior is correct as user has " +
            "built the datasource")
    public void testPassThroughButBuildPayload() throws IOException {
        HttpResponse response = HttpClientRequest.doPost(
                serverInstance.getServiceURLHttp(servicePort, "initiator/passthroughsvc/buildPayload"), "Small payload",
                new HashMap<>());
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(), "{\"foo\":\"Trailer for echo payload\", \"baz\":\"this trailer " +
                "will get replaced\", \"count\":3}");
        Assert.assertEquals(response.getHeaders().get("response-trailer"), "foo, baz, barr");
    }
}
