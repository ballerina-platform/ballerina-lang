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

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

/**
 * WebSocket test class for WebSocket Connector Listener.
 */
public class WebSocketTestServerConnectorListener implements WebSocketConnectorListener {

    private static final Logger log = LoggerFactory.getLogger(WebSocketTestServerConnectorListener.class);

    private static final String PING = "ping";
    private List<WebSocketConnection> connectionList = new LinkedList<>();
    private boolean isIdleTimeout = false;

    public WebSocketTestServerConnectorListener() {
    }

    @Override
    public void onMessage(WebSocketInitMessage initMessage) {
        HandshakeFuture future = initMessage.handshake(null, true, 3000);
        future.setHandshakeListener(new HandshakeListener() {
            @Override
            public void onSuccess(WebSocketConnection webSocketConnection) {
                connectionList.forEach(
                        currentConn -> currentConn.pushText(WebSocketTestConstants.PAYLOAD_NEW_CLIENT_CONNECTED));
                connectionList.add(webSocketConnection);
                webSocketConnection.startReadingFrames();
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
        WebSocketConnection webSocketConnection = textMessage.getWebSocketConnection();
        String receivedTextToClient = textMessage.getText();
        log.debug("text: " + receivedTextToClient);
        if (PING.equals(receivedTextToClient)) {
            webSocketConnection.ping(ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5}));
            return;
        }
        webSocketConnection.pushText(receivedTextToClient);
    }

    @Override
    public void onMessage(WebSocketBinaryMessage binaryMessage) {
        WebSocketConnection webSocketConnection = binaryMessage.getWebSocketConnection();
        ByteBuffer receivedByteBufferToClient = binaryMessage.getByteBuffer();
        log.debug("ByteBuffer: " + receivedByteBufferToClient);
        webSocketConnection.pushBinary(receivedByteBufferToClient);
    }

    @Override
    public void onMessage(WebSocketControlMessage controlMessage) {
        if (controlMessage.getControlSignal() == WebSocketControlSignal.PING) {
            WebSocketConnection webSocketConnection = controlMessage.getWebSocketConnection();
            webSocketConnection.pong(controlMessage.getPayload()).addListener(future -> {
                if (!future.isSuccess()) {
                    Assert.assertTrue(false, "Could not send the message. "
                            + future.cause().getMessage());
                }
            });
        }
    }

    @Override
    public void onMessage(WebSocketCloseMessage closeMessage) {
        connectionList.forEach(currentConn -> currentConn.pushText(WebSocketTestConstants.PAYLOAD_CLIENT_LEFT));
    }

    @Override
    public void onError(Throwable throwable) {
        handleError(throwable);
    }

    @Override
    public void onIdleTimeout(WebSocketControlMessage controlMessage) {
        this.isIdleTimeout = true;
        WebSocketConnection webSocketConnection = controlMessage.getWebSocketConnection();
        webSocketConnection.close(1001, "Connection timeout").addListener(future -> {
           if (!future.isSuccess()) {
               log.error("Error occurred while closing the connection: " + future.cause().getMessage());
           }
        });
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
