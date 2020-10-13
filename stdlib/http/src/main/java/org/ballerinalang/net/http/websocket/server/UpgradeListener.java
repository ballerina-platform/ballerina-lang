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

import io.ballerina.runtime.api.values.BObject;
import org.ballerinalang.net.http.websocket.WebSocketConstants;
import org.ballerinalang.net.http.websocket.WebSocketResourceDispatcher;
import org.ballerinalang.net.http.websocket.WebSocketUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.websocket.ServerHandshakeListener;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;

/**
 * The ServerHandshakeListener that dispatches the onOpen resource onSuccess.
 */
class UpgradeListener implements ServerHandshakeListener {
    private static final Logger logger = LoggerFactory.getLogger(UpgradeListener.class);

    private final WebSocketServerService wsService;
    private final WebSocketConnectionManager connectionManager;

    UpgradeListener(WebSocketServerService wsService, WebSocketConnectionManager connectionManager) {
        this.wsService = wsService;
        this.connectionManager = connectionManager;
    }

    @Override
    public void onSuccess(WebSocketConnection webSocketConnection) {
        BObject webSocketCaller = WebSocketUtil.createAndPopulateWebSocketCaller(webSocketConnection, wsService,
                                                                                 connectionManager);
        WebSocketResourceDispatcher.dispatchOnOpen(webSocketConnection, webSocketCaller, wsService);
    }

    @Override
    public void onError(Throwable throwable) {
        String msg = "Unable to complete WebSocket handshake: ";
        logger.error(msg, throwable);
        throw WebSocketUtil.getWebSocketException("", throwable, WebSocketConstants.ErrorCode.WsGenericError.
                errorCode(), null);
    }
}
