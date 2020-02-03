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
import org.ballerinalang.net.http.websocket.WebSocketConstants;
import org.ballerinalang.net.http.websocket.WebSocketResourceDispatcher;
import org.ballerinalang.net.http.websocket.WebSocketUtil;
import org.ballerinalang.net.http.websocket.server.WebSocketConnectionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.websocket.WebSocketCloseMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;

import java.io.IOException;

/**
 * Client listener of the WebSocket.
 *
 * @since 1.2.0
 */
public class WebSocketClientConnectorListenerForRetry extends WebSocketClientConnectorListener {

    private WebSocketConnectionInfo connectionInfo = null;
    private static final Logger logger = LoggerFactory.getLogger(WebSocketClientConnectorListenerForRetry.class);

    @Override
    public void setConnectionInfo(WebSocketConnectionInfo connectionInfo) {
        super.setConnectionInfo(connectionInfo);
        this.connectionInfo = connectionInfo;
    }

    @Override
    public void onMessage(WebSocketCloseMessage webSocketCloseMessage) {
        ObjectValue webSocketClient = connectionInfo.getWebSocketEndpoint();
        int statusCode = webSocketCloseMessage.getCloseCode();
        if (WebSocketUtil.hasRetryConfig(webSocketClient)) {
            if (statusCode == WebSocketConstants.STATUS_CODE_ABNORMAL_CLOSURE &&
                    WebSocketUtil.reconnect(connectionInfo)) {
                return;
            } else {
                if (statusCode != WebSocketConstants.STATUS_CODE_ABNORMAL_CLOSURE) {
                    logger.debug(WebSocketConstants.LOG_MESSAGE, "Reconnect attempt not made because of " +
                            "close initiated by the server: ", webSocketClient.getStringValue(WebSocketConstants.
                            CLIENT_URL_CONFIG));
                }
            }
        }
        WebSocketUtil.dispatchOnClose(connectionInfo, webSocketCloseMessage);
    }

    @Override
    public void onError(WebSocketConnection webSocketConnection, Throwable throwable) {
        ObjectValue webSocketClient = connectionInfo.getWebSocketEndpoint();
        if (WebSocketUtil.hasRetryConfig(webSocketClient) && throwable instanceof IOException &&
                WebSocketUtil.reconnect(connectionInfo)) {
            return;
        }
        WebSocketResourceDispatcher.dispatchOnError(connectionInfo, throwable);
    }
}
