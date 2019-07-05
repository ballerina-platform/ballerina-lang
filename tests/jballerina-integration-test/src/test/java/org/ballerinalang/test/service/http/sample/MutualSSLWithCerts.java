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

package org.ballerinalang.test.service.http.sample;

import org.apache.commons.lang3.StringEscapeUtils;
import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.LogLeecher;
import org.testng.annotations.Test;

import java.nio.file.Paths;

/**
 * Test mutual ssl with certificates and keys.
 */
@Test(groups = "http-test")
public class MutualSSLWithCerts extends BaseTest {

    @Test(description = "Test mutual ssl")
    public void testMutualSSLWithCerts() throws Exception {
        String serverResponse = "Response received";
        String privateKey = StringEscapeUtils.escapeJava(Paths.get("src", "test", "resources", "certsAndKeys",
                                                                   "private.key").toAbsolutePath().toString());
        String publicCert = StringEscapeUtils.escapeJava(Paths.get("src", "test", "resources", "certsAndKeys",
                                                                   "public.crt").toAbsolutePath().toString());

        String balFile = Paths.get("src", "test", "resources", "mutualSSL", "ssl_client.bal").toAbsolutePath()
                .toString();

        String[] flags = { "-e", "certificate.key=" + privateKey, "-e", "public.cert=" + publicCert};

        BMainInstance ballerinaClient = new BMainInstance(balServer);
        LogLeecher clientLeecher = new LogLeecher(serverResponse);
        ballerinaClient.runMain(balFile, flags, null, new LogLeecher[]{clientLeecher});
        clientLeecher.waitForText(20000);
    }
}

