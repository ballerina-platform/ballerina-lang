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
import org.wso2.transport.http.netty.contract.websocket.HandshakeFuture;
import org.wso2.transport.http.netty.contract.websocket.HandshakeListener;
import org.wso2.transport.http.netty.contract.websocket.WebSocketBinaryMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketCloseMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnectorListener;
import org.wso2.transport.http.netty.contract.websocket.WebSocketControlMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketControlSignal;
import org.wso2.transport.http.netty.contract.websocket.WebSocketInitMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketTextMessage;
import org.wso2.transport.http.netty.util.client.websocket.WebSocketTestConstants;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import javax.websocket.CloseReason;
import javax.websocket.Session;

/**
 * WebSocket test class for WebSocket Connector Listener.
 */
public class WebSocketTestServerConnectorListener implements WebSocketConnectorListener {

    private static final Logger log = LoggerFactory.getLogger(WebSocketTestServerConnectorListener.class);

    private static final String PING = "ping";
    private List<Session> sessionList = new LinkedList<>();
    private boolean isIdleTimeout = false;

    public WebSocketTestServerConnectorListener() {
    }

    @Override
    public void onMessage(WebSocketInitMessage initMessage) {
        HandshakeFuture future = initMessage.handshake(null, true, 3000);
        future.setHandshakeListener(new HandshakeListener() {
            @Override
            public void onSuccess(WebSocketConnection webSocketConnection) {
                sessionList.forEach(
                        currentSession -> {
                            currentSession.getAsyncRemote().
                                    sendText(WebSocketTestConstants.PAYLOAD_NEW_CLIENT_CONNECTED);
                        }
                );
                sessionList.add(webSocketConnection.getSession());
            }

            @Override
            public void onError(Throwable t) {
                log.error(t.getMessage());
                Assert.assertTrue(false, "Error: " + t.getMessage());
            }
        });
    }

    @Override
    public void onMessage(WebSocketTextMessage textMessage) {
        Session session = textMessage.getWebSocketConnection().getSession();
        String receivedTextToClient = textMessage.getText();
        log.debug("text: " + receivedTextToClient);
        try {
            if (PING.equals(receivedTextToClient)) {
                session.getAsyncRemote().sendPing(ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5}));
                return;
            }
            session.getAsyncRemote().sendText(receivedTextToClient);
        } catch (IOException e) {
            handleError(e);
        }
    }

    @Override
    public void onMessage(WebSocketBinaryMessage binaryMessage) {
        Session session = binaryMessage.getWebSocketConnection().getSession();
        ByteBuffer receivedByteBufferToClient = binaryMessage.getByteBuffer();
        log.debug("ByteBuffer: " + receivedByteBufferToClient);
        session.getAsyncRemote().sendBinary(receivedByteBufferToClient);
    }

    @Override
    public void onMessage(WebSocketControlMessage controlMessage) {
        if (controlMessage.getControlSignal() == WebSocketControlSignal.PONG) {
            boolean isPongReceived = true;
            return;
        }

        if (controlMessage.getControlSignal() == WebSocketControlSignal.PING) {
            Session session = controlMessage.getWebSocketConnection().getSession();
            try {
                session.getAsyncRemote().sendPong(controlMessage.getPayload());
            } catch (IOException e) {
                Assert.assertTrue(false, "Could not send the message.");
            }
        }
    }

    @Override
    public void onMessage(WebSocketCloseMessage closeMessage) {
        sessionList.forEach(
                currentSession -> {
                    currentSession.getAsyncRemote().
                            sendText(WebSocketTestConstants.PAYLOAD_CLIENT_LEFT);
                }
        );
    }

    @Override
    public void onError(Throwable throwable) {
        handleError(throwable);
    }

    @Override
    public void onIdleTimeout(WebSocketControlMessage controlMessage) {
        this.isIdleTimeout = true;
        try {
            Session session = controlMessage.getWebSocketConnection().getSession();
            session.close(new CloseReason(() -> 1001, "Connection timeout"));
        } catch (IOException e) {
            log.error("Error occurred while closing the connection: " + e.getMessage());
        }
    }

    private void handleError(Throwable throwable) {
        log.error(throwable.getMessage());
    }

    public boolean isIdleTimeout() {
        boolean temp = isIdleTimeout;
        isIdleTimeout = false;
        return temp;
    }

}
