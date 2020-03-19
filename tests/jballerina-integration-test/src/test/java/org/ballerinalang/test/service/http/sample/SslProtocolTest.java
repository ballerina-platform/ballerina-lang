/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.LogLeecher;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Paths;

/**
 * Testing SSL protocols.
 */
@Test(groups = "http-test")
public class SslProtocolTest extends BaseTest {

    @Test(description = "Test ssl protocols")
    public void testSslProtocols() throws Exception {
        String clientLog = "SSL connection failed:Server chose TLSv1.1, but that protocol version is not enabled or "
                + "not supported by the client. localhost/127.0.0.1:9249";

        String balFile = new File("src" + File.separator + "test" + File.separator + "resources" + File.separator +
                                          "ssl" + File.separator + "ssl_protocol_client.bal").getAbsolutePath();

        String trustStore = StringEscapeUtils.escapeJava(
                Paths.get("src", "test", "resources", "certsAndKeys", "ballerinaTruststore.p12").toAbsolutePath()
                        .toString());
        String[] flags = new String[]{"--truststore=" + trustStore};

        BMainInstance ballerinaClient = new BMainInstance(balServer);
        LogLeecher clientLeecher = new LogLeecher(clientLog);
        ballerinaClient.runMain(balFile, flags, null, new LogLeecher[]{clientLeecher});
        clientLeecher.waitForText(20000);
    }
}
