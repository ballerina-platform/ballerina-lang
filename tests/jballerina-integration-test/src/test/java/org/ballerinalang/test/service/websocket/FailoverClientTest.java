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
 * Tests failover support of the WebSocket client.
 */
@Test(groups = {"websocket-test"})
public class FailoverClientTest extends WebSocketTestCommons {

    private WebSocketRemoteServer remoteServer15300;
    private WebSocketRemoteServer remoteServer15200;
    private static final String URL = "ws://localhost:21032";
    private static final int PORT = 15300;
    private static final int SEVER_PORT = 15200;
    private static final String MESSAGE = "hi all";
    private static final String text = "hi";

    @Test(description = "Tests the failover webSocket client by starting the second server in the target URLs.")
    public void testTextFrameWithSecondServer() throws URISyntaxException, InterruptedException,
            BallerinaTestException {
        remoteServer15200 = initiateServer(SEVER_PORT);
        WebSocketTestClient client = initiateClient(URL);
        sendTextDataAndAssert(client, MESSAGE);
        closeConnection(client, remoteServer15200);
    }

    @Test(description = "Tests the failover webSocket client by starting the first server in the target URLs.")
    public void testBinaryFrameForFailover() throws URISyntaxException, InterruptedException, BallerinaTestException {
        remoteServer15300 = initiateServer(PORT);
        WebSocketTestClient client = initiateClient(URL);
        sendBinaryDataAndAssert(client);
        closeConnection(client, remoteServer15300);
    }

    @Test(description = "Tests the failover webSocket client by doesn't start the any server in the targets URLs")
    public void testFailingFailover() throws URISyntaxException, InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        WebSocketTestClient client = initiateClient(URL);
        client.setCountDownLatch(countDownLatch);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        CloseWebSocketFrame closeWebSocketFrame = client.getReceivedCloseFrame();
        Assert.assertNotNull(closeWebSocketFrame);
        Assert.assertEquals(closeWebSocketFrame.statusCode(), 1011);
        Assert.assertTrue(closeWebSocketFrame.reasonText().contains("Unexpected condition"));
        closeWebSocketFrame.release();
    }

    @Test(description = "Tests the failover webSocket client by starting the both server in the target URLs.")
    public void testFailoverWithBothServer() throws URISyntaxException, InterruptedException,
            BallerinaTestException {
        remoteServer15200 = initiateServer(SEVER_PORT);
        remoteServer15300 = initiateServer(PORT);
        WebSocketTestClient client = initiateClient(URL);
        sendTextDataAndAssert(client, MESSAGE);
        remoteServer15300.stop();
        sendTextDataAndAssert(client, text);
        sendBinaryDataAndAssert(client);
        closeConnection(client, remoteServer15200);
    }

    @Test(description = "Tests the failover webSocket client by starting the both server in the target URLs.")
    public void testFailoverWithBothServerFirstStartSecondServer() throws URISyntaxException, InterruptedException,
            BallerinaTestException {
        remoteServer15200 = initiateServer(SEVER_PORT);
        WebSocketTestClient client = initiateClient(URL);
        sendTextDataAndAssert(client, MESSAGE);
        remoteServer15300 = initiateServer(PORT);
        remoteServer15200.stop();
        sendTextDataAndAssert(client, text);
        closeConnection(client, remoteServer15300);
    }

    @Test(description = "Tests handshake's waiting time exception")
    public void testCountDownLatch() throws URISyntaxException, InterruptedException, BallerinaTestException {
        remoteServer15300 = initiateServer(PORT);
        WebSocketTestClient client = initiateClient("ws://localhost:21034");
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        sendTextDataAndAssert(client, MESSAGE);
        remoteServer15300.stop();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getTextReceived(), "error {ballerina/http}WsGenericError " +
                "message={ballerina/http}WsGenericError");
        client.shutDown();
    }

    private WebSocketRemoteServer initiateServer(int port) throws InterruptedException, BallerinaTestException {
        WebSocketRemoteServer remoteServer = new WebSocketRemoteServer(port);
        remoteServer.run();
        return remoteServer;
    }

    private WebSocketTestClient initiateClient(String url) throws InterruptedException, URISyntaxException {
        WebSocketTestClient client = new WebSocketTestClient(url);
        client.handshake();
        return client;
    }

    private void closeConnection(WebSocketTestClient client, WebSocketRemoteServer remoteServer) throws
            InterruptedException {
        client.shutDown();
        remoteServer.stop();
    }

    private void sendTextDataAndAssert(WebSocketTestClient client, String text) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        client.setCountDownLatch(countDownLatch);
        client.sendText(text);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getTextReceived(), text);
    }

    private void sendBinaryDataAndAssert(WebSocketTestClient client) throws InterruptedException {
        ByteBuffer data = ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 6});
        CountDownLatch countDownLatch = new CountDownLatch(1);
        client.setCountDownLatch(countDownLatch);
        client.sendBinary(data);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getBufferReceived(), data);
    }
}
