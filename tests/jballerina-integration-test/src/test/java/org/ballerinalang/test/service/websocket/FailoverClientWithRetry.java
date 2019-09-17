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
    private String url = "ws://localhost:30004";
    private int port = 15100;
    private WebSocketRemoteServer remoteServer15200 = new WebSocketRemoteServer(15200);

    @Test(description = "Tests the retry function using failover webSocket client (starting the first server " +
            "in the target URLs, sending and receiving text frames Afterthat restart that server and do the same)")
    public void testTextFrameFailoverRetry() throws URISyntaxException, InterruptedException, BallerinaTestException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        remoteServer15100 = new WebSocketRemoteServer(port);
        remoteServer15100.run();
        String textSent = "hi all";
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
        CountDownLatch countDown = new CountDownLatch(1);
        countDown.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        remoteServer15100.run();
        countDown.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        client.sendText(textSent);
        countDown.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getTextReceived(), textSent);
        countDown.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        client.shutDown();
        remoteServer15100.stop();
        CountDownLatch noOfLatch = new CountDownLatch(1);
        noOfLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
    }

    @Test(description = "Tests the retry function using failover webSocket client (starting the second server " +
            "in the target URLs, sending and receiving text frames Afterthat restart that server and do the same)")
    public void testFailoverRetryWithSecondServer() throws URISyntaxException, InterruptedException,
            BallerinaTestException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        remoteServer15200 = new WebSocketRemoteServer(port);
        remoteServer15200.run();
        String textSent = "hi all";
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        WebSocketTestClient client = new WebSocketTestClient(url);
        client.handshake();
        client.setCountDownLatch(countDownLatch);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        client.sendText(textSent);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getTextReceived(), textSent);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        remoteServer15200.stop();
        CountDownLatch countDown = new CountDownLatch(1);
        countDown.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        remoteServer15200.run();
        countDown.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        client.sendText(textSent);
        countDown.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getTextReceived(), textSent);
        countDown.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        client.shutDown();
        remoteServer15200.stop();
        CountDownLatch noOfLatch = new CountDownLatch(1);
        noOfLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
    }

    @Test(description = "Tests the retry function using failover webSocket client (starting the first server " +
            "in the target URLs, sending and receiving binary frames Afterthat restart that server and do the same)")
    public void testBinaryFrameFailoverRetry() throws URISyntaxException, InterruptedException,
            BallerinaTestException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        ByteBuffer bufferSent = ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5});
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
        CountDownLatch countOfLatch = new CountDownLatch(1);
        countOfLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        remoteServer15100.run();
        countOfLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        client.sendBinary(bufferSent);
        countOfLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getBufferReceived(), bufferSent);
        countOfLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        client.shutDown();
        remoteServer15100.stop();
        CountDownLatch noOfLatch = new CountDownLatch(1);
        noOfLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
    }

@Test(description = "Tests the retry function using failover webSocket client (starting the given servers in the target URLs, " +
        "first sending and receiving text frames Afterthat stop the first server in the target URLs, " +
        "sending and receiving binary frames. Stop that server and starting the first server again, " +
        "sending and receiving text frames)")
    public void testFailoverRetryWithBothServer() throws URISyntaxException, InterruptedException,
            BallerinaTestException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        String text = "hi madam";
        ByteBuffer bufferData = ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 6});
        remoteServer15100 = new WebSocketRemoteServer(port);
        remoteServer15100.run();
        remoteServer15200.run();
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        WebSocketTestClient client = new WebSocketTestClient(url);
        client.handshake();
        client.setCountDownLatch(countDownLatch);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        client.sendText(text);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getTextReceived(), text);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        remoteServer15100.stop();
        CountDownLatch countDown = new CountDownLatch(1);
        countDown.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        client.sendBinary(bufferData);
        countDown.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getBufferReceived(), bufferData);
        remoteServer15200.stop();
        CountDownLatch countOfLatch = new CountDownLatch(1);
        countOfLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        remoteServer15100.run();
        countOfLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        String textSend = "hi";
        client.sendText(textSend);
        countOfLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getTextReceived(), textSend);
        client.shutDown();
        remoteServer15100.stop();
        CountDownLatch noOfLatch = new CountDownLatch(1);
        noOfLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
    }

//    @Test(description = "Tests sending and receiving binary frames for retry function of the WebSocket" +
//            "failover client")
//    public void testFailoverRetryCount() throws URISyntaxException, InterruptedException,
//            BallerinaTestException {
//        CountDownLatch countDownLatch = new CountDownLatch(1);
//        String expectingErrorLog = "reconnecting";
//        LogLeecher logLeecher = new LogLeecher(expectingErrorLog);
//        serverInstance.addLogLeecher(logLeecher);
//        String text = "hi madam";
//        ByteBuffer bufferData = ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 6});
//        remoteServer15100 = new WebSocketRemoteServer(port);
//        remoteServer15100.run();
//        remoteServer15200.run();
//        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
//        WebSocketTestClient client = new WebSocketTestClient(url);
//        client.handshake();
//        client.setCountDownLatch(countDownLatch);
//        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
//        client.sendText(text);
//        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
//        Assert.assertEquals(client.getTextReceived(), text);
//        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
//        remoteServer15100.stop();
//        CountDownLatch countDown = new CountDownLatch(1);
//        countDown.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
//        client.sendBinary(bufferData);
//        countDown.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
//        Assert.assertEquals(client.getBufferReceived(), bufferData);
//        remoteServer15200.stop();
//        CountDownLatch noOfLatch = new CountDownLatch(1);
//        noOfLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
//        int count = 0;
//        if (logLeecher.text.contains(expectingErrorLog)) {
//            count = count + 1;
//        }
//        Assert.assertEquals(count, 5);
//    }
}
