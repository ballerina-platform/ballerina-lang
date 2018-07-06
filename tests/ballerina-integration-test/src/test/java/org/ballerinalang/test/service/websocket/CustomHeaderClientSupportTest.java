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

import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.util.websocket.client.WebSocketTestClient;
import org.ballerinalang.test.util.websocket.server.WebSocketRemoteServer;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * This Class tests receiving and sending of custom headers by Ballerina WebSocket client.
 */
public class CustomHeaderClientSupportTest extends WebSocketIntegrationTest {

    private WebSocketRemoteServer remoteServer;
    private WebSocketTestClient client;
    private static final String URL = "ws://localhost:9090/pingpong/ws";
    private static final String RECEIVED_TEXT = "some-header-value";

    @BeforeClass(description = "Initializes the Ballerina server with the custom_header_client.bal file")
    public void setup() throws InterruptedException, BallerinaTestException, URISyntaxException {
        remoteServer = new WebSocketRemoteServer(REMOTE_SERVER_PORT);
        remoteServer.run();

        initBallerinaServer("custom_header_client.bal");

        client = new WebSocketTestClient(URL);
        client.handshake();
    }

    @Test(description = "Tests when the client sends custom headers to the server")
    public void testClientSentCustomHeader() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        client.setCountDownLatch(countDownLatch);
        client.sendText("custom-headers");
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getTextReceived(), RECEIVED_TEXT);
    }

    @Test(description = "Tests the client receiving custom headers from the server")
    public void testClientReceivedCustomHeader() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        client.setCountDownLatch(countDownLatch);
        client.sendText("server-headers");
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getTextReceived(), "server-header-value");
    }

    @AfterClass(description = "Stops the Ballerina server")
    public void cleanup() throws BallerinaTestException, InterruptedException {
        stopBallerinaServerInstance();
        client.shutDown();
        remoteServer.stop();
    }
}
