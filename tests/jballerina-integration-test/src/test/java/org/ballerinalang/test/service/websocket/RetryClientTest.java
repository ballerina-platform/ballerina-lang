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
 * This Class tests the retry support of the WebSocket client and server.
 */
@Test(groups = {"websocket-test"})
public class RetryClientTest extends WebSocketTestCommons {

    private WebSocketRemoteServer remoteServer;
    private String url =  "ws://localhost:21030";
    private int port = 15300;

    @Test(description = "Tests the retry function using webSocket client (starting the server " +
            ", sending and receiving text frames Afterthat restart that server and do the same)")
    public void testRetry() throws URISyntaxException, InterruptedException, BallerinaTestException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        String textSent = "hi all";
        remoteServer = new WebSocketRemoteServer(port);
        remoteServer.run();
        WebSocketTestClient client = new WebSocketTestClient(url);
        client.handshake();
        client.setCountDownLatch(countDownLatch);
        client.sendText(textSent);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getTextReceived(), textSent);
        remoteServer.stop();
        CountDownLatch latchForRestart = new CountDownLatch(1);
        remoteServer.run();
        latchForRestart.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        client.sendText(textSent);
        latchForRestart.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getTextReceived(), textSent);
        remoteServer.stop();
        client.shutDown();
    }

    @Test(description = "Tests the retry function with maximum count using webSocket client (starting the given" +
            " server, sending and receiving text frames Afterthat restart that server, sending and receiving" +
            " text frames. Again, restart that server, sending and receiving binary frames)")
    public void testMultipleRetryAttempts() throws URISyntaxException, InterruptedException, BallerinaTestException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        String text = "hi madam";
        ByteBuffer bufferData = ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 6});
        remoteServer = new WebSocketRemoteServer(port);
        remoteServer.run();
        WebSocketTestClient client = new WebSocketTestClient(url);
        client.handshake();
        client.setCountDownLatch(countDownLatch);
        client.sendText(text);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getTextReceived(), text);
        remoteServer.stop();
        CountDownLatch latchForRestart = new CountDownLatch(1);
        remoteServer.run();
        latchForRestart.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        client.sendText(text);
        latchForRestart.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getTextReceived(), text);
        remoteServer.stop();
        CountDownLatch countDownLatchForRestart = new CountDownLatch(1);
        remoteServer.run();
        countDownLatchForRestart.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        client.sendBinary(bufferData);
        countDownLatchForRestart.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getBufferReceived(), bufferData);
        client.shutDown();
        remoteServer.stop();
    }

    @Test(description = "Tests the retry function using webSocket client (starting the server " +
            ", sending and receiving binary frames Afterthat restart that server and do the same)")
    public void testBinaryFrameForRetryWithMaxCount() throws URISyntaxException, InterruptedException,
            BallerinaTestException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        ByteBuffer bufferSent = ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5});
        remoteServer = new WebSocketRemoteServer(port);
        remoteServer.run();
        WebSocketTestClient client = new WebSocketTestClient("ws://localhost:21031");
        client.handshake();
        client.setCountDownLatch(countDownLatch);
        client.sendBinary(bufferSent);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getBufferReceived(), bufferSent);
        remoteServer.stop();
        CountDownLatch latchForRestart = new CountDownLatch(1);
        latchForRestart.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        remoteServer.run();
        client.sendBinary(bufferSent);
        latchForRestart.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertNull(client.getBufferReceived());
        client.shutDown();
        remoteServer.stop();
    }

    @Test(description = "Tests the retry function using webSocket client (starting the server " +
     ", sending and receiving binary frames Afterthat restart that server and do the same)")
    public void testCoundownLatchForRetry() throws URISyntaxException, InterruptedException,
            BallerinaTestException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        ByteBuffer bufferSent = ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5});
        remoteServer = new WebSocketRemoteServer(port);
        remoteServer.run();
        WebSocketTestClient client = new WebSocketTestClient("ws://localhost:21032");
        client.handshake();
        client.setCountDownLatch(countDownLatch);
        client.sendBinary(bufferSent);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getBufferReceived(), bufferSent);
        remoteServer.stop();
        CountDownLatch latchForRestart = new CountDownLatch(1);
        latchForRestart.await(6, TimeUnit.MINUTES);
        remoteServer.run();
        latchForRestart.await(2, TimeUnit.MINUTES);
        client.sendBinary(bufferSent);
        latchForRestart.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getBufferReceived(), bufferSent);
        client.shutDown();
        remoteServer.stop();
    }
}
