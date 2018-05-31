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

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Test case to test simple WebSocket pass through scenarios.
 */
public class WebSocketSimpleProxyTestCase extends WebSocketIntegrationTest {

    private WebSocketRemoteServer remoteServer;
    private static final String URL = "ws://localhost:9090/proxy/ws";

    @BeforeClass(description = "Initializes Ballerina")
    public void setup() throws InterruptedException, BallerinaTestException {
        remoteServer = new WebSocketRemoteServer(REMOTE_SERVER_PORT);
        remoteServer.run();
        initBallerinaServer("simple_proxy_server.bal");
    }

    @Test(priority = 1, description = "Tests sending and receiving of text frames in WebSockets")
    public void testSendText() throws URISyntaxException, InterruptedException {
        WebSocketTestClient client = new WebSocketTestClient(URL);
        client.handshake();
        String textSent = "hi all";
        CountDownLatch countDownLatch = new CountDownLatch(1);
        client.setCountDownLatch(countDownLatch);
        client.sendText(textSent);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getTextReceived(), textSent);
        client.shutDown();
    }

    @Test(priority = 2, description = "Tests sending and receiving of binary frames in WebSockets")
    public void testSendBinary() throws URISyntaxException, InterruptedException, IOException {
        WebSocketTestClient client = new WebSocketTestClient(URL);
        client.handshake();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        client.setCountDownLatch(countDownLatch);
        ByteBuffer bufferSent = ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5});
        client.sendBinary(bufferSent);
        countDownLatch.await(1000, TimeUnit.SECONDS);
        Assert.assertEquals(client.getBufferReceived(), bufferSent);
        client.shutDown();
    }

    @AfterClass(description = "Stops Ballerina")
    public void cleanup() throws BallerinaTestException {
        stopBallerinaServerInstance();
        remoteServer.stop();
    }
}
