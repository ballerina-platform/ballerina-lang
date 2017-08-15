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

package org.wso2.carbon.transport.http.netty.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.messaging.exceptions.ServerConnectorException;
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.contract.ServerConnector;
import org.wso2.carbon.transport.http.netty.contractimpl.HTTPConnectorFactoryImpl;
import org.wso2.carbon.transport.http.netty.listener.ServerBootstrapConfiguration;
import org.wso2.carbon.transport.http.netty.util.client.websocket.WebSocketClient;
import org.wso2.carbon.transport.http.netty.util.client.websocket.WebSocketTestConstants;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import javax.net.ssl.SSLException;

import static org.testng.AssertJUnit.assertTrue;

/**
 * Test class for WebSocket protocol.
 */
public class WebSocketServerTestCase extends WebSocketTestCase {

    private static final Logger log = LoggerFactory.getLogger(WebSocketServerTestCase.class);

    private HTTPConnectorFactoryImpl httpConnectorFactory = new HTTPConnectorFactoryImpl();
    private final int threadSleepTime = 100;
    private final int messageDeliveryCountdown = 40;
    private WebSocketClient primaryClient = new WebSocketClient();
    private WebSocketClient secondaryClient = new WebSocketClient();
    private ServerConnector serverConnector;
    private WebSocketTestServerConnectorListener connectorListener;

    @BeforeClass
    public void setup() {
        log.info(System.lineSeparator() +
                         "--------------------- WebSocket Server Test Cases ---------------------");
        ListenerConfiguration listenerConfiguration = new ListenerConfiguration();
        listenerConfiguration.setHost("localhost");
        listenerConfiguration.setPort(9009);
        serverConnector = httpConnectorFactory.getServerConnector(ServerBootstrapConfiguration.getInstance(),
                listenerConfiguration);
        connectorListener = new WebSocketTestServerConnectorListener();
        serverConnector.start().setWSConnectorListener(connectorListener);
    }

    @Test
    public void handshakeTest() throws URISyntaxException, SSLException, InterruptedException {
        assertTrue(primaryClient.handhshake());
    }

    @Test
    public void testText() throws URISyntaxException, InterruptedException, SSLException {
        primaryClient.handhshake();
        String textSent = "test";
        primaryClient.sendText(textSent);
        assertWebSocketClientTextMessage(primaryClient, textSent);
        primaryClient.shutDown();
    }

    @Test
    public void testBinary() throws InterruptedException, URISyntaxException, IOException {
        primaryClient.handhshake();
        byte[] bytes = {1, 2, 3, 4, 5};
        ByteBuffer bufferSent = ByteBuffer.wrap(bytes);
        primaryClient.sendBinary(bufferSent);
        assertWebSocketClientBinaryMessage(primaryClient, bufferSent);
        primaryClient.shutDown();
    }

    /**
     * Primary client is the one who is checking the connections of the Server.
     * When secondary server is connecting to the endpoint, message will be sent to the primary
     * client indicating the state of the secondary client.
     */
    @Test
    public void testClientConnected() throws InterruptedException, SSLException, URISyntaxException {
        primaryClient.handhshake();
        secondaryClient.handhshake();
        assertWebSocketClientTextMessage(primaryClient, WebSocketTestConstants.PAYLOAD_NEW_CLIENT_CONNECTED);
        secondaryClient.shutDown();
        primaryClient.shutDown();
    }

    /**
     * Primary client is the one who is checking the connections of the Server.
     * When secondary server is closing the connection, message will be sent to the primary
     * client indicating the state of the secondary client.
     */
    @Test
    public void testClientCloseConnection() throws InterruptedException, URISyntaxException, SSLException {
        primaryClient.handhshake();
        secondaryClient.handhshake();
        secondaryClient.shutDown();
        assertWebSocketClientTextMessage(primaryClient, WebSocketTestConstants.PAYLOAD_CLIENT_LEFT);
        primaryClient.shutDown();
        secondaryClient.shutDown();
    }

    @Test
    public void testPingPongMessage() throws InterruptedException, IOException, URISyntaxException {
        primaryClient.handhshake();
        byte[] bytes = {6, 7, 8, 9, 10, 11};
        ByteBuffer bufferSent = ByteBuffer.wrap(bytes);
        primaryClient.sendPing(bufferSent);
        assertWebSocketClientBinaryMessage(primaryClient, bufferSent);
    }

    @AfterClass
    public void cleaUp() throws ServerConnectorException, InterruptedException {
        serverConnector.stop();
    }
}
