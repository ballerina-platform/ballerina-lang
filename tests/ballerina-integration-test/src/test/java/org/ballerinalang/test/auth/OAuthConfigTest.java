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

package org.ballerinalang.test.auth;

import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.LogLeecher;
import org.testng.annotations.Test;

import java.nio.file.Paths;

/**
 * Testing OAuth Client Authentication Configs.
 */
@Test(groups = "auth-test")
public class OAuthConfigTest extends AuthBaseTest {

    private BMainInstance ballerinaClient;
    private static final String BAL_FILE = Paths.get("src", "test", "resources", "auth",
            "authclients", "oauth-client.bal").toFile().getAbsolutePath();

    @Test(description = "Test client credentials grant type")
    public void testClientCredentialsGrantType() throws Exception {
        final String serverResponse = "{\"success\":\"access_granted\"}";
        LogLeecher serverLeecher = new LogLeecher(serverResponse);
        serverInstance.addLogLeecher(serverLeecher);
        ballerinaClient = new BMainInstance(balServer);
        ballerinaClient.runMain(BAL_FILE, null, new String[]{"CLIENT_CREDENTIALS_GRANT_TYPE"});
        serverLeecher.waitForText(20000);
    }

    @Test(description = "Test client credentials grant type with scopes")
    public void testClientCredentialsGrantTypeWithScopes() throws Exception {
        final String serverResponse = "{\"success\":\"access_granted\"}";
        LogLeecher serverLeecher = new LogLeecher(serverResponse);
        serverInstance.addLogLeecher(serverLeecher);
        ballerinaClient = new BMainInstance(balServer);
        ballerinaClient.runMain(BAL_FILE, null, new String[]{"CLIENT_CREDENTIALS_GRANT_TYPE_WITH_SCOPES"});
        serverLeecher.waitForText(20000);
    }

    @Test(description = "Test client credentials grant type with scopes and post body bearer")
    public void testClientCredentialsGrantTypeWithScopesAndPostBodyBearer() throws Exception {
        final String serverResponse = "{\"success\":\"access_granted\"}";
        LogLeecher serverLeecher = new LogLeecher(serverResponse);
        serverInstance.addLogLeecher(serverLeecher);
        ballerinaClient = new BMainInstance(balServer);
        ballerinaClient.runMain(BAL_FILE, null,
                new String[]{"CLIENT_CREDENTIALS_GRANT_TYPE_WITH_SCOPES_AND_POST_BODY_BEARER"});
        serverLeecher.waitForText(20000);
    }

    @Test(description = "Test client credentials grant type with invalid client credentials")
    public void testClientCredentialsGrantTypeWithInvalidCredentials() throws Exception {
        final String serverResponse = "{\"error\":\"invalid_client\"}";
        LogLeecher serverLeecher = new LogLeecher(serverResponse);
        serverInstance.addLogLeecher(serverLeecher);
        ballerinaClient = new BMainInstance(balServer);
        ballerinaClient.runMain(BAL_FILE, null,
                new String[]{"CLIENT_CREDENTIALS_GRANT_TYPE_WITH_INVALID_CREDENTIALS"});
        serverLeecher.waitForText(20000);
    }

    @Test(description = "Test password grant type")
    public void testPasswordGrantType() throws Exception {
        final String serverResponse = "{\"success\":\"access_granted\"}";
        LogLeecher serverLeecher = new LogLeecher(serverResponse);
        serverInstance.addLogLeecher(serverLeecher);
        ballerinaClient = new BMainInstance(balServer);
        ballerinaClient.runMain(BAL_FILE, null, new String[]{"PASSWORD_GRANT_TYPE"});
        serverLeecher.waitForText(20000);
    }

    @Test(description = "Test password grant type with refresh config")
    public void testPasswordGrantTypeWithRefreshConfig() throws Exception {
        final String serverResponse = "{\"success\":\"access_granted\"}";
        LogLeecher serverLeecher = new LogLeecher(serverResponse);
        serverInstance.addLogLeecher(serverLeecher);
        ballerinaClient = new BMainInstance(balServer);
        ballerinaClient.runMain(BAL_FILE, null, new String[]{"PASSWORD_GRANT_TYPE_WITH_REFRESH_CONFIG"});
        serverLeecher.waitForText(20000);
    }

    @Test(description = "Test password grant type with refresh config and invalid username password")
    public void testPasswordGrantTypeWithRefreshConfigAndInvalidUsernamePassword() throws Exception {
        final String serverResponse = "{\"error\":\"unauthorized_client\"}";
        LogLeecher serverLeecher = new LogLeecher(serverResponse);
        serverInstance.addLogLeecher(serverLeecher);
        ballerinaClient = new BMainInstance(balServer);
        ballerinaClient.runMain(BAL_FILE, null,
                new String[]{"PASSWORD_GRANT_TYPE_WITH_REFRESH_CONFIG_AND_INVALID_USERNAME_PASSWORD"});
        serverLeecher.waitForText(20000);
    }

    @Test(description = "Test direct token mode")
    public void testDirectToken() throws Exception {
        final String serverResponse = "{\"success\":\"access_granted\"}";
        LogLeecher serverLeecher = new LogLeecher(serverResponse);
        serverInstance.addLogLeecher(serverLeecher);
        ballerinaClient = new BMainInstance(balServer);
        ballerinaClient.runMain(BAL_FILE, null, new String[]{"DIRECT_TOKEN"});
        serverLeecher.waitForText(20000);
    }

    @Test(description = "Test direct token mode with invalid access token")
    public void testDirectTokenWithInvalidAccessToken() throws Exception {
        final String serverResponse = "{\"success\":\"access_granted\"}";
        LogLeecher serverLeecher = new LogLeecher(serverResponse);
        serverInstance.addLogLeecher(serverLeecher);
        ballerinaClient = new BMainInstance(balServer);
        ballerinaClient.runMain(BAL_FILE, null, new String[]{"DIRECT_TOKEN_WITH_INVALID_ACCESS_TOKEN"});
        serverLeecher.waitForText(20000);
    }

    @Test(description = "Test direct token mode with invalid access token and invalid refresh token")
    public void testDirectTokenWithInvalidAccessTokenAndInvalidRefreshToken() throws Exception {
        final String serverResponse = "{\"error\":\"invalid_grant\"}";
        LogLeecher serverLeecher = new LogLeecher(serverResponse);
        serverInstance.addLogLeecher(serverLeecher);
        ballerinaClient = new BMainInstance(balServer);
        ballerinaClient.runMain(BAL_FILE, null,
                new String[]{"DIRECT_TOKEN_WITH_INVALID_ACCESS_TOKEN_AND_INVALID_REFRESH_TOKEN"});
        serverLeecher.waitForText(20000);
    }
}
