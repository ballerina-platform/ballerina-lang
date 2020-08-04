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
import org.testng.Assert;
import org.testng.annotations.Test;

import java.net.URISyntaxException;

/**
 * Tests the authentication support of the WebSocket client.
 */
@Test(groups = {"websocket-test"})
public class AuthenticationTest {

    private WebSocketTestClient client;
    private LogLeecher logLeecher;
    String expectingErrorLog;

    @Test(description = "Tests with correct credential")
    public void testBasicAuthenticationSuccess() throws URISyntaxException, InterruptedException,
            BallerinaTestException {
        expectingErrorLog = "Hello World!";
        logLeecher = new LogLeecher(expectingErrorLog);
        WebSocketTestCommons.serverInstance.addLogLeecher(logLeecher);
        client = new WebSocketTestClient("ws://localhost:21039");
        client.handshake();
        logLeecher.waitForText(WebSocketTestCommons.TIMEOUT_IN_SECS * 1000);
        client.shutDown();
    }

    @Test(description = "Tests with wrong credential")
    public void testBasicAuthenticationFailure() throws URISyntaxException, InterruptedException,
            BallerinaTestException {
        expectingErrorLog = "error InvalidHandshakeError: Invalid handshake response getStatus: 401 Unauthorized";
        logLeecher = new LogLeecher(expectingErrorLog);
        WebSocketTestCommons.serverInstance.addLogLeecher(logLeecher);
        client = new WebSocketTestClient("ws://localhost:21041");
        client.handshake();
        Assert.assertTrue(client.isOpen());
        logLeecher.waitForText(WebSocketTestCommons.TIMEOUT_IN_SECS * 1000);
        client.shutDown();
    }
}
