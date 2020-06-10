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

package org.ballerinalang.test.auth.ldap;

import org.ballerinalang.test.auth.AuthBaseTest;
import org.ballerinalang.test.util.HttpResponse;
import org.ballerinalang.test.util.HttpsClientRequest;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Test cases for LDAP user store based authentication/authorization scenarios.
 *
 * @since 0.983.0
 */
@Test(groups = "auth-test")
public class LdapAuthStoreTest extends AuthBaseTest {

    private final int servicePort = 20300;

    @Test(description = "Test authentication failure request against ldap auth store")
    public void testAuthenticationFailure() throws Exception {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Basic dmlqaXRoYTp2aWppdGhhQDEyMw==");
        HttpResponse response = HttpsClientRequest.doGet(ldapAuthServerInstance.getServiceURLHttps(servicePort,
                "ldapAuth/disableAuthz"), headersMap, ldapAuthServerInstance.getServerHome());
        assertUnauthorized(response);
    }

    @Test(description = "Test authentication success request against ldap auth store")
    public void testAuthenticationSuccess() throws Exception {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Basic dmlqaXRoYTpiYWxsZXJpbmE=");
        HttpResponse response = HttpsClientRequest.doGet(ldapAuthServerInstance.getServiceURLHttps(servicePort,
                "ldapAuth/disableAuthz"), headersMap, ldapAuthServerInstance.getServerHome());
        assertOK(response);
    }

    @Test(description = "Test authentication success and authorization success request against ldap auth store")
    public void testAuthenticationSuccessAndAuthorizationSuccess() throws Exception {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Basic dmlqaXRoYTpiYWxsZXJpbmE=");
        HttpResponse response = HttpsClientRequest.doGet(ldapAuthServerInstance.getServiceURLHttps(servicePort,
                "ldapAuth/enableAuthz"), headersMap, ldapAuthServerInstance.getServerHome());
        assertOK(response);
    }

    @Test(description = "Test authentication success and authorization failure request against ldap auth store")
    public void testAuthenticationSuccessAndAuthorizationFailure() throws Exception {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Basic dmlqaXRoYTpiYWxsZXJpbmE=");
        HttpResponse response = HttpsClientRequest.doGet(ldapAuthServerInstance.getServiceURLHttps(servicePort,
                "ldapAuth/failAuthz"), headersMap, ldapAuthServerInstance.getServerHome());
        assertForbidden(response);
    }
}
