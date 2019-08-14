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
 * This Class tests failover support of WebSocket client.
 */
@Test(groups = {"websocket-test"})
public class FailoverClientTest extends WebSocketTestCommons {

    private WebSocketRemoteServer remoteServer15100;
    private WebSocketRemoteServer remoteServer15200;
    private String url;
    private int port;
    private ByteBuffer bufferSent = ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5});
    private boolean sslEnabled;
    private int time = 50;

    public FailoverClientTest(int port, boolean sslEnabled, String url) {
        this.port = port;
        this.sslEnabled = sslEnabled;
        this.url = url;
    }

    @Test(description = "Tests sending and receiving of text frames in WebSockets")
    public void testTextFrameForFailover() throws URISyntaxException, InterruptedException, BallerinaTestException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        remoteServer15100 = new WebSocketRemoteServer(port, sslEnabled);
        remoteServer15100.run();
        countDownLatch.await(time, TimeUnit.SECONDS);
        WebSocketTestClient client = new WebSocketTestClient(url);
        client.handshake();
        client.setCountDownLatch(countDownLatch);
        countDownLatch.await(time, TimeUnit.SECONDS);
        String textSent = "hi all";
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        client.sendText(textSent);
        countDownLatch.await(10, TimeUnit.SECONDS);
        Assert.assertEquals(client.getTextReceived(), textSent);
        countDownLatch.await(15, TimeUnit.SECONDS);
        client.shutDown();
        remoteServer15100.stop();
        CountDownLatch countDownLatch1 = new CountDownLatch(1);
        countDownLatch1.await(12, TimeUnit.SECONDS);
    }

    @Test(description = "Tests sending and receiving of binary frames in WebSocket")
    public void testBinaryFrameForFailover() throws URISyntaxException, InterruptedException, BallerinaTestException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        remoteServer15100 = new WebSocketRemoteServer(port, sslEnabled);
        remoteServer15100.run();
        countDownLatch.await(time, TimeUnit.SECONDS);
        WebSocketTestClient client = new WebSocketTestClient(url);
        client.handshake();
        client.setCountDownLatch(countDownLatch);
        countDownLatch.await(time, TimeUnit.SECONDS);
        ByteBuffer bufferSent = ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5});
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        client.sendBinary(bufferSent);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getBufferReceived(), bufferSent);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        client.shutDown();
        remoteServer15100.stop();
        CountDownLatch countDownLatch1 = new CountDownLatch(1);
        countDownLatch1.await(12, TimeUnit.SECONDS);
    }

    @Test(description = "Tests the client initialization failing")
    public void testFailingFailover() throws URISyntaxException, InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        WebSocketTestClient client = new WebSocketTestClient(url);
        client.handshake();
        client.setCountDownLatch(countDownLatch);
        countDownLatch.await(time, TimeUnit.SECONDS);
        CloseWebSocketFrame closeWebSocketFrame = client.getReceivedCloseFrame();

        Assert.assertNotNull(closeWebSocketFrame);
        Assert.assertEquals(closeWebSocketFrame.statusCode(), 1011);
        Assert.assertTrue(closeWebSocketFrame.reasonText().contains("Connection refused: " +
                "localhost/127.0.0.1:15200"));

        closeWebSocketFrame.release();
    }

    @Test(description = "Tests sending and receiving of binary frames in WebSocket")
    public void testFailoverWithBothServer() throws URISyntaxException, InterruptedException,
            BallerinaTestException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        remoteServer15200 = new WebSocketRemoteServer(15200);
        remoteServer15100 = new WebSocketRemoteServer(port);
        remoteServer15100.run();
        remoteServer15200.run();
        countDownLatch.await(12, TimeUnit.SECONDS);
        WebSocketTestClient client = new WebSocketTestClient(url);
        client.handshake();
        client.setCountDownLatch(countDownLatch);
        countDownLatch.await(12, TimeUnit.SECONDS);
        String textSent = "hi all";
        countDownLatch.await(12, TimeUnit.SECONDS);
        client.sendText(textSent);
        countDownLatch.await(10, TimeUnit.SECONDS);
        Assert.assertEquals(client.getTextReceived(), textSent);
        countDownLatch.await(15, TimeUnit.SECONDS);
        remoteServer15100.stop();
        CountDownLatch countDownLatch1 = new CountDownLatch(1);
        countDownLatch1.await(20, TimeUnit.SECONDS);
        String text = "hi";
        countDownLatch.await(12, TimeUnit.SECONDS);
        client.sendText(text);
        countDownLatch.await(10, TimeUnit.SECONDS);
        Assert.assertEquals(client.getTextReceived(), text);
        client.sendBinary(bufferSent);
        countDownLatch1.await(12, TimeUnit.SECONDS);
        Assert.assertEquals(client.getBufferReceived(), bufferSent);
        client.shutDown();
        remoteServer15200.stop();
        countDownLatch.await(time, TimeUnit.SECONDS);
        CountDownLatch countDownLatch2 = new CountDownLatch(1);
        countDownLatch2.await(12, TimeUnit.SECONDS);
    }
}
