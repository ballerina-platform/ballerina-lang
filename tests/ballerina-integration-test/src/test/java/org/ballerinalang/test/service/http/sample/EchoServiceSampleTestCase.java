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
package org.ballerinalang.test.service.http.sample;

import io.netty.handler.codec.http.HttpHeaderNames;
import org.ballerinalang.test.util.BaseTest;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.ballerinalang.test.util.HttpsClientRequest;
import org.ballerinalang.test.util.TestConstant;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Testing the Echo service sample located in
 * ballerina_home/samples/echoService/echoService.bal.
 * Request message should be returned as response message
 */
@Test(groups = "http-test")
public class EchoServiceSampleTestCase extends BaseTest {
    private final String requestMessage = "{\"exchange\":\"nyse\",\"name\":\"WSO2\",\"value\":\"127.50\"}";

    @Test(description = "Test echo service sample test case invoking base path")
    public void testEchoServiceByBasePath() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_TEXT_PLAIN);
        HttpResponse response = HttpClientRequest.doPost(serverInstance.getServiceURLHttp(9094, "echo"), requestMessage,
                headers);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getHeaders().get(HttpHeaderNames.CONTENT_TYPE.toString())
                , TestConstant.CONTENT_TYPE_TEXT_PLAIN, "Content-Type mismatched");
        //request should be returned as response
        Assert.assertEquals(response.getData(), requestMessage, "Message content mismatched");
    }

    @Test(description = "Test echo service with dynamic port sample test case")
    public void testEchoServiceWithDynamicPortByBasePath() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_JSON);
        String serviceUrl = "http://localhost:9099/echo";
        String requestMsg = "{\"key\":\"value\"}";
        HttpResponse response = HttpClientRequest.doPost(serviceUrl, requestMsg, headers);
        if (response == null) {
            //Retrying to avoid intermittent test failure
            response = HttpClientRequest.doPost(serviceUrl, requestMsg, headers);
        }
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getHeaders().get(HttpHeaderNames.CONTENT_TYPE.toString())
                , TestConstant.CONTENT_TYPE_TEXT_PLAIN, "Content-Type mismatched");
        String respMsg = "hello world";
        Assert.assertEquals(response.getData(), respMsg, "Message content mismatched");
    }

    @Test(description = "Test echo service with dynamic port shared")
    public void testEchoServiceWithDynamicPortShared() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_JSON);
        String serviceUrl = "http://localhost:9099/echoOne/abc";
        String requestMsg = "{\"key\":\"value\"}";
        HttpResponse response = HttpClientRequest.doPost(serviceUrl, requestMsg, headers);
        if (response == null) {
            //Retrying to avoid intermittent test failure
            response = HttpClientRequest.doPost(serviceUrl, requestMsg, headers);
        }
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getHeaders().get(HttpHeaderNames.CONTENT_TYPE.toString())
                , TestConstant.CONTENT_TYPE_TEXT_PLAIN, "Content-Type mismatched");
        String respMsg = "hello world";
        Assert.assertEquals(response.getData(), respMsg, "Message content mismatched");
    }

    @Test(description = "Test echo service with dynamic port and scheme https")
    public void testEchoServiceWithDynamicPortHttpsByBasePath() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_JSON);
        String serviceUrl = "https://localhost:9111/echo";
        String serverHome = serverInstance.getServerHome();
        String requestMsg = "{\"key\":\"value\"}";
        HttpResponse response = HttpsClientRequest.doPost(serviceUrl, requestMsg, headers, serverHome);
        if (response == null) {
            //Retrying to avoid intermittent test failure
            response = HttpsClientRequest.doPost(serviceUrl, requestMsg, headers, serverHome);
        }
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getHeaders().get(HttpHeaderNames.CONTENT_TYPE.toString())
                , TestConstant.CONTENT_TYPE_TEXT_PLAIN, "Content-Type mismatched");
        String respMsg = "hello world";
        Assert.assertEquals(response.getData(), respMsg, "Message content mismatched");
    }

    @Test(description = "Test echo service with dynamic port and scheme https with port shared")
    public void testEchoServiceWithDynamicPortHttpsShared() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_JSON);
        String serviceUrl = "https://localhost:9111/echoOne/abc";
        String serverHome = serverInstance.getServerHome();
        String requestMsg = "{\"key\":\"value\"}";
        HttpResponse response = HttpsClientRequest.doPost(serviceUrl, requestMsg, headers, serverHome);
        if (response == null) {
            //Retrying to avoid intermittent test failure
            response = HttpsClientRequest.doPost(serviceUrl, requestMsg, headers, serverHome);
        }
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getHeaders().get(HttpHeaderNames.CONTENT_TYPE.toString())
                , TestConstant.CONTENT_TYPE_TEXT_PLAIN, "Content-Type mismatched");
        String respMsg = "hello world";
        Assert.assertEquals(response.getData(), respMsg, "Message content mismatched");
    }

    @Test(description = "Test echo service sample test case invoking base path")
    public void testHttpImportAsAlias() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_TEXT_PLAIN);
        HttpResponse response = HttpClientRequest.doPost(serverInstance.getServiceURLHttp(9101, "echo"),
                requestMessage, headers);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getHeaders().get(HttpHeaderNames.CONTENT_TYPE.toString()),
                TestConstant.CONTENT_TYPE_TEXT_PLAIN, "Content-Type mismatched");
        //request should be returned as response
        Assert.assertEquals(response.getData(), requestMessage, "Message content mismatched");
    }
}
