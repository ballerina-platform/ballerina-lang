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

import org.ballerinalang.test.util.websocket.client.WebSocketClient;
import org.ballerinalang.test.util.websocket.server.WebSocketRemoteServerFrameHandler;
import org.ballerinalang.test.util.websocket.server.WebSocketRemoteServerInitializer;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test class for WebSocket client connector.
 * This test the mediation of wsClient <-> balServer <-> balWSClient <-> remoteServer.
 */
public class WebSocketClientTest extends WebSocketIntegrationTest {

    private final int threadSleepTime = 100;
    private final int clientCount = 5;
    private final WebSocketClient[] wsClients = new WebSocketClient[clientCount];

    {
        for (int i = 0; i < clientCount; i++) {
            wsClients[i] = new WebSocketClient("ws://localhost:9090/client-connector/ws");
        }
    }

    @Test
    public void testFullMediation() throws Exception {
        handshakeAllClients(wsClients);

        for (int i = 0; i < clientCount; i++) {
            // Send and wait to receive message back from the remote server.
            wsClients[i].sendText(i + "");
        }

        Thread.sleep(threadSleepTime);
        for (int i = 0; i < clientCount; i++) {
            Assert.assertEquals(wsClients[i].getTextReceived(), "client service : " + i);
        }

    }

    @Test
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

    @Test
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
        shutDownAllClients(wsClients);
    }
}
