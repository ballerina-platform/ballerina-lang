/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.URISyntaxException;

/**
 * Test some of the errors using a Websocket server to test the error design in WebSocket.
 */
@Test(groups = {"websocket-test"})
public class ServerErrorsTest extends WebSocketTestCommons {

    private WebSocketTestClient client;
    private static final String URL = "ws://localhost:21028/server/errors";

    @BeforeMethod(description = "Related file 28_server_exceptions.bal")
    public void setup() throws InterruptedException, URISyntaxException, BallerinaTestException {
        client = new WebSocketTestClient(URL);
        client.handshake();
    }

    @Test(description = "Corrupted frame error")
    public void testCorruptedFrameError() throws InterruptedException, BallerinaTestException {
        String expectingErrorLog =
                "error {ballerina/http}WsProtocolError message=received continuation data frame outside fragmented " +
                        "message";
        LogLeecher logLeecher = new LogLeecher(expectingErrorLog);
        serverInstance.addLogLeecher(logLeecher);
        client.sendCorruptedFrame();
        logLeecher.waitForText(TIMEOUT_IN_SECS * 1000);
    }

    @Test(description = "Frame continuation error")
    public void testContinuationFrameError() throws BallerinaTestException, InterruptedException {
        String expectingErrorLog =
                "error {ballerina/http}WsInvalidContinuationFrameError message=Cannot interrupt WebSocket text frame " +
                        "continuation";
        LogLeecher logLeecher = new LogLeecher(expectingErrorLog);
        serverInstance.addLogLeecher(logLeecher);
        client.sendText("continuation");
        logLeecher.waitForText(TIMEOUT_IN_SECS * 1000);
    }
}
