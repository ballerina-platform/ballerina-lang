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
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Test whether resource failure causes a close frame to be sent.
 */
@Test(groups = {"websocket-test"})
public class ResourceFailureTest extends WebSocketTestCommons {

    private WebSocketTestClient client;
    private static final String URL = "ws://localhost:21016/simple7?q1=name";
    private static final ByteBuffer SENDING_BYTE_BUFFER = ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5});
    private CountDownLatch countDownLatch;

    @BeforeMethod(description = "Creates a new client and performs handshake")
    public void clientSetup() throws URISyntaxException, InterruptedException {
        client = new WebSocketTestClient(URL);
        client.handshake();
        countDownLatch = new CountDownLatch(1);
        client.setCountDownLatch(countDownLatch);
    }

    @Test(description = "Tests failure of onText resource")
    public void testOnTextResource() throws InterruptedException {
        client.sendText("Hello world!!");
        assertCloseFrame();
    }

    @Test(description = "Tests failure of onBinary resource")
    public void testOnBinaryResource() throws InterruptedException {
        client.sendBinary(SENDING_BYTE_BUFFER);
        assertCloseFrame();
    }

    @Test(description = "Tests failure of onPing resource")
    public void testOnPingResource() throws InterruptedException {
        client.sendPing(SENDING_BYTE_BUFFER);
        assertCloseFrame();
    }

    @Test(description = "Tests failure of onIdleTimeout resource")
    public void testOnIdleTimeoutResource() throws InterruptedException {
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        assertCloseFrame();
    }

    @Test(description = "Tests failure of onClose resource")
    public void testOnCloseResource() throws InterruptedException {
        client.shutDown();
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        CloseWebSocketFrame closeWebSocketFrame = client.getReceivedCloseFrame();

        Assert.assertNotNull(closeWebSocketFrame);
        Assert.assertEquals(closeWebSocketFrame.statusCode(), -1);

        closeWebSocketFrame.release();
    }

    @AfterMethod(description = "Shuts down the client")
    public void stopClient() throws InterruptedException {
        client.shutDown();
    }

    private void assertCloseFrame() throws InterruptedException {
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        CloseWebSocketFrame closeWebSocketFrame = client.getReceivedCloseFrame();

        Assert.assertNotNull(closeWebSocketFrame);
        Assert.assertEquals(closeWebSocketFrame.statusCode(), 1011);
        Assert.assertEquals(closeWebSocketFrame.reasonText(), "Unexpected condition");

        closeWebSocketFrame.release();
    }
}
