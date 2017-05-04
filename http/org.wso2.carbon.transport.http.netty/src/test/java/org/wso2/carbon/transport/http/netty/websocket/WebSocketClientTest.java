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
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.messaging.BinaryCarbonMessage;
import org.wso2.carbon.messaging.ClientConnector;
import org.wso2.carbon.messaging.ControlCarbonMessage;
import org.wso2.carbon.messaging.TextCarbonMessage;
import org.wso2.carbon.messaging.exceptions.ClientConnectorException;
import org.wso2.carbon.messaging.exceptions.ServerConnectorException;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.config.ConfigurationBuilder;
import org.wso2.carbon.transport.http.netty.config.TransportsConfiguration;
import org.wso2.carbon.transport.http.netty.listener.HTTPServerConnector;
import org.wso2.carbon.transport.http.netty.sender.websocket.WebSocketClientConnector;
import org.wso2.carbon.transport.http.netty.util.TestUtil;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Test cases for the WebSocket Client implementation.
 */
public class WebSocketClientTest {

    private static final Logger log = LoggerFactory.getLogger(WebSocketClientTest.class);

    WebSocketMessageProcessor messageProcessor = new WebSocketMessageProcessor();
    ClientConnector clientConnector = new WebSocketClientConnector();
    private List<HTTPServerConnector> serverConnectors;
    private final String url = "ws://localhost:8490/websocket";
    private final String clientId1 = "clientId1";
    private final String clientId2 = "clientId2";
    private final int sleepTime = 100;

    @BeforeClass
    public void setup() {
        log.info(System.lineSeparator() +
                         "---------------------WebSocket Client Connector Test Cases---------------------");
        TransportsConfiguration configuration = ConfigurationBuilder.getInstance().getConfiguration(
                "src/test/resources/simple-test-config/netty-transports.yml");
        serverConnectors = TestUtil.startConnectors(configuration, messageProcessor);
        clientConnector.setMessageProcessor(messageProcessor);
    }

    @Test(description = "Test the WebSocket handshake and sending and receiving text messages.")
    public void testTextReceived() throws ClientConnectorException, InterruptedException {
        handshake(clientId1);
        String text = "testText";
        TextCarbonMessage textCarbonMessage = new TextCarbonMessage(text);
        textCarbonMessage.setProperty(Constants.WEBSOCKET_CLIENT_ID, clientId1);
        clientConnector.send(textCarbonMessage, null);
        Thread.sleep(sleepTime);
        Assert.assertEquals(messageProcessor.getReceivedTextToClient(), text);
        shutDownClient(clientId1);
    }

    @Test(description = "Test binary message sending and receiving.")
    public void testBinaryReceived() throws ClientConnectorException, InterruptedException {
        handshake(clientId1);
        byte[] bytes = {1, 2, 3, 4, 5};
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        BinaryCarbonMessage binaryCarbonMessage = new BinaryCarbonMessage(buffer, true);
        binaryCarbonMessage.setProperty(Constants.WEBSOCKET_CLIENT_ID, clientId1);
        clientConnector.send(binaryCarbonMessage, null);
        Thread.sleep(sleepTime);
        Assert.assertEquals(messageProcessor.getReceivedByteBufferToClient(), buffer);
        shutDownClient(clientId1);
    }

    @Test(description = "Test ping pong messaging. ")
    public void testPingPong() throws ClientConnectorException, InterruptedException {
        handshake(clientId1);
        byte[] bytes = {1, 2, 3, 4, 5};
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        ControlCarbonMessage controlCarbonMessage = new ControlCarbonMessage(
                org.wso2.carbon.messaging.Constants.CONTROL_SIGNAL_HEARTBEAT, buffer, true);
        controlCarbonMessage.setProperty(Constants.WEBSOCKET_CLIENT_ID, clientId1);
        clientConnector.send(controlCarbonMessage, null);
        Thread.sleep(sleepTime);
        Assert.assertTrue(messageProcessor.isPongReceivedToClient());
        shutDownClient(clientId1);
    }

    @Test(description = "Test removal of a client",
          expectedExceptions = ClientConnectorException.class)
    public void testRemovedClient() throws ClientConnectorException {
        handshake(clientId1);
        shutDownClient(clientId1);
        String text = "testText";
        TextCarbonMessage textCarbonMessage = new TextCarbonMessage(text);
        textCarbonMessage.setProperty(Constants.WEBSOCKET_CLIENT_ID, clientId1);
        clientConnector.send(textCarbonMessage, null);
    }

    @Test(description = "Test multiple clients handling, sending and receiving text messages for them.")
    public void testMultipleClients() throws ClientConnectorException, InterruptedException {
        handshake(clientId1);
        handshake(clientId2);
        String text1 = "testText1";
        String text2 = "testText2";

        TextCarbonMessage textCarbonMessage = new TextCarbonMessage(text1);
        textCarbonMessage.setProperty(Constants.WEBSOCKET_CLIENT_ID, clientId1);
        clientConnector.send(textCarbonMessage, null);
        Thread.sleep(sleepTime);
        Assert.assertEquals(messageProcessor.getReceivedTextToClient(), text1);

        textCarbonMessage = new TextCarbonMessage(text2);
        textCarbonMessage.setProperty(Constants.WEBSOCKET_CLIENT_ID, clientId2);
        clientConnector.send(textCarbonMessage, null);
        Thread.sleep(sleepTime);
        Assert.assertEquals(messageProcessor.getReceivedTextToClient(), text2);

        textCarbonMessage = new TextCarbonMessage(text2);
        textCarbonMessage.setProperty(Constants.WEBSOCKET_CLIENT_ID, clientId1);
        clientConnector.send(textCarbonMessage, null);
        Thread.sleep(sleepTime);
        Assert.assertEquals(messageProcessor.getReceivedTextToClient(), text2);

        shutDownClient(clientId1);
        shutDownClient(clientId2);
    }

    @AfterClass
    public void cleanUp() throws ServerConnectorException, InterruptedException {
        serverConnectors.forEach(
                serverConnector -> {
                    serverConnector.stop();
                }
        );
    }

    private void handshake(String clientId) throws ClientConnectorException {
        ControlCarbonMessage controlCarbonMessage = new ControlCarbonMessage(
                org.wso2.carbon.messaging.Constants.CONTROL_SIGNAL_OPEN);
        controlCarbonMessage.setProperty(Constants.TO, url);
        controlCarbonMessage.setProperty(Constants.WEBSOCKET_CLIENT_ID, clientId);
        clientConnector.send(controlCarbonMessage, null);
    }

    private void shutDownClient(String clientId) throws ClientConnectorException {
        ControlCarbonMessage controlCarbonMessage = new ControlCarbonMessage(
                org.wso2.carbon.messaging.Constants.CONTROL_SIGNAL_CLOSE);
        controlCarbonMessage.setProperty(Constants.WEBSOCKET_CLOSE_CODE, 1000);
        controlCarbonMessage.setProperty(Constants.WEBSOCKET_CLOSE_REASON, "Normal Closure");
        controlCarbonMessage.setProperty(Constants.TO, url);
        controlCarbonMessage.setProperty(Constants.WEBSOCKET_CLIENT_ID, clientId);
        clientConnector.send(controlCarbonMessage, null);
    }
}
