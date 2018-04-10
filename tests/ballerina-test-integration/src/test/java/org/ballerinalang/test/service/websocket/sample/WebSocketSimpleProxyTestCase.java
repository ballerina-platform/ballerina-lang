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
import org.ballerinalang.test.util.websocket.server.WebSocketRemoteServer;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLException;

/**
 * Test case to test simple WebSocket pass through scenarios.
 */
public class WebSocketSimpleProxyTestCase {

    private WebSocketRemoteServer remoteServer;
    private ServerInstance ballerinaServerInstance;
    private static final int REMOTE_SERVER_PORT = 15500;
    private static final String URL = "ws://localhost:9090/proxy/ws";

    @BeforeClass
    public void setup() throws InterruptedException, BallerinaTestException {
        remoteServer = new WebSocketRemoteServer(REMOTE_SERVER_PORT);
        remoteServer.run();

        String balPath = new File("src/test/resources/websocket/SimpleProxyServer.bal").getAbsolutePath();
        ballerinaServerInstance = ServerInstance.initBallerinaServer();
        ballerinaServerInstance.startBallerinaServer(balPath);
    }

    @Test(priority = 1)
    public void testSendText() throws URISyntaxException, InterruptedException, SSLException {
        WebSocketTestClient client = new WebSocketTestClient(URL);
        handshakeAndAck(client);
        String textSent = "hi all";
        CountDownLatch countDownLatch = new CountDownLatch(1);
        client.setCountDownLatch(countDownLatch);
        client.sendText(textSent);
        countDownLatch.await(10, TimeUnit.SECONDS);
        Assert.assertEquals(client.getTextReceived(), textSent);
        client.shutDown();
    }

    @Test(priority = 2)
    public void testSendBinary() throws URISyntaxException, InterruptedException, IOException {
        WebSocketTestClient client = new WebSocketTestClient(URL);
        handshakeAndAck(client);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        client.setCountDownLatch(countDownLatch);
        ByteBuffer bufferSent = ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5});
        client.sendBinary(bufferSent);
        countDownLatch.await(10, TimeUnit.SECONDS);
        Assert.assertEquals(client.getBufferReceived(), bufferSent);
        client.shutDown();
    }

    @AfterClass
    public void cleanup() throws BallerinaTestException {
        ballerinaServerInstance.stopServer();
        remoteServer.stop();
    }

    private void handshakeAndAck(WebSocketTestClient client)
            throws InterruptedException, URISyntaxException, SSLException {
        CountDownLatch ackCountDownLatch = new CountDownLatch(1);
        client.setCountDownLatch(ackCountDownLatch);
        client.handshake();
        ackCountDownLatch.await(10, TimeUnit.SECONDS);
        if (!"send".equals(client.getTextReceived())) {
            throw new IllegalArgumentException("Could not receive acknowledgment");
        }
    }
}
