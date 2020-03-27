/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.service.http.sample;

import org.apache.commons.text.StringEscapeUtils;
import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.test.service.http.HttpBaseTest;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Paths;

/**
 * Testing Mutual SSL.
 */
@Test(groups = "http-test")
public class MutualSSLTestCase extends HttpBaseTest {

    @Test(description = "Test mutual ssl")
    public void testMutualSSL() throws Exception {
        String serverMessage = "successful";
        String serverResponse = "hello world";

        LogLeecher serverLeecher = new LogLeecher(serverMessage);
        serverInstance.addLogLeecher(serverLeecher);

        String balFile = new File("src" + File.separator + "test" + File.separator + "resources" + File.separator +
                                          "ssl" + File.separator + "mutual_ssl_client.bal").getAbsolutePath();

        String keyStore = StringEscapeUtils.escapeJava(
                Paths.get("src", "test", "resources", "certsAndKeys", "ballerinaKeystore.p12").toAbsolutePath()
                        .toString());
        String trustStore = StringEscapeUtils.escapeJava(
                Paths.get("src", "test", "resources", "certsAndKeys", "ballerinaTruststore.p12").toAbsolutePath()
                        .toString());
        String[] flags = new String[]{"--keystore=" + keyStore, "--truststore=" + trustStore};

        BMainInstance ballerinaClient = new BMainInstance(balServer);
        LogLeecher clientLeecher = new LogLeecher(serverResponse);
        ballerinaClient.runMain(balFile, flags, null, new LogLeecher[]{clientLeecher});
        serverLeecher.waitForText(20000);
        clientLeecher.waitForText(20000);
    }
}
