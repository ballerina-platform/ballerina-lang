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

package org.ballerinalang.net.http.websocket.client;

import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.net.http.websocket.WebSocketConstants;
import org.ballerinalang.net.http.websocket.WebSocketResourceDispatcher;
import org.ballerinalang.net.http.websocket.WebSocketService;
import org.ballerinalang.net.http.websocket.WebSocketUtil;
import org.ballerinalang.net.http.websocket.server.WebSocketConnectionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.websocket.ClientHandshakeListener;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;
import org.wso2.transport.http.netty.message.HttpCarbonResponse;

import java.io.IOException;

/**
 * The handshake listener for the client.
 *
 * @since 0.983.1
 */
public class WebSocketClientHandshakeListener implements ClientHandshakeListener {

    private final WebSocketService wsService;
    private final WebSocketClientListener clientConnectorListener;
    private final ObjectValue webSocketClient;
    private final boolean readyOnConnect;
    private static final Logger logger = LoggerFactory.getLogger(WebSocketClientHandshakeListener.class);

    public WebSocketClientHandshakeListener(ObjectValue webSocketClient,
                                            WebSocketService wsService,
                                            WebSocketClientListener clientConnectorListener,
                                            boolean readyOnConnect) {
        this.webSocketClient = webSocketClient;
        this.wsService = wsService;
        this.clientConnectorListener = clientConnectorListener;
        this.readyOnConnect = readyOnConnect;
    }

    @Override
    public void onSuccess(WebSocketConnection webSocketConnection, HttpCarbonResponse carbonResponse) {
        RetryContext retryConfig = null;
        if (WebSocketUtil.hasRetryConfig(webSocketClient)) {
            retryConfig = (RetryContext) webSocketClient.getNativeData(WebSocketConstants.RETRY_CONFIG);
        }
        WebSocketUtil.setWebSocketClient(webSocketClient, carbonResponse, webSocketConnection, retryConfig,
                null);
        clientConnectorListener.setConnectionInfo(WebSocketUtil.getWebSocketOpenConnectionInfo(
                webSocketConnection, wsService, webSocketClient, readyOnConnect));
        if (readyOnConnect || (WebSocketUtil.hasRetryConfig(webSocketClient)
                && ((RetryContext) webSocketClient.getNativeData(WebSocketConstants.RETRY_CONFIG)).
                isConnectionMade())) {
            webSocketConnection.readNextFrame();
        }
        WebSocketUtil.countDownForHandshake(webSocketClient);
        logger.debug(WebSocketConstants.LOG_MESSAGE, WebSocketConstants.CONNECTED_TO, webSocketClient.getStringValue(
                WebSocketConstants.CLIENT_URL_CONFIG));
        // The following are created for future connections.
        // Checks whether the config has retry config or not.
        // If it has a retry config, set these variables to the default variable.
        if (retryConfig != null) {
            setWebSocketEndpoint(retryConfig, webSocketClient, webSocketConnection);
            setReconnectContexValue(retryConfig);
        }
    }

    @Override
    public void onError(Throwable throwable, HttpCarbonResponse response) {
        if (response != null) {
            webSocketClient.set(WebSocketConstants.CLIENT_RESPONSE_FIELD, HttpUtil.createResponseStruct(response));
        }
        WebSocketConnectionInfo connectionInfo = WebSocketUtil.getWebSocketOpenConnectionInfo(null,
               wsService, webSocketClient, readyOnConnect);
        if (throwable instanceof IOException) {
            if (WebSocketUtil.hasRetryConfig(webSocketClient) && WebSocketUtil.reconnect(connectionInfo)) {
                return;
            }
        }
        dispatchOnError(connectionInfo, throwable);
    }

    private void setReconnectContexValue(RetryContext retryConfig) {
        retryConfig.setConnectionMade();
        retryConfig.setReconnectAttempts(0);
    }

    private void dispatchOnError(WebSocketConnectionInfo connectionInfo, Throwable throwable) {
        WebSocketUtil.countDownForHandshake(webSocketClient);
        WebSocketResourceDispatcher.dispatchOnError(connectionInfo, throwable);
    }

    private void setWebSocketEndpoint(RetryContext retryConfig, ObjectValue webSocketClient,
                                      WebSocketConnection webSocketConnection) {
        if (retryConfig.isConnectionMade()) {
            webSocketClient.set(WebSocketConstants.LISTENER_ID_FIELD, webSocketConnection.getChannelId());
        } else {
            WebSocketUtil.populateWebSocketEndpoint(webSocketConnection, webSocketClient);
        }
    }
}
