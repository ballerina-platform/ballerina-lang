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
package org.ballerinalang.test.service.httpv2.sample;

import org.ballerinalang.test.service.httpv2.HttpBaseTest;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Test client data binding and error responses, Related file 01_client_data_binding.bal")
 *
 * @since 1.x.0 - //TBD
 */
@Test(groups = "http-test")
public class HTTPClientDataBindingTestCase extends HttpBaseTest {
    private final int servicePort = 9300;

    @Test(description = "Test HTTP basic client with all binding data types(targetTypes)")
    public void testAllBindingDataTypes() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(servicePort, "call/allTypes"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(), "{\"id\":\"chamil\", \"values\":{\"a\":2, \"b\":45, " +
                "\"c\":{\"x\":\"mnb\", \"y\":\"uio\"}}} | <name>Ballerina</name> | This is my @4491*&&#$^($@ | " +
                "BinaryPayload is textVal | chamil | wso2 | 3 | data-binding");
    }

    @Test(description = "Test basic client with all HTTP request methods")
    public void testDifferentMethods() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(servicePort, "call/allMethods"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(), "{\"id\":\"chamil\", \"values\":{\"a\":2, \"b\":45, " +
                "\"c\":{\"x\":\"mnb\", \"y\":\"uio\"}}} | application/xml | This is my @4491*&&#$^($@ | BinaryPayload" +
                " is textVal | chamil | wso2 | 3");
    }

    @Test(description = "Test HTTP redirect client data binding")
    public void testRedirectClientDataBinding() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(servicePort, "call/redirect"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(), "{\"id\":\"chamil\", \"values\":{\"a\":2, \"b\":45, " +
                "\"c\":{\"x\":\"mnb\", \"y\":\"uio\"}}}");
    }

    @Test(description = "Test HTTP retry client data binding")
    public void testRetryClientDataBinding() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(servicePort, "call/retry"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(), "Hello World!!!");
    }

    @Test(description = "Test cast error panic for incompatible types")
    public void testCastError() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(servicePort, "call/cast"));
        Assert.assertEquals(response.getResponseCode(), 500, "Response code mismatched");
        Assert.assertEquals(response.getData(), "incompatible types: 'map<json>' cannot be cast to 'xml'");
    }

    @Test(description = "Test 500 error panic")
    public void test5XXErrorPanic() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(servicePort, "call/500"));
        Assert.assertEquals(response.getResponseCode(), 500, "Response code mismatched");
        Assert.assertEquals(response.getData(),
                            "incompatible types: 'http:RemoteServerError' cannot be cast to 'json'");
    }

    @Test(description = "Test 500 error handle")
    public void test5XXHandleError() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(servicePort, "call/500handle"));
        Assert.assertEquals(response.getResponseCode(), 501, "Response code mismatched");
        Assert.assertEquals(response.getData(), "data-binding-failed-with-501");
    }

    @Test(description = "Test 404 error panic")
    public void test4XXErrorPanic() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(servicePort, "call/404"));
        Assert.assertEquals(response.getResponseCode(), 500, "Response code mismatched");
        Assert.assertEquals(response.getData(),
                            "incompatible types: 'http:ClientRequestError' cannot be cast to 'json'");
    }

    @Test(description = "Test 404 error handle")
    public void test4XXHandleError() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(servicePort, "call/404/handle"));
        Assert.assertEquals(response.getResponseCode(), 404, "Response code mismatched");
        Assert.assertEquals(response.getData(), "no matching resource found for path : /backend/handle , method : POST");
    }

    @Test(description = "Test 405 error handle")
    public void test405HandleError() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(servicePort, "call/404/get4XX"));
        Assert.assertEquals(response.getResponseCode(), 405, "Response code mismatched");
        Assert.assertEquals(response.getData(), "method not allowed");
    }
}
