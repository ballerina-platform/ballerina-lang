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
 * This Class tests retry support of WebSocket client and server.
 */
@Test(groups = {"websocket-test"})
public class RetryClientTest extends WebSocketTestCommons {

    private WebSocketRemoteServer remoteServer;
    private String url;
    private int port;
    private boolean sslEnabled;

    public RetryClientTest(int port, boolean sslEnabled, String url) {
        this.port = port;
        this.sslEnabled = sslEnabled;
        this.url = url;
    }

    @Test(description = "Tests sending and receiving of text frames in WebSockets")
    public void testSendText() throws URISyntaxException, InterruptedException, BallerinaTestException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        remoteServer = new WebSocketRemoteServer(port, sslEnabled);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        remoteServer.run();
        WebSocketTestClient client = new WebSocketTestClient(url);
        client.handshake();
        client.setCountDownLatch(countDownLatch);
        remoteServer.stop();
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        remoteServer.run();
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        String textSent = "hi all";
        client.sendText(textSent);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getTextReceived(), textSent);
        remoteServer.stop();
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        client.shutDown();
    }

    @Test(description = "Tests sending and receiving of binary frames in WebSocket")
    public void testSendBinary() throws URISyntaxException, InterruptedException, BallerinaTestException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        remoteServer = new WebSocketRemoteServer(port, sslEnabled);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        remoteServer.run();
        WebSocketTestClient client = new WebSocketTestClient(url);
        client.handshake();
        client.setCountDownLatch(countDownLatch);
        remoteServer.stop();
        countDownLatch.await(50, TimeUnit.SECONDS);
        remoteServer.run();
        countDownLatch.await(50, TimeUnit.SECONDS);
        ByteBuffer bufferSent = ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5});
        client.sendBinary(bufferSent);
        countDownLatch.await(20, TimeUnit.SECONDS);
        Assert.assertEquals(client.getBufferReceived(), bufferSent);
        client.shutDown();
        remoteServer.stop();
    }

    @Test(description = "Tests the client initialization failing in a resource")
    public void testClientEndpointFailureInResource() throws URISyntaxException, InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        WebSocketTestClient client = new WebSocketTestClient(url);
        client.handshake();
        client.setCountDownLatch(countDownLatch);
        countDownLatch.await(50, TimeUnit.SECONDS);
        CloseWebSocketFrame closeWebSocketFrame = client.getReceivedCloseFrame();

        Assert.assertNotNull(closeWebSocketFrame);
        Assert.assertEquals(closeWebSocketFrame.statusCode(), 1011);
        Assert.assertTrue(closeWebSocketFrame.reasonText().contains("Connection refused: localhost/127.0.0.1:15300"));

        closeWebSocketFrame.release();
    }

    @Test(description = "Tests sending and receiving of binary frames in WebSocket")
    public void testSendBinary1() throws URISyntaxException, InterruptedException, BallerinaTestException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        remoteServer = new WebSocketRemoteServer(port, sslEnabled);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        remoteServer.run();
        WebSocketTestClient client = new WebSocketTestClient(url);
        client.handshake();
        client.setCountDownLatch(countDownLatch);
        remoteServer.stop();
        countDownLatch.await(50, TimeUnit.SECONDS);
        remoteServer.run();
        countDownLatch.await(100, TimeUnit.SECONDS);
        ByteBuffer bufferSent = ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5});
        client.sendBinary(bufferSent);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getBufferReceived(), bufferSent);
        remoteServer.stop();
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        remoteServer.run();
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        client.sendBinary(bufferSent);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getBufferReceived(), bufferSent);
        client.shutDown();
        remoteServer.stop();
    }
}
