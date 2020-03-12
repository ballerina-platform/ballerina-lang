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

package org.ballerinalang.test.service.http2;

import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.LogLeecher;
import org.testng.annotations.Test;

import java.io.File;

/**
 * Testing Mutual SSL.
 */
@Test(groups = "http2-test")
public class Http2MutualSslTestCase extends Http2BaseTest {

    @Test(description = "Test mutual ssl with http2 prior knowledge")
    public void testMutualSSL() throws Exception {
        String serverResponse = "Passed";

        String balFile = new File("src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "mutualSSL" + File.separator + "http2_mutual_ssl_client.bal").getAbsolutePath();

        BMainInstance ballerinaClient = new BMainInstance(balServer);
        LogLeecher clientLeecher = new LogLeecher(serverResponse);
        ballerinaClient.runMain(balFile, new LogLeecher[]{clientLeecher});
        clientLeecher.waitForText(20000);
    }
}
