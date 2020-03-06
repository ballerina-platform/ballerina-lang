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

package org.ballerinalang.test.service.httpv2;

import org.apache.commons.text.StringEscapeUtils;
import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BServerInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;

import java.nio.file.Paths;

/**
 * Base test class for Http version 2.0.0 integration test cases which starts/stops the http services as ballerina
 * package before and after tests are run.
 */
public class HttpBaseTest extends BaseTest {
    protected static BServerInstance serverInstance;

    @BeforeGroups(value = "http-test", alwaysRun = true)
    public void start() throws BallerinaTestException {
        int[] requiredPorts = new int[]{9300, 9301, 9302};
        String balFile = Paths.get("src", "test", "resources", "httpv2").toAbsolutePath().toString();
        String privateKey = StringEscapeUtils.escapeJava(Paths.get("src", "test", "resources", "certsAndKeys",
                                                                   "private.key").toAbsolutePath().toString());
        String publicCert = StringEscapeUtils.escapeJava(
                Paths.get("src", "test", "resources", "certsAndKeys", "public.crt").toAbsolutePath().toString());
        String keyStore = StringEscapeUtils.escapeJava(
                Paths.get("src", "test", "resources", "certsAndKeys", "ballerinaKeystore.p12").toAbsolutePath()
                        .toString());
        String trustStore = StringEscapeUtils.escapeJava(
                Paths.get("src", "test", "resources", "certsAndKeys", "ballerinaTruststore.p12").toAbsolutePath()
                        .toString());
        String[] args = new String[] { "--certificate.key=" + privateKey, "--public.cert=" + publicCert,
                "--keystore=" + keyStore, "--truststore=" + trustStore };
        serverInstance = new BServerInstance(balServer);
        serverInstance.startServer(balFile, "httpservices", null, args, requiredPorts);
    }

    @AfterGroups(value = "http-test", alwaysRun = true)
    public void cleanup() throws Exception {
        serverInstance.removeAllLeechers();
        serverInstance.shutdownServer();
    }
}
