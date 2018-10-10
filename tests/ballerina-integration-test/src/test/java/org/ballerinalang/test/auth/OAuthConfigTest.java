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

    @Test(description = "Test client authentication sent as body param")
    public void testCredentialBearerAsBodyParam() throws Exception {
        final String serverResponse = "{\"clientIdInBody\":true, \"hasAuthHeader\":false, \"tokenScope\":false}";

        LogLeecher serverLeecher = new LogLeecher(serverResponse);
        serverInstance.addLogLeecher(serverLeecher);

        ballerinaClient = new BMainInstance(balServer);
        ballerinaClient.runMain(BAL_FILE, null, new String[]{"POST_BODY_BEARER"});
        serverLeecher.waitForText(20000);
    }

    @Test(description = "Test client authentication sent as authentication header")
    public void testCredentialBearerAsHeader() throws Exception {
        final String serverResponse = "{\"clientIdInBody\":false, \"hasAuthHeader\":true, \"tokenScope\":false}";

        LogLeecher serverLeecher = new LogLeecher(serverResponse);
        serverInstance.addLogLeecher(serverLeecher);

        ballerinaClient = new BMainInstance(balServer);
        ballerinaClient.runMain(BAL_FILE, null, new String[]{"AUTH_HEADER_BEARER"});
        serverLeecher.waitForText(20000);
    }

    @Test(description = "Test client authentication sent as authentication header by default")
    public void testCredentialBearerDefaultToHeader() throws Exception {
        final String serverResponse = "{\"clientIdInBody\":false, \"hasAuthHeader\":true, \"tokenScope\":false}";

        LogLeecher serverLeecher = new LogLeecher(serverResponse);
        serverInstance.addLogLeecher(serverLeecher);

        ballerinaClient = new BMainInstance(balServer);
        ballerinaClient.runMain(BAL_FILE, null, new String[]{"NO_CONFIG"});
        serverLeecher.waitForText(20000);
    }

    @Test(description = "Test sending scope param in request body")
    public void testScopeConfig() throws Exception {
        final String serverResponse = "{\"clientIdInBody\":false, \"hasAuthHeader\":true, \"tokenScope\":true}";

        LogLeecher serverLeecher = new LogLeecher(serverResponse);
        serverInstance.addLogLeecher(serverLeecher);

        ballerinaClient = new BMainInstance(balServer);
        ballerinaClient.runMain(BAL_FILE, null, new String[]{"SCOPE"});
        serverLeecher.waitForText(20000);
    }

    @Test(description = "Test sending scope param in request body with credential in post body")
    public void testScopeConfigWithCredentialBearerInPostBody() throws Exception {
        final String serverResponse = "{\"clientIdInBody\":true, \"hasAuthHeader\":false, \"tokenScope\":true}";

        LogLeecher serverLeecher = new LogLeecher(serverResponse);
        serverInstance.addLogLeecher(serverLeecher);

        ballerinaClient = new BMainInstance(balServer);
        ballerinaClient.runMain(BAL_FILE, null, new String[]{"SCOPE_POST_BODY_BEARER"});
        serverLeecher.waitForText(20000);
    }
}
