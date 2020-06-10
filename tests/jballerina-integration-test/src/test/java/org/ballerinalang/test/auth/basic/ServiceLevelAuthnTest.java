/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * Test cases for service level authentication/authorization.
 */
@Test(groups = "auth-test")
public class ServiceLevelAuthnTest extends AuthBaseTest {

    private final int servicePort = 20005;

    @Test(description = "Authn and authz success test case")
    public void testAuthSuccessWithServiceLevelConfigs() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Basic aXN1cnU6eHh4");
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort, "echo/test"),
                headers, basicAuthServerInstance.getServerHome());
        assertOK(response);
    }

    @Test(description = "Authn success and authz failure test case")
    public void testAuthzFailureWithServiceLevelConfigs() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Basic aXNoYXJhOmFiYw==");
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort, "echo/test"),
                headers, basicAuthServerInstance.getServerHome());
        assertForbidden(response);
    }

    @Test(description = "Authn and authz failure test case")
    public void testAuthFailureWithServiceLevelConfigs() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Basic dGVzdDp0ZXN0MTIz");
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort, "echo/test"),
                headers, basicAuthServerInstance.getServerHome());
        assertUnauthorized(response);
    }
}
