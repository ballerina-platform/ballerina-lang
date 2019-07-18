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

import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BServerInstance;
import org.ballerinalang.test.util.HttpResponse;
import org.testng.Assert;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;

import java.io.File;

/**
 * Base test class for Auth integration test cases which starts/stops the Auth services as ballerina packages before
 * and after tests are run.
 */
public class AuthBaseTest extends BaseTest {

    protected static BServerInstance serverInstance;
    private static EmbeddedDirectoryServer embeddedDirectoryServer;

    @BeforeGroups(value = "auth-test", alwaysRun = true)
    public void start() throws Exception {
        int[] requiredPorts = new int[]{20000, 20001, 20002, 20003, 20004, 20005, 20006, 20007, 20008, 20009, 20010,
                20011, 20012, 20013, 20014, 20015, 20016, 20017, 20018, 20019, 20020, 20021, 20022, 20023, 20024,
                20025, 20026, 20027, 20028, 20101, 20102};
        embeddedDirectoryServer = new EmbeddedDirectoryServer();
        embeddedDirectoryServer.startLdapServer(20100);

        String basePath = new File("src" + File.separator + "test" + File.separator + "resources" + File.separator +
                "auth").getAbsolutePath();
        String ballerinaConfPath = basePath + File.separator + "ballerina.conf";
        String[] args = new String[]{"--config", ballerinaConfPath};
        serverInstance = new BServerInstance(balServer);
        serverInstance.startServer(basePath, "authservices", args, requiredPorts);
    }

    @AfterGroups(value = "auth-test", alwaysRun = true)
    public void cleanup() throws Exception {
        embeddedDirectoryServer.stopLdapService();
        serverInstance.removeAllLeechers();
        serverInstance.shutdownServer();
    }

    void assertOK(HttpResponse response) {
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
    }

    void assertUnauthorized(HttpResponse response) {
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), 401, "Response code mismatched");
    }

    void assertForbidden(HttpResponse response) {
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), 403, "Response code mismatched");
    }

    void assertContains(HttpResponse response, String text) {
        Assert.assertTrue(response.getData().contains(text));
    }
}
