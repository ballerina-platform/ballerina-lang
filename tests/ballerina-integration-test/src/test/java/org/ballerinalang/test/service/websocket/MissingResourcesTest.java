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
import org.testng.annotations.Test;

import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Class to test the use cases when certain resources are missing and messages come to these resources.
 */
@Test(groups = "websocket-test")
public class MissingResourcesTest extends WebSocketTestCommons {

    private WebSocketTestClient client;
    private static final ByteBuffer SENDING_BYTE_BUFFER = ByteBuffer.wrap("hey".getBytes());
    private static final String SENDING_TEXT = "hello World!!";

    @Test(description = "Tests behavior when onText resource is missing and a text message is received")
    public void testMissingOnText() throws InterruptedException, URISyntaxException {
        client = new WebSocketTestClient("ws://localhost:9086/onlyOnBinary");
        client.handshake();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        client.setCountDownLatch(countDownLatch);
        client.sendText(SENDING_TEXT);
        client.sendBinary(SENDING_BYTE_BUFFER);
        countDownLatch.await(1000, TimeUnit.SECONDS);
        Assert.assertNull(client.getTextReceived());
        Assert.assertEquals(client.getBufferReceived(), SENDING_BYTE_BUFFER);
        client.shutDown();
    }

    @Test(description = "Tests behavior when onPong resource is missing and a pong is received")
    public void testMissingOnPong() throws InterruptedException, URISyntaxException {
        client = new WebSocketTestClient("ws://localhost:9086/onlyOnBinary");
        client.handshake();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        client.setCountDownLatch(countDownLatch);
        client.sendPong(SENDING_BYTE_BUFFER);
        client.sendBinary(SENDING_BYTE_BUFFER);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getBufferReceived(), SENDING_BYTE_BUFFER);
        client.shutDown();
    }

    @Test(description = "Tests behavior when onBinary resource is missing and binary message is received")
    public void testMissingOnBinary() throws InterruptedException, URISyntaxException {
        client = new WebSocketTestClient("ws://localhost:9087/onlyOnText");
        client.handshake();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        client.setCountDownLatch(countDownLatch);
        client.sendBinary(SENDING_BYTE_BUFFER);
        client.sendText(SENDING_TEXT);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertNull(client.getBufferReceived());
        Assert.assertEquals(client.getTextReceived(), SENDING_TEXT);
        client.shutDown();
    }

    @Test(description = "Tests behavior when onBinary resource is missing and binary message is received")
    public void testMissingOnIdleTimeout() throws InterruptedException, URISyntaxException {
        client = new WebSocketTestClient("ws://localhost:9087/onlyOnText");
        client.handshake();
        CountDownLatch countDownLatch = new CountDownLatch(2);
        client.setCountDownLatch(countDownLatch);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        client.sendText(SENDING_TEXT);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getTextReceived(), SENDING_TEXT);
        client.shutDown();
    }
}

