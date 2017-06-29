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

package org.ballerinalang.test.service.jms.sample;

import org.apache.activemq.broker.BrokerService;
import org.ballerinalang.test.IntegrationTestCase;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.Constant;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.test.context.ServerInstance;
import org.ballerinalang.test.context.Utils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Testing the JMS connector.
 */
public class JMSServiceSampleTestCase extends IntegrationTestCase {

    private BrokerService broker = new BrokerService();
    ServerInstance ballerinaServer;

    private String serverZipPath;

    /**
     * Setup an embedded activemq broker and prepare the ballerina distribution to run jms samples.
     *
     * @throws Exception if setting up fails
     */
    @BeforeClass
    private void setup() throws Exception {

        BrokerService broker = new BrokerService();
        broker.setPersistent(false);
        broker.addConnector("tcp://localhost:61616");

        broker.start();

        serverZipPath = System.getProperty(Constant.SYSTEM_PROP_SERVER_ZIP);
        ballerinaServer = new JMSServerInstance(serverZipPath, 9091);
    }

    /**
     * Stops the started activemq broker and ballerina server.
     *
     * @throws Exception if stopping of any of the above fails
     */
    @AfterClass
    private void cleanup() throws Exception {
        broker.stop();
        ballerinaServer.stopServer();
    }

    @Test(description = "Test simple JMS message send and receive via ballerina")
    public void testJMSSendReceive() throws Exception {

        // Start receiver

        String serviceSampleDir = ballerinaServer.getServerHome() + File.separator + Constant.SERVICE_SAMPLE_DIR;

        //Adding temporary echo service so the server start can be monitored using that. (since this is a jms service
        //there won't be any http port openings, hence current logic cannot identify whether server is started or not)
        String[] receiverArgs = {serviceSampleDir + File.separator + "jms" + File.separator + "jmsReceiver.bal",
                serviceSampleDir + File.separator + "echoService" + File.separator + "echoService.bal"};

        ballerinaServer.setArguments(receiverArgs);

        ballerinaServer.startServer();

        String messageText = "Hello from JMS";

        LogLeecher leecher = new LogLeecher(messageText);

        ballerinaServer.addLogLeecher(leecher);


        ServerInstance jmsSender = new JMSServerInstance(serverZipPath);
        // Start sender
        String[] senderArgs = {serviceSampleDir + File.separator + "jms" + File.separator + "jmsSender.bal"};


        jmsSender.runMain(senderArgs);


        // Wait for expected text

        leecher.waitForText(5000);
    }

}

/**
 * The Ballerina server instance which is specifically configured to run jms tests by copying activemq client libraries.
 */
class JMSServerInstance extends ServerInstance {

    JMSServerInstance(String serverDistributionPath) throws BallerinaTestException {
        super(serverDistributionPath);
    }

    JMSServerInstance(String serverDistributionPath, int serverHttpPort) throws BallerinaTestException {
        super(serverDistributionPath, serverHttpPort);
    }

    /**
     * Copies activemq client libraries to ballerina libraries to run jms client functions.
     *
     * @throws BallerinaTestException if preparing the server fails
     */
    @Override
    protected void configServer() throws BallerinaTestException {
        super.configServer();

        // Copy JMS libraries to the ballerina lib for testing

        // Source jar
        Path source = Paths.get(System.getProperty(Constant.PROJECT_BUILD_DIR),
                System.getProperty(Constant.ACTIVEMQ_ALL_JAR));

        // Target lib folder

        Path target = Paths.get(getServerHome(), "bre/lib", System.getProperty(Constant.ACTIVEMQ_ALL_JAR));

        Utils.copyFile(source, target);
    }
}
