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

import org.ballerinalang.test.service.http.HttpBaseTest;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;

/**
 * Testing the Http headers availability in pass-through scenarios.
 */
@Test(groups = "http-test")
public class HttpHeaderTestCases extends HttpBaseTest {

    private final int servicePort = 9106;

    @Test(description = "Test outbound request headers availability at backend with URL. /product/value")
    public void testOutboundRequestHeaders() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(servicePort, "product/value"));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(), "{\"header1\":\"aaa\", \"header2\":\"bbb\"}");
    }

    @Test(description = "Test inbound response headers availability with URL. /product/id")
    public void testInboundResponseHeaders() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(servicePort, "product/id"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(), "{\"header1\":\"kkk\", \"header2\":\"jjj\"}");
    }

    @Test(description = "Test outbound request content-length header availability when nil is sent")
    public void testOutboundNonEntityBodyGetRequestHeaders() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(
                serverInstance.getServiceURLHttp(servicePort, "product/nonEntityBodyGet"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(), "No Content size related header present");
    }

    @Test(description = "Test outbound request content-length header availability when nil is sent")
    public void testOutboundEntityBodyGetRequestHeaders() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(
                serverInstance.getServiceURLHttp(servicePort, "product/entityBodyGet"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(), "Content-length header available");
    }

    @Test(description = "Test outbound request content-length header availability when request sent without body")
    public void testOutboundEntityGetRequestHeaders() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(
                serverInstance.getServiceURLHttp(servicePort, "product/entityGet"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(), "No Content size related header present");
    }

    @Test(description = "Test outbound request content-length header availability when forwarding a GET request")
    public void testOutboundForwardNoEntityBodyRequestHeaders() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(
                serverInstance.getServiceURLHttp(servicePort, "product/entityForward"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(), "No Content size related header present");
    }

    @Test(description = "Test outbound request content-length header availability when forwarding a POST request")
    public void testOutboundForwardEntityBodyRequestHeaders() throws IOException {
        HttpResponse response = HttpClientRequest.doPost(
                serverInstance.getServiceURLHttp(servicePort, "product/entityForward"), "hello",
                new HashMap<>());
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(), "Content-length header available");
    }

    @Test(description = "Test outbound request content-length header availability when using EXECUTE action")
    public void testHeadersWithExecuteAction() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(
                serverInstance.getServiceURLHttp(servicePort, "product/entityExecute"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(), "Content-length header available");
    }

    @Test(description = "Test outbound request content-length header when using EXECUTE action without body")
    public void testHeadersWithExecuteActionWithoutBody() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(
                serverInstance.getServiceURLHttp(servicePort, "product/noEntityExecute"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(), "No Content size related header present");
    }

    @Test(description = "Test converting Post payload to GET outbound call in passthrough")
    public void testPassthruWithBody() throws IOException {
        HttpResponse response = HttpClientRequest.doPost(
                serverInstance.getServiceURLHttp(servicePort, "product/passthruGet"), "HelloWorld", new HashMap<>());
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(), "Content-length header available");
    }
}
