/*
 * Copyright (c) 2019, WSO2 Inc. (http:www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http:www.apache.orglicensesLICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specif ic language governing permissions and limitations
 * under the License.
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
 * This Class tests the support retry function for the failover WebSocket client.
 */
@Test(groups = {"websocket-test"})
public class FailoverClientWithRetry extends WebSocketTestCommons {

    private WebSocketRemoteServer remoteServer15300;
    private String retryUrl = "ws://localhost:21034";
    private String url = "ws://localhost:21031";
    private int port = 15300;
    private WebSocketRemoteServer remoteServer15200;
    private int time = 2;

    @Test(description = "Tests the retry function using failover webSocket client (starting the second server " +
            "in the target URLs, sending and receiving text frames Afterthat restart that server and do the same)")
    public void testFailoverRetryWithSecondServer() throws URISyntaxException, InterruptedException,
            BallerinaTestException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        remoteServer15200 = new WebSocketRemoteServer(15200);
        remoteServer15200.run();
        String textSent = "hi all";
        WebSocketTestClient client = new WebSocketTestClient(retryUrl);
        client.handshake();
        client.setCountDownLatch(countDownLatch);
        client.sendText(textSent);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getTextReceived(), textSent);
        remoteServer15200.stop();
        CountDownLatch countDownLatchForRetry = new CountDownLatch(1);
        remoteServer15200.run();
        countDownLatchForRetry.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        client.sendText(textSent);
        countDownLatchForRetry.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getTextReceived(), textSent);
        client.shutDown();
        remoteServer15200.stop();
    }

    @Test(description = "Tests the retry function using failover webSocket client (starting the first server " +
            "in the target URLs, sending and receiving binary frames Afterthat restart that server and do the same)")
    public void testBinaryFrameFailoverRetry() throws URISyntaxException, InterruptedException,
            BallerinaTestException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        ByteBuffer bufferSent = ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5});
        remoteServer15300 = new WebSocketRemoteServer(port);
        remoteServer15300.run();
        WebSocketTestClient client = new WebSocketTestClient(retryUrl);
        client.handshake();
        client.setCountDownLatch(countDownLatch);
        client.sendBinary(bufferSent);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getBufferReceived(), bufferSent);
        remoteServer15300.stop();
        CountDownLatch latchForRestart = new CountDownLatch(1);
        remoteServer15300.run();
        latchForRestart.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        client.sendBinary(bufferSent);
        latchForRestart.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getBufferReceived(), bufferSent);
        client.shutDown();
        remoteServer15300.stop();
    }

@Test(description = "Tests the retry function using failover webSocket client (starting the given servers in " +
        "the target URLs, first sending and receiving text frames Afterthat stop the first server in the target " +
        "URLs, sending and receiving binary frames. Stop that server and starting the first server again, " +
        "sending and receiving text frames)")
    public void testFailoverRetryWithBothServer() throws URISyntaxException, InterruptedException,
            BallerinaTestException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        String text = "hi madam";
        ByteBuffer bufferData = ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 6});
        remoteServer15300 = new WebSocketRemoteServer(port);
        remoteServer15200 = new WebSocketRemoteServer(15200);
        remoteServer15300.run();
        remoteServer15200.run();
        WebSocketTestClient client = new WebSocketTestClient(retryUrl);
        client.handshake();
        client.setCountDownLatch(countDownLatch);
        client.sendText(text);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getTextReceived(), text);
        remoteServer15300.stop();
        CountDownLatch countDownLatchForRetry = new CountDownLatch(1);
        countDownLatchForRetry.await(time, TimeUnit.SECONDS);
        client.sendBinary(bufferData);
        countDownLatchForRetry.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getBufferReceived(), bufferData);
        remoteServer15200.stop();
        CountDownLatch latchForRetry = new CountDownLatch(1);
        remoteServer15300.run();
        latchForRetry.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        String textSend = "hi";
        client.sendText(textSend);
        latchForRetry.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getTextReceived(), textSend);
        client.shutDown();
        remoteServer15300.stop();
    }

    @Test(description = "Tests the failover webSocket client's reconnect function by doesn't start the any server" +
            " in the targets URLs")
    public void testFailingFailover() throws URISyntaxException, InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        WebSocketTestClient client = new WebSocketTestClient(url);
        client.handshake();
        client.setCountDownLatch(countDownLatch);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        CloseWebSocketFrame closeWebSocketFrame = client.getReceivedCloseFrame();
        Assert.assertNotNull(closeWebSocketFrame);
        Assert.assertEquals(closeWebSocketFrame.statusCode(), 1011);
        Assert.assertTrue(closeWebSocketFrame.reasonText().contains("Unexpected condition"));
        closeWebSocketFrame.release();
    }

    @Test(description = "Tests the retry function using webSocket client (starting the server " +
            ", sending and receiving binary frames Afterthat restart that server and do the same)")
    public void testBinaryFrameForRetryWithMaxCount() throws URISyntaxException, InterruptedException,
            BallerinaTestException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        ByteBuffer bufferSent = ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5});
        remoteServer15300 = new WebSocketRemoteServer(port);
        remoteServer15300.run();
        WebSocketTestClient client = new WebSocketTestClient(url);
        client.handshake();
        client.setCountDownLatch(countDownLatch);
        client.sendBinary(bufferSent);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getBufferReceived(), bufferSent);
        remoteServer15300.stop();
        CountDownLatch latchForRestart = new CountDownLatch(1);
        latchForRestart.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        remoteServer15300.run();
        client.sendBinary(bufferSent);
        latchForRestart.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertNull(client.getBufferReceived());
        client.shutDown();
        remoteServer15300.stop();
    }
}
