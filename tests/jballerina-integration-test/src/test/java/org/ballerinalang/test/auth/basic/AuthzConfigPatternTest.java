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

package org.ballerinalang.test.auth.basic;

import org.ballerinalang.test.auth.AuthBaseTest;
import org.ballerinalang.test.util.HttpResponse;
import org.ballerinalang.test.util.HttpsClientRequest;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Test cases for authorization config pattern test scenarios.
 * The followings are the config patterns used for authorization scopes:
 * Pattern 1 - scopes: ["scope1"]
 * Pattern 2 - scopes: ["scope1", "scope2"]
 * Pattern 3 - scopes: [["scope1"]]
 * Pattern 4 - scopes: [["scope1"], ["scope3"]]
 * Pattern 5 - scopes: [["scope1", "scope2"], ["scope3", "scope4"]]
 */
@Test(groups = "auth-test")
public class AuthzConfigPatternTest extends AuthBaseTest {

    private final int servicePort = 20010;

    @Test(description = "Test valid user for pattern 1")
    public void testValidUserForPattern1() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Basic Y2hhbmFrYToxMjM=");
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort, "echo/test1"),
                headers, basicAuthServerInstance.getServerHome());
        assertOK(response);
    }

    @Test(description = "Test valid user for pattern 2")
    public void testValidUserForPattern2() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Basic Y2hhbmFrYToxMjM=");
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort, "echo/test2"),
                headers, basicAuthServerInstance.getServerHome());
        assertOK(response);
    }

    @Test(description = "Test valid user for pattern 3")
    public void testValidUserForPattern3() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Basic Y2hhbmFrYToxMjM=");
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort, "echo/test3"),
                headers, basicAuthServerInstance.getServerHome());
        assertOK(response);
    }

    @Test(description = "Test valid user for pattern 4")
    public void testValidUserForPattern4() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Basic Y2hhbmFrYToxMjM=");
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort, "echo/test4"),
                headers, basicAuthServerInstance.getServerHome());
        assertOK(response);
    }

    @Test(description = "Test valid user for pattern 5")
    public void testValidUserForPattern5() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Basic Y2hhbmFrYToxMjM=");
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort, "echo/test5"),
                headers, basicAuthServerInstance.getServerHome());
        assertOK(response);
    }

    @Test(description = "Test invalid user for pattern 1")
    public void testInvalidUserForPattern1() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Basic aXN1cnU6eHh4");
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort, "echo/test1"),
                headers, basicAuthServerInstance.getServerHome());
        assertForbidden(response);
    }

    @Test(description = "Test invalid user for pattern 2")
    public void testInvalidUserForPattern2() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Basic aXN1cnU6eHh4");
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort, "echo/test2"),
                headers, basicAuthServerInstance.getServerHome());
        assertForbidden(response);
    }

    @Test(description = "Test invalid user for pattern 3")
    public void testInvalidUserForPattern3() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Basic aXN1cnU6eHh4");
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort, "echo/test3"),
                headers, basicAuthServerInstance.getServerHome());
        assertForbidden(response);
    }

    @Test(description = "Test invalid user for pattern 4")
    public void testInvalidUserForPattern4() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Basic aXN1cnU6eHh4");
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort, "echo/test4"),
                headers, basicAuthServerInstance.getServerHome());
        assertForbidden(response);
    }

    @Test(description = "Test invalid user for pattern 5")
    public void testInvalidUserForPattern5() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Basic aXN1cnU6eHh4");
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort, "echo/test5"),
                headers, basicAuthServerInstance.getServerHome());
        assertForbidden(response);
    }
}
