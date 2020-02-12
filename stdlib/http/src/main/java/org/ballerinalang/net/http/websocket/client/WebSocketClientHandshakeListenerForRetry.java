/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.net.http.websocket.client;

import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.net.http.websocket.WebSocketConstants;
import org.ballerinalang.net.http.websocket.WebSocketResourceDispatcher;
import org.ballerinalang.net.http.websocket.WebSocketService;
import org.ballerinalang.net.http.websocket.WebSocketUtil;
import org.ballerinalang.net.http.websocket.observability.WebSocketObservabilityUtil;
import org.ballerinalang.net.http.websocket.server.WebSocketConnectionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.websocket.ClientHandshakeListener;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;
import org.wso2.transport.http.netty.message.HttpCarbonResponse;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * The retry handshake listener for the client.
 *
 * @since 1.2.0
 */
public class WebSocketClientHandshakeListenerForRetry implements ClientHandshakeListener {

    private final WebSocketService wsService;
    private final WebSocketClientConnectorListenerForRetry clientConnectorListener;
    private final boolean readyOnConnect;
    private final ObjectValue webSocketClient;
    private CountDownLatch countDownLatch;
    private RetryContext retryConfig;
    private static final Logger logger = LoggerFactory.getLogger(WebSocketClientHandshakeListenerForRetry.class);

    public WebSocketClientHandshakeListenerForRetry(ObjectValue webSocketClient,
                                                    WebSocketService wsService,
                                                    WebSocketClientConnectorListenerForRetry clientConnectorListener,
                                                    boolean readyOnConnect, CountDownLatch countDownLatch,
                                                    RetryContext retryConfig) {
        this.webSocketClient = webSocketClient;
        this.wsService = wsService;
        this.clientConnectorListener = clientConnectorListener;
        this.readyOnConnect = readyOnConnect;
        this.countDownLatch = countDownLatch;
        this.retryConfig = retryConfig;
    }

    @Override
    public void onSuccess(WebSocketConnection webSocketConnection, HttpCarbonResponse carbonResponse) {
        ObjectValue webSocketConnector;
        if (!retryConfig.isFirstConnectionMadeSuccessfully()) {
            webSocketConnector = WebSocketUtil.createWebSocketConnector(readyOnConnect);
        } else {
            webSocketConnector = (ObjectValue) webSocketClient.get(WebSocketConstants.CLIENT_CONNECTOR_FIELD);

        }
        setWebSocketClient(webSocketClient, carbonResponse, webSocketConnection, retryConfig);
        WebSocketConnectionInfo connectionInfo = WebSocketUtil.getWebSocketOpenConnectionInfo(webSocketConnection,
                webSocketConnector, webSocketClient, wsService);
        clientConnectorListener.setConnectionInfo(connectionInfo);
        // Reads the next frame when `readyOnConnect` is true or `isReady` is true.
        WebSocketUtil.readNextFrame(readyOnConnect, webSocketClient, webSocketConnection);
        countDownLatch.countDown();
        // Calls the countDown() to initial connection's countDown latch
        WebSocketUtil.countDownForHandshake(webSocketClient);
        WebSocketObservabilityUtil.observeConnection(connectionInfo);
        if (logger.isDebugEnabled()) {
            logger.debug(WebSocketConstants.LOG_MESSAGE, "Connected to ", webSocketClient.getStringValue(
                    WebSocketConstants.CLIENT_URL_CONFIG));
        }
        // The following are created for future connections.
        // Checks whether the config has a retry config or not.
        // If it has a retry config, set these variables to the default variable.
        adjustContextOnSuccess(retryConfig);
    }

    @Override
    public void onError(Throwable throwable, HttpCarbonResponse response) {
        WebSocketConnectionInfo connectionInfo = WebSocketUtil.getConnectionInfoForOnError(response, webSocketClient,
                wsService, countDownLatch);
        if (throwable instanceof IOException && WebSocketUtil.reconnect(connectionInfo)) {
            return;
        }
        dispatchOnError(connectionInfo, throwable);
    }

    private void setWebSocketClient(ObjectValue webSocketClient, HttpCarbonResponse carbonResponse,
                                    WebSocketConnection webSocketConnection, RetryContext retryConfig) {
        //Using only one service endpoint in the client as there can be only one connection.
        webSocketClient.set(WebSocketConstants.CLIENT_RESPONSE_FIELD, HttpUtil.createResponseStruct(carbonResponse));
        if (retryConfig.isFirstConnectionMadeSuccessfully()) {
            webSocketClient.set(WebSocketConstants.LISTENER_ID_FIELD, webSocketConnection.getChannelId());
        } else {
            WebSocketUtil.populateWebSocketEndpoint(webSocketConnection, webSocketClient);
        }
    }

    /**
     * Sets the value into the `retryContext`.
     *
     * @param retryConfig - the retry context that represents a retry config
     */
    private void adjustContextOnSuccess(RetryContext retryConfig) {
        retryConfig.setFirstConnectionMadeSuccessfully();
        retryConfig.setReconnectAttempts(0);
    }

    private void dispatchOnError(WebSocketConnectionInfo connectionInfo, Throwable throwable) {
        WebSocketUtil.countDownForHandshake(webSocketClient);
        WebSocketResourceDispatcher.dispatchOnError(connectionInfo, throwable);
    }
}
