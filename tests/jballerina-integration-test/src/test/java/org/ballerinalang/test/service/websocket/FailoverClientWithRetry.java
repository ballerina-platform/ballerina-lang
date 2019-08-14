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
 * This Class tests support retry function for failover WebSocket client.
 */
@Test(groups = {"websocket-test"})
public class FailoverClientWithRetry extends WebSocketTestCommons {

    private WebSocketRemoteServer remoteServer15100;
    private String url = "ws://localhost:21029";
    private int port = 15100;
    private ByteBuffer bufferSent = ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5});
    private String textSent = "hi all";

    @Test(description = "Tests sending and receiving text frames for retry function of the WebSocket failover client")
    public void testTextFrameFailoverRetry() throws URISyntaxException, InterruptedException, BallerinaTestException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        remoteServer15100 = new WebSocketRemoteServer(port);
        remoteServer15100.run();
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        WebSocketTestClient client = new WebSocketTestClient(url);
        client.handshake();
        client.setCountDownLatch(countDownLatch);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        client.sendText(textSent);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getTextReceived(), textSent);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        remoteServer15100.stop();
        CountDownLatch countDownLatch1 = new CountDownLatch(1);
        countDownLatch1.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        remoteServer15100.run();
        countDownLatch1.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        client.sendText(textSent);
        countDownLatch1.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getTextReceived(), textSent);
        countDownLatch1.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        client.shutDown();
        remoteServer15100.stop();
        CountDownLatch countDownLatch2 = new CountDownLatch(1);
        countDownLatch2.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
    }

    @Test(description = "Tests sending and receiving binary frames for retry function of the WebSocket" +
    "failover client")
    public void testBinaryFrameFailoverRetry() throws URISyntaxException, InterruptedException,
            BallerinaTestException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        remoteServer15100 = new WebSocketRemoteServer(port);
        remoteServer15100.run();
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        WebSocketTestClient client = new WebSocketTestClient(url);
        client.handshake();
        client.setCountDownLatch(countDownLatch);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        client.sendBinary(bufferSent);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getBufferReceived(), bufferSent);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        remoteServer15100.stop();
        CountDownLatch countDownLatch1 = new CountDownLatch(1);
        countDownLatch1.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        remoteServer15100.run();
        countDownLatch1.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        client.sendBinary(bufferSent);
        countDownLatch1.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getBufferReceived(), bufferSent);
        countDownLatch1.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        client.shutDown();
        remoteServer15100.stop();
        CountDownLatch countDownLatch2 = new CountDownLatch(1);
        countDownLatch2.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
    }

    @Test(description = "Tests sending and receiving frames for retry function of the WebSocket failover" +
    "client with both server")
    public void testFailoverRetryWithBothServer() throws URISyntaxException, InterruptedException,
            BallerinaTestException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        WebSocketRemoteServer remoteServer15200 = new WebSocketRemoteServer(15200);
        remoteServer15100 = new WebSocketRemoteServer(port);
        remoteServer15100.run();
        remoteServer15200.run();
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        WebSocketTestClient client = new WebSocketTestClient(url);
        client.handshake();
        client.setCountDownLatch(countDownLatch);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        client.sendText(textSent);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getTextReceived(), textSent);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        remoteServer15100.stop();
        CountDownLatch countDownLatch1 = new CountDownLatch(1);
        countDownLatch1.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        client.sendBinary(bufferSent);
        countDownLatch1.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getBufferReceived(), bufferSent);
        remoteServer15200.stop();
        CountDownLatch countDownLatch2 = new CountDownLatch(1);
        countDownLatch2.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        remoteServer15100.run();
        countDownLatch2.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        String text = "hi";
        client.sendText(text);
        countDownLatch2.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getTextReceived(), text);
        client.shutDown();
        remoteServer15100.stop();
        CountDownLatch countDownLatch3 = new CountDownLatch(1);
        countDownLatch3.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
    }
}
