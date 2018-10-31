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
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.test.util.websocket.client.WebSocketTestClient;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Test whether the errors are received correctly to the onError resource in WebSocket server.
 */
@Test(groups = "websocket-test")
public class OnErrorWebSocketTest extends WebSocketTestCommons {

    private WebSocketTestClient client;
    private static final String URL = "ws://localhost:9094/error/ws";
    private LogLeecher logLeecher;

    @BeforeClass(description = "Initializes the Ballerina server with the error_log_service.bal file")
    public void setup() throws InterruptedException, URISyntaxException {
        String expectingErrorLog = "received continuation data frame outside fragmented message";
        logLeecher = new LogLeecher(expectingErrorLog);
        serverInstance.addLogLeecher(logLeecher);

        client = new WebSocketTestClient(URL);
        client.handshake();
    }

    @Test
    public void testOnError() throws InterruptedException, BallerinaTestException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        client.setCountDownLatch(countDownLatch);
        client.sendCorruptedFrame();
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        logLeecher.waitForText(TIMEOUT_IN_SECS * 1000);
        CloseWebSocketFrame closeWebSocketFrame = client.getReceivedCloseFrame();

        Assert.assertNotNull(closeWebSocketFrame);
        Assert.assertEquals(closeWebSocketFrame.statusCode(), 1002);

        closeWebSocketFrame.release();
    }

    @AfterClass(description = "Stops the Ballerina server")
    public void cleanup() throws InterruptedException {
        client.shutDown();
    }
}
