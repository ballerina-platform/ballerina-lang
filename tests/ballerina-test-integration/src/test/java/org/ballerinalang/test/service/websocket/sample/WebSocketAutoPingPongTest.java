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

package org.ballerinalang.test.service.websocket.sample;

import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.ServerInstance;
import org.ballerinalang.test.util.websocket.client.WebSocketTestClient;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import javax.net.ssl.SSLException;

import static org.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;

public class WebSocketAutoPingPongTest extends WebSocketIntegrationTest {

    private final int awaitTimeInSecs = 10;
    private WebSocketTestClient wsClient;
    private ServerInstance ballerinaServer;

    @BeforeClass
    public void setup() throws InterruptedException, BallerinaTestException, SSLException, URISyntaxException {
        // Initializing ballerina server instance.
        ballerinaServer = ServerInstance.initBallerinaServer();
        String balFilePath =
                new File("src/test/resources/websocket/SimpleServerWithoutPingResource.bal").getAbsolutePath();
        ballerinaServer.startBallerinaServer(balFilePath);

        // Initializing and handshaking WebSocket client.
        wsClient = new WebSocketTestClient("ws://localhost:9090/test/without/ping/resource");
        wsClient.handshake();
    }

    @Test(priority = 1)
    public void testPingPongSupport() throws IOException, InterruptedException {
        // Test ping and receive pong from server
        await().atMost(awaitTimeInSecs, SECONDS).until(() -> {
            wsClient.sendPing(ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5}));
            return wsClient.isPong();
        });
    }

    @AfterClass
    private void cleanup() throws Exception {
        wsClient.shutDown();
        ballerinaServer.stopServer();
    }
}
