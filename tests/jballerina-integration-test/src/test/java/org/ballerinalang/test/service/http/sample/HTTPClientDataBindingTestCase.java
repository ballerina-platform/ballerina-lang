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
package org.ballerinalang.test.service.http.sample;

import org.ballerinalang.test.service.http.HttpBaseTest;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.ballerinalang.test.util.TestUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;

/**
 * Test client data binding and error responses, Related file 47_client_data_binding.bal")
 *
 * @since 1.x.0 - //TBD
 */
@Test(groups = "http-test")
public class HTTPClientDataBindingTestCase extends HttpBaseTest {
    private final int servicePort = 9259;

    @Test(description = "Test HTTP basic client with all binding data types(targetTypes)")
    public void testAllBindingDataTypes() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(servicePort, "call/allTypes"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(), "{\"foo\":\"Trailer for chunked payload\", \"baz\":\"The second " +
                "trailer\"}");
    }

    @Test(description = "Test basic client with all HTTP request methods")
    public void testDifferentMethods() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(servicePort, "call/allMethods"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(), "{\"foo\":\"Trailer for chunked payload\", \"baz\":\"The second " +
                "trailer\"}");
    }

    @Test(description = "Test HTTP redirect client data binding")
    public void testRedirectClientDataBinding() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(servicePort, "call/redirect"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(), "{\"foo\":\"Trailer for chunked payload\", \"baz\":\"The second " +
                "trailer\"}");
    }

    @Test(description = "Test HTTP retry client data binding")
    public void testRetryClientDataBinding() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(servicePort, "call/retry"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(), "{\"foo\":\"Trailer for chunked payload\", \"baz\":\"The second " +
                "trailer\"}");
    }
}
