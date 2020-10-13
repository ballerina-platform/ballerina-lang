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

import io.ballerina.runtime.api.values.BObject;
import org.ballerinalang.net.http.websocket.WebSocketConstants;
import org.ballerinalang.net.http.websocket.WebSocketResourceDispatcher;
import org.ballerinalang.net.http.websocket.WebSocketService;
import org.ballerinalang.net.http.websocket.WebSocketUtil;
import org.ballerinalang.net.http.websocket.client.RetryContext;
import org.ballerinalang.net.http.websocket.server.WebSocketConnectionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;
import org.wso2.transport.http.netty.message.HttpCarbonResponse;

import java.io.IOException;

/**
 * Handshake listener for retrying of the WebSocket client.
 *
 * @since 1.2.0
 */
public class RetryHandshakeListener implements ExtendedHandshakeListener {

    private final RetryContext retryContext;
    private final WebSocketService wsService;
    private final ExtendedHandshakeListener handshakeListener;
    private static final Logger logger = LoggerFactory.getLogger(RetryHandshakeListener.class);

    public RetryHandshakeListener(ExtendedHandshakeListener handshakeListener,
                                  RetryContext retryContext, WebSocketService wsService) {
        this.handshakeListener = handshakeListener;
        this.retryContext = retryContext;
        this.wsService = wsService;
    }

    @Override
    public void onSuccess(WebSocketConnection webSocketConnection, HttpCarbonResponse response) {
        handshakeListener.onSuccess(webSocketConnection, response);
        if (logger.isDebugEnabled()) {
            logger.debug("{} {}", "Connected to ", getWebSocketClient().getStringValue(WebSocketConstants.
                    CLIENT_URL_CONFIG));
        }
        retryContext.setFirstConnectionEstablished();
        retryContext.setReconnectAttempts(0);
    }

    @Override
    public void onError(Throwable throwable, HttpCarbonResponse response) {
        handshakeListener.onError(throwable, response);
        if (throwable instanceof IOException && WebSocketUtil.reconnect(getWebSocketClient(), wsService)) {
            return;
        }
        WebSocketResourceDispatcher.dispatchOnError(getWebSocketConnectionInfo(), throwable);
    }

    @Override
    public BObject getWebSocketClient() {
        return handshakeListener.getWebSocketClient();
    }

    @Override
    public WebSocketConnectionInfo getWebSocketConnectionInfo() {
        return handshakeListener.getWebSocketConnectionInfo();
    }
}
