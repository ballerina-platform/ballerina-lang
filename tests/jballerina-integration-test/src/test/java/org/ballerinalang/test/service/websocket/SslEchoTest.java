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
 * Tests an ssl WebSocket echo server.
 *
 * @since 0.990.1
 */
@Test(groups = {"websocket-test"})
public class SslEchoTest extends WebSocketTestCommons {

    private static final String URL = "wss://localhost:9076/sslEcho";
    private WebSocketTestClient client;

    // Related file 21_ssl_echo.bal
    @BeforeClass(description = "Initializes the ssl client")
    public void setup() throws URISyntaxException, InterruptedException {
        client = new WebSocketTestClient(URL, true);
        client.handshake();
    }

    @Test(description = "Tests echoed text message from the ssl server")
    public void sslTextEcho() throws InterruptedException {
        String msg = "Hello";
        CountDownLatch countDownLatch = new CountDownLatch(1);
        client.setCountDownLatch(countDownLatch);
        client.sendText(msg);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getTextReceived(), msg, "Invalid text message received");
    }

    @Test(description = "Tests echoed binary message from the ssl server")
    public void sslBinaryEcho() throws InterruptedException {
        ByteBuffer msg = ByteBuffer.wrap("hey".getBytes());
        CountDownLatch countDownLatch = new CountDownLatch(1);
        client.setCountDownLatch(countDownLatch);
        client.sendBinary(msg);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getBufferReceived(), msg, "Invalid binary message received");
    }

    @AfterClass
    public void cleanUp() throws InterruptedException {
        client.shutDown();
    }
}
