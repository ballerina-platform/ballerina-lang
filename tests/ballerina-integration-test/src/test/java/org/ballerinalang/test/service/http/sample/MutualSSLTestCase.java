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

import org.ballerinalang.test.IntegrationTestCase;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.Constant;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.test.context.ServerInstance;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;
import java.io.File;

/**
 * Testing Mutual SSL.
 */
public class MutualSSLTestCase extends IntegrationTestCase {

    private ServerInstance ballerinaServer;
    private ServerInstance ballerinaClient;
    private String serverZipPath;

    @Test (enabled = false)
    public void setUp() throws BallerinaTestException {
        String serverBal = new File(
                "src" + File.separator + "test" + File.separator + "resources" + File.separator + "mutualSSL"
                        + File.separator + "mutualSSLServer.bal").getAbsolutePath();
        ballerinaServer = ServerInstance.initBallerinaServer();
        ballerinaServer.startBallerinaServer(serverBal);
    }

    @Test (enabled = false, description = "Test mutual ssl")
    public void testMutualSSL() throws Exception {
        serverZipPath = System.getProperty(Constant.SYSTEM_PROP_SERVER_ZIP);
        String serverMessage = "successful";
        String serverResponse = "hello world";

        LogLeecher serverLeecher = new LogLeecher(serverMessage);
        ballerinaServer.addLogLeecher(serverLeecher);

        String[] clientArgs = {new File("src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "mutualSSL" + File.separator + "mutualSSLClient.bal").getAbsolutePath()};

        ballerinaClient = new ServerInstance(serverZipPath);
        LogLeecher clientLeecher = new LogLeecher(serverResponse);
        ballerinaClient.addLogLeecher(clientLeecher);
        ballerinaClient.runMain(clientArgs);
        serverLeecher.waitForText(20000);
        clientLeecher.waitForText(20000);
    }

    @AfterClass (enabled = false)
    private void cleanup() throws Exception {
        ballerinaServer.stopServer();
        ballerinaClient.stopServer();
    }
}
