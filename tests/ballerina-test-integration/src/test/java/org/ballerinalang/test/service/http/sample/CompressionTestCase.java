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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.service.http.sample;

import io.netty.handler.codec.http.HttpHeaderNames;
import org.ballerinalang.test.context.ServerInstance;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.ballerinalang.test.util.TestConstant;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.ballerinalang.test.util.TestConstant.DEFLATE;
import static org.ballerinalang.test.util.TestConstant.ENCODING_GZIP;
import static org.ballerinalang.test.util.TestConstant.IDENTITY;

/**
 * Integration test for Compression.
 *
 * @since 0.966.0
 */
public class CompressionTestCase {
    private ServerInstance ballerinaServer;

    @BeforeClass
    private void setup() throws Exception {
        ballerinaServer = ServerInstance.initBallerinaServer();
        String balFile = new File("src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "httpService" + File.separator + "compression-annotation-test.bal")
                .getAbsolutePath();
        ballerinaServer.startBallerinaServer(balFile);
    }

    @Test(description = "Test default compression case, when no value is received for Accept-Encoding")
    public void testDefaultCompressionWithoutAcceptEncoding() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(ballerinaServer.getServiceURLHttp(
                "test1"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getHeaders().get(HttpHeaderNames.CONTENT_TYPE.toString())
                , TestConstant.CONTENT_TYPE_TEXT_PLAIN, "Content-Type mismatched");
        Assert.assertEquals(response.getHeaders().get(HttpHeaderNames.CONTENT_ENCODING.toString()),
                IDENTITY, "The content-encoding header should be identity");
    }

    @Test(description = "Test default compression case, with Accept-Encoding header")
    public void testDefaultCompressionWithAcceptEncoding() throws IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.ACCEPT_ENCODING.toString(), ENCODING_GZIP);
        HttpResponse response = HttpClientRequest.doGet(ballerinaServer.getServiceURLHttp(
                "test1"), headers);
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getHeaders().get(HttpHeaderNames.CONTENT_TYPE.toString())
                , TestConstant.CONTENT_TYPE_TEXT_PLAIN, "Content-Type mismatched");
        Assert.assertEquals(response.getHeaders().get(HttpHeaderNames.CONTENT_ENCODING.toString()),
                ENCODING_GZIP, "The content-encoding header should be gzip");
    }

    @Test(description = "Explicitly enable compression, with Accept-Encoding header")
    public void testExplicitCompressionEnabled() throws IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.ACCEPT_ENCODING.toString(), "deflate;q=1.0, gzip;q=0.8");
        HttpResponse response = HttpClientRequest.doGet(ballerinaServer.getServiceURLHttp(
                "test2"), headers);
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getHeaders().get(HttpHeaderNames.CONTENT_TYPE.toString())
                , TestConstant.CONTENT_TYPE_TEXT_PLAIN, "Content-Type mismatched");
        Assert.assertEquals(response.getHeaders().get(HttpHeaderNames.CONTENT_ENCODING.toString()),
                DEFLATE, "The content-encoding header should be deflate");
    }

    @Test(description = "Test Accept-Encoding header with a q value of 0, which means not acceptable")
    public void testAcceptEncodingWithQValueZero() throws IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.ACCEPT_ENCODING.toString(), "gzip;q=0");
        HttpResponse response = HttpClientRequest.doGet(ballerinaServer.getServiceURLHttp(
                "test2"), headers);
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getHeaders().get(HttpHeaderNames.CONTENT_TYPE.toString())
                , TestConstant.CONTENT_TYPE_TEXT_PLAIN, "Content-Type mismatched");
        Assert.assertNull(response.getHeaders().get(HttpHeaderNames.CONTENT_ENCODING.toString()),
                "qvalue of 0 means not acceptable");
    }

    @Test(description = "Explicitly disable compression, with Accept-Encoding header. When no Accept-Encoding header " +
            "present, back end, by default compresses it using gzip, hence identity should be received, if no " +
            "modification was done to content.")
    public void testExplicitCompressionDisabled() throws IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.ACCEPT_ENCODING.toString(), ENCODING_GZIP);
        HttpResponse response = HttpClientRequest.doGet(ballerinaServer.getServiceURLHttp(
                "test3"), headers);
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getHeaders().get(HttpHeaderNames.CONTENT_TYPE.toString())
                , TestConstant.CONTENT_TYPE_TEXT_PLAIN, "Content-Type mismatched");
        Assert.assertEquals(response.getHeaders().get(HttpHeaderNames.CONTENT_ENCODING.toString()),
                IDENTITY, "The content-encoding header should be identity");
    }

    @Test(description = "Explicitly disable compression, without Accept-Encoding header. When no Accept-Encoding  " +
            "header present, back end, by default compresses it using gzip, hence identity should be received, if no " +
            "modification was done to content.")
    public void testCompressionDisabledWithoutAcceptEncoding() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(ballerinaServer.getServiceURLHttp(
                "test3"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getHeaders().get(HttpHeaderNames.CONTENT_TYPE.toString())
                , TestConstant.CONTENT_TYPE_TEXT_PLAIN, "Content-Type mismatched");
        Assert.assertEquals(response.getHeaders().get(HttpHeaderNames.CONTENT_ENCODING.toString()),
                IDENTITY, "The content-encoding header should be identity");
    }

    @AfterClass
    private void cleanup() throws Exception {
        ballerinaServer.stopServer();
    }
}
