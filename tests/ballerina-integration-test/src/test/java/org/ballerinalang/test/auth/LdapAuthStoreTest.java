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

import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Test cases for ldap user store based authentication/authorization scenarios.
 *
 * @since 0.983.0
 */
@Test(groups = "auth-test")
public class LdapAuthStoreTest extends AuthBaseTest {

    private final int servicePort = 9096;
    private final int authzServicePort = 9097;

    @Test(description = "Test authenticate and authorize request against ldap auth store")
    public void testAuthenticationWithInvalidCredentials() throws Exception {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Basic dmlqaXRoYTp2aWppdGhhQDEyMw==");
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(servicePort,
                "ldapAuth/disableAuthz"), headersMap);
        assertResponse(response, 401, "Authentication failure");
    }

    @Test(description = "Test authenticate request against ldap auth store")
    public void testAuthenticationWithLDAPAuthstoreWithoutAuthorization() throws Exception {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Basic dmlqaXRoYTpiYWxsZXJpbmE=");
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(servicePort,
                "ldapAuth/disableAuthz"), headersMap);
        assertResponse(response, 200, "Hello, World!!!");
    }

    @Test(description = "Test authenticate and authorize request against ldap auth store")
    public void testAuthenticationWithLDAPAuthstoreWithAuthorization() throws Exception {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Basic dmlqaXRoYTpiYWxsZXJpbmE=");
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(servicePort,
                "ldapAuth/enableAuthz"), headersMap);
        assertResponse(response, 200, "Hello, World!!!");
    }

    @Test(description = "Test the failure of authorization request against ldap auth store")
    public void testAuthorizatioFailureWithLDAPAuthstore() throws Exception {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Basic dmlqaXRoYTpiYWxsZXJpbmE=");
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(authzServicePort,
                "auth/failAuthz"), headersMap);
        assertResponse(response, 403, "Authorization failure");
    }

    private void assertResponse(HttpResponse response, int statusCode, String message) {
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), statusCode, "Response code mismatched");
        Assert.assertEquals(response.getData(), message, "Response message content mismatched.");
    }
}
