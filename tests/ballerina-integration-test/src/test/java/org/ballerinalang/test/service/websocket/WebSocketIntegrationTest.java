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

package org.ballerinalang.test.service.websocket;

import org.ballerinalang.test.IntegrationTestCase;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.test.context.ServerInstance;
import org.ballerinalang.test.util.websocket.client.WebSocketTestClient;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Facilitate the common functionality of WebSocket integration tests.
 */
public class WebSocketIntegrationTest extends IntegrationTestCase{
    protected static final int TIMEOUT_IN_SECS = 10;
    protected static final int REMOTE_SERVER_PORT = 15500;


    /**
     * Initializes Ballerina with the given bal file.
     *
     * @param fileName the filename to initialize the Ballerina server with.
     * @param logLeecher Log leecher match the logs in the bal file.
     * @throws BallerinaTestException on Ballerina related issues.
     */
    public void initBallerinaServer(String fileName, LogLeecher logLeecher) throws BallerinaTestException {
        String balPath = new File("src/test/resources/websocket/" + fileName).getAbsolutePath();
        serverInstance.addLogLeecher(logLeecher);
        serverInstance.startBallerinaServer(balPath);
    }

    /**
     * Initializes Ballerina with the given bal file.
     *
     * @param fileName the filename to initialize the Ballerina server with.
     * @throws BallerinaTestException on Ballerina related issues.
     */
    public void initBallerinaServer(String fileName) throws BallerinaTestException {
        String balPath = new File("src/test/resources/websocket/" + fileName).getAbsolutePath();
        serverInstance.startBallerinaServer(balPath);
    }

    @BeforeGroups("websocket-test")
    public void start() throws BallerinaTestException {
        String balFile = new File("src" + File.separator + "test" + File.separator + "resources" + File.separator +
                "websocket").getAbsolutePath();
        String[] args = new String[] {"--sourceroot", balFile};
        serverInstance.startBallerinaServer("wsServices", args);
    }

    @AfterGroups("websocket-test")
    public void stop() throws Exception {
        serverInstance.removeAllLeechers();
        serverInstance.stopServer();
    }

    public void stopBallerinaServerInstance() throws BallerinaTestException {
        serverInstance.stopServer();
    }
    /**
     * This method is only used when ack is needed from Ballerina WebSocket Proxy in order to sync ballerina
     * WebSocket Server and Client after the handshake in initiated from the {@link WebSocketTestClient}.
     *
     * @param client {@link WebSocketTestClient}.
     * @throws InterruptedException If connection is interrupted during handshake.
     */
    protected void handshakeAndAck(WebSocketTestClient client) throws InterruptedException {
        CountDownLatch ackCountDownLatch = new CountDownLatch(1);
        client.setCountDownLatch(ackCountDownLatch);
        client.handshake();
        ackCountDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        if (!"send".equals(client.getTextReceived())) {
            throw new IllegalArgumentException("Could not receive acknowledgment");
        }
    }
}
