/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.test.util.websocket.client.WebSocketTestClient;
import org.testng.annotations.Test;

import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Tests the cookie support of the WebSocket client.
 */
@Test(groups = {"websocket-test"})
public class CookieTest extends WebSocketTestCommons {

    private WebSocketTestClient client;
    private static final String URL = "ws://localhost:21037";
    private LogLeecher logLeecher;
    String expectingErrorLog;

    @Test(description = "Test the cookie support")
    public void testCookieSupport() throws InterruptedException, URISyntaxException, BallerinaTestException {
        expectingErrorLog = "Hello World!";
        logLeecher = new LogLeecher(expectingErrorLog);
        serverInstance.addLogLeecher(logLeecher);
        client = new WebSocketTestClient(URL);
        client.handshake();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await(4, TimeUnit.SECONDS);
        logLeecher.waitForText(TIMEOUT_IN_SECS * 1000);
        client.shutDown();
    }

    @Test(description = "Test with incorrect cookie")
    public void negativeTestCase() throws InterruptedException, URISyntaxException, BallerinaTestException {
        expectingErrorLog = "Error sending message error InvalidHandshakeError: Invalid handshake response getStatus:" +
                " 401 Unauthorized";
        logLeecher = new LogLeecher(expectingErrorLog);
        serverInstance.addLogLeecher(logLeecher);
        client = new WebSocketTestClient("ws://localhost:21038");
        client.handshake();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await(4, TimeUnit.SECONDS);
        logLeecher.waitForText(TIMEOUT_IN_SECS * 1000);
        client.shutDown();
    }
}
