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
 * specif ic language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.net.http;

import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.net.http.exception.WebSocketException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.websocket.WebSocketBinaryMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketCloseMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;
import org.wso2.transport.http.netty.contract.websocket.WebSocketControlMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketHandshaker;
import org.wso2.transport.http.netty.contract.websocket.WebSocketTextMessage;

import java.io.IOException;

import static org.ballerinalang.net.http.WebSocketConstants.STATEMENT_FOR_RECONNECT;
import static org.ballerinalang.net.http.WebSocketConstants.STATUS_CODE_ABNORMAL_CLOSURE;
import static org.ballerinalang.net.http.WebSocketUtil.hasRetryConfig;
import static org.ballerinalang.net.http.WebSocketUtil.reconnect;
import static org.ballerinalang.net.http.WebSocketUtil.setCloseMessage;

/**
 * Client connector listener for WebSocket.
 *
 * @since 1.0.0
 */
public class ClientListener extends WebSocketClientConnectorListener {
    private WebSocketOpenConnectionInfo connectionInfo;
    private static final Logger logger = LoggerFactory.getLogger(ClientListener.class);

    public void setConnectionInfo(WebSocketOpenConnectionInfo connectionInfo) {
        this.connectionInfo = connectionInfo;
    }

    @Override
    public void onHandshake(WebSocketHandshaker webSocketHandshaker) {
        throw new WebSocketException("onHandshake and onOpen is not supported for WebSocket client service");
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
        ObjectValue webSocketClient = connectionInfo.getWebSocketEndpoint();
        int statusCode = webSocketCloseMessage.getCloseCode();
        if (hasRetryConfig(webSocketClient) && statusCode == STATUS_CODE_ABNORMAL_CLOSURE) {
            if (!reconnect(connectionInfo)) {
                logger.info(STATEMENT_FOR_RECONNECT +
                        webSocketClient.getStringValue(WebSocketConstants.CLIENT_URL_CONFIG));
                setCloseMessage(connectionInfo, webSocketCloseMessage);
            }
        } else {
            if (hasRetryConfig(webSocketClient) && statusCode != STATUS_CODE_ABNORMAL_CLOSURE) {
                logger.info("Couldn't connect to the server because the given server: " +
                        webSocketClient.getStringValue(WebSocketConstants.CLIENT_URL_CONFIG) +
                        "have sent the close request to the client.");
            }
            setCloseMessage(connectionInfo, webSocketCloseMessage);
        }
    }

    @Override
    public void onError(WebSocketConnection webSocketConnection, Throwable throwable) {
        ObjectValue webSocketClient = connectionInfo.getWebSocketEndpoint();
        if (throwable instanceof IOException) {
            if (hasRetryConfig(webSocketClient)) {
                if (!reconnect(connectionInfo)) {
                    logger.info(STATEMENT_FOR_RECONNECT +
                            webSocketClient.getStringValue(WebSocketConstants.CLIENT_URL_CONFIG));
                }
            }
            WebSocketDispatcher.dispatchError(connectionInfo, throwable);
        } else {
            if (hasRetryConfig(webSocketClient)) {
                logger.info("Unable do the retry because the connection: " +
                        webSocketClient.getStringValue(WebSocketConstants.CLIENT_URL_CONFIG) +
                        "has some issue that needs to fix.");
            }
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
