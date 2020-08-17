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

package org.ballerinalang.net.netty.websocket.passthrough;

import org.ballerinalang.net.netty.contract.HttpWsConnectorFactory;
import org.ballerinalang.net.netty.contract.websocket.ClientHandshakeFuture;
import org.ballerinalang.net.netty.contract.websocket.ClientHandshakeListener;
import org.ballerinalang.net.netty.contract.websocket.ServerHandshakeFuture;
import org.ballerinalang.net.netty.contract.websocket.ServerHandshakeListener;
import org.ballerinalang.net.netty.contract.websocket.WebSocketBinaryMessage;
import org.ballerinalang.net.netty.contract.websocket.WebSocketClientConnector;
import org.ballerinalang.net.netty.contract.websocket.WebSocketClientConnectorConfig;
import org.ballerinalang.net.netty.contract.websocket.WebSocketCloseMessage;
import org.ballerinalang.net.netty.contract.websocket.WebSocketConnection;
import org.ballerinalang.net.netty.contract.websocket.WebSocketConnectorListener;
import org.ballerinalang.net.netty.contract.websocket.WebSocketControlMessage;
import org.ballerinalang.net.netty.contract.websocket.WebSocketHandshaker;
import org.ballerinalang.net.netty.contract.websocket.WebSocketTextMessage;
import org.ballerinalang.net.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.ballerinalang.net.netty.message.HttpCarbonResponse;
import org.ballerinalang.net.netty.util.TestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

/**
 * Server Connector Listener to check WebSocket pass-through scenarios.
 */
public class WebSocketPassThroughServerConnectorListener implements WebSocketConnectorListener {

    private static final Logger LOG = LoggerFactory.getLogger(WebSocketPassThroughServerConnectorListener.class);

    private final HttpWsConnectorFactory connectorFactory = new DefaultHttpWsConnectorFactory();

    @Override
    public void onHandshake(WebSocketHandshaker webSocketHandshaker) {
        String remoteUrl = String.format("ws://%s:%d/%s", "localhost",
                                         TestUtil.WEBSOCKET_REMOTE_SERVER_PORT, "websocket");
        WebSocketClientConnectorConfig configuration = new WebSocketClientConnectorConfig(remoteUrl);
        configuration.setAutoRead(false);
        WebSocketClientConnector clientConnector = connectorFactory.createWsClientConnector(configuration);
        WebSocketConnectorListener clientConnectorListener = new WebSocketPassThroughClientConnectorListener();
        ClientHandshakeFuture clientHandshakeFuture = clientConnector.connect();
        clientHandshakeFuture.setWebSocketConnectorListener(clientConnectorListener);
        clientHandshakeFuture.setClientHandshakeListener(new ClientHandshakeListener() {
            @Override
            public void onSuccess(WebSocketConnection clientWebSocketConnection, HttpCarbonResponse response) {
                ServerHandshakeFuture serverFuture = webSocketHandshaker.handshake();
                serverFuture.setHandshakeListener(new ServerHandshakeListener() {
                    @Override
                    public void onSuccess(WebSocketConnection serverWebSocketConnection) {
                        WebSocketPassThroughTestConnectionManager.getInstance().
                                interRelateSessions(serverWebSocketConnection, clientWebSocketConnection);
                        serverWebSocketConnection.readNextFrame();
                        clientWebSocketConnection.readNextFrame();
                    }

                    @Override
                    public void onError(Throwable t) {
                        LOG.error(t.getMessage());
                        Assert.fail("Error: " + t.getMessage());
                    }
                });
            }

            @Override
            public void onError(Throwable t, HttpCarbonResponse response) {
                Assert.fail(t.getMessage());
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
        int statusCode = closeMessage.getCloseCode();
        String closeReason = closeMessage.getCloseReason();
        WebSocketConnection clientConnection = WebSocketPassThroughTestConnectionManager.getInstance()
                .getClientConnection(closeMessage.getWebSocketConnection());
        clientConnection.initiateConnectionClosure(statusCode, closeReason).addListener(
                future -> closeMessage.getWebSocketConnection().finishConnectionClosure(statusCode, null));
    }

    @Override
    public void onClose(WebSocketConnection webSocketConnection) {
        //Do nothing
    }

    @Override
    public void onError(WebSocketConnection webSocketConnection, Throwable throwable) {

    }

    @Override
    public void onIdleTimeout(WebSocketControlMessage controlMessage) {
    }
}
