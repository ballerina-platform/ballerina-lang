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

package org.ballerinalang.test.auth;

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

    @Test(description = "Secured resource, secured service test case with no auth headers")
    public void testNoAuthHeaders1() throws Exception {
        HttpResponse response = HttpsClientRequest.doGet(serverInstance.getServiceURLHttps(servicePort, "echo1/test1"),
                serverInstance.getServerHome());
        assertUnauthorized(response);
    }

    @Test(description = "Non secured resource, secured service test case with no auth headers")
    public void testNoAuthHeaders2() throws Exception {
        HttpResponse response = HttpsClientRequest.doGet(serverInstance.getServiceURLHttps(servicePort, "echo1/test2"),
                serverInstance.getServerHome());
        assertOK(response);
    }

    @Test(description = "Secured resource, non secured service test case with no auth headers")
    public void testNoAuthHeaders3() throws Exception {
        HttpResponse response = HttpsClientRequest.doGet(serverInstance.getServiceURLHttps(servicePort, "echo2/test1"),
                serverInstance.getServerHome());
        assertUnauthorized(response);
    }

    @Test(description = "Non secured resource, non secured service test case with no auth headers")
    public void testNoAuthHeaders4() throws Exception {
        HttpResponse response = HttpsClientRequest.doGet(serverInstance.getServiceURLHttps(servicePort, "echo2/test2"),
                serverInstance.getServerHome());
        assertOK(response);
    }

    @Test(description = "Secured resource, secured service test case with valid auth headers")
    public void testValidAuthHeaders1() throws Exception {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Basic aXNoYXJhOmFiYw==");
        HttpResponse response = HttpsClientRequest.doGet(serverInstance.getServiceURLHttps(servicePort, "echo1/test1"),
                headersMap, serverInstance.getServerHome());
        assertOK(response);
    }

    @Test(description = "Non secured resource, secured service test case with valid auth headers")
    public void testValidAuthHeaders2() throws Exception {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Basic aXNoYXJhOmFiYw==");
        HttpResponse response = HttpsClientRequest.doGet(serverInstance.getServiceURLHttps(servicePort, "echo1/test2"),
                headersMap, serverInstance.getServerHome());
        assertOK(response);
    }

    @Test(description = "Secured resource, non secured service test case with valid auth headers")
    public void testValidAuthHeaders3() throws Exception {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Basic aXNoYXJhOmFiYw==");
        HttpResponse response = HttpsClientRequest.doGet(serverInstance.getServiceURLHttps(servicePort, "echo2/test1"),
                headersMap, serverInstance.getServerHome());
        assertOK(response);
    }

    @Test(description = "Non secured resource, non secured service test case with valid auth headers")
    public void testValidAuthHeaders4() throws Exception {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Basic aXNoYXJhOmFiYw==");
        HttpResponse response = HttpsClientRequest.doGet(serverInstance.getServiceURLHttps(servicePort, "echo2/test2"),
                headersMap, serverInstance.getServerHome());
        assertOK(response);
    }

    @Test(description = "Secured resource, secured service test case with invalid auth headers")
    public void testInvalidAuthHeaders1() throws Exception {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Basic dGVzdDp0ZXN0MTIz");
        HttpResponse response = HttpsClientRequest.doGet(serverInstance.getServiceURLHttps(servicePort, "echo1/test1"),
                headersMap, serverInstance.getServerHome());
        assertUnauthorized(response);
    }

    @Test(description = "Non secured resource, secured service test case with invalid auth headers")
    public void testInvalidAuthHeaders2() throws Exception {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Basic dGVzdDp0ZXN0MTIz");
        HttpResponse response = HttpsClientRequest.doGet(serverInstance.getServiceURLHttps(servicePort, "echo1/test2"),
                headersMap, serverInstance.getServerHome());
        assertOK(response);
    }

    @Test(description = "Secured resource, non secured service test case with invalid auth headers")
    public void testInvalidAuthHeaders3() throws Exception {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Basic dGVzdDp0ZXN0MTIz");
        HttpResponse response = HttpsClientRequest.doGet(serverInstance.getServiceURLHttps(servicePort, "echo2/test1"),
                headersMap, serverInstance.getServerHome());
        assertUnauthorized(response);
    }

    @Test(description = "Non secured resource, non secured service test case with invalid auth headers")
    public void testInvalidAuthHeaders4() throws Exception {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Basic dGVzdDp0ZXN0MTIz");
        HttpResponse response = HttpsClientRequest.doGet(serverInstance.getServiceURLHttps(servicePort, "echo2/test2"),
                headersMap, serverInstance.getServerHome());
        assertOK(response);
    }
}
