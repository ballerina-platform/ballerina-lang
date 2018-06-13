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

import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.util.websocket.client.WebSocketTestClient;
import org.ballerinalang.test.util.websocket.server.WebSocketRemoteServer;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * This Class tests ping pong support of WebSocket client and server.
 */
public class PingPongSupportTestCase extends WebSocketIntegrationTest {

    private WebSocketRemoteServer remoteServer;
    private WebSocketTestClient client;
    private static final String URL = "ws://localhost:9090/pingpong/ws";
    private static final ByteBuffer SENDING_BYTE_BUFFER = ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5});
    private static final ByteBuffer RECEIVING_BYTE_BUFFER = ByteBuffer.wrap("data".getBytes(StandardCharsets.UTF_8));

    @BeforeClass(description = "Initializes the Ballerina server with the ping_pong.bal file")
    public void setup() throws InterruptedException, BallerinaTestException, URISyntaxException {
        remoteServer = new WebSocketRemoteServer(REMOTE_SERVER_PORT);
        remoteServer.run();
        initBallerinaServer("ping_pong.bal");
        client = new WebSocketTestClient(URL);
        client.handshake();
    }

    @Test(description = "Tests ping to Ballerina WebSocket server")
    public void testPingToBallerinaServer() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        client.setCountDownLatch(countDownLatch);
        client.sendPing(SENDING_BYTE_BUFFER);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getBufferReceived(), SENDING_BYTE_BUFFER);
        Assert.assertTrue(client.isPong());
    }

    @Test(description = "Tests ping from Ballerina WebSocket server")
    public void testPingFromBallerinaServer() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        client.setCountDownLatch(countDownLatch);
        client.sendText("ping-me");
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getBufferReceived(), RECEIVING_BYTE_BUFFER);
        Assert.assertTrue(client.isPing());
    }

    @Test(description = "Tests ping from Ballerina client to the remote server")
    public void testPingFromBallerinaClientToRemoteServer() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        client.setCountDownLatch(countDownLatch);
        client.sendText("ping-remote-server");
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getTextReceived(), "pong-from-remote-server-received");
    }

    @Test(description = "Tests ping to Ballerina WebSocket server")
    public void testPingFromRemoteServerToBallerinaClient() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        client.setCountDownLatch(countDownLatch);
        client.sendText("tell-remote-server-to-ping");
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getTextReceived(), "ping-from-remote-server-received");
    }

    @AfterClass(description = "Stops the Ballerina server")
    public void cleanup() throws BallerinaTestException, InterruptedException {
        client.shutDown();
        stopBallerinaServerInstance();
        remoteServer.stop();
    }
}
