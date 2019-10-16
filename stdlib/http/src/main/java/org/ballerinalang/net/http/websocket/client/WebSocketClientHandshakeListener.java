/*
 * Copyright (c) 2019, WSO2 Inc. (http:www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http:www.apache.orglicensesLICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.net.http.websocket.client;

import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.net.http.websocket.WebSocketConstants;
import org.ballerinalang.net.http.websocket.WebSocketResourceDispatcher;
import org.ballerinalang.net.http.websocket.WebSocketUtil;
import org.ballerinalang.net.http.websocket.server.WebSocketOpenConnectionInfo;
import org.ballerinalang.net.http.websocket.server.WebSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;
import org.wso2.transport.http.netty.message.HttpCarbonResponse;

import java.io.IOException;

import static org.ballerinalang.net.http.HttpConstants.PROTOCOL_HTTP_PKG_ID;
import static org.ballerinalang.net.http.websocket.WebSocketConstants.CONNECTED_TO;
import static org.ballerinalang.net.http.websocket.WebSocketConstants.RETRY_CONFIG;
import static org.ballerinalang.net.http.websocket.WebSocketUtil.countDownForHandshake;
import static org.ballerinalang.net.http.websocket.WebSocketUtil.hasRetryConfig;
import static org.ballerinalang.net.http.websocket.WebSocketUtil.reconnect;

/**
 * The handshake listener for the client.
 *
 * @since 1.1.0
 */
public class WebSocketClientHandshakeListener extends WebSocketClientHandshakeListenerImpl {

    private final ObjectValue webSocketClient;
    private static final Logger logger = LoggerFactory.getLogger(WebSocketClientHandshakeListener.class);

    public WebSocketClientHandshakeListener(ObjectValue webSocketClient, WebSocketService wsService,
                                            WebSocketClientListener clientConnectorListener, boolean readyOnConnect) {
        super(webSocketClient, wsService, clientConnectorListener, readyOnConnect);
        this.webSocketClient = webSocketClient;
    }

    @Override
    public void onSuccess(WebSocketConnection webSocketConnection, HttpCarbonResponse carbonResponse) {
        super.onSuccess(webSocketConnection, carbonResponse);
        logger.debug(CONNECTED_TO + webSocketClient.getStringValue(WebSocketConstants.
                CLIENT_URL_CONFIG));
        // Following these are created for future connection
        // Check whether the config has retry config or not
        // It has retry config, set these variables to default variable
        RetryContext retryConfig;
        if (hasRetryConfig(webSocketClient)) {
            retryConfig = (RetryContext) webSocketClient.getNativeData(RETRY_CONFIG);
            setWebSocketEndpoint(retryConfig, webSocketClient, webSocketConnection);
            // Read the next frame when readyOnConnect is true in first the connection or after the first connection
            if (retryConfig.isConnectionMade()) {
                webSocketConnection.readNextFrame();
            }
            setReconnectContexValue(retryConfig);
        }
        countDownForHandshake(webSocketClient);
    }

    @Override
    public void onError(Throwable throwable, HttpCarbonResponse response) {
        if (response != null) {
            webSocketClient.set(WebSocketConstants.CLIENT_RESPONSE_FIELD, HttpUtil.createResponseStruct(response));
        }
        ObjectValue webSocketConnector = BallerinaValues.createObjectValue(PROTOCOL_HTTP_PKG_ID,
                WebSocketConstants.WEBSOCKET_CONNECTOR);
        WebSocketOpenConnectionInfo connectionInfo = getWebSocketOpenConnectionInfo(null,
                webSocketConnector);
        if (throwable instanceof IOException) {
            if (hasRetryConfig(webSocketClient) && reconnect(connectionInfo)) {
                return;
            }
        }
        setError(connectionInfo, throwable);
    }

    private void setReconnectContexValue(RetryContext retryConfig) {
        retryConfig.setConnectionMade();
        retryConfig.setReconnectAttempts(0);
    }

    private void setError(WebSocketOpenConnectionInfo connectionInfo, Throwable throwable) {
        countDownForHandshake(webSocketClient);
        WebSocketResourceDispatcher.dispatchOnError(connectionInfo, throwable);
    }

    private void setWebSocketEndpoint(RetryContext retryConfig, ObjectValue webSocketClient,
                                      WebSocketConnection webSocketConnection) {
        if (retryConfig.isConnectionMade()) {
            webSocketClient.set(WebSocketConstants.LISTENER_ID_FIELD, webSocketConnection.getChannelId());
        } else {
            WebSocketUtil.populateWebSocketCaller(webSocketConnection, webSocketClient);
        }
    }
}
