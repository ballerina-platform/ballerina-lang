/*
 * Copyright (c) 2020, WSO2 Inc. (http:www.wso2.org) All Rights Reserved.
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
    private WebSocketTestClient client;
    private static final String url =  "ws://localhost:21030";
    private static final int port = 15300;

    @Test(description = "Tests the retry function using the WebSocket client (Restart the server and send the data")
    public void testRetry() throws URISyntaxException, InterruptedException, BallerinaTestException {
        remoteServer = initiateServer();
        client = initiateClient(url);
        sendTextDataAndAssert("Hi");
        restartServerAndGiveTimeClientConnectToServer();
        sendTextDataAndAssert("Hi madam");
        closeConnection();
    }

    @Test(description = "Tests the retry function with the maximum count using the WebSocket client (" +
            "Restart the server twice and send the data for every restart)")
    public void testMultipleRetryAttempts() throws URISyntaxException, InterruptedException, BallerinaTestException {
        remoteServer = initiateServer();
        client = initiateClient(url);
        sendTextDataAndAssert("Hi");
        restartServerAndGiveTimeClientConnectToServer();
        sendTextDataAndAssert("Hi madam");
        restartServerAndGiveTimeClientConnectToServer();
        sendBinaryDataAndAssert();
        closeConnection();
    }

    @Test(description = "Tests the retry function using the WebSocket client (Restart the server and " +
            "check the maximum count")
    public void testBinaryFrameForRetryWithMaxCount() throws URISyntaxException, InterruptedException,
            BallerinaTestException {
        remoteServer = initiateServer();
        client = initiateClient("ws://localhost:21031");
        sendBinaryDataAndAssert();
        restartServerAndGiveTimeClientConnectToServer();
        CountDownLatch latchForSendBinary = new CountDownLatch(1);
        client.setCountDownLatch(latchForSendBinary);
        client.sendBinary(ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 6}));
        latchForSendBinary.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertNull(client.getBufferReceived());
        closeConnection();
    }

    @Test(description = "Tests the `countDownLatch` for the retry function using the WebSocket client (" +
            "Restart the server and check the countDownLatch for handshake)")
    public void testCountdownLatchForRetry() throws URISyntaxException, InterruptedException, BallerinaTestException {
        remoteServer = initiateServer();
        client = initiateClient(url);
        sendBinaryDataAndAssert();
        restartServerAndGiveTimeClientConnectToServer();
        sendBinaryDataAndAssert();
        closeConnection();
    }

    private WebSocketRemoteServer initiateServer() throws InterruptedException, BallerinaTestException {
        remoteServer = new WebSocketRemoteServer(port);
        remoteServer.run();
        return remoteServer;
    }

    private WebSocketTestClient initiateClient(String url) throws InterruptedException, URISyntaxException {
        client = new WebSocketTestClient(url);
        client.handshake();
        return client;
    }

    private void closeConnection() throws InterruptedException {
        client.shutDown();
        remoteServer.stop();
    }

    private void restartServerAndGiveTimeClientConnectToServer() throws InterruptedException, BallerinaTestException {
        remoteServer.stop();
        CountDownLatch latchForRestart = new CountDownLatch(1);
        latchForRestart.await(7, TimeUnit.SECONDS);
        remoteServer.run();
        latchForRestart.await(2, TimeUnit.SECONDS);
    }

    private void sendTextDataAndAssert(String text) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        client.setCountDownLatch(countDownLatch);
        client.sendText(text);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getTextReceived(), text);
    }

    private void sendBinaryDataAndAssert() throws InterruptedException {
        ByteBuffer data = ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 6});
        CountDownLatch countDownLatch = new CountDownLatch(1);
        client.setCountDownLatch(countDownLatch);
        client.sendBinary(data);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getBufferReceived(), data);
    }
}
