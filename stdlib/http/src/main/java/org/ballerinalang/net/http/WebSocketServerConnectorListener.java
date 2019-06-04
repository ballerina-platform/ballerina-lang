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

import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.connector.api.Executor;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.services.ErrorHandlerUtils;
import org.ballerinalang.util.observability.ObserveUtils;
import org.ballerinalang.util.observability.ObserverContext;
import org.wso2.transport.http.netty.contract.websocket.WebSocketBinaryMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketCloseMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnectorListener;
import org.wso2.transport.http.netty.contract.websocket.WebSocketControlMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketHandshaker;
import org.wso2.transport.http.netty.contract.websocket.WebSocketMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketTextMessage;

import static org.ballerinalang.util.observability.ObservabilityConstants.SERVER_CONNECTOR_WEBSOCKET;

/**
 * Ballerina Connector listener for WebSocket.
 *
 * @since 0.93
 */
public class WebSocketServerConnectorListener implements WebSocketConnectorListener {

    private final WebSocketServicesRegistry servicesRegistry;
    private final WebSocketConnectionManager connectionManager;
    private final Struct httpEndpointConfig;

    public WebSocketServerConnectorListener(WebSocketServicesRegistry servicesRegistry, Struct httpEndpointConfig) {
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
            Resource balResource = onUpgradeResource.getBalResource();
            BValue[] signatureParams = HttpDispatcher.getSignatureParameters(onUpgradeResource, webSocketHandshaker
                    .getHttpCarbonRequest(), httpEndpointConfig);

            BMap<String, BValue> httpConnection = (BMap<String, BValue>) signatureParams[0];
            httpConnection.addNativeData(WebSocketConstants.WEBSOCKET_MESSAGE, webSocketHandshaker);
            httpConnection.addNativeData(WebSocketConstants.WEBSOCKET_SERVICE, wsService);
            httpConnection.addNativeData(HttpConstants.NATIVE_DATA_WEBSOCKET_CONNECTION_MANAGER, connectionManager);

            // TODO: Need to revisit this code of observation.
            ObserverContext observerContext = null;
            if (ObserveUtils.isObservabilityEnabled()) {
                observerContext = new ObserverContext();
                observerContext.setConnectorName(SERVER_CONNECTOR_WEBSOCKET);
                observerContext.setServiceName(ObserveUtils.getFullServiceName(wsService.getServiceInfo()));
                observerContext.setResourceName(balResource.getName());
            }

            Executor.submit(balResource, new OnUpgradeResourceCallableUnitCallback(webSocketHandshaker, wsService),
                            null, observerContext, signatureParams);

        } else {
            WebSocketUtil.handleHandshake(wsService, connectionManager, null, webSocketHandshaker, null, null);
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
                WebSocketUtil.handleHandshake(wsService, connectionManager, null, webSocketHandshaker, null, null);
            } else {
                if (!webSocketHandshaker.isCancelled()) {
                    Resource onOpenResource = wsService.getResourceByName(
                            WebSocketConstants.RESOURCE_NAME_ON_OPEN);
                    WebSocketOpenConnectionInfo connectionInfo =
                            connectionManager.getConnectionInfo(webSocketHandshaker.getChannelId());
                    WebSocketConnection webSocketConnection = null;
                    try {
                        webSocketConnection = connectionInfo.getWebSocketConnection();
                    } catch (IllegalAccessException e) {
                        // Ignore as it is not possible have an Illegal access
                    }
                    BMap<String, BValue> webSocketEndpoint = connectionInfo.getWebSocketEndpoint();
                    BMap<String, BValue> webSocketConnector = (BMap<String, BValue>) webSocketEndpoint
                            .get(WebSocketConstants.LISTENER_CONNECTOR_FIELD);
                    if (onOpenResource != null) {
                        WebSocketUtil.executeOnOpenResource(onOpenResource, webSocketEndpoint,
                                                            webSocketConnection);
                    } else {
                        WebSocketUtil.readFirstFrame(webSocketConnection, webSocketConnector);
                    }
                }
            }
        }

        @Override
        public void notifyFailure(BError error) {
            ErrorHandlerUtils.printError(BLangVMErrors.getPrintableStackTrace(error));
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

