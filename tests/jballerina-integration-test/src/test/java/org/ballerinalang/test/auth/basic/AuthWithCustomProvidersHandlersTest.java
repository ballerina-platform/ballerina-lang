/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.auth.basic;

import org.ballerinalang.test.auth.AuthBaseTest;
import org.ballerinalang.test.util.HttpResponse;
import org.ballerinalang.test.util.HttpsClientRequest;
import org.testng.annotations.Test;

/**
 * Test cases for authentication with inbound/outbound custom providers and handlers.
 */
@Test(groups = "auth-test")
public class AuthWithCustomProvidersHandlersTest extends AuthBaseTest {

    private final int servicePort = 20007;

    @Test(description = "Auth success with outbound custom auth provider initially generated token")
    public void testAuthSuccess1() throws Exception {
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort, "echo/test1"),
                basicAuthServerInstance.getServerHome());
        assertOK(response);
    }

    @Test(description = "Auth success with outbound custom auth provider generated token after inspection")
    public void testAuthSuccess2() throws Exception {
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort, "echo/test2"),
                basicAuthServerInstance.getServerHome());
        assertOK(response);
    }

    @Test(description = "Auth failure with outbound custom auth provider initially generated token and token after " +
            "inspection")
    public void testAuthFailure() throws Exception {
        HttpResponse response = HttpsClientRequest.doGet(
                basicAuthServerInstance.getServiceURLHttps(servicePort, "echo/test3"),
                basicAuthServerInstance.getServerHome());
        assertForbidden(response);
    }
}
