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
import org.wso2.carbon.messaging.BinaryCarbonMessage;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.messaging.ClientConnector;
import org.wso2.carbon.messaging.ControlCarbonMessage;
import org.wso2.carbon.messaging.StatusCarbonMessage;
import org.wso2.carbon.messaging.TextCarbonMessage;
import org.wso2.carbon.messaging.TransportSender;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.util.client.websocket.WebSocketTestConstants;

import java.io.IOException;
import java.net.ProtocolException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.websocket.Session;

/**
 * A Message Processor class to be used for test pass through scenarios
 */
public class WebSocketMessageProcessor implements CarbonMessageProcessor {
    private static final Logger log = LoggerFactory.getLogger(WebSocketMessageProcessor.class);
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private ClientConnector clientConnector;
    private List<Session> sessionList = new LinkedList<>();

    private String receivedTextToClient;
    private ByteBuffer receivedByteBufferToClient;
    private boolean isPongReceivedToClient;
    private StatusCarbonMessage lastOnOpenCarbonMessage;

    @Override
    public boolean receive(CarbonMessage carbonMessage, CarbonCallback carbonCallback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String protocol = (String) carbonMessage.getProperty(Constants.PROTOCOL);
                    boolean isServer = (boolean) carbonMessage.getProperty(Constants.IS_WEBSOCKET_SERVER);

                    if (!Constants.WEBSOCKET_PROTOCOL.equals(protocol)) {
                        throw new ProtocolException("Protocol is not valid :" + protocol);
                    }

                    if (isServer) {
                        // If the message coming from WebSocket server it is processed here.
                        log.info("WebSocket server message received.");
                        if (carbonMessage instanceof TextCarbonMessage) {
                            handleTextMessage(carbonMessage);
                        } else if (carbonMessage instanceof BinaryCarbonMessage) {
                            handleBinaryMessage(carbonMessage);
                        } else if (carbonMessage instanceof StatusCarbonMessage) {
                            handleStatusMessage(carbonMessage, carbonCallback);
                        }
                    } else {
                        // If the message coming from WebSocket client it is processed here.
                        log.info("WebSocket client message received.");
                        if (carbonMessage instanceof TextCarbonMessage) {
                            receivedTextToClient = ((TextCarbonMessage) carbonMessage).getText();
                        } else if (carbonMessage instanceof ControlCarbonMessage) {
                            isPongReceivedToClient = true;
                        } else if (carbonMessage instanceof BinaryCarbonMessage) {
                            receivedByteBufferToClient = ((BinaryCarbonMessage) carbonMessage).readBytes();
                        }
                    }
                } catch (IOException e) {
                    log.error("IO exception occurred : " + e.getMessage(), e);
                }
            }
        });

        return true;
    }

    /**
     * Handle incoming text messages.
     * Extract the text from carbon message and send it back.
     * @param carbonMessage {@link CarbonMessage} to process.
     * @throws IOException if an error occurred in sending the message back.
     */
    private void handleTextMessage(CarbonMessage carbonMessage) throws IOException {
        log.info("Text Frame received for URI : " + carbonMessage.getProperty(Constants.TO));
        TextCarbonMessage textCarbonMessage = (TextCarbonMessage) carbonMessage;
        Session session = (Session) textCarbonMessage.getProperty(Constants.WEBSOCKET_SERVER_SESSION);
        session.getBasicRemote().sendText(textCarbonMessage.getText());
    }

    /**
     * Handle incoming binary messages.
     * Extract the byte buffer from carbon message and send it back.
     * @param carbonMessage {@link CarbonMessage} to process.
     * @throws IOException if an error occurred in sending the message back.
     */
    private void handleBinaryMessage(CarbonMessage carbonMessage) throws IOException {
        BinaryCarbonMessage binaryCarbonMessage = (BinaryCarbonMessage) carbonMessage;
        Session session = (Session) binaryCarbonMessage.getProperty(Constants.WEBSOCKET_SERVER_SESSION);
        session.getBasicRemote().sendBinary(binaryCarbonMessage.readBytes());
    }

    /**
     * Handle incoming status messages when opening a connection and closing a connection.
     * @param carbonMessage {@link CarbonMessage} to process.
     */
    private void handleStatusMessage(CarbonMessage carbonMessage, CarbonCallback callback) {
        StatusCarbonMessage statusCarbonMessage = (StatusCarbonMessage) carbonMessage;
        if (org.wso2.carbon.messaging.Constants.STATUS_OPEN.equals(statusCarbonMessage.getStatus())) {
            callback.done(carbonMessage);
            lastOnOpenCarbonMessage = statusCarbonMessage;
            log.info("Status open carbon message received.");
            Session session = (Session) statusCarbonMessage.getProperty(Constants.WEBSOCKET_SERVER_SESSION);
            sessionList.forEach(
                    currentSession -> {
                        try {
                            currentSession.getBasicRemote().
                                    sendText(WebSocketTestConstants.PAYLOAD_NEW_CLIENT_CONNECTED);
                        } catch (IOException e) {
                            log.error("IO exception when sending data : " + e.getMessage(), e);
                        }
                    }
            );
            sessionList.add(session);
        } else if (org.wso2.carbon.messaging.Constants.STATUS_CLOSE.
                equals(statusCarbonMessage.getStatus())) {
            log.info("Status closed carbon message received.");
            sessionList.forEach(
                    currentSession -> {
                        try {
                            currentSession.getBasicRemote().
                                    sendText(WebSocketTestConstants.PAYLOAD_CLIENT_LEFT);
                        } catch (IOException e) {
                            log.error("IO exception when sending data : " + e.getMessage(), e);
                        }
                    }
            );
        }
    }

    /**
     * Retrieve the latest text received to client.
     * @return the latest text received to the client.
     */
    public String getReceivedTextToClient() {
        String tmp = receivedTextToClient;
        receivedTextToClient = null;
        return tmp;
    }

    /**
     * Retrieve the latest {@link ByteBuffer} received to client.
     * @return the latest {@link ByteBuffer} received to client.
     */
    public ByteBuffer getReceivedByteBufferToClient() {
        ByteBuffer tmp = receivedByteBufferToClient;
        receivedByteBufferToClient = null;
        return tmp;
    }

    /**
     * Retrieve whether a pong is received client.
     * @return true if a pong is received to client.
     */
    public boolean isPongReceivedToClient() {
        boolean tmp = isPongReceivedToClient;
        isPongReceivedToClient = false;
        return tmp;
    }

    /**
     * Get value of a header received by carbon message.
     * @param headerKey key of the header.
     * @return the value of the header.
     */
    public String getHeader(String headerKey) {
        return lastOnOpenCarbonMessage.getHeader(headerKey);
    }

    @Override
    public void setTransportSender(TransportSender transportSender) {
    }

    @Override
    public void setClientConnector(ClientConnector clientConnector) {
        this.clientConnector = clientConnector;
    }

    @Override
    public String getId() {
        return Constants.WEBSOCKET_PROTOCOL;
    }
}
