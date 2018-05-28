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

import io.netty.handler.codec.http.websocketx.WebSocketHandshakeException;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.util.websocket.client.WebSocketTestClient;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URISyntaxException;

/**
 * This Class tests the cancelWebSocketUpgrade method of the http connector.
 */
public class CancelWebSocketUpgradeTest extends WebSocketIntegrationTest {

    private WebSocketTestClient client;

    @BeforeClass(description = "Initializes Ballerina with the cancel_websocket_upgrade.bal file")
    public void setup() throws BallerinaTestException {
        initBallerinaServer("cancel_websocket_upgrade.bal");
    }

    @Test(description = "Tests the cancelWebSocketUpgrade method",
          expectedExceptions = WebSocketHandshakeException.class,
          expectedExceptionsMessageRegExp = "Invalid handshake response getStatus: 404 Not Found")
    public void testCancelUpgrade() throws InterruptedException, URISyntaxException {
        client = new WebSocketTestClient("ws://localhost:9090/simple/cancel");
        client.handshake();
        client.shutDown();
    }

    @Test(description = "Tests the cancelWebSocketUpgrade method with a success status code",
          expectedExceptions = WebSocketHandshakeException.class,
          expectedExceptionsMessageRegExp = "Invalid handshake response getStatus: 400 Bad Request")
    public void testCancelUpgradeSuccessStatusCode() throws InterruptedException, URISyntaxException {
        client = new WebSocketTestClient("ws://localhost:9090/cannotcancel/cannot/cancel");
        client.handshake();
        client.shutDown();
    }

    @AfterClass(description = "Stops Ballerina")
    public void cleanup() throws BallerinaTestException {
        stopBallerinaServerInstance();
    }
}

