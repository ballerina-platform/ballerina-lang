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

package org.ballerinalang.test.service.http2;

import org.apache.commons.text.StringEscapeUtils;
import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.LogLeecher;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Paths;

/**
 * Testing Mutual SSL with certificates.
 */
@Test(groups = "http2-test")
public class Http2MutualSslWithCerts extends Http2BaseTest {
    @Test(description = "Test mutual ssl with http2 with certificates")
    public void testMutualSslWithcerts() throws Exception {
        String serverResponse = "Response received";

        String privateKey = StringEscapeUtils.escapeJava(Paths.get("src", "test", "resources", "certsAndKeys",
                "private.key").toAbsolutePath().toString());
        String publicCert = StringEscapeUtils.escapeJava(Paths.get("src", "test", "resources", "certsAndKeys",
                "public.crt").toAbsolutePath().toString());

        String balFile = new File("src" + File.separator + "test" + File.separator + "resources" + File.separator +
                "ssl" + File.separator + "http2_mutual_ssl_client_with_certs.bal").getAbsolutePath();
        String[] flags = {"--certificate.key=" + privateKey, "--public.cert=" + publicCert};

        BMainInstance ballerinaClient = new BMainInstance(balServer);
        LogLeecher clientLeecher = new LogLeecher(serverResponse);
        ballerinaClient.runMain(balFile, flags, null, new LogLeecher[]{clientLeecher});
        clientLeecher.waitForText(20000);
    }
}
