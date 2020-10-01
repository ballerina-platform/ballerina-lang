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

package org.ballerinalang.net.http.websocket.client.listener;

import org.ballerinalang.jvm.api.values.BObject;
import org.ballerinalang.net.http.websocket.WebSocketConstants;
import org.ballerinalang.net.http.websocket.WebSocketUtil;
import org.ballerinalang.net.http.websocket.server.WebSocketConnectionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.websocket.WebSocketBinaryMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketCloseMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;
import org.wso2.transport.http.netty.contract.websocket.WebSocketControlMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketHandshaker;
import org.wso2.transport.http.netty.contract.websocket.WebSocketTextMessage;

import java.io.IOException;

/**
 * Client listener of the retry WebSocket client.
 *
 * @since 1.2.0
 */
public class RetryConnectorListener implements ExtendedConnectorListener {

    private WebSocketConnectionInfo connectionInfo = null;
    private static final Logger logger = LoggerFactory.getLogger(RetryConnectorListener.class);
    private ExtendedConnectorListener connectorListener;
    public static final String LOG_MESSAGE = "{} {}";

    public RetryConnectorListener(ExtendedConnectorListener connectorListener) {
        this.connectorListener = connectorListener;
    }

    @Override
    public void setConnectionInfo(WebSocketConnectionInfo connectionInfo) {
        connectorListener.setConnectionInfo(connectionInfo);
        this.connectionInfo = connectionInfo;
    }

    @Override
    public void onHandshake(WebSocketHandshaker webSocketHandshaker) {
        connectorListener.onHandshake(webSocketHandshaker);
    }

    @Override
    public void onMessage(WebSocketTextMessage textMessage) {
        connectorListener.onMessage(textMessage);
    }

    @Override
    public void onMessage(WebSocketBinaryMessage binaryMessage) {
        connectorListener.onMessage(binaryMessage);
    }

    @Override
    public void onMessage(WebSocketControlMessage controlMessage) {
        connectorListener.onMessage(controlMessage);
    }

    @Override
    public void onMessage(WebSocketCloseMessage webSocketCloseMessage) {
        BObject webSocketClient = connectionInfo.getWebSocketEndpoint();
        int statusCode = webSocketCloseMessage.getCloseCode();
        if (WebSocketUtil.hasRetryContext(webSocketClient)) {
            if (statusCode == WebSocketConstants.STATUS_CODE_ABNORMAL_CLOSURE &&
                    WebSocketUtil.reconnect(webSocketClient, connectionInfo.getService())) {
                return;
            } else {
                if (statusCode != WebSocketConstants.STATUS_CODE_ABNORMAL_CLOSURE && logger.isDebugEnabled()) {
                    logger.debug(LOG_MESSAGE, "Reconnect attempt not made because of " +
                            "close initiated by the server: ", webSocketClient.getStringValue(WebSocketConstants.
                            CLIENT_URL_CONFIG));
                }
            }
        }
        connectorListener.onMessage(webSocketCloseMessage);
    }

    @Override
    public void onClose(WebSocketConnection webSocketConnection) {
        connectorListener.onClose(webSocketConnection);
    }

    @Override
    public void onError(WebSocketConnection webSocketConnection, Throwable throwable) {
        BObject webSocketClient = connectionInfo.getWebSocketEndpoint();
        if (WebSocketUtil.hasRetryContext(webSocketClient) && throwable instanceof IOException &&
                WebSocketUtil.reconnect(webSocketClient, connectionInfo.getService())) {
            return;
        }
        connectorListener.onError(webSocketConnection, throwable);
    }

    @Override
    public void onIdleTimeout(WebSocketControlMessage controlMessage) {
        connectorListener.onIdleTimeout(controlMessage);
    }
}
