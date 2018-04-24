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
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contract.websocket.HandshakeFuture;
import org.wso2.transport.http.netty.contract.websocket.HandshakeListener;
import org.wso2.transport.http.netty.contract.websocket.WebSocketBinaryMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketClientConnector;
import org.wso2.transport.http.netty.contract.websocket.WebSocketCloseMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnectorListener;
import org.wso2.transport.http.netty.contract.websocket.WebSocketControlMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketInitMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketTextMessage;
import org.wso2.transport.http.netty.contract.websocket.WsClientConnectorConfig;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.wso2.transport.http.netty.util.TestUtil;

/**
 * Server Connector Listener to check WebSocket pass-through scenarios.
 */
public class WebSocketPassthroughServerConnectorListener implements WebSocketConnectorListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketPassthroughServerConnectorListener.class);

    private final HttpWsConnectorFactory connectorFactory = new DefaultHttpWsConnectorFactory();

    @Override
    public void onMessage(WebSocketInitMessage initMessage) {
        String remoteUrl = String.format("ws://%s:%d/%s", "localhost",
                                         TestUtil.REMOTE_WS_SERVER_PORT, "websocket");
        WsClientConnectorConfig configuration = new WsClientConnectorConfig(remoteUrl);
        configuration.setAutoRead(false);
        WebSocketClientConnector clientConnector = connectorFactory.createWsClientConnector(configuration);
        WebSocketConnectorListener clientConnectorListener = new WebSocketPassthroughClientConnectorListener();
        clientConnector.connect(clientConnectorListener).setHandshakeListener(new HandshakeListener() {
            @Override
            public void onSuccess(WebSocketConnection clientWebSocketConnection) {
                HandshakeFuture serverFuture = initMessage.handshake();
                serverFuture.setHandshakeListener(new HandshakeListener() {
                    @Override
                    public void onSuccess(WebSocketConnection serverWebSocketConnection) {
                        WebSocketPassThroughTestConnectionManager.getInstance().
                                interRelateSessions(serverWebSocketConnection, clientWebSocketConnection);
                        serverWebSocketConnection.readNextFrame();
                        clientWebSocketConnection.readNextFrame();
                    }

                    @Override
                    public void onError(Throwable t) {
                        logger.error(t.getMessage());
                        Assert.assertTrue(false, "Error: " + t.getMessage());
                    }
                });
            }

            @Override
            public void onError(Throwable t) {
                Assert.assertTrue(false, t.getMessage());
            }
        });
    }

    @Override
    public void onMessage(WebSocketTextMessage textMessage) {
        WebSocketConnection serverConnection = WebSocketPassThroughTestConnectionManager.getInstance().
                getClientConnection(textMessage.getWebSocketConnection());
        serverConnection.pushText(textMessage.getText());
        serverConnection.readNextFrame();
    }

    @Override
    public void onMessage(WebSocketBinaryMessage binaryMessage) {
        WebSocketConnection serverConnection = WebSocketPassThroughTestConnectionManager.getInstance().
                getClientConnection(binaryMessage.getWebSocketConnection());
        serverConnection.pushBinary(binaryMessage.getByteBuffer());
        serverConnection.readNextFrame();
    }

    @Override
    public void onMessage(WebSocketControlMessage controlMessage) {
        // Do nothing.
    }

    @Override
    public void onMessage(WebSocketCloseMessage closeMessage) {
        WebSocketConnection clientConnection = WebSocketPassThroughTestConnectionManager.getInstance()
                .getClientConnection(closeMessage.getWebSocketConnection());
        clientConnection.close();
    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void onIdleTimeout(WebSocketControlMessage controlMessage) {
    }
}
