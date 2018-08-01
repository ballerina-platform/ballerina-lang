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
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;
import java.io.File;

/**
 * Testing Mutual SSL.
 */
@Test(groups = "http-test")
public class MutualSSLTestCase extends IntegrationTestCase {

    private ServerInstance ballerinaClient;


    @Test (description = "Test mutual ssl")
    public void testMutualSSL() throws Exception {
        String serverZipPath = System.getProperty(Constant.SYSTEM_PROP_SERVER_ZIP);
        String serverMessage = "successful";
        String serverResponse = "hello world";

        LogLeecher serverLeecher = new LogLeecher(serverMessage);
        serverInstance.addLogLeecher(serverLeecher);

        String[] clientArgs = {new File("src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "mutualSSL" + File.separator + "mutualSSLClient.bal").getAbsolutePath()};

        ballerinaClient = new ServerInstance(serverZipPath);
        LogLeecher clientLeecher = new LogLeecher(serverResponse);
        ballerinaClient.addLogLeecher(clientLeecher);
        ballerinaClient.runMain(clientArgs);
        serverLeecher.waitForText(20000);
        clientLeecher.waitForText(20000);
    }

    @AfterClass
    private void cleanup() throws Exception {
        ballerinaClient.stopServer();
    }
}
