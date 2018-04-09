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
import org.ballerinalang.test.util.websocket.client.WebSocketTestClient;
import org.ballerinalang.test.util.websocket.server.WebSocketRemoteServer;
import org.ballerinalang.test.util.websocket.server.WebSocketRemoteServerFrameHandler;
import org.ballerinalang.test.util.websocket.server.WebSocketRemoteServerInitializer;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import static org.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Test class for WebSocket client connector.
 * This test the mediation of wsClient <-> balServer <-> balWSClient <-> remoteServer.
 */
public class WebSocketPassThroughTestCase extends WebSocketIntegrationTest {

    private final int awaitTimeInSecs = 10;
    private final int clientCount = 5;
    private final WebSocketTestClient[] wsClients = new WebSocketTestClient[clientCount];
    private ServerInstance ballerinaServer;
    private WebSocketRemoteServer webSocketRemoteServer;
    private final String name = "john";
    private final String age = "25";

    @BeforeClass
    private void setup() throws Exception {
        // Initiating WebSocket remote server for pass through check.
        int remoteServerPort = 15500;
        webSocketRemoteServer = new WebSocketRemoteServer(remoteServerPort);
        webSocketRemoteServer.run();

        // Initializing ballerina server instance.
        ballerinaServer = ServerInstance.initBallerinaServer();
        String balFilePath = new File("src/test/resources/websocket/SimpleProxyServer.bal").getAbsolutePath();
        ballerinaServer.startBallerinaServer(balFilePath);

        // Initializing and handshaking WebSocket clients.
        for (int i = 0; i < clientCount; i++) {
            wsClients[i] = new WebSocketTestClient("ws://localhost:9090/proxy/ws/" + name + "?age=" + age);
        }
        handshakeAllClients(wsClients);
    }

    @Test(priority = 0)
    public void testFullTextMediation() throws Exception {
        for (int i = 0; i < clientCount; i++) {
            final int clientNo = i;
            final String expectedMessage = name + "(" + age + ") client service: " + name + "(" + age + ") " + clientNo;
            await().atMost(awaitTimeInSecs, SECONDS).until(() -> {
                wsClients[clientNo].sendText(clientNo + "");
                return expectedMessage.equals(wsClients[clientNo].getTextReceived());
            });
        }
    }

    @Test(priority = 1)
    public void testPingPongSupport() throws IOException, InterruptedException {
        WebSocketTestClient client = wsClients[0];

        // Test ping and receive pong from server
        await().atMost(awaitTimeInSecs, SECONDS).until(() -> {
            client.sendPing(ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5}));
            return client.isPong();
        });

        // Test ping and receive pong from remote server when ballerina client send a ping
        final String expectedPongMessage = name +  " remote_server_pong";
        await().atMost(awaitTimeInSecs, SECONDS).until(() -> {
            client.sendText("client_ping");
            return expectedPongMessage.equals(client.getTextReceived());
        });

        // Test ping received from server
        await().atMost(awaitTimeInSecs, SECONDS).until(() -> {
            client.sendText("ping");
            return client.isPing();
        });

        // Test ping received from remote server
        final String expectedPingMessage = name +  " remote_server_ping";
        await().atMost(awaitTimeInSecs, SECONDS).until(() -> {
            client.sendText("client_ping_req");
            return expectedPingMessage.equals(client.getTextReceived());
        });
    }

    @Test(priority = 2, enabled = false)
    public void testRemoteConnectionClosureFromRemoteClient() throws InterruptedException {
        await().atMost(awaitTimeInSecs, SECONDS).until(() -> {
            wsClients[0].shutDown();
            return !wsClients[0].isOpen();
        });
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
        await().atMost(awaitTimeInSecs, SECONDS).until(() -> {
            wsClients[1].sendText("closeMe");
            return !wsClients[1].isOpen();
        });
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
