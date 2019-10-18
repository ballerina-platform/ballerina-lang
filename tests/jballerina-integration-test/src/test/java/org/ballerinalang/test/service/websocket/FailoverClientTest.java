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
 * This Class tests failover support of the WebSocket client.
 */
@Test(groups = {"websocket-test"})
public class FailoverClientTest extends WebSocketTestCommons {

    private WebSocketRemoteServer remoteServer15300;
    private WebSocketRemoteServer remoteServer15200;
    private String url;
    private int port;
    private boolean sslEnabled;
    private int time = 2;

    public FailoverClientTest(int port, boolean sslEnabled, String url) {
        this.port = port;
        this.sslEnabled = sslEnabled;
        this.url = url;
    }

    @Test(description = "Tests starting the second server in the target URLs, sending and receiving text frames " +
            "using the failover websocket client")
    public void testTextFrameWithSecondServer() throws URISyntaxException, InterruptedException,
            BallerinaTestException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        remoteServer15200 = new WebSocketRemoteServer(15200);
        remoteServer15200.run();
        WebSocketTestClient client = new WebSocketTestClient(url);
        client.handshake();
        String textSent = "hi all";
        client.sendText(textSent);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getTextReceived(), textSent);
        client.shutDown();
        remoteServer15200.stop();
    }

    @Test(description = "Tests starting one of the server in the target URLs, sending and receiving binary" +
            " frames using the failover websocket client")
    public void testBinaryFrameForFailover() throws URISyntaxException, InterruptedException, BallerinaTestException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        remoteServer15300 = new WebSocketRemoteServer(port, sslEnabled);
        remoteServer15300.run();
        WebSocketTestClient client = new WebSocketTestClient(url);
        client.setCountDownLatch(countDownLatch);
        client.handshake();
        ByteBuffer bufferSent = ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5});
        client.sendBinary(bufferSent);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getBufferReceived(), bufferSent);
        client.shutDown();
        remoteServer15300.stop();
    }

    @Test(description = "Tests the failover webSocket client by doesn't start the any server in the targets URLs")
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

    @Test(description = "Test the Failover websocket client (starting the given servers in the target URLs, " +
            "first sending and receiving text frames Afterthat stop the first server in the target URLs, " +
            "sending and receiving binary frames) ")
    public void testFailoverWithBothServer() throws URISyntaxException, InterruptedException,
            BallerinaTestException {
        CountDownLatch countDownLatchFor15300 = new CountDownLatch(1);
        remoteServer15200 = new WebSocketRemoteServer(15200);
        remoteServer15300 = new WebSocketRemoteServer(port);
        remoteServer15300.run();
        remoteServer15200.run();
        ByteBuffer bufferSent = ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 6});
        WebSocketTestClient client = new WebSocketTestClient(url);
        client.handshake();
        String textSent = "hi all";
        client.sendText(textSent);
        countDownLatchFor15300.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getTextReceived(), textSent);
        remoteServer15300.stop();
        CountDownLatch countDownLatchFor15200 = new CountDownLatch(1);
        countDownLatchFor15200.await(time, TimeUnit.SECONDS);
        String text = "hi";
        client.sendText(text);
        countDownLatchFor15200.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getTextReceived(), text);
        client.sendBinary(bufferSent);
        countDownLatchFor15200.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getBufferReceived(), bufferSent);
        client.shutDown();
        remoteServer15200.stop();
    }
}
