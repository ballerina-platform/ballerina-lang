/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import org.awaitility.Awaitility;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.util.websocket.client.WebSocketTestClient;
import org.ballerinalang.test.util.websocket.server.WebSocketRemoteServer;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Tests the failover support of the WebSocket client.
 */
@Test(groups = {"websocket-test"})
public class FailoverClientTest extends WebSocketTestCommons {

    private static final String URL = "ws://localhost:21032";
    private static final int FIRST_SERVER_PORT = 15300;
    private static final int SECOND_SERVER_PORT = 15200;
    private static final int THIRD_SERVER_PORT = 15400;
    private static final int TIME = 8;
    private static final int INTERVAL = 5;
    private static final int WAITING_TIME = 4;
    private static final String MESSAGE = "hi all";
    private WebSocketRemoteServer firstRemoteServer;
    private WebSocketRemoteServer secondRemoteServer;
    private WebSocketRemoteServer thirdRemoteServer;

    @Test(description = "Tests the failover webSocket client by starting the second server in the target URLs.")
    public void testTextFrameWithSecondServer() throws URISyntaxException, InterruptedException,
            BallerinaTestException {
        secondRemoteServer = initiateServer(SECOND_SERVER_PORT);
        WebSocketTestClient client = initiateClient(URL);
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(TIME, TimeUnit.SECONDS);
        sendTextDataAndAssert(client);
        closeConnection(client, secondRemoteServer);
    }

    @Test(description = "Tests the failover webSocket client by starting the first server in the target URLs.")
    public void testBinaryFrameWithFirstServer() throws URISyntaxException, InterruptedException,
            BallerinaTestException {
        firstRemoteServer = initiateServer(FIRST_SERVER_PORT);
        WebSocketTestClient client = initiateClient(URL);
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(WAITING_TIME, TimeUnit.SECONDS);
        sendBinaryDataAndAssert(client);
        closeConnection(client, firstRemoteServer);
    }

    @Test(description = "Tests the failover webSocket client by starting the third server in the target URLs.")
    public void testBinaryFrameWithThirdServer() throws URISyntaxException, InterruptedException,
            BallerinaTestException {
        thirdRemoteServer = initiateServer(THIRD_SERVER_PORT);
        WebSocketTestClient client = initiateClient(URL);
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(TIME, TimeUnit.SECONDS);
        sendBinaryDataAndAssert(client);
        closeConnection(client, thirdRemoteServer);
    }

    @Test(description = "Tests the failover webSocket client by not starting any of the servers in the targets URLs")
    public void testFailingFailover() throws URISyntaxException, InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        WebSocketTestClient client = initiateClient("ws://localhost:21035");
        client.setCountDownLatch(countDownLatch);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        CloseWebSocketFrame closeWebSocketFrame = client.getReceivedCloseFrame();
        Assert.assertNotNull(closeWebSocketFrame);
        Assert.assertEquals(closeWebSocketFrame.statusCode(), 1011);
        Assert.assertTrue(closeWebSocketFrame.reasonText().contains("Unexpected condition"));
        closeWebSocketFrame.release();
    }

    @Test(description = "Tests the failover client when getting a handshake timeout")
    public void testHandshakeTimeout() throws URISyntaxException, InterruptedException, BallerinaTestException {
        WebSocketTestClient client = initiateClient("ws://localhost:21034");
        firstRemoteServer = initiateServer(FIRST_SERVER_PORT);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        sendTextDataAndAssert(client);
        closeConnection(client, firstRemoteServer);
    }

    @Test(description = "Tests the failover webSocket client by starting the both server in the target URLs.")
    public void testFailoverWithBothServer() throws URISyntaxException, InterruptedException,
            BallerinaTestException {
        secondRemoteServer = initiateServer(SECOND_SERVER_PORT);
        firstRemoteServer = initiateServer(FIRST_SERVER_PORT);
        WebSocketTestClient client = initiateClient(URL);
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(WAITING_TIME, TimeUnit.SECONDS);
        sendTextDataAndAssert(client);
        firstRemoteServer.stop();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await(INTERVAL, TimeUnit.SECONDS);
        sendTextDataAndAssert(client);
        sendBinaryDataAndAssert(client);
        closeConnection(client, secondRemoteServer);
    }

    @Test(description = "Tests the failover webSocket client by starting the both server in the target URLs.")
    public void testFailoverWithBothServerFirstStartSecondServer() throws URISyntaxException, InterruptedException,
            BallerinaTestException {
        secondRemoteServer = initiateServer(SECOND_SERVER_PORT);
        WebSocketTestClient client = initiateClient(URL);
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(INTERVAL, TimeUnit.SECONDS);
        sendTextDataAndAssert(client);
        firstRemoteServer = initiateServer(FIRST_SERVER_PORT);
        secondRemoteServer.stop();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await(TIME, TimeUnit.SECONDS);
        sendTextDataAndAssert(client);
        closeConnection(client, firstRemoteServer);
    }

    @Test(description = "Tests the complex scenario of the failover webSocket client.")
    public void testComplexFailover() throws URISyntaxException, InterruptedException, BallerinaTestException {
        secondRemoteServer = initiateServer(SECOND_SERVER_PORT);
        WebSocketTestClient client = initiateClient(URL);
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(INTERVAL, TimeUnit.SECONDS);
        sendTextDataAndAssert(client);
        firstRemoteServer = initiateServer(FIRST_SERVER_PORT);
        secondRemoteServer.stop();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await(TIME, TimeUnit.SECONDS);
        sendBinaryDataAndAssert(client);
        thirdRemoteServer = initiateServer(THIRD_SERVER_PORT);
        firstRemoteServer.stop();
        CountDownLatch thirdAttemptsLatch = new CountDownLatch(1);
        thirdAttemptsLatch.await(TIME, TimeUnit.SECONDS);
        sendTextDataAndAssert(client);
        secondRemoteServer = initiateServer(SECOND_SERVER_PORT);
        thirdRemoteServer.stop();
        CountDownLatch foruthAttemptsLatch = new CountDownLatch(1);
        foruthAttemptsLatch.await(TIME, TimeUnit.SECONDS);
        sendBinaryDataAndAssert(client);
        closeConnection(client, secondRemoteServer);
    }

    private WebSocketRemoteServer initiateServer(int port) throws InterruptedException, BallerinaTestException {
        WebSocketRemoteServer remoteServer = new WebSocketRemoteServer(port);
        remoteServer.run();
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(2, TimeUnit.SECONDS);
        return remoteServer;
    }

    private WebSocketTestClient initiateClient(String url) throws InterruptedException, URISyntaxException {
        WebSocketTestClient client = new WebSocketTestClient(url);
        client.handshake();
        Awaitility.await().atMost(TIMEOUT_IN_SECS, TimeUnit.SECONDS).until(() -> client.isOpen());
        return client;
    }

    private void closeConnection(WebSocketTestClient client, WebSocketRemoteServer remoteServer) throws
            InterruptedException {
        client.shutDown();
        remoteServer.stop();
    }

    private void sendTextDataAndAssert(WebSocketTestClient client) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(1, TimeUnit.SECONDS);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        client.setCountDownLatch(countDownLatch);
        client.sendText(MESSAGE);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getTextReceived(), MESSAGE);
    }

    private void sendBinaryDataAndAssert(WebSocketTestClient client) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(1, TimeUnit.SECONDS);
        ByteBuffer data = ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 6});
        CountDownLatch countDownLatch = new CountDownLatch(1);
        client.setCountDownLatch(countDownLatch);
        client.sendBinary(data);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getBufferReceived(), data);
    }
}
