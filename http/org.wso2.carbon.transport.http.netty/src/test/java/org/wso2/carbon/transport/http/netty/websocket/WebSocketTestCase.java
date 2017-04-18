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
import org.wso2.carbon.transport.http.netty.config.TransportsConfiguration;
import org.wso2.carbon.transport.http.netty.config.YAMLTransportConfigurationBuilder;
import org.wso2.carbon.transport.http.netty.listener.HTTPServerConnector;
import org.wso2.carbon.transport.http.netty.util.TestUtil;
import org.wso2.carbon.transport.http.netty.util.client.websocket.WebSocketClient;
import org.wso2.carbon.transport.http.netty.util.client.websocket.WebSocketTestConstants;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.List;

import javax.net.ssl.SSLException;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

/**
 * Test class for WebSocket protocol.
 */
public class WebSocketTestCase {

    Logger logger = LoggerFactory.getLogger(WebSocketTestCase.class);
    private List<HTTPServerConnector> serverConnectors;
    private final int threadSleepTime = 100;
    private WebSocketClient primaryClient = new WebSocketClient();
    private WebSocketClient secondaryClient = new WebSocketClient();

    @BeforeClass
    public void setup() {
        logger.info(System.lineSeparator() + "-------WebSocket Test Cases-------");
        TransportsConfiguration configuration = YAMLTransportConfigurationBuilder
                .build("src/test/resources/simple-test-config/netty-transports.yml");
        serverConnectors = TestUtil.startConnectors(configuration, new WebSocketMessageProcessor());
    }

    @Test
    public void handshakeTest() throws URISyntaxException, SSLException, InterruptedException {
        assertTrue(primaryClient.handhshake());
        logger.info("Handshake test completed.");
    }

    @Test
    public void testText() throws URISyntaxException, InterruptedException, SSLException {
        primaryClient.handhshake();
        String textSent = "test";
        primaryClient.sendText(textSent);
        Thread.sleep(threadSleepTime);
        String textReceived = primaryClient.getTextReceived();
        assertEquals("Not received the same text.", textReceived, textSent);
        logger.info("pushing and receiving text data from server completed.");
        primaryClient.shutDown();
    }

    @Test
    public void testBinary() throws InterruptedException, URISyntaxException, IOException {
        primaryClient.handhshake();
        byte[] bytes = {1, 2, 3, 4, 5};
        ByteBuffer bufferSent = ByteBuffer.wrap(bytes);
        primaryClient.sendBinary(bufferSent);
        Thread.sleep(threadSleepTime);
        ByteBuffer bufferReceived = primaryClient.getBufferReceived();
        assertTrue("Buffer capacity is not the same.",
                   bufferSent.capacity() == bufferReceived.capacity());
        assertEquals("Buffers data are not equal.", bufferReceived, bufferSent);
        logger.info("pushing and receiving binary data from server completed.");
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
        Thread.sleep(threadSleepTime);
        String textReceived = primaryClient.getTextReceived();
        logger.info("Received text : " + textReceived);
        assertEquals("New Client was not connected.",
                     textReceived, WebSocketTestConstants.NEW_CLIENT_CONNECTED);
        logger.info("New client successfully connected to the server.");
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
        Thread.sleep(threadSleepTime);
        secondaryClient.shutDown();
        Thread.sleep(threadSleepTime);
        String textReceived = primaryClient.getTextReceived();
        logger.info("Received Text : " + textReceived);
        assertEquals("Connection close is unsuccessful.", textReceived, WebSocketTestConstants.CLIENT_LEFT);
        logger.info("Client left the server successfully.");
        primaryClient.shutDown();
        secondaryClient.shutDown();
    }

    @Test
    public void testPingPongMessage() throws InterruptedException, IOException, URISyntaxException {
        primaryClient.handhshake();
        byte[] bytes = {6, 7, 8, 9, 10, 11};
        ByteBuffer bufferSent = ByteBuffer.wrap(bytes);
        primaryClient.sendPing(bufferSent);
        Thread.sleep(threadSleepTime);
        ByteBuffer bufferReceived = primaryClient.getBufferReceived();
        assertEquals("Didn't receive the correct pong.", bufferReceived, bufferSent);
        logger.info("Receiving a pong message is completed.");
    }

    @AfterClass
    public void cleaUp() throws ServerConnectorException, InterruptedException {
        serverConnectors.forEach(
                serverConnector -> {
                    serverConnector.stop();
                }
        );
    }
}
