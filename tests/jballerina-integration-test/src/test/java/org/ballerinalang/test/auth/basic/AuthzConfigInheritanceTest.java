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
 * Test cases for authorization config inheritance scenarios.
 */
@Test(groups = "auth-test")
public class AuthzConfigInheritanceTest extends AuthBaseTest {

    private final int servicePort1 = 20001;
    private final int servicePort2 = 20002;
    private final int servicePort3 = 20003;

    @Test(description = "Listener - valid scopes, service - valid scopes and resource - valid scopes")
    public void testValidScopesAtListener1() throws Exception {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Basic aXNoYXJhOmFiYw==");
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort1, "echo1/test1"),
                headersMap, basicAuthServerInstance.getServerHome());
        assertOK(response);
    }

    @Test(description = "Listener - valid scopes, service - valid scopes and resource - invalid scopes")
    public void testValidScopesAtListener2() throws Exception {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Basic aXNoYXJhOmFiYw==");
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort1, "echo1/test2"),
                headersMap, basicAuthServerInstance.getServerHome());
        assertForbidden(response);
    }

    @Test(description = "Listener - valid scopes, service - valid scopes and resource - scopes not given")
    public void testValidScopesAtListener3() throws Exception {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Basic aXNoYXJhOmFiYw==");
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort1, "echo1/test3"),
                headersMap, basicAuthServerInstance.getServerHome());
        assertOK(response);
    }

    @Test(description = "Listener - valid scopes, service - invalid scopes and resource - valid scopes")
    public void testValidScopesAtListener4() throws Exception {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Basic aXNoYXJhOmFiYw==");
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort1, "echo2/test1"),
                headersMap, basicAuthServerInstance.getServerHome());
        assertOK(response);
    }

    @Test(description = "Listener - valid scopes, service - invalid scopes and resource - invalid scopes")
    public void testValidScopesAtListener5() throws Exception {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Basic aXNoYXJhOmFiYw==");
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort1, "echo2/test2"),
                headersMap, basicAuthServerInstance.getServerHome());
        assertForbidden(response);
    }

    @Test(description = "Listener - valid scopes, service - invalid scopes and resource - scopes not given")
    public void testValidScopesAtListener6() throws Exception {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Basic aXNoYXJhOmFiYw==");
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort1, "echo2/test3"),
                headersMap, basicAuthServerInstance.getServerHome());
        assertForbidden(response);
    }

    @Test(description = "Listener - valid scopes, service - scopes not given and resource - valid scopes")
    public void testValidScopesAtListener7() throws Exception {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Basic aXNoYXJhOmFiYw==");
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort1, "echo3/test1"),
                headersMap, basicAuthServerInstance.getServerHome());
        assertOK(response);
    }

    @Test(description = "Listener - valid scopes, service - scopes not given and resource - invalid scopes")
    public void testValidScopesAtListener8() throws Exception {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Basic aXNoYXJhOmFiYw==");
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort1, "echo3/test2"),
                headersMap, basicAuthServerInstance.getServerHome());
        assertForbidden(response);
    }

    @Test(description = "Listener - valid scopes, service - scopes not given and resource - scopes not given")
    public void testValidScopesAtListener9() throws Exception {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Basic aXNoYXJhOmFiYw==");
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort1, "echo3/test3"),
                headersMap, basicAuthServerInstance.getServerHome());
        assertOK(response);
    }

    @Test(description = "Listener - invalid scopes, service - valid scopes and resource - valid scopes")
    public void testInValidScopesAtListener1() throws Exception {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Basic aXNoYXJhOmFiYw==");
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort2, "echo1/test1"),
                headersMap, basicAuthServerInstance.getServerHome());
        assertOK(response);
    }

    @Test(description = "Listener - invalid scopes, service - valid scopes and resource - invalid scopes")
    public void testInValidScopesAtListener2() throws Exception {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Basic aXNoYXJhOmFiYw==");
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort2, "echo1/test2"),
                headersMap, basicAuthServerInstance.getServerHome());
        assertForbidden(response);
    }

    @Test(description = "Listener - invalid scopes, service - valid scopes and resource - scopes not given")
    public void testInValidScopesAtListener3() throws Exception {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Basic aXNoYXJhOmFiYw==");
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort2, "echo1/test3"),
                headersMap, basicAuthServerInstance.getServerHome());
        assertOK(response);
    }

    @Test(description = "Listener - invalid scopes, service - invalid scopes and resource - valid scopes")
    public void testInValidScopesAtListener4() throws Exception {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Basic aXNoYXJhOmFiYw==");
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort2, "echo2/test1"),
                headersMap, basicAuthServerInstance.getServerHome());
        assertOK(response);
    }

    @Test(description = "Listener - invalid scopes, service - invalid scopes and resource - invalid scopes")
    public void testInValidScopesAtListener5() throws Exception {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Basic aXNoYXJhOmFiYw==");
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort2, "echo2/test2"),
                headersMap, basicAuthServerInstance.getServerHome());
        assertForbidden(response);
    }

    @Test(description = "Listener - invalid scopes, service - invalid scopes and resource - scopes not given")
    public void testInValidScopesAtListener6() throws Exception {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Basic aXNoYXJhOmFiYw==");
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort2, "echo2/test3"),
                headersMap, basicAuthServerInstance.getServerHome());
        assertForbidden(response);
    }

    @Test(description = "Listener - invalid scopes, service - scopes not given and resource - valid scopes")
    public void testInValidScopesAtListener7() throws Exception {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Basic aXNoYXJhOmFiYw==");
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort2, "echo3/test1"),
                headersMap, basicAuthServerInstance.getServerHome());
        assertOK(response);
    }

    @Test(description = "Listener - invalid scopes, service - scopes not given and resource - invalid scopes")
    public void testInValidScopesAtListener8() throws Exception {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Basic aXNoYXJhOmFiYw==");
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort2, "echo3/test2"),
                headersMap, basicAuthServerInstance.getServerHome());
        assertForbidden(response);
    }

    @Test(description = "Listener - invalid scopes, service - scopes not given and resource - scopes not given")
    public void testInValidScopesAtListener9() throws Exception {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Basic aXNoYXJhOmFiYw==");
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort2, "echo3/test3"),
                headersMap, basicAuthServerInstance.getServerHome());
        assertForbidden(response);
    }

    @Test(description = "Listener - scopes not given, service - valid scopes and resource - valid scopes")
    public void testNotGivenScopesAtListener1() throws Exception {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Basic aXNoYXJhOmFiYw==");
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort3, "echo1/test1"),
                headersMap, basicAuthServerInstance.getServerHome());
        assertOK(response);
    }

    @Test(description = "Listener - scopes not given, service - valid scopes and resource - invalid scopes")
    public void testNotGivenScopesAtListener2() throws Exception {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Basic aXNoYXJhOmFiYw==");
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort3, "echo1/test2"),
                headersMap, basicAuthServerInstance.getServerHome());
        assertForbidden(response);
    }

    @Test(description = "Listener - scopes not given, service - valid scopes and resource - scopes not given")
    public void testNotGivenScopesAtListener3() throws Exception {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Basic aXNoYXJhOmFiYw==");
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort3, "echo1/test3"),
                headersMap, basicAuthServerInstance.getServerHome());
        assertOK(response);
    }

    @Test(description = "Listener - scopes not given, service - invalid scopes and resource - valid scopes")
    public void testNotGivenScopesAtListener4() throws Exception {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Basic aXNoYXJhOmFiYw==");
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort3, "echo2/test1"),
                headersMap, basicAuthServerInstance.getServerHome());
        assertOK(response);
    }

    @Test(description = "Listener - scopes not given, service - invalid scopes and resource - invalid scopes")
    public void testNotGivenScopesAtListener5() throws Exception {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Basic aXNoYXJhOmFiYw==");
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort3, "echo2/test2"),
                headersMap, basicAuthServerInstance.getServerHome());
        assertForbidden(response);
    }

    @Test(description = "Listener - scopes not given, service - invalid scopes and resource - scopes not given")
    public void testNotGivenScopesAtListener6() throws Exception {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Basic aXNoYXJhOmFiYw==");
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort3, "echo2/test3"),
                headersMap, basicAuthServerInstance.getServerHome());
        assertForbidden(response);
    }

    @Test(description = "Listener - scopes not given, service - scopes not given and resource - valid scopes")
    public void testNotGivenScopesAtListener7() throws Exception {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Basic aXNoYXJhOmFiYw==");
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort3, "echo3/test1"),
                headersMap, basicAuthServerInstance.getServerHome());
        assertOK(response);
    }

    @Test(description = "Listener - scopes not given, service - scopes not given and resource - invalid scopes")
    public void testNotGivenScopesAtListener8() throws Exception {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Basic aXNoYXJhOmFiYw==");
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort3, "echo3/test2"),
                headersMap, basicAuthServerInstance.getServerHome());
        assertForbidden(response);
    }

    @Test(description = "Listener - scopes not given, service - scopes not given and resource - scopes not given")
    public void testNotGivenScopesAtListener9() throws Exception {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Basic aXNoYXJhOmFiYw==");
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort3, "echo3/test3"),
                headersMap, basicAuthServerInstance.getServerHome());
        assertOK(response);
    }
}
