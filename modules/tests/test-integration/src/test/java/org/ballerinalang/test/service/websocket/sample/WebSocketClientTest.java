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

package org.ballerinalang.test.service.websocket.sample;

import org.ballerinalang.test.context.Constant;
import org.ballerinalang.test.context.ServerInstance;
import org.ballerinalang.test.util.websocket.client.WebSocketClient;
import org.ballerinalang.test.util.websocket.server.WebSocketRemoteServer;
import org.ballerinalang.test.util.websocket.server.WebSocketRemoteServerFrameHandler;
import org.ballerinalang.test.util.websocket.server.WebSocketRemoteServerInitializer;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;

/**
 * Test class for WebSocket client connector.
 * This test the mediation of wsClient <-> balServer <-> balWSClient <-> remoteServer.
 */
public class WebSocketClientTest extends WebSocketIntegrationTest {

    private final int threadSleepTime = 1000;
    private final int clientCount = 5;
    private final WebSocketClient[] wsClients = new WebSocketClient[clientCount];
    private ServerInstance ballerinaServer;
    private WebSocketRemoteServer webSocketRemoteServer;

    @BeforeClass
    private void setup() throws Exception {
        // Initiating WebSocket remote server for pass through check.
        webSocketRemoteServer = new WebSocketRemoteServer(15500);
        webSocketRemoteServer.run();

        // Initializing ballerina server instance.
        ballerinaServer = ServerInstance.initBallerinaServer();
        String serviceSampleDir = ballerinaServer.getServerHome() + File.separator + Constant.SERVICE_SAMPLE_DIR;
        String balFile = serviceSampleDir + File.separator + "websocket" + File.separator + "clientConnector"
                + File.separator + "clientConnectorSample.balx";
        ballerinaServer.startBallerinaServer(balFile);

        // Initializing WebSocket clients.
        for (int i = 0; i < clientCount; i++) {
            wsClients[i] = new WebSocketClient("ws://localhost:9090/client-connector/ws");
        }
    }

    @Test(priority = 0)
    public void testFullMediation() throws Exception {
        handshakeAllClients(wsClients);

        // Send and wait to receive message back from the remote server.
        for (int i = 0; i < clientCount; i++) {
            wsClients[i].sendText(i + "");
        }

        /* WebSocket message delivery is asynchronous.
           So assertion should wait multiple times in order to check whether
           the message is delivered back correctly. */
        int messageDeliveryCountDown = 5;
        boolean received = false;
        for (int i = 0; i < clientCount; i++) {
            for (int j = 0; j < messageDeliveryCountDown; j++) {
                Thread.sleep(threadSleepTime);
                String expectedMessage = "client service : " + i;
                if (received = expectedMessage.equals(wsClients[i].getTextReceived())) {
                    Assert.assertTrue(received);
                    break;
                }
            }
            Assert.assertTrue(received);
            received = false;
        }
    }

    @Test(priority = 1)
    public void testRemoteConnectionClosureFromRemoteClient() throws InterruptedException {
        wsClients[0].shutDown();
        Thread.sleep(threadSleepTime);
        boolean isConnectionClosed = false;
        for (WebSocketRemoteServerFrameHandler frameHandler : WebSocketRemoteServerInitializer.FRAME_HANDLERS) {
            if (!frameHandler.isOpen()) {
                isConnectionClosed = true;
                break;
            }
        }
        Assert.assertTrue(isConnectionClosed);
    }

    @Test(priority = 2)
    public void testRemoteConnectionClosureFromBallerina() throws InterruptedException {
        wsClients[1].sendText("closeMe");
        Thread.sleep(threadSleepTime);
        boolean isConnectionClosed = false;
        for (WebSocketRemoteServerFrameHandler frameHandler : WebSocketRemoteServerInitializer.FRAME_HANDLERS) {
            if (!frameHandler.isOpen()) {
                isConnectionClosed = true;
                break;
            }
        }
        Assert.assertTrue(isConnectionClosed);
    }

    @AfterClass
    private void cleanup() throws Exception {
        shutDownAllClients(wsClients);
        ballerinaServer.stopServer();
        webSocketRemoteServer.stop();
    }
}
