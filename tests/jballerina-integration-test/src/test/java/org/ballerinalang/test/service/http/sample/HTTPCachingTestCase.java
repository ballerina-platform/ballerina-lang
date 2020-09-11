/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.service.http.sample;

import io.netty.handler.codec.http.HttpHeaderNames;
import org.ballerinalang.test.service.http.HttpBaseTest;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

/**
 * Test cases for HTTP caching.
 *
 * @since 1.0
 */
@Test(groups = "http-test")
public class HTTPCachingTestCase extends HttpBaseTest {

    private final String serviceHitCount = "x-service-hit-count";
    private final String payload = "{\"message\":\"Hello, World!\"}";
    private final String proxyHitCount = "x-proxy-hit-count";
    private final int cachingProxyPort = 9244;

    @Test(description = "Test basic caching behaviour")
    public void testPassthroughServiceByBasePath() throws IOException, InterruptedException {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(9239, "cache"));
        assertEquals(response.getResponseCode(), 200);
        assertEquals(response.getHeaders().get(serviceHitCount), "1");
        assertEquals(response.getHeaders().get(proxyHitCount), "1");
        assertEquals(response.getData(), payload);

        response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(9239, "cache"));
        assertEquals(response.getResponseCode(), 200);
        assertEquals(response.getHeaders().get(serviceHitCount), "1");
        assertEquals(response.getHeaders().get(proxyHitCount), "2");
        assertEquals(response.getData(), payload);

        // Wait for a while before sending the next request
        Thread.sleep(1000);

        response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(9239, "cache"));
        assertEquals(response.getResponseCode(), 200);
        assertEquals(response.getHeaders().get(serviceHitCount), "1");
        assertEquals(response.getHeaders().get(proxyHitCount), "3");
        assertEquals(response.getData(), payload);
    }

    @Test(description = "Test no-cache cache control")
    public void testNoCacheCacheControl() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(cachingProxyPort, "nocache"));
        assertEquals(response.getData(), "{\"message\":\"1st response\"}");
        assertEquals(response.getHeaders().get(serviceHitCount), "1");

        response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(cachingProxyPort, "nocache"));
        assertEquals(response.getData(), "{\"message\":\"2nd response\"}");
        assertEquals(response.getHeaders().get(serviceHitCount), "2");

        response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(cachingProxyPort, "nocache"));
        assertEquals(response.getData(), "{\"message\":\"2nd response\"}");
        assertEquals(response.getHeaders().get(serviceHitCount), "3");
    }

    @Test(description = "Test max-age cache control")
    public void testMaxAgeCacheControl() throws IOException, InterruptedException {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(cachingProxyPort, "maxAge"));
        assertEquals(response.getData(), "public,max-age=5");

        response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(cachingProxyPort, "maxAge"));
        assertEquals(response.getData(), "public,max-age=5");

        Thread.sleep(5000);

        response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(cachingProxyPort, "maxAge"));
        assertEquals(response.getData(), "{\"message\":\"after cache expiration\"}");
    }

    @Test(description = "Test must-revalidate cache control")
    public void testMMustRevalidateCacheControl() throws IOException, InterruptedException {
        HttpResponse response = HttpClientRequest.doGet(
                serverInstance.getServiceURLHttp(cachingProxyPort, "mustRevalidate"));
        assertEquals(response.getData(), payload);
        assertEquals(response.getHeaders().get(serviceHitCount), "1");
        assertEquals(response.getHeaders().get(proxyHitCount), "1");

        response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(cachingProxyPort, "mustRevalidate"));
        assertEquals(response.getData(), payload);
        assertEquals(response.getHeaders().get(serviceHitCount), "1");
        assertEquals(response.getHeaders().get(proxyHitCount), "2");

        Thread.sleep(5000);

        response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(cachingProxyPort, "mustRevalidate"));
        assertEquals(response.getData(), payload);
        assertEquals(response.getHeaders().get(serviceHitCount), "2");
        assertEquals(response.getHeaders().get(proxyHitCount), "3");
    }

    @Test(description = "Test preservation of caller request headers in the validation request")
    public void testCallerRequestHeaderPreservation() throws IOException, InterruptedException {
        final String callerReqHeader = "x-caller-req-header";
        Map<String, String> headers = new HashMap<>();
        headers.put(callerReqHeader, "First Request");

        HttpResponse response = HttpClientRequest.doGet(
                serverInstance.getServiceURLHttp(cachingProxyPort, "validation-request"), headers);
        assertEquals(response.getData(), payload);
        assertEquals(response.getHeaders().get(callerReqHeader), "First Request");
        assertFalse(response.getHeaders().containsKey(HttpHeaderNames.IF_NONE_MATCH.toString()));
        assertFalse(response.getHeaders().containsKey(HttpHeaderNames.IF_MODIFIED_SINCE.toString()));

        // Since this request gets served straight from the cache, the value of 'x-caller-req-header' doesn't change.
        headers.put(callerReqHeader, "Second Request");
        response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(cachingProxyPort, "validation-request"),
                                           headers);
        assertEquals(response.getData(), payload);
        assertEquals(response.getHeaders().get(callerReqHeader), "First Request");
        assertFalse(response.getHeaders().containsKey(HttpHeaderNames.IF_NONE_MATCH.toString()));
        assertFalse(response.getHeaders().containsKey(HttpHeaderNames.IF_MODIFIED_SINCE.toString()));

        Thread.sleep(3000);

        headers.put(callerReqHeader, "Third Request");
        response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(cachingProxyPort, "validation-request"),
                                           headers);
        assertEquals(response.getData(), payload);
        assertEquals(response.getHeaders().get(callerReqHeader), "Third Request");
        assertFalse(response.getHeaders().containsKey(HttpHeaderNames.IF_NONE_MATCH.toString()));
        assertFalse(response.getHeaders().containsKey(HttpHeaderNames.IF_MODIFIED_SINCE.toString()));
    }

    @Test(description = "Test preservation of caller request headers in the validation request")
    public void testCallerRequestHeaderPreservation2() throws IOException, InterruptedException {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.IF_NONE_MATCH.toString(), "c854ce2c");

        HttpResponse response = HttpClientRequest.doGet(
                serverInstance.getServiceURLHttp(cachingProxyPort, "validation-request"), headers);
        assertEquals(response.getResponseCode(), 304);
        assertEquals(response.getResponseMessage(), "Not Modified");
        assertEquals(response.getData(), "");
    }
}
