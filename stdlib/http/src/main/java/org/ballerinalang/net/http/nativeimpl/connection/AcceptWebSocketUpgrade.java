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

import io.netty.handler.codec.http.DefaultHttpHeaders;
import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.connector.NonBlockingCallback;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.websocket.WebSocketConstants;
import org.ballerinalang.net.http.websocket.WebSocketException;
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

    public static Object acceptWebSocketUpgrade(ObjectValue httpCaller,
                                                MapValue<String, String> headers) {
        NonBlockingCallback callback = new NonBlockingCallback(Scheduler.getStrand());
        try {
            WebSocketHandshaker webSocketHandshaker =
                    (WebSocketHandshaker) httpCaller.getNativeData(WebSocketConstants.WEBSOCKET_HANDSHAKER);
            WebSocketServerService webSocketService = (WebSocketServerService) httpCaller.getNativeData(
                    WebSocketConstants.WEBSOCKET_SERVICE);
            WebSocketConnectionManager connectionManager = (WebSocketConnectionManager) httpCaller
                    .getNativeData(HttpConstants.NATIVE_DATA_WEBSOCKET_CONNECTION_MANAGER);

            if (webSocketHandshaker == null || webSocketService == null || connectionManager == null) {
                callback.notifyFailure(new WebSocketException(WebSocketConstants.ErrorCode.WsInvalidHandshakeError,
                                                              "Not a WebSocket upgrade request. Cannot upgrade from " +
                                                                      "HTTP to WS"));
                return null;
            }

            ServerHandshakeFuture future = webSocketHandshaker.handshake(
                    webSocketService.getNegotiableSubProtocols(), webSocketService.getIdleTimeoutInSeconds() * 1000,
                    populateAndGetHttpHeaders(headers), webSocketService.getMaxFrameSize());
            future.setHandshakeListener(
                    new AcceptUpgradeHandshakeListener(webSocketService, connectionManager, callback));

        } catch (Exception e) {
            log.error("Error when upgrading to WebSocket", e);
            callback.notifyFailure(WebSocketUtil.createErrorByType(e));
        }
        return null;
    }

    private static DefaultHttpHeaders populateAndGetHttpHeaders(MapValue<String, String> headers) {
        DefaultHttpHeaders httpHeaders = new DefaultHttpHeaders();
        Object[] keys = headers.getKeys();
        for (Object key : keys) {
            httpHeaders.add(key.toString(), headers.get(key.toString()));
        }
        return httpHeaders;
    }


    private AcceptWebSocketUpgrade() {
    }

    /**
     * The ServerHandshakeListener that notifies the callback on success or failure. Dispatching of the onOpen resource
     * is done after the successful completion of the upgrade resource.
     */
    private static class AcceptUpgradeHandshakeListener implements ServerHandshakeListener {
        private final WebSocketServerService webSocketService;
        private final WebSocketConnectionManager connectionManager;
        private final NonBlockingCallback callback;

        AcceptUpgradeHandshakeListener(
                WebSocketServerService webSocketService, WebSocketConnectionManager connectionManager,
                NonBlockingCallback callback) {
            this.webSocketService = webSocketService;
            this.connectionManager = connectionManager;
            this.callback = callback;
        }

        @Override
        public void onSuccess(WebSocketConnection webSocketConnection) {
            ObjectValue webSocketEndpoint = WebSocketUtil.createAndPopulateWebSocketCaller(
                    webSocketConnection, webSocketService, connectionManager);
            callback.setReturnValues(webSocketEndpoint);
            callback.notifySuccess();
        }

        @Override
        public void onError(Throwable throwable) {
            String msg = "WebSocket handshake failed: ";
            log.error(msg, throwable);
            callback.notifyFailure(
                    new WebSocketException(WebSocketConstants.ErrorCode.WsInvalidHandshakeError,
                                           msg + throwable.getMessage()));
        }
    }
}
