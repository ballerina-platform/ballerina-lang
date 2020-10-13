/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.net.http.websocket.server;

import io.ballerina.runtime.api.connector.CallableUnitCallback;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.services.ErrorHandlerUtils;
import org.ballerinalang.net.http.websocket.WebSocketResourceDispatcher;
import org.ballerinalang.net.http.websocket.WebSocketUtil;
import org.wso2.transport.http.netty.contract.websocket.ServerHandshakeFuture;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;
import org.wso2.transport.http.netty.contract.websocket.WebSocketHandshaker;

/**
 * The onUpgrade resource callback.
 */
public class OnUpgradeResourceCallback implements CallableUnitCallback {
    private final WebSocketHandshaker webSocketHandshaker;
    private final WebSocketServerService wsService;
    private final WebSocketConnectionManager connectionManager;

    public OnUpgradeResourceCallback(WebSocketHandshaker webSocketHandshaker, WebSocketServerService wsService,
                                     WebSocketConnectionManager connectionManager) {
        this.webSocketHandshaker = webSocketHandshaker;
        this.wsService = wsService;
        this.connectionManager = connectionManager;
    }

    @Override
    public void notifySuccess() {
        if (!webSocketHandshaker.isCancelled() && !webSocketHandshaker.isHandshakeStarted()) {
            ServerHandshakeFuture future = webSocketHandshaker.handshake(
                    wsService.getNegotiableSubProtocols(), wsService.getIdleTimeoutInSeconds() * 1000, null,
                    wsService.getMaxFrameSize());
            future.setHandshakeListener(new UpgradeListener(wsService, connectionManager));
        } else {
            // If the acceptWebSocketUpgrade function has not been called inside the upgrade resource
            if (!webSocketHandshaker.isCancelled()) {

                WebSocketConnectionInfo connectionInfo =
                        connectionManager.getConnectionInfo(webSocketHandshaker.getChannelId());
                WebSocketConnection webSocketConnection = null;
                try {
                    webSocketConnection = connectionInfo.getWebSocketConnection();
                } catch (IllegalAccessException e) {
                    // Ignore as it is not possible have an Illegal access
                }
                WebSocketResourceDispatcher.dispatchOnOpen(webSocketConnection, connectionInfo.getWebSocketEndpoint(),
                                                           wsService);
            }
        }
    }

    @Override
    public void notifyFailure(BError error) {
        ErrorHandlerUtils.printError(error.getPrintableStackTrace());
        WebSocketConnectionInfo connectionInfo =
                connectionManager.getConnectionInfo(webSocketHandshaker.getChannelId());
        if (connectionInfo != null) {
            try {
                WebSocketUtil.closeDuringUnexpectedCondition(connectionInfo.getWebSocketConnection());
            } catch (IllegalAccessException e) {
                // Ignore as it is not possible have an Illegal access
            }
        }
    }
}

