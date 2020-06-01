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

import org.apache.commons.text.StringEscapeUtils;
import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.auth.ldap.EmbeddedDirectoryServer;
import org.ballerinalang.test.context.BServerInstance;
import org.ballerinalang.test.util.HttpResponse;
import org.testng.Assert;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;

import java.io.File;
import java.nio.file.Paths;

/**
 * Base test class for Auth integration test cases which starts/stops the Auth services as ballerina packages before
 * and after tests are run.
 */
public class AuthBaseTest extends BaseTest {

    protected static BServerInstance basicAuthServerInstance;
    protected static BServerInstance jwtAuthServerInstance;
    protected static BServerInstance oauth2ServerInstance;
    protected static BServerInstance ldapAuthServerInstance;
    private static EmbeddedDirectoryServer embeddedDirectoryServer;

    @BeforeGroups(value = "auth-test", alwaysRun = true)
    public void start() throws Exception {
        int[] basicAuthRequiredPorts = new int[]{20000, 20001, 20002, 20003, 20004, 20005, 20006, 20007, 20008,
                20009, 20010};
        int[] jwtAuthRequiredPorts = new int[]{20100, 20101, 20102, 20103, 20104, 20105, 20106, 20107, 20108,
                20109, 20110, 20111, 20112, 20113, 20114, 20199};
        int[] oauth2RequiredPorts = new int[]{20200, 20201, 20298, 20299};
        int[] ldapAuthRequiredPorts = new int[]{20300};

        String keyStore = StringEscapeUtils.escapeJava(
                Paths.get("src", "test", "resources", "certsAndKeys", "ballerinaKeystore.p12").toAbsolutePath()
                        .toString());
        String trustStore = StringEscapeUtils.escapeJava(
                Paths.get("src", "test", "resources", "certsAndKeys", "ballerinaTruststore.p12").toAbsolutePath()
                        .toString());

        String basePath = new File("src" + File.separator + "test" + File.separator + "resources" + File.separator +
                                           "auth").getAbsolutePath();
        String usersTomlPath = basePath + File.separator + "src" + File.separator + "basic" + File.separator +
                "users.toml";
        String usersLdifPath = basePath + File.separator + "src" + File.separator + "ldap" + File.separator +
                "users.ldif";

        embeddedDirectoryServer = new EmbeddedDirectoryServer();
        embeddedDirectoryServer.startLdapServer(20399, usersLdifPath);

        String[] args = new String[]{"--b7a.config.file=" + usersTomlPath, "--keystore=" + keyStore,
                "--truststore=" + trustStore};

        basicAuthServerInstance = new BServerInstance(balServer);
        jwtAuthServerInstance = new BServerInstance(balServer);
        oauth2ServerInstance = new BServerInstance(balServer);
        ldapAuthServerInstance = new BServerInstance(balServer);

        basicAuthServerInstance.startServer(basePath, "basic", null, args, basicAuthRequiredPorts);
        jwtAuthServerInstance.startServer(basePath, "jwt", null, args, jwtAuthRequiredPorts);
        oauth2ServerInstance.startServer(basePath, "oauth2", null, args, oauth2RequiredPorts);
        ldapAuthServerInstance.startServer(basePath, "ldap", null, args, ldapAuthRequiredPorts);
    }

    @AfterGroups(value = "auth-test", alwaysRun = true)
    public void cleanup() throws Exception {
        embeddedDirectoryServer.stopLdapService();
        basicAuthServerInstance.removeAllLeechers();
        basicAuthServerInstance.shutdownServer();
        jwtAuthServerInstance.removeAllLeechers();
        jwtAuthServerInstance.shutdownServer();
        oauth2ServerInstance.removeAllLeechers();
        oauth2ServerInstance.shutdownServer();
        ldapAuthServerInstance.removeAllLeechers();
        ldapAuthServerInstance.shutdownServer();
    }

    protected void assertOK(HttpResponse response) {
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
    }

    protected void assertUnauthorized(HttpResponse response) {
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), 401, "Response code mismatched");
    }

    protected void assertForbidden(HttpResponse response) {
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), 403, "Response code mismatched");
    }

    protected void assertContains(HttpResponse response, String text) {
        Assert.assertTrue(response.getData().contains(text));
    }
}
