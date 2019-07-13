/*
 * Copyright (c) 2019, WSO2 Inc. (http:www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http:www.apache.orglicensesLICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specif ic language governing permissions and limitations
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

import java.util.HashMap;
import java.util.Map;

/**
 * Testing the header server name for HTTP methods.
 */
@Test(groups = "http-test")
public class HTTPHeaderServerTestCase extends HttpBaseTest {
    private final String requestMessage = "{\"exchange\":\"nyse\",\"name\":\"WSO2\",\"value\":\"127.50\"}";

    @Test(description = "Test header server name in the successful response")
    public void testHeaderServerFromSuccessResponse() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_TEXT_PLAIN);
        HttpResponse response = HttpClientRequest.doPost(serverInstance.getServiceURLHttp(9094, "echo"),
                requestMessage, headers);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getHeaders().get(HttpHeaderNames.SERVER.toString()), "Mysql");
    }

    @Test(description = "Test header server name in the successful response")
    public void testsetHeaderServerManuallyFromSuccessResponse() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_TEXT_PLAIN);
        headers.put(HttpHeaderNames.SERVER.toString(), "JMS");
        HttpResponse response = HttpClientRequest.doPost(serverInstance.getServiceURLHttp(9094, "echo"),
                requestMessage, headers);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getHeaders().get(HttpHeaderNames.SERVER.toString()), "Mysql");
    }

    @Test(description = "Test header server name in the unsuccessful response")
    public void testDefaultHeaderServerFromSuccessResponse() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_TEXT_PLAIN);
        HttpResponse response = HttpClientRequest.doPost(serverInstance.getServiceURLHttp(9101, "echo"),
                requestMessage, headers);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertTrue(response.getHeaders().get(HttpHeaderNames.SERVER.toString()).contains("ballerina"));
    }

    @Test(description = "Test header server name in the unsuccessful response")
    public void testHeaderServerFromUnSuccessResponse() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_TEXT_PLAIN);
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(9094, "echo"));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), 405, "Response code mismatched");
        Assert.assertEquals(response.getHeaders().get(HttpHeaderNames.SERVER.toString()), "Mysql");
    }

    @Test(description = "Test default header server name in the unsuccessful response")
    public void testDefaultHeaderServerFromUnSuccessResponse() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_TEXT_PLAIN);
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(9101, "echo"));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), 405, "Response code mismatched");
        Assert.assertTrue(response.getHeaders().get(HttpHeaderNames.SERVER.toString()).contains("ballerina"));
    }

    @Test(description = "Test header server name in the unsuccessful response")
    public void testHeaderServerFromUnSuccessResponse1() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_TEXT_PLAIN);
        HttpResponse response = HttpClientRequest.doPost(serverInstance.getServiceURLHttp(9094, "/echo"),
                requestMessage, headers);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), 404, "Response code mismatched");
        Assert.assertEquals(response.getHeaders().get(HttpHeaderNames.SERVER.toString()), "Mysql");
    }

    @Test(description = "Test default header server name in the unsuccessful response")
    public void testDefaultHeaderServerFromUnSuccessResponse1() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_TEXT_PLAIN);
        HttpResponse response = HttpClientRequest.doPost(serverInstance.getServiceURLHttp(9101, "/echo"),
                requestMessage, headers);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), 404, "Response code mismatched");
        Assert.assertTrue(response.getHeaders().get(HttpHeaderNames.SERVER.toString()).contains("ballerina"));
    }

    @Test(description = "Test header server name if 500 response is returned when the server times out. " +
            "In this case a sleep is introduced in the server.")
    public void test500Response() throws Exception {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(9112,
                "idle/timeout500"));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getHeaders().get(HttpHeaderNames.SERVER.toString()), "Mysql");
        Assert.assertEquals(response.getResponseCode(), 500, "Response code mismatched");
    }
}
