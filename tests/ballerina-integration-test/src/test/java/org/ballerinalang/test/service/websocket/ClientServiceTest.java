/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.ballerinalang.test.util.websocket.client.WebSocketTestClient;
import org.ballerinalang.test.util.websocket.server.WebSocketRemoteServer;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Tests success and failure of client endpoint creation based on the different ways of its creation.
 */
public class ClientServiceTest extends WebSocketTestCommons {

    private WebSocketTestClient client;
    private static final String URL = "ws://localhost:9090/client/service";
    private WebSocketRemoteServer remoteServer;

    @BeforeClass(description = "Initializes the Ballerina server with the client_service.bal file")
    public void setup() throws URISyntaxException, InterruptedException {
        remoteServer = new WebSocketRemoteServer(REMOTE_SERVER_PORT);
        remoteServer.run();
        client = new WebSocketTestClient(URL);
    }

    @Test(priority = 1, description = "Tests the client initialization without a callback service")
    public void testClientSuccessWithoutService() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        client.setCountDownLatch(countDownLatch);
        client.handshake();
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        String text = client.getTextReceived();

        Assert.assertNotNull(text);
        Assert.assertEquals(text, "Client worked");
    }

    @Test(priority = 2,
          description = "Tests the client initialization with a WebSocketClientService but without any resources")
    public void testClientSuccessWithWebSocketClientService() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        client.sendText("hey");
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);

        String text = client.getTextReceived();

        Assert.assertNotNull(text);
        Assert.assertEquals(text, "Client worked");
    }

    @Test(priority = 3, description = "Tests the client initialization failure when used with a WebSocketService")
    public void testClientFailureWithWebSocketService() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        client.sendBinary(ByteBuffer.wrap("hey".getBytes()));
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);

        CloseWebSocketFrame closeWebSocketFrame = client.getReceivedCloseFrame();

        Assert.assertNotNull(closeWebSocketFrame);
        Assert.assertEquals(closeWebSocketFrame.statusCode(), 1011);
        Assert.assertEquals(closeWebSocketFrame.reasonText(), "Unexpected condition");

        closeWebSocketFrame.release();
    }

    @AfterClass(description = "Stops the Ballerina server")
    public void cleanup() throws InterruptedException {
        remoteServer.stop();
        client.shutDown();
    }
}
