/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.net.http.nativeimpl.connection;

import io.ballerina.runtime.api.BalEnv;
import io.ballerina.runtime.api.BalFuture;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.websocket.WebSocketConstants;
import org.ballerinalang.net.http.websocket.WebSocketUtil;
import org.ballerinalang.net.http.websocket.server.WebSocketConnectionManager;
import org.ballerinalang.net.http.websocket.server.WebSocketServerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.websocket.ServerHandshakeFuture;
import org.wso2.transport.http.netty.contract.websocket.ServerHandshakeListener;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;
import org.wso2.transport.http.netty.contract.websocket.WebSocketHandshaker;

/**
 * {@code AcceptWebSocketUpgrade} is the AcceptWebSocketUpgrade action implementation of the HTTP Connector.
 */
public class AcceptWebSocketUpgrade {
    private static final Logger log = LoggerFactory.getLogger(AcceptWebSocketUpgrade.class);

    public static Object acceptWebSocketUpgrade(BalEnv env, BObject httpCaller,
                                                BMap<BString, BString> headers) {
        BalFuture balFuture = env.markAsync();
        try {
            WebSocketHandshaker webSocketHandshaker =
                    (WebSocketHandshaker) httpCaller.getNativeData(WebSocketConstants.WEBSOCKET_HANDSHAKER);
            WebSocketServerService webSocketService = (WebSocketServerService) httpCaller.getNativeData(
                    WebSocketConstants.WEBSOCKET_SERVICE);
            WebSocketConnectionManager connectionManager = (WebSocketConnectionManager) httpCaller
                    .getNativeData(HttpConstants.NATIVE_DATA_WEBSOCKET_CONNECTION_MANAGER);

            if (webSocketHandshaker == null || webSocketService == null || connectionManager == null) {
                WebSocketUtil.setNotifyFailure("Not a WebSocket upgrade request. Cannot cancel the request", balFuture);
                return null;
            }

            ServerHandshakeFuture future = webSocketHandshaker.handshake(
                    webSocketService.getNegotiableSubProtocols(), webSocketService.getIdleTimeoutInSeconds() * 1000,
                    populateAndGetHttpHeaders(headers), webSocketService.getMaxFrameSize());
            future.setHandshakeListener(
                    new AcceptUpgradeHandshakeListener(webSocketService, connectionManager, balFuture));

        } catch (Exception e) {
            log.error("Error when upgrading to WebSocket", e);
            balFuture.complete(WebSocketUtil.createErrorByType(e));
        }
        return null;
    }

    private static DefaultHttpHeaders populateAndGetHttpHeaders(BMap<BString, BString> headers) {
        DefaultHttpHeaders httpHeaders = new DefaultHttpHeaders();
        BString[] keys = headers.getKeys();
        for (BString key : keys) {
            httpHeaders.add(key.toString(), headers.get(key).getValue());
        }
        return httpHeaders;
    }


    private AcceptWebSocketUpgrade() {
    }

    /**
     * The ServerHandshakeListener that notifies the future on success or failure. Dispatching of the onOpen resource
     * is done after the successful completion of the upgrade resource.
     */
    private static class AcceptUpgradeHandshakeListener implements ServerHandshakeListener {
        private final WebSocketServerService webSocketService;
        private final WebSocketConnectionManager connectionManager;
        private final BalFuture future;

        AcceptUpgradeHandshakeListener(WebSocketServerService webSocketService,
                                       WebSocketConnectionManager connectionManager, BalFuture future) {
            this.webSocketService = webSocketService;
            this.connectionManager = connectionManager;
            this.future = future;
        }

        @Override
        public void onSuccess(WebSocketConnection webSocketConnection) {
            BObject webSocketEndpoint = WebSocketUtil.createAndPopulateWebSocketCaller(
                    webSocketConnection, webSocketService, connectionManager);
            future.complete(webSocketEndpoint);
        }

        @Override
        public void onError(Throwable throwable) {
            String msg = "WebSocket handshake failed: ";
            log.error(msg, throwable);
            WebSocketUtil.setNotifyFailure(throwable.getMessage(), future);
        }
    }
}
