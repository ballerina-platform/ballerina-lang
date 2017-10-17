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
public class WebSocketPassThroughTestCase extends WebSocketIntegrationTest {

    private final int threadSleepTime = 100;
    private final int messageDeliveryCountDown = 100;
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
        String balFilePath =
                new File("src" + File.separator + "test" + File.separator + "resources" + File.separator +
                                 "websocket" + File.separator + "SimpleProxyServer.bal").getAbsolutePath();
        ballerinaServer.startBallerinaServer(balFilePath);

        // Initializing WebSocket clients.
        for (int i = 0; i < clientCount; i++) {
            wsClients[i] = new WebSocketClient("ws://localhost:9090/proxy/ws");
        }
    }

    @Test(priority = 0)
    public void testFullTextMediation() throws Exception {
        handshakeAllClients(wsClients);
        // Send and wait to receive message back from the remote server.
        for (int i = 0; i < clientCount; i++) {
            wsClients[i].sendText(i + "");
        }

        for (int i = 0; i < clientCount; i++) {
            String expectedMessage = "client service: " + i;
            assertWebSocketClientStringMessage(wsClients[i], expectedMessage, threadSleepTime,
                                               messageDeliveryCountDown);
        }
    }

    @Test(priority = 2)
    public void testRemoteConnectionClosureFromRemoteClient() throws InterruptedException {
        wsClients[0].shutDown();
        Thread.sleep(threadSleepTime);
        boolean isConnectionClosed = false;
        for (WebSocketRemoteServerFrameHandler frameHandler : WebSocketRemoteServerInitializer.FRAME_HANDLERS) {
            if (!frameHandler.isOpen()) {
                isConnectionClosed = true;
                WebSocketRemoteServerInitializer.FRAME_HANDLERS.remove(frameHandler);
                break;
            }
        }
        Assert.assertTrue(isConnectionClosed);
    }

    @Test(priority = 3)
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
