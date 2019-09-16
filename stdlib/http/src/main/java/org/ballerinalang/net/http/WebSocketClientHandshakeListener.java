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

import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.net.http.websocketclientendpoint.RetryContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;
import org.wso2.transport.http.netty.message.HttpCarbonResponse;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import static org.ballerinalang.net.http.HttpConstants.PROTOCOL_HTTP_PKG_ID;
import static org.ballerinalang.net.http.WebSocketConstants.CONNECTED_TO;
import static org.ballerinalang.net.http.WebSocketConstants.RETRY_CONFIG;
import static org.ballerinalang.net.http.WebSocketUtil.hasRetryConfig;
import static org.ballerinalang.net.http.WebSocketUtil.reconnect;

/**
 * The handshake listener for the client.
 *
 * @since 0.983.1
 */
public class WebSocketClientHandshakeListener extends WebSocketClientHandshakeConnectorListener {

    private final WebSocketService wsService;
    private final WebSocketClientListener clientConnectorListener;
    private final boolean readyOnConnect;
    private final ObjectValue webSocketClient;
    private CountDownLatch countDownLatch;
    private static final Logger logger = LoggerFactory.getLogger(WebSocketClientHandshakeListener.class);

    public WebSocketClientHandshakeListener(ObjectValue webSocketClient, WebSocketService wsService,
                                            WebSocketClientListener clientConnectorListener, boolean readyOnConnect,
                                   CountDownLatch countDownLatch) {
        super(webSocketClient, wsService, clientConnectorListener, readyOnConnect, countDownLatch);
        this.webSocketClient = webSocketClient;
        this.wsService = wsService;
        this.clientConnectorListener = clientConnectorListener;
        this.readyOnConnect = readyOnConnect;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void onSuccess(WebSocketConnection webSocketConnection, HttpCarbonResponse carbonResponse) {
        webSocketClient.set(WebSocketConstants.CLIENT_RESPONSE_FIELD,
                HttpUtil.createResponseStruct(carbonResponse));
        ObjectValue webSocketConnector = BallerinaValues.createObjectValue(PROTOCOL_HTTP_PKG_ID,
                WebSocketConstants.WEBSOCKET_CONNECTOR);
        WebSocketOpenConnectionInfo connectionInfo = getWebSocketOpenConnectionInfo(webSocketConnection,
                webSocketConnector);
        WebSocketUtil.populateEndpoint(webSocketConnection, webSocketClient);
        clientConnectorListener.setConnectionInfo(connectionInfo);
        if (hasRetryConfig(webSocketClient)) {
            if (readyOnConnect) {
                webSocketConnection.readNextFrame();
            }
            WebSocketUtil.populateEndpoint(webSocketConnection, webSocketClient);
            logger.info(CONNECTED_TO + webSocketClient.getStringValue(WebSocketConstants.
                    CLIENT_URL_CONFIG));
        } else {
            RetryContext retryConfig = (RetryContext) webSocketClient.getNativeData(RETRY_CONFIG);
            logger.info(CONNECTED_TO + webSocketClient.getStringValue(WebSocketConstants.
                    CLIENT_URL_CONFIG));
            setWebSocketEndpoint(retryConfig, webSocketClient, webSocketConnection);
            if (retryConfig.isConnectionMade() || readyOnConnect) {
                webSocketConnection.readNextFrame();
            }
            setReconnectContexValue(retryConfig);
        }
        countDownLatch.countDown();
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
        countDownLatch.countDown();
        WebSocketDispatcher.dispatchError(connectionInfo, throwable);
        if (throwable instanceof IOException) {
            if (hasRetryConfig(webSocketClient) && reconnect(connectionInfo)) {
                return;
            }
        }
        WebSocketDispatcher.dispatchError(connectionInfo, throwable);
    }

    private void setReconnectContexValue(RetryContext retryConfig) {
        retryConfig.setConnectionMade();
        retryConfig.setReconnectAttempts(0);
    }

    private void setWebSocketEndpoint(RetryContext retryConfig, ObjectValue webSocketClient,
                                     WebSocketConnection webSocketConnection) {
        if (retryConfig.isConnectionMade()) {
            webSocketClient.set(WebSocketConstants.LISTENER_ID_FIELD, webSocketConnection.getChannelId());
        } else {
            WebSocketUtil.populateEndpoint(webSocketConnection, webSocketClient);
        }
    }
}
