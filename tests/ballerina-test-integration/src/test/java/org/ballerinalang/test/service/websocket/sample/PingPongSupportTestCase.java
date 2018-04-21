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

package org.ballerinalang.test.service.websocket.sample;

import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.ServerInstance;
import org.ballerinalang.test.util.websocket.client.WebSocketTestClient;
import org.ballerinalang.test.util.websocket.server.WebSocketRemoteServer;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLException;

/**
 * This Class tests ping pong support of WebSocket client and server.
 */
public class PingPongSupportTestCase extends WebSocketIntegrationTest {

    private WebSocketRemoteServer remoteServer;
    private ServerInstance ballerinaServerInstance;
    private WebSocketTestClient client;
    private static final String URL = "ws://localhost:9090/pingpong/ws";
    private static final ByteBuffer SENDING_BYTE_BUFFER = ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5});
    private static final ByteBuffer RECEIVING_BYTE_BUFFER = ByteBuffer.wrap("data".getBytes(StandardCharsets.UTF_8));

    @BeforeClass
    public void setup() throws InterruptedException, BallerinaTestException, URISyntaxException, SSLException {
        remoteServer = new WebSocketRemoteServer(REMOTE_SERVER_PORT);
        remoteServer.run();

        String balPath = new File("src/test/resources/websocket/PingPongSupport.bal").getAbsolutePath();
        ballerinaServerInstance = ServerInstance.initBallerinaServer();
        ballerinaServerInstance.startBallerinaServer(balPath);

        client = new WebSocketTestClient(URL);
        client.handshake();
    }

    @Test
    public void testPingToBallerinaServer() throws IOException, InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        client.setCountDownLatch(countDownLatch);
        client.sendPing(SENDING_BYTE_BUFFER);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getBufferReceived(), SENDING_BYTE_BUFFER);
        Assert.assertTrue(client.isPong());
    }

    @Test
    public void testPingFromBallerinaServer() throws IOException, InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        client.setCountDownLatch(countDownLatch);
        client.sendText("ping-me");
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getBufferReceived(), RECEIVING_BYTE_BUFFER);
        Assert.assertTrue(client.isPing());
    }

    @Test
    public void testPingFromBallerinaClientToRemoteServer() throws IOException, InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        client.setCountDownLatch(countDownLatch);
        client.sendText("ping-remote-server");
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getTextReceived(), "pong-from-remote-server-received");
    }

    @Test
    public void testPingFromRemoteServerToBallerinaClient() throws IOException, InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        client.setCountDownLatch(countDownLatch);
        client.sendText("tell-remote-server-to-ping");
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getTextReceived(), "ping-from-remote-server-received");
    }

    @AfterClass
    public void cleanup() throws BallerinaTestException, InterruptedException {
        client.shutDown();
        ballerinaServerInstance.stopServer();
        remoteServer.stop();
    }
}
