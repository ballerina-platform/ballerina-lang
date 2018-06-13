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
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * This Class tests auto ping pong support of WebSocket client server if there's no onPing resource.
 */
public class WebSocketAutoPingPongTest extends WebSocketIntegrationTest {

    private WebSocketTestClient client;
    private static final String URL = "ws://localhost:9090/test/without/ping/resource";
    private static final ByteBuffer SENDING_BYTE_BUFFER = ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5});

    @BeforeClass(description = "Initializes Ballerina with the simple_server_without_ping_resource.bal file")
    public void setup() throws InterruptedException, BallerinaTestException, URISyntaxException {
        initBallerinaServer("simple_server_without_ping_resource.bal");
        client = new WebSocketTestClient(URL);
        client.handshake();
    }

    @Test(description = "Tests the auto ping pong support in Ballerina if there is no onPing resource")
    public void testAutoPingPongSupport() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        client.setCountDownLatch(countDownLatch);
        client.sendPing(SENDING_BYTE_BUFFER);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertTrue(client.isPong());
        Assert.assertEquals(client.getBufferReceived(), SENDING_BYTE_BUFFER);
    }

    @AfterClass(description = "Stops Ballerina")
    public void cleanup() throws BallerinaTestException, InterruptedException {
        client.shutDown();
        stopBallerinaServerInstance();
    }
}
