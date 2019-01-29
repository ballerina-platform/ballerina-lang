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
 * Test continuation for the onBinaryResource.
 *
 * @since 0.982.0
 */
@Test(groups = {"websocket-test"})
public class OnBinaryContinuationTest extends WebSocketTestCommons {

    private WebSocketTestClient client;

    @Test(description = "Tests XML support for pushText and onText")
    public void testBinaryContinuation() throws URISyntaxException, InterruptedException {
        String url = "http://localhost:9088/onBinaryContinuation";
        client = new WebSocketTestClient(url);
        client.handshake();
        String msg = "<note><to>Tove</to></note>";
        assertContinuationSuccess(msg, "<note>", "<to>Tove", "</to>", "</note>");
        client.shutDown();
    }

    private void assertContinuationSuccess(String expectedMsg, String... msg)
            throws InterruptedException {
        ByteBuffer buffer;
        CountDownLatch countDownLatch = new CountDownLatch(1);
        client.setCountDownLatch(countDownLatch);
        for (int i = 0; i < msg.length - 1; i++) {
            buffer = ByteBuffer.wrap(msg[i].getBytes());
            client.sendBinary(buffer, false);
        }
        buffer = ByteBuffer.wrap(msg[msg.length - 1].getBytes());
        client.sendBinary(buffer, true);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        buffer = client.getBufferReceived();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        Assert.assertEquals(new String(bytes).trim(), expectedMsg, "Invalid message received");
    }
}
