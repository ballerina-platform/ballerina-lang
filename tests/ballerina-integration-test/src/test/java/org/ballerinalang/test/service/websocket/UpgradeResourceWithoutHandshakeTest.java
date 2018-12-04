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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Tests when acceptWebSocketUpgrade() is not invoked in the upgrade resource.
 *
 * @since 0.982.0
 */
@Test(groups = {"websocket-test"})
public class UpgradeResourceWithoutHandshakeTest extends WebSocketTestCommons {

    @Test
    public void testUpgradeResourceWithoutHandshake() throws URISyntaxException, InterruptedException {
        WebSocketTestClient client = new WebSocketTestClient("ws://localhost:9079/UpgradeWithoutHandshake");
        CountDownLatch countDownLatch = new CountDownLatch(1);
        client.setCountDownLatch(countDownLatch);
        client.handshake();
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        String text = client.getTextReceived();
        Assert.assertNotNull(text);
        Assert.assertEquals(text, "Handshake check");
        client.shutDown();
    }
}
