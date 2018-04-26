/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

package org.wso2.transport.http.netty.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.config.ListenerConfiguration;
import org.wso2.transport.http.netty.contract.ServerConnector;
import org.wso2.transport.http.netty.contract.ServerConnectorException;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.wso2.transport.http.netty.util.TestUtil;
import org.wso2.transport.http.netty.util.client.websocket.WebSocketTestClient;
import org.wso2.transport.http.netty.util.client.websocket.WebSocketTestConstants;

import java.io.IOException;
import java.net.ProtocolException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLException;

import static org.testng.AssertJUnit.assertTrue;

/**
 * Test class for WebSocket protocol.
 */
public class WebSocketServerTestCase {

    private static final Logger log = LoggerFactory.getLogger(WebSocketServerTestCase.class);

    private final int latchCountDownInSecs = 10;
    private DefaultHttpWsConnectorFactory httpConnectorFactory = new DefaultHttpWsConnectorFactory();
    private ServerConnector serverConnector;

    @BeforeClass
    public void setup() throws InterruptedException {
        ListenerConfiguration listenerConfiguration = new ListenerConfiguration();
        listenerConfiguration.setHost("localhost");
        listenerConfiguration.setPort(TestUtil.SERVER_CONNECTOR_PORT);
        serverConnector = httpConnectorFactory.createServerConnector(TestUtil.getDefaultServerBootstrapConfig(),
                listenerConfiguration);
        ServerConnectorFuture connectorFuture = serverConnector.start();
        connectorFuture.setWSConnectorListener(new WebSocketTestServerConnectorListener());
    }

    @Test
    public void handshakeTest() throws URISyntaxException, SSLException, InterruptedException, ProtocolException {
        WebSocketTestClient primaryClient = new WebSocketTestClient();
        assertTrue(primaryClient.handhshake());
        primaryClient.shutDown();
    }

    @Test
    public void testText() throws URISyntaxException, InterruptedException, SSLException, ProtocolException {
        CountDownLatch latch = new CountDownLatch(1);
        WebSocketTestClient primaryClient = new WebSocketTestClient(latch);
        primaryClient.handhshake();
        String textSent = "test";
        primaryClient.sendText(textSent);
        latch.await(latchCountDownInSecs, TimeUnit.SECONDS);
        Assert.assertEquals(primaryClient.getTextReceived(), textSent);
        primaryClient.shutDown();
    }

    @Test
    public void testBinary() throws InterruptedException, URISyntaxException, IOException {
        CountDownLatch latch = new CountDownLatch(1);
        WebSocketTestClient primaryClient = new WebSocketTestClient(latch);
        primaryClient.handhshake();
        byte[] bytes = {1, 2, 3, 4, 5};
        ByteBuffer bufferSent = ByteBuffer.wrap(bytes);
        primaryClient.sendBinary(bufferSent);
        latch.await(latchCountDownInSecs, TimeUnit.SECONDS);
        Assert.assertEquals(primaryClient.getBufferReceived(), bufferSent);
        primaryClient.shutDown();
    }

    /**
     * Primary client is the one who is checking the connections of the Server.
     * When secondary server is connecting to the endpoint, message will be sent to the primary
     * client indicating the state of the secondary client.
     */
    @Test
    public void testClientConnected() throws InterruptedException, SSLException, URISyntaxException, ProtocolException {
        CountDownLatch latch = new CountDownLatch(1);
        WebSocketTestClient primaryClient = new WebSocketTestClient(latch);
        WebSocketTestClient secondaryClient = new WebSocketTestClient();
        primaryClient.handhshake();
        secondaryClient.handhshake();
        latch.await(latchCountDownInSecs, TimeUnit.SECONDS);
        Assert.assertEquals(primaryClient.getTextReceived(), WebSocketTestConstants.PAYLOAD_NEW_CLIENT_CONNECTED);
        secondaryClient.shutDown();
        primaryClient.shutDown();
    }

    /**
     * Primary client is the one who is checking the connections of the Server.
     * When secondary server is closing the connection, message will be sent to the primary
     * client indicating the state of the secondary client.
     */
    @Test
    public void testClientCloseConnection()
            throws InterruptedException, URISyntaxException, SSLException, ProtocolException {
        CountDownLatch latch = new CountDownLatch(2);
        WebSocketTestClient primaryClient = new WebSocketTestClient(latch);
        WebSocketTestClient secondaryClient = new WebSocketTestClient();
        primaryClient.handhshake();
        secondaryClient.handhshake();
        secondaryClient.shutDown();
        latch.await(latchCountDownInSecs, TimeUnit.SECONDS);
        Assert.assertEquals(primaryClient.getTextReceived(), WebSocketTestConstants.PAYLOAD_CLIENT_LEFT);
        primaryClient.shutDown();
    }

    @Test
    public void testPingPongMessage() throws InterruptedException, IOException, URISyntaxException {
        // Check the ping receive.
        final String ping = "ping";
        CountDownLatch pingLatch = new CountDownLatch(1);
        WebSocketTestClient pingCheckClient = new WebSocketTestClient(pingLatch);
        pingCheckClient.handhshake();
        pingCheckClient.sendText(ping);
        pingLatch.await(latchCountDownInSecs, TimeUnit.SECONDS);
        ByteBuffer expectedBuffer = ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5});
        Assert.assertTrue(pingCheckClient.isPingReceived(), "Should receive a ping from the server");
        Assert.assertEquals(pingCheckClient.getBufferReceived(), expectedBuffer);
        pingCheckClient.shutDown();

        // Check the pong receive.
        CountDownLatch pongLatch = new CountDownLatch(1);
        WebSocketTestClient pongCheckClient = new WebSocketTestClient(pongLatch);
        pongCheckClient.handhshake();
        byte[] bytes = {6, 7, 8, 9, 10, 11};
        ByteBuffer bufferSent = ByteBuffer.wrap(bytes);
        pongCheckClient.sendPing(bufferSent);
        pongLatch.await(latchCountDownInSecs, TimeUnit.SECONDS);
        Assert.assertTrue(pongCheckClient.isPongReceived(), "Should receive a pong from the server");
        Assert.assertEquals(pongCheckClient.getBufferReceived(), bufferSent);
        pongCheckClient.shutDown();
    }

    @Test(priority = 1)
    public void testIdleTimeout() throws InterruptedException, ProtocolException, SSLException, URISyntaxException {
        // TODO: Fix this. This fails intermittently. Issue #38
        ListenerConfiguration listenerConfiguration = new ListenerConfiguration();
        listenerConfiguration.setHost("localhost");
        listenerConfiguration.setPort(TestUtil.ALTER_INTERFACE_PORT);
        ServerConnector alterServerConnector = httpConnectorFactory.createServerConnector(
                TestUtil.getDefaultServerBootstrapConfig(),
                listenerConfiguration);
        ServerConnectorFuture connectorFuture = alterServerConnector.start();
        WebSocketTestServerConnectorListener listener = new WebSocketTestServerConnectorListener();
        connectorFuture.setWSConnectorListener(listener);
        String url = System.getProperty("url", String.format("ws://%s:%d/%s",
                                                             TestUtil.TEST_HOST, TestUtil.ALTER_INTERFACE_PORT,
                                                             "test"));
        CountDownLatch latch = new CountDownLatch(1);
        WebSocketTestClient primaryClient = new WebSocketTestClient(url, latch);
        primaryClient.handhshake();
        latch.await(latchCountDownInSecs, TimeUnit.SECONDS);
        Assert.assertFalse(primaryClient.isOpen());
        Assert.assertTrue(listener.isIdleTimeout());
        alterServerConnector.stop();
    }

    @AfterClass
    public void cleaUp() throws ServerConnectorException, InterruptedException {
        serverConnector.stop();
    }
}
