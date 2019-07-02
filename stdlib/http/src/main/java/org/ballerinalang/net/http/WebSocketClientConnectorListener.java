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
 */

package org.ballerinalang.net.http;

import org.ballerinalang.jvm.util.exceptions.BallerinaConnectorException;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.values.BString;
import org.wso2.transport.http.netty.contract.websocket.WebSocketBinaryMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketClientConnector;
import org.wso2.transport.http.netty.contract.websocket.WebSocketCloseMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnectorListener;
import org.wso2.transport.http.netty.contract.websocket.WebSocketControlMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketHandshaker;
import org.wso2.transport.http.netty.contract.websocket.WebSocketTextMessage;

import java.io.PrintStream;

import static org.ballerinalang.net.http.WebSocketConstants.CLIENT_CONNECTOR;
import static org.ballerinalang.net.http.WebSocketConstants.MAX_RETRY_COUNT;
import static org.ballerinalang.net.http.WebSocketConstants.MAX_RETRY_INTERVAL;
import static org.ballerinalang.net.http.WebSocketConstants.RECONNECTING;
import static org.ballerinalang.net.http.WebSocketConstants.RECONNECT_ATTEMPTS;
import static org.ballerinalang.net.http.WebSocketConstants.RECONNECT_INTERVAL;
import static org.ballerinalang.net.http.WebSocketConstants.RETRY_CONFIG;
import static org.ballerinalang.net.http.WebSocketConstants.RETRY_DECAY;
import static org.ballerinalang.net.http.WebSocketConstants.WEBSOCKET_CLIENT;
import static org.ballerinalang.net.http.WebSocketUtil.reconnect;

/**
 * Ballerina Connector listener for WebSocket.
 *
 * @since 0.93
 */
public class WebSocketClientConnectorListener implements WebSocketConnectorListener {
    private WebSocketOpenConnectionInfo connectionInfo;
    private static final PrintStream console = System.out;

    public void setConnectionInfo(WebSocketOpenConnectionInfo connectionInfo) {
        this.connectionInfo = connectionInfo;
    }

    @Override
    public void onHandshake(WebSocketHandshaker webSocketHandshaker) {
        throw new BallerinaConnectorException("onHandshake and onOpen is not supported for WebSocket client service");
    }

    @Override
    public void onMessage(WebSocketTextMessage webSocketTextMessage) {
        try {
            WebSocketDispatcher.dispatchTextMessage(connectionInfo, webSocketTextMessage);
        } catch (IllegalAccessException e) {
            // Ignore as it is not possible have an Illegal access
        }
    }

    @Override
    public void onMessage(WebSocketBinaryMessage webSocketBinaryMessage) {
        try {
            WebSocketDispatcher.dispatchBinaryMessage(connectionInfo, webSocketBinaryMessage);
        } catch (IllegalAccessException e) {
            // Ignore as it is not possible have an Illegal access
        }
    }

    @Override
    public void onMessage(WebSocketControlMessage webSocketControlMessage) {
        try {
            WebSocketDispatcher.dispatchControlMessage(connectionInfo, webSocketControlMessage);
        } catch (IllegalAccessException e) {
            // Ignore as it is not possible have an Illegal access
        }
    }

    @Override
    public void onMessage(WebSocketCloseMessage webSocketCloseMessage) {
        if (connectionInfo.getWebSocketEndpoint().getType().getName().equalsIgnoreCase(WEBSOCKET_CLIENT)) {
            ObjectValue clientEndpointConfig = connectionInfo.getWebSocketEndpoint();
            MapValue<String, Object> clientConfig = (MapValue<String, Object>) clientEndpointConfig.getMapValue(
                    HttpConstants.CLIENT_ENDPOINT_CONFIG);
            MapValue<String, Object> reconnectConfig = (MapValue<String, Object>) clientConfig
                    .getMapValue(RETRY_CONFIG);
            int reconnectInterval = Math.toIntExact(reconnectConfig.getIntValue(RECONNECT_INTERVAL));
            Double reconnectDecay = reconnectConfig.getFloatValue(RETRY_DECAY);
            int maxReconnectInterval = Math.toIntExact(reconnectConfig.getIntValue(MAX_RETRY_INTERVAL));
            int maxReconnectAttempts = Math.toIntExact(reconnectConfig.getIntValue(MAX_RETRY_COUNT));
            int reconnectAttempts = Integer.valueOf(reconnectConfig.get(RECONNECT_ATTEMPTS).toString());
            int timeout = (int) (reconnectInterval * Math.pow(reconnectDecay, reconnectAttempts));
            ObjectValue webSocketClient = connectionInfo.getWebSocketEndpoint();
            WebSocketService wsService = connectionInfo.getService();
            WebSocketClientConnector clientConnector = (WebSocketClientConnector) clientConfig.get(CLIENT_CONNECTOR);
            if (timeout > maxReconnectInterval) {
                maxReconnectInterval = timeout;
            }
            if (reconnectAttempts < maxReconnectAttempts) {
                console.println(RECONNECTING);
                ((MapValue<String, Object>) clientEndpointConfig.getMapValue(
                        HttpConstants.CLIENT_ENDPOINT_CONFIG)).getMapValue(RETRY_CONFIG).
                        addNativeData(RECONNECT_ATTEMPTS, new BString(String.valueOf(reconnectAttempts + 1)));
                reconnect(clientConnector, webSocketClient, maxReconnectInterval, wsService);
            } else if (reconnectAttempts == 0 && maxReconnectAttempts == 0) {
                console.println(RECONNECTING);
                reconnect(clientConnector, webSocketClient, maxReconnectInterval, wsService);
            } else {
                try {
                    console.println("Couldn't connect to the server");
                    WebSocketDispatcher.dispatchCloseMessage(connectionInfo, webSocketCloseMessage);
                } catch (IllegalAccessException e) {
                    // Ignore as it is not possible have an Illegal access
                }
            }
        } else {
            try {
                console.println("Couldn't connect to the server");
                WebSocketDispatcher.dispatchCloseMessage(connectionInfo, webSocketCloseMessage);
            } catch (IllegalAccessException e) {
                // Ignore as it is not possible have an Illegal access
            }
        }
    }

    @Override
    public void onError(WebSocketConnection webSocketConnection, Throwable throwable) {
        if (connectionInfo.getWebSocketEndpoint().getType().getName().equalsIgnoreCase(WEBSOCKET_CLIENT)) {
            ObjectValue clientEndpointConfig = connectionInfo.getWebSocketEndpoint();
            MapValue<String, Object> clientConfig = (MapValue<String, Object>) clientEndpointConfig.getMapValue(
                    HttpConstants.CLIENT_ENDPOINT_CONFIG);
            MapValue<String, Object> reconnectConfig = (MapValue<String, Object>) clientConfig
                    .getMapValue(RETRY_CONFIG);
            int reconnectInterval = Math.toIntExact(reconnectConfig.getIntValue(RECONNECT_INTERVAL));
            Double reconnectDecay = reconnectConfig.getFloatValue(RETRY_DECAY);
            int maxReconnectInterval = Math.toIntExact(reconnectConfig.getIntValue(MAX_RETRY_INTERVAL));
            int maxReconnectAttempts = Math.toIntExact(reconnectConfig.getIntValue(MAX_RETRY_COUNT));
            int reconnectAttempts = Integer.valueOf(reconnectConfig.get(RECONNECT_ATTEMPTS).toString());
            int timeout = (int) (reconnectInterval * Math.pow(reconnectDecay, reconnectAttempts));
            ObjectValue webSocketClient = connectionInfo.getWebSocketEndpoint();
            WebSocketService wsService = connectionInfo.getService();
            WebSocketClientConnector clientConnector = (WebSocketClientConnector) clientConfig.get(CLIENT_CONNECTOR);
            if (timeout > maxReconnectInterval) {
                maxReconnectInterval = timeout;
            }
            if (reconnectAttempts < maxReconnectAttempts) {
                console.println(RECONNECTING);
                ((MapValue<String, Object>) clientEndpointConfig.getMapValue(
                        HttpConstants.CLIENT_ENDPOINT_CONFIG)).getMapValue(RETRY_CONFIG).
                        addNativeData(RECONNECT_ATTEMPTS, new BString(String.valueOf(reconnectAttempts + 1)));
                reconnect(clientConnector, webSocketClient, maxReconnectInterval, wsService);
            } else if (reconnectAttempts == 0 && maxReconnectAttempts == 0) {
                console.println(RECONNECTING);
                reconnect(clientConnector, webSocketClient, maxReconnectInterval, wsService);
            } else {
                console.println("Couldn't connect to the server");
                WebSocketDispatcher.dispatchError(connectionInfo, throwable);
            }
        } else {
            console.println("Couldn't connect to the server");
            WebSocketDispatcher.dispatchError(connectionInfo, throwable);
        }
    }

    @Override
    public void onIdleTimeout(WebSocketControlMessage controlMessage) {
        try {
            WebSocketDispatcher.dispatchIdleTimeout(connectionInfo);
        } catch (IllegalAccessException e) {
            // Ignore as it is not possible have an Illegal access
        }
    }

    @Override
    public void onClose(WebSocketConnection webSocketConnection) {
        try {
            WebSocketUtil.setListenerOpenField(connectionInfo);
        } catch (IllegalAccessException e) {
            // Ignore as it is not possible have an Illegal access
        }
    }
}
