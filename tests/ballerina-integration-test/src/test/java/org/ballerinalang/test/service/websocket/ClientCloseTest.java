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
import org.testng.annotations.Test;

import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Test client close without a close frame and using a close frame without a status code.
 */
@Test(groups = "websocket-test")
public class ClientCloseTest extends WebSocketTestCommons {

    private WebSocketTestClient client;
    private static final String URL = "ws://localhost:9085";
    private LogLeecher logLeecher;

    @Test(description = "Test client closing the connection without a close frame")
    public void testCloseWithoutCloseFrame() throws InterruptedException, BallerinaTestException, URISyntaxException {
        String expectingErrorLog = "Status code: 1006";
        logLeecher = new LogLeecher(expectingErrorLog);
        serverInstance.addLogLeecher(logLeecher);
        client = new WebSocketTestClient(URL);
        client.handshake();
        client.shutDownWithoutCloseFrame();
        logLeecher.waitForText(TIMEOUT_IN_SECS * 1000);
    }

    @Test(description = "Test client sending a close frame without a close code")
    public void testCloseWithoutCloseCode() throws InterruptedException, BallerinaTestException, URISyntaxException {
        String expectingErrorLog = "Status code: 1005";
        logLeecher = new LogLeecher(expectingErrorLog);
        serverInstance.addLogLeecher(logLeecher);
        client = new WebSocketTestClient(URL);
        client.handshake();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        client.setCountDownLatch(countDownLatch);
        client.sendCloseFrameWithoutCloseCode();
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        logLeecher.waitForText(TIMEOUT_IN_SECS * 1000);
        CloseWebSocketFrame closeWebSocketFrame = client.getReceivedCloseFrame();

        Assert.assertNotNull(closeWebSocketFrame);
        Assert.assertEquals(closeWebSocketFrame.statusCode(), -1);

        closeWebSocketFrame.release();
        client.shutDown();
    }
}
