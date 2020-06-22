/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.ballerinalang.test.auth.oauth2;

import org.ballerinalang.test.auth.AuthBaseTest;
import org.ballerinalang.test.util.HttpResponse;
import org.ballerinalang.test.util.HttpsClientRequest;
import org.testng.annotations.Test;

/**
 * Testing OAuth2 client authentication configurations.
 */
@Test(groups = "auth-test")
public class OAuth2ClientTest extends AuthBaseTest {

    private final int servicePort = 20201;

    @Test(description = "Test client credentials grant type with valid credentials")
    public void testClientCredentialsGrantType1() throws Exception {
        final String serverResponse = "access_granted";
        HttpResponse response = HttpsClientRequest.doGet(oauth2ServerInstance.getServiceURLHttps(servicePort,
                "echo/oauth2/CLIENT_CREDENTIALS_GRANT_TYPE_WITH_VALID_CREDENTIALS"),
                oauth2ServerInstance.getServerHome());
        assertContains(response, serverResponse);
    }

    @Test(description = "Test client credentials grant type with invalid client credentials")
    public void testClientCredentialsGrantType2() throws Exception {
        final String serverResponse = "invalid_client";
        HttpResponse response = HttpsClientRequest.doGet(oauth2ServerInstance.getServiceURLHttps(servicePort,
                "echo/oauth2/CLIENT_CREDENTIALS_GRANT_TYPE_WITH_INVALID_CREDENTIALS"),
                oauth2ServerInstance.getServerHome());
        assertContains(response, serverResponse);
    }

    @Test(description = "Test client credentials grant type with post body bearer and valid credentials")
    public void testClientCredentialsGrantType3() throws Exception {
        final String serverResponse = "access_granted";
        HttpResponse response = HttpsClientRequest.doGet(oauth2ServerInstance.getServiceURLHttps(servicePort,
                "echo/oauth2/CLIENT_CREDENTIALS_GRANT_TYPE_WITH_POST_BODY_BEARER_AND_VALID_CREDENTIALS"),
                oauth2ServerInstance.getServerHome());
        assertContains(response, serverResponse);
    }

    @Test(description = "Test client credentials grant type with post body bearer and invalid credentials")
    public void testClientCredentialsGrantType4() throws Exception {
        final String serverResponse = "invalid_client";
        HttpResponse response = HttpsClientRequest.doGet(oauth2ServerInstance.getServiceURLHttps(servicePort,
                "echo/oauth2/CLIENT_CREDENTIALS_GRANT_TYPE_WITH_POST_BODY_BEARER_AND_INVALID_CREDENTIALS"),
                oauth2ServerInstance.getServerHome());
        assertContains(response, serverResponse);
    }

    @Test(description = "Test password grant type with valid credentials")
    public void testPasswordGrantType1() throws Exception {
        final String serverResponse = "access_granted";
        HttpResponse response = HttpsClientRequest.doGet(oauth2ServerInstance.getServiceURLHttps(servicePort,
                "echo/oauth2/PASSWORD_GRANT_TYPE_WITH_VALID_CREDENTIALS"),
                oauth2ServerInstance.getServerHome());
        assertContains(response, serverResponse);
    }

    @Test(description = "Test password grant type with valid credentials and valid refresh config")
    public void testPasswordGrantType2() throws Exception {
        final String serverResponse = "access_granted";
        HttpResponse response = HttpsClientRequest.doGet(oauth2ServerInstance.getServiceURLHttps(servicePort,
                "echo/oauth2/PASSWORD_GRANT_TYPE_WITH_VALID_CREDENTIALS_AND_VALID_REFRESH_CONFIG"),
                oauth2ServerInstance.getServerHome());
        assertContains(response, serverResponse);
    }

    @Test(description = "Test password grant type with invalid username password and valid refresh config")
    public void testPasswordGrantType3() throws Exception {
        final String serverResponse = "unauthorized_client";
        HttpResponse response = HttpsClientRequest.doGet(oauth2ServerInstance.getServiceURLHttps(servicePort,
                "echo/oauth2/PASSWORD_GRANT_TYPE_WITH_INVALID_CREDENTIALS_AND_VALID_REFRESH_CONFIG"),
                oauth2ServerInstance.getServerHome());
        assertContains(response, serverResponse);
    }

    @Test(description = "Test password grant type with no credentials bearer and valid username, password")
    public void testPasswordGrantType4() throws Exception {
        final String serverResponse = "access_granted";
        HttpResponse response = HttpsClientRequest.doGet(oauth2ServerInstance.getServiceURLHttps(servicePort,
                "echo/oauth2/PASSWORD_GRANT_TYPE_WITH_NO_BEARER_AND_VALID_CREDENTIALS"),
                oauth2ServerInstance.getServerHome());
        assertContains(response, serverResponse);
    }

    @Test(description = "Test direct token mode with valid credentials and no refresh config")
    public void testDirectToken1() throws Exception {
        final String serverResponse = "access_granted";
        HttpResponse response = HttpsClientRequest.doGet(oauth2ServerInstance.getServiceURLHttps(servicePort,
                "echo/oauth2/DIRECT_TOKEN_WITH_VALID_CREDENTIALS_AND_NO_REFRESH_CONFIG"),
                oauth2ServerInstance.getServerHome());
        assertContains(response, serverResponse);
    }

    @Test(description = "Test direct token mode with invalid access token and no refresh config")
    public void testDirectToken2() throws Exception {
        final String serverResponse = "Failed to refresh access token since DirectRefreshTokenConfig is not provided.";
        HttpResponse response = HttpsClientRequest.doGet(oauth2ServerInstance.getServiceURLHttps(servicePort,
                "echo/oauth2/DIRECT_TOKEN_WITH_INVALID_CREDENTIALS_AND_NO_REFRESH_CONFIG"),
                oauth2ServerInstance.getServerHome());
        assertContains(response, serverResponse);
    }

    @Test(description = "Test direct token mode with invalid access token and valid refresh config")
    public void testDirectToken3() throws Exception {
        final String serverResponse = "access_granted";
        HttpResponse response = HttpsClientRequest.doGet(oauth2ServerInstance.getServiceURLHttps(servicePort,
                "echo/oauth2/DIRECT_TOKEN_WITH_INVALID_CREDENTIALS_AND_VALID_REFRESH_CONFIG"),
                oauth2ServerInstance.getServerHome());
        assertContains(response, serverResponse);
    }

    @Test(description = "Test direct token mode with invalid access token and no refresh config " +
            "but retry request is set as false")
    public void testDirectToken4() throws Exception {
        final String serverResponse = "Failed to get the access token since retry request is set as false.";
        HttpResponse response = HttpsClientRequest.doGet(oauth2ServerInstance.getServiceURLHttps(servicePort,
                "echo/oauth2/DIRECT_TOKEN_WITH_INVALID_CREDENTIALS_AND_NO_REFRESH_CONFIG_BUT_RETRY_REQUEST_FALSE"),
                oauth2ServerInstance.getServerHome());
        assertContains(response, serverResponse);
    }

    @Test(description = "Test direct token mode with invalid access token and valid refresh config " +
            "but retry request is set as false")
    public void testDirectToken5() throws Exception {
        final String serverResponse = "Failed to get the access token since retry request is set as false.";
        HttpResponse response = HttpsClientRequest.doGet(oauth2ServerInstance.getServiceURLHttps(servicePort,
                "echo/oauth2/DIRECT_TOKEN_WITH_INVALID_CREDENTIALS_AND_VALID_REFRESH_CONFIG_BUT_RETRY_REQUEST_FALSE"),
                oauth2ServerInstance.getServerHome());
        assertContains(response, serverResponse);
    }

    @Test(description = "Test direct token mode with invalid access token and invalid refresh config")
    public void testDirectToken6() throws Exception {
        final String serverResponse = "invalid_grant";
        HttpResponse response = HttpsClientRequest.doGet(oauth2ServerInstance.getServiceURLHttps(servicePort,
                "echo/oauth2/DIRECT_TOKEN_WITH_INVALID_CREDENTIALS_AND_INVALID_REFRESH_CONFIG"),
                oauth2ServerInstance.getServerHome());
        assertContains(response, serverResponse);
    }

    @Test(description = "Test direct token mode with valid access token and no refresh config " +
            "but retry request is set as false")
    public void testDirectToken7() throws Exception {
        final String serverResponse = "access_granted";
        HttpResponse response = HttpsClientRequest.doGet(oauth2ServerInstance.getServiceURLHttps(servicePort,
                "echo/oauth2/DIRECT_TOKEN_WITH_VALID_CREDENTIALS_AND_NO_REFRESH_CONFIG_BUT_RETRY_REQUEST_FALSE"),
                oauth2ServerInstance.getServerHome());
        assertContains(response, serverResponse);
    }
}
