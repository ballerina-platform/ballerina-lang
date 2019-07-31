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

import org.ballerinalang.jvm.observability.ObservabilityConstants;
import org.ballerinalang.jvm.observability.ObserveUtils;
import org.ballerinalang.jvm.observability.ObserverContext;
import org.ballerinalang.jvm.types.AttachedFunction;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.connector.CallableUnitCallback;
import org.ballerinalang.jvm.values.connector.Executor;
import org.ballerinalang.services.ErrorHandlerUtils;
import org.wso2.transport.http.netty.contract.websocket.WebSocketBinaryMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketCloseMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnectorListener;
import org.wso2.transport.http.netty.contract.websocket.WebSocketControlMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketHandshaker;
import org.wso2.transport.http.netty.contract.websocket.WebSocketMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketTextMessage;

import java.util.HashMap;
import java.util.Map;

import static org.ballerinalang.jvm.observability.ObservabilityConstants.SERVER_CONNECTOR_WEBSOCKET;

/**
 * Ballerina Connector listener for WebSocket.
 *
 * @since 0.93
 */
public class WebSocketServerConnectorListener implements WebSocketConnectorListener {

    private final WebSocketServicesRegistry servicesRegistry;
    private final WebSocketConnectionManager connectionManager;
    private final MapValue httpEndpointConfig;

    public WebSocketServerConnectorListener(WebSocketServicesRegistry servicesRegistry, MapValue httpEndpointConfig) {
        this.servicesRegistry = servicesRegistry;
        this.connectionManager = new WebSocketConnectionManager();
        this.httpEndpointConfig = httpEndpointConfig;
    }

    @Override
    public void onHandshake(WebSocketHandshaker webSocketHandshaker) {
        WebSocketService wsService = WebSocketDispatcher.findService(servicesRegistry, webSocketHandshaker);
        if (wsService == null) {
            return;
        }
        HttpResource onUpgradeResource = wsService.getUpgradeResource();
        if (onUpgradeResource != null) {
            webSocketHandshaker.getHttpCarbonRequest().setProperty(HttpConstants.RESOURCES_CORS,
                                                                   onUpgradeResource.getCorsHeaders());
            AttachedFunction balResource = onUpgradeResource.getBalResource();
            Object[] signatureParams = HttpDispatcher.getSignatureParameters(onUpgradeResource, webSocketHandshaker
                    .getHttpCarbonRequest(), httpEndpointConfig);

            ObjectValue httpConnection = (ObjectValue) signatureParams[0];
            httpConnection.addNativeData(WebSocketConstants.WEBSOCKET_MESSAGE, webSocketHandshaker);
            httpConnection.addNativeData(WebSocketConstants.WEBSOCKET_SERVICE, wsService);
            httpConnection.addNativeData(HttpConstants.NATIVE_DATA_WEBSOCKET_CONNECTION_MANAGER, connectionManager);

            ObserverContext observerContext = null;
            Map<String, Object> properties = new HashMap<>();
            if (ObserveUtils.isObservabilityEnabled()) {
                observerContext = new ObserverContext();
                observerContext.setConnectorName(SERVER_CONNECTOR_WEBSOCKET);
                observerContext.setResourceName(balResource.getName());
                // TODO: extract span context as a map and add to the observer context
                properties.put(ObservabilityConstants.KEY_OBSERVER_CONTEXT, observerContext);
            }
            //TODO this is temp fix till we get the service.start() API
            Executor.submit(wsService.getScheduler(), onUpgradeResource.getParentService().getBalService(),
                            balResource.getName(), new OnUpgradeResourceCallableUnitCallback(
                            webSocketHandshaker, wsService), properties, signatureParams);
        } else {
            WebSocketUtil.handleHandshake(wsService, connectionManager, null, webSocketHandshaker, null);
        }
    }

    private class OnUpgradeResourceCallableUnitCallback implements CallableUnitCallback {
        private final WebSocketHandshaker webSocketHandshaker;
        private final WebSocketService wsService;

        public OnUpgradeResourceCallableUnitCallback(WebSocketHandshaker webSocketHandshaker,
                                                     WebSocketService wsService) {
            this.webSocketHandshaker = webSocketHandshaker;
            this.wsService = wsService;
        }

        @Override
        public void notifySuccess() {
            if (!webSocketHandshaker.isCancelled() && !webSocketHandshaker.isHandshakeStarted()) {
                WebSocketUtil.handleHandshake(wsService, connectionManager, null,
                                              webSocketHandshaker, null);
            } else {
                if (!webSocketHandshaker.isCancelled()) {
                    AttachedFunction onOpenResource = wsService.getResourceByName(
                            WebSocketConstants.RESOURCE_NAME_ON_OPEN);
                    WebSocketOpenConnectionInfo connectionInfo =
                            connectionManager.getConnectionInfo(webSocketHandshaker.getChannelId());
                    WebSocketConnection webSocketConnection = null;
                    try {
                        webSocketConnection = connectionInfo.getWebSocketConnection();
                    } catch (IllegalAccessException e) {
                        // Ignore as it is not possible have an Illegal access
                    }
                    ObjectValue webSocketEndpoint = connectionInfo.getWebSocketEndpoint();
                    ObjectValue webSocketConnector = (ObjectValue) webSocketEndpoint
                            .get(WebSocketConstants.LISTENER_CONNECTOR_FIELD);
                    if (onOpenResource != null) {
                        WebSocketUtil.executeOnOpenResource(wsService, onOpenResource, webSocketEndpoint,
                                                            webSocketConnection);
                    } else {
                        WebSocketUtil.readFirstFrame(webSocketConnection, webSocketConnector);
                    }
                }
            }
        }

        @Override
        public void notifyFailure(ErrorValue error) {
            //TODO test the output of BLangVMErrors.getPrintableStackTrace(error) with error.toString()
            ErrorHandlerUtils.printError(error.toString());
            WebSocketOpenConnectionInfo connectionInfo =
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

    @Override
    public void onMessage(WebSocketTextMessage webSocketTextMessage) {
        try {
            WebSocketDispatcher.dispatchTextMessage(
                    connectionManager.getConnectionInfo(getConnectionId(webSocketTextMessage)), webSocketTextMessage);
        } catch (IllegalAccessException e) {
            // Ignore as it is not possible have an Illegal access
        }
    }

    @Override
    public void onMessage(WebSocketBinaryMessage webSocketBinaryMessage) {
        try {
            WebSocketDispatcher.dispatchBinaryMessage(
                    connectionManager.getConnectionInfo(getConnectionId(webSocketBinaryMessage)),
                    webSocketBinaryMessage);
        } catch (IllegalAccessException e) {
            // Ignore as it is not possible have an Illegal access
        }
    }

    @Override
    public void onMessage(WebSocketControlMessage webSocketControlMessage) {
        try {
            WebSocketDispatcher.dispatchControlMessage(
                    connectionManager.getConnectionInfo(getConnectionId(webSocketControlMessage)),
                    webSocketControlMessage);
        } catch (IllegalAccessException e) {
            // Ignore as it is not possible have an Illegal access
        }
    }

    @Override
    public void onMessage(WebSocketCloseMessage webSocketCloseMessage) {
        try {
            WebSocketDispatcher.dispatchCloseMessage(
                    connectionManager.getConnectionInfo(getConnectionId(webSocketCloseMessage)), webSocketCloseMessage);
        } catch (IllegalAccessException e) {
            // Ignore as it is not possible have an Illegal access
        }
    }

    @Override
    public void onClose(WebSocketConnection webSocketConnection) {
        try {
            WebSocketUtil.setListenerOpenField(
                    connectionManager.removeConnectionInfo(webSocketConnection.getChannelId()));
        } catch (IllegalAccessException e) {
            // Ignore as it is not possible have an Illegal access
        }
    }

    @Override
    public void onError(WebSocketConnection webSocketConnection, Throwable throwable) {
        WebSocketDispatcher.dispatchError(
                connectionManager.getConnectionInfo(webSocketConnection.getChannelId()), throwable);
    }

    @Override
    public void onIdleTimeout(WebSocketControlMessage controlMessage) {
        try {
            WebSocketDispatcher.dispatchIdleTimeout(
                    connectionManager.getConnectionInfo(getConnectionId(controlMessage)));
        } catch (IllegalAccessException e) {
            // Ignore as it is not possible have an Illegal access
        }
    }

    private String getConnectionId(WebSocketMessage webSocketMessage) {
        return webSocketMessage.getWebSocketConnection().getChannelId();
    }
}

