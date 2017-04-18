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
import org.wso2.carbon.messaging.exceptions.ClientConnectorException;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.util.TestUtil;
import org.wso2.carbon.transport.http.netty.util.client.websocket.WebSocketTestConstants;

import java.io.IOException;
import java.net.ProtocolException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.websocket.Session;

/**
 * A Message Processor class to be used for test pass through scenarios
 */
public class WebSocketMessageProcessor implements CarbonMessageProcessor {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketMessageProcessor.class);
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private ClientConnector clientConnector;
    private List<Session> sessionList = new LinkedList<>();

    @Override
    public boolean receive(CarbonMessage carbonMessage, CarbonCallback carbonCallback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String protocol = (String) carbonMessage.getProperty(Constants.PROTOCOL);

                    if (!Constants.WEBSOCKET_PROTOCOL.equals(protocol)) {
                        throw new ProtocolException("Protocol is not valid :" + protocol);
                    }
                    if (carbonMessage instanceof TextCarbonMessage) {
                        handleTextMessage(carbonMessage);
                    } else if (carbonMessage instanceof BinaryCarbonMessage) {
                        handleBinaryMessage(carbonMessage);
                    } else if (carbonMessage instanceof StatusCarbonMessage) {
                        handleStatusMessage(carbonMessage);
                    } else if (carbonMessage instanceof ControlCarbonMessage) {
                        handleControlMessage(carbonMessage);
                    } else {
                        carbonMessage.setProperty(Constants.HOST, TestUtil.TEST_HOST);
                        carbonMessage.setProperty(Constants.PORT, TestUtil.TEST_SERVER_PORT);
                        clientConnector.send(carbonMessage, carbonCallback);
                    }
                } catch (ClientConnectorException e) {
                    logger.error("MessageProcessor is not supported ", e);
                } catch (IOException e) {
                    logger.error("IO exception occurred : " + e.getMessage(), e);
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
        logger.info("Text Frame received for URI : " + carbonMessage.getProperty(Constants.TO));
        TextCarbonMessage textCarbonMessage = (TextCarbonMessage) carbonMessage;
        Session session = (Session) textCarbonMessage.getProperty(Constants.WEBSOCKET_SESSION);
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
        Session session = (Session) binaryCarbonMessage.getProperty(Constants.WEBSOCKET_SESSION);
        session.getBasicRemote().sendBinary(binaryCarbonMessage.readBytes());
    }

    /**
     * Handle incoming status messages when opening a connection and closing a connection.
     * @param carbonMessage {@link CarbonMessage} to process.
     */
    private void handleStatusMessage(CarbonMessage carbonMessage) {
        StatusCarbonMessage statusCarbonMessage = (StatusCarbonMessage) carbonMessage;
        if (org.wso2.carbon.messaging.Constants.STATUS_OPEN.equals(statusCarbonMessage.getStatus())) {
            logger.info("Status open carbon message received.");
            Session session = (Session) statusCarbonMessage.
                    getProperty(Constants.WEBSOCKET_SESSION);
            sessionList.forEach(
                    currentSession -> {
                        try {
                            currentSession.getBasicRemote().
                                    sendText(WebSocketTestConstants.NEW_CLIENT_CONNECTED);
                        } catch (IOException e) {
                            logger.error("IO exception when sending data : " + e.getMessage(), e);
                        }
                    }
            );
            sessionList.add(session);
        } else if (org.wso2.carbon.messaging.Constants.STATUS_CLOSE.
                equals(statusCarbonMessage.getStatus())) {
            logger.info("Status closed carbon message received.");
            Session session = (Session) statusCarbonMessage.
                    getProperty(Constants.WEBSOCKET_SESSION);
            sessionList.forEach(
                    currentSession -> {
                        try {
                            currentSession.getBasicRemote().
                                    sendText(WebSocketTestConstants.CLIENT_LEFT);
                        } catch (IOException e) {
                            logger.error("IO exception when sending data : " + e.getMessage(), e);
                        }
                    }
            );
        }
    }

    /**
     * Handle pong messages.
     * Extract the content of the pong message (byte buffer) and create new one and send it back.
     * @param carbonMessage {@link CarbonMessage} to process.
     * @throws IOException if an error occurred in sending the message back.
     */
    private void handleControlMessage(CarbonMessage carbonMessage) throws IOException {
        ControlCarbonMessage controlCarbonMessage = (ControlCarbonMessage) carbonMessage;
        Session session = (Session) controlCarbonMessage.
                getProperty(Constants.WEBSOCKET_SESSION);
        session.getBasicRemote().sendPong(controlCarbonMessage.readBytes());
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
        return "passthrough";
    }
}
