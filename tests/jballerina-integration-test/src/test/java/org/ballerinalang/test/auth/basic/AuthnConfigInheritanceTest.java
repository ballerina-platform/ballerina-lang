/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.auth.basic;

import org.ballerinalang.test.auth.AuthBaseTest;
import org.ballerinalang.test.util.HttpResponse;
import org.ballerinalang.test.util.HttpsClientRequest;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Test cases for authentication config inheritance scenarios.
 */
@Test(groups = "auth-test")
public class AuthnConfigInheritanceTest extends AuthBaseTest {

    private final int servicePort = 20000;

    @Test(description = "Auth enabled resource, Auth enabled service test case with no auth headers")
    public void testNoAuthHeaders1() throws Exception {
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort, "echo1/test1"),
                basicAuthServerInstance.getServerHome());
        assertUnauthorized(response);
    }

    @Test(description = "Auth disabled resource, Auth enabled service test case with no auth headers")
    public void testNoAuthHeaders2() throws Exception {
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort, "echo1/test2"),
                basicAuthServerInstance.getServerHome());
        assertOK(response);
    }

    @Test(description = "Auth default resource, Auth enabled service test case with no auth headers")
    public void testNoAuthHeaders3() throws Exception {
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort, "echo1/test3"),
                basicAuthServerInstance.getServerHome());
        assertUnauthorized(response);
    }

    @Test(description = "Auth enabled resource, Auth disabled service test case with no auth headers")
    public void testNoAuthHeaders4() throws Exception {
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort, "echo2/test1"),
                basicAuthServerInstance.getServerHome());
        assertUnauthorized(response);
    }

    @Test(description = "Auth disabled resource, Auth disabled service test case with no auth headers")
    public void testNoAuthHeaders5() throws Exception {
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort, "echo2/test2"),
                basicAuthServerInstance.getServerHome());
        assertOK(response);
    }

    @Test(description = "Auth default resource, Auth disabled service test case with no auth headers")
    public void testNoAuthHeaders6() throws Exception {
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort, "echo2/test3"),
                basicAuthServerInstance.getServerHome());
        assertOK(response);
    }

    @Test(description = "Auth enabled resource, Auth default service test case with no auth headers")
    public void testNoAuthHeaders7() throws Exception {
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort, "echo3/test1"),
                basicAuthServerInstance.getServerHome());
        assertUnauthorized(response);
    }

    @Test(description = "Auth disabled resource, Auth default service test case with no auth headers")
    public void testNoAuthHeaders8() throws Exception {
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort, "echo3/test2"),
                basicAuthServerInstance.getServerHome());
        assertOK(response);
    }

    @Test(description = "Auth default resource, Auth default service test case with no auth headers")
    public void testNoAuthHeaders9() throws Exception {
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort, "echo3/test3"),
                basicAuthServerInstance.getServerHome());
        assertUnauthorized(response);
    }

    @Test(description = "Auth enabled resource, Auth enabled service test case with valid auth headers")
    public void testValidAuthHeaders1() throws Exception {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Basic aXNoYXJhOmFiYw==");
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort, "echo1/test1"),
                headersMap, basicAuthServerInstance.getServerHome());
        assertOK(response);
    }

    @Test(description = "Auth disabled resource, Auth enabled service test case with valid auth headers")
    public void testValidAuthHeaders2() throws Exception {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Basic aXNoYXJhOmFiYw==");
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort, "echo1/test2"),
                headersMap, basicAuthServerInstance.getServerHome());
        assertOK(response);
    }

    @Test(description = "Auth default resource, Auth enabled service test case with valid auth headers")
    public void testValidAuthHeaders3() throws Exception {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Basic aXNoYXJhOmFiYw==");
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort, "echo1/test3"),
                headersMap, basicAuthServerInstance.getServerHome());
        assertOK(response);
    }

    @Test(description = "Auth enabled resource, Auth disabled service test case with valid auth headers")
    public void testValidAuthHeaders4() throws Exception {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Basic aXNoYXJhOmFiYw==");
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort, "echo2/test1"),
                headersMap, basicAuthServerInstance.getServerHome());
        assertOK(response);
    }

    @Test(description = "Auth disabled resource, Auth disabled service test case with valid auth headers")
    public void testValidAuthHeaders5() throws Exception {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Basic aXNoYXJhOmFiYw==");
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort, "echo2/test2"),
                headersMap, basicAuthServerInstance.getServerHome());
        assertOK(response);
    }

    @Test(description = "Auth default resource, Auth disabled service test case with valid auth headers")
    public void testValidAuthHeaders6() throws Exception {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Basic aXNoYXJhOmFiYw==");
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort, "echo2/test3"),
                headersMap, basicAuthServerInstance.getServerHome());
        assertOK(response);
    }

    @Test(description = "Auth enabled resource, Auth default service test case with valid auth headers")
    public void testValidAuthHeaders7() throws Exception {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Basic aXNoYXJhOmFiYw==");
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort, "echo3/test1"),
                headersMap, basicAuthServerInstance.getServerHome());
        assertOK(response);
    }

    @Test(description = "Auth disabled resource, Auth default service test case with valid auth headers")
    public void testValidAuthHeaders8() throws Exception {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Basic aXNoYXJhOmFiYw==");
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort, "echo3/test2"),
                headersMap, basicAuthServerInstance.getServerHome());
        assertOK(response);
    }

    @Test(description = "Auth default resource, Auth default service test case with valid auth headers")
    public void testValidAuthHeaders9() throws Exception {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Basic aXNoYXJhOmFiYw==");
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort, "echo3/test3"),
                headersMap, basicAuthServerInstance.getServerHome());
        assertOK(response);
    }

    @Test(description = "Auth enabled resource, Auth enabled service test case with invalid auth headers")
    public void testInvalidAuthHeaders1() throws Exception {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Basic dGVzdDp0ZXN0MTIz");
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort, "echo1/test1"),
                headersMap, basicAuthServerInstance.getServerHome());
        assertUnauthorized(response);
    }

    @Test(description = "Auth disabled resource, Auth enabled service test case with invalid auth headers")
    public void testInvalidAuthHeaders2() throws Exception {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Basic dGVzdDp0ZXN0MTIz");
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort, "echo1/test2"),
                headersMap, basicAuthServerInstance.getServerHome());
        assertOK(response);
    }

    @Test(description = "Auth default resource, Auth enabled service test case with invalid auth headers")
    public void testInvalidAuthHeaders3() throws Exception {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Basic dGVzdDp0ZXN0MTIz");
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort, "echo1/test3"),
                headersMap, basicAuthServerInstance.getServerHome());
        assertUnauthorized(response);
    }

    @Test(description = "Auth enabled resource, Auth disabled service test case with invalid auth headers")
    public void testInvalidAuthHeaders4() throws Exception {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Basic dGVzdDp0ZXN0MTIz");
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort, "echo2/test1"),
                headersMap, basicAuthServerInstance.getServerHome());
        assertUnauthorized(response);
    }

    @Test(description = "Auth disabled resource, Auth disabled service test case with invalid auth headers")
    public void testInvalidAuthHeaders5() throws Exception {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Basic dGVzdDp0ZXN0MTIz");
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort, "echo2/test2"),
                headersMap, basicAuthServerInstance.getServerHome());
        assertOK(response);
    }

    @Test(description = "Auth default resource, Auth disabled service test case with invalid auth headers")
    public void testInvalidAuthHeaders6() throws Exception {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Basic dGVzdDp0ZXN0MTIz");
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort, "echo2/test3"),
                headersMap, basicAuthServerInstance.getServerHome());
        assertOK(response);
    }

    @Test(description = "Auth enabled resource, Auth default service test case with invalid auth headers")
    public void testInvalidAuthHeaders7() throws Exception {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Basic dGVzdDp0ZXN0MTIz");
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort, "echo3/test1"),
                headersMap, basicAuthServerInstance.getServerHome());
        assertUnauthorized(response);
    }

    @Test(description = "Auth disabled resource, Auth default service test case with invalid auth headers")
    public void testInvalidAuthHeaders8() throws Exception {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Basic dGVzdDp0ZXN0MTIz");
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort, "echo3/test2"),
                headersMap, basicAuthServerInstance.getServerHome());
        assertOK(response);
    }

    @Test(description = "Auth default resource, Auth default service test case with invalid auth headers")
    public void testInvalidAuthHeaders9() throws Exception {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Basic dGVzdDp0ZXN0MTIz");
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort, "echo3/test3"),
                headersMap, basicAuthServerInstance.getServerHome());
        assertUnauthorized(response);
    }
}
