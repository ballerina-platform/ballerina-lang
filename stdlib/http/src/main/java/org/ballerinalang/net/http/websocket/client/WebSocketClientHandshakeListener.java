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

import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.net.http.websocket.WebSocketConstants;
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
 * The handshake listener for the client.
 *
 * @since 0.983.1
 */
public class WebSocketClientHandshakeListener implements ClientHandshakeListener {

    private final WebSocketService wsService;
    private final WebSocketClientListener clientConnectorListener;
    private final boolean readyOnConnect;
    private final ObjectValue webSocketClient;
    private final CountDownLatch countDownLatch;
    private static final Logger logger = LoggerFactory.getLogger(WebSocketClientHandshakeListener.class);
    private static final String CONNECTED_TO = "Connected to ";

    public WebSocketClientHandshakeListener(ObjectValue webSocketClient,
                                            WebSocketService wsService,
                                            WebSocketClientListener clientConnectorListener,
                                            boolean readyOnConnect, CountDownLatch countDownLatch) {
        this.webSocketClient = webSocketClient;
        this.wsService = wsService;
        this.clientConnectorListener = clientConnectorListener;
        this.readyOnConnect = readyOnConnect;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void onSuccess(WebSocketConnection webSocketConnection, HttpCarbonResponse carbonResponse) {
        RetryContext retryConfig = null;
        if (WebSocketUtil.hasRetryConfig(webSocketClient)) {
            retryConfig = (RetryContext) webSocketClient.getNativeData(WebSocketConstants.RETRY_CONFIG);
        }
        ObjectValue webSocketConnector;
        if (retryConfig != null && !retryConfig.isFirstConnectionMadeSuccessfully()) {
            webSocketConnector = WebSocketUtil.createWebSocketConnector(readyOnConnect);
        } else {
            webSocketConnector = (ObjectValue) webSocketClient.get(WebSocketConstants.CLIENT_CONNECTOR_FIELD);
        }
        setWebSocketClient(webSocketClient, carbonResponse, webSocketConnection, retryConfig);
        WebSocketConnectionInfo connectionInfo = WebSocketUtil.getWebSocketOpenConnectionInfo(webSocketConnection,
                webSocketConnector, webSocketClient, wsService);
        clientConnectorListener.setConnectionInfo(connectionInfo);
        // Read the next frame when readyOnConnect is true or isReady is true
        WebSocketUtil.readNextFrame(readyOnConnect, webSocketClient, webSocketConnection);
        WebSocketUtil.countDownForSuccess(countDownLatch, webSocketClient);
        WebSocketObservabilityUtil.observeConnection(connectionInfo);
        logger.debug(WebSocketConstants.LOG_MESSAGE, CONNECTED_TO, webSocketClient.getStringValue(
                WebSocketConstants.CLIENT_URL_CONFIG));
        // The following are created for future connections.
        // Checks whether the config has retry config or not.
        // If it has a retry config, set these variables to the default variable.
        if (retryConfig != null) {
            setReconnectContexValue(retryConfig);
        }
    }

    @Override
    public void onError(Throwable throwable, HttpCarbonResponse response) {
        if (response != null) {
            webSocketClient.set(WebSocketConstants.CLIENT_RESPONSE_FIELD, HttpUtil.createResponseStruct(response));
        }
        ObjectValue webSocketConnector = BallerinaValues.createObjectValue(HttpConstants.PROTOCOL_HTTP_PKG_ID,
                                                                           WebSocketConstants.WEBSOCKET_CONNECTOR);
        WebSocketConnectionInfo connectionInfo = WebSocketUtil.getWebSocketOpenConnectionInfo(null,
                webSocketConnector, webSocketClient, wsService);
        if (countDownLatch != null) {
            countDownLatch.countDown();
        }
        if (throwable instanceof IOException && WebSocketUtil.hasRetryConfig(webSocketClient) &&
                WebSocketUtil.reconnect(connectionInfo)) {
                return;
        }
        WebSocketUtil.dispatchOnError(connectionInfo, throwable);
    }

    private static void setWebSocketClient(ObjectValue webSocketClient, HttpCarbonResponse carbonResponse,
                                           WebSocketConnection webSocketConnection, RetryContext retryConfig) {
        //Using only one service endpoint in the client as there can be only one connection.
        webSocketClient.set(WebSocketConstants.CLIENT_RESPONSE_FIELD, HttpUtil.createResponseStruct(carbonResponse));
        if (retryConfig != null && retryConfig.isFirstConnectionMadeSuccessfully()) {
            webSocketClient.set(WebSocketConstants.LISTENER_ID_FIELD, webSocketConnection.getChannelId());
        } else {
            WebSocketUtil.populateWebSocketEndpoint(webSocketConnection, webSocketClient);
        }
    }

    /**
     * Set the value into the retryContext.
     *
     * @param retryConfig retry context
     */
    private void setReconnectContexValue(RetryContext retryConfig) {
        if (!retryConfig.isFirstConnectionMadeSuccessfully()) {
            retryConfig.setFirstConnectionMadeSuccessfully();
        }
        retryConfig.setReconnectAttempts(0);
    }
}
