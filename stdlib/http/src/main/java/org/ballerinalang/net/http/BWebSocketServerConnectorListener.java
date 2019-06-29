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
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.services.ErrorHandlerUtils;
import org.wso2.transport.http.netty.contract.websocket.WebSocketBinaryMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketCloseMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnectorListener;
import org.wso2.transport.http.netty.contract.websocket.WebSocketControlMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketHandshaker;
import org.wso2.transport.http.netty.contract.websocket.WebSocketMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketTextMessage;

/**
 * Ballerina Connector listener for WebSocket.
 *
 * @since 0.93
 */
public class BWebSocketServerConnectorListener implements WebSocketConnectorListener {

    private final BWebSocketServicesRegistry servicesRegistry;
    private final BWebSocketConnectionManager connectionManager;
    private final Struct httpEndpointConfig;

    public BWebSocketServerConnectorListener(BWebSocketServicesRegistry servicesRegistry, Struct httpEndpointConfig) {
        this.servicesRegistry = servicesRegistry;
        this.connectionManager = new BWebSocketConnectionManager();
        this.httpEndpointConfig = httpEndpointConfig;
    }

    @Override
    public void onHandshake(WebSocketHandshaker webSocketHandshaker) {
        BWebSocketService wsService = BWebSocketDispatcher.findService(servicesRegistry, webSocketHandshaker);

        BHttpResource onUpgradeResource = wsService.getUpgradeResource();
        if (onUpgradeResource != null) {
            webSocketHandshaker.getHttpCarbonRequest().setProperty(HttpConstants.RESOURCES_CORS,
                                                                   onUpgradeResource.getCorsHeaders());
            Resource balResource = onUpgradeResource.getBalResource();
            BValue[] signatureParams = BHttpDispatcher.getSignatureParameters(onUpgradeResource, webSocketHandshaker
                    .getHttpCarbonRequest(), httpEndpointConfig);

            BMap<String, BValue> httpConnection = (BMap<String, BValue>) signatureParams[0];
            httpConnection.addNativeData(WebSocketConstants.WEBSOCKET_MESSAGE, webSocketHandshaker);
            httpConnection.addNativeData(WebSocketConstants.WEBSOCKET_SERVICE, wsService);
            httpConnection.addNativeData(HttpConstants.NATIVE_DATA_WEBSOCKET_CONNECTION_MANAGER, connectionManager);

            // TODO: Need to revisit this code of observation.
//            ObserverContext observerContext = null;
//            if (ObserveUtils.isObservabilityEnabled()) {
//                observerContext = new ObserverContext();
//                observerContext.setConnectorName(SERVER_CONNECTOR_WEBSOCKET);
//                observerContext.setServiceName(ObserveUtils.getFullServiceName(wsService.getServiceInfo()));
//                observerContext.setResourceName(balResource.getName());
//            }
//
//            Executor.submit(balResource, new OnUpgradeResourceCallableUnitCallback(webSocketHandshaker, wsService),
//                            null, observerContext, signatureParams);

        } else {
            BWebSocketUtil.handleHandshake(wsService, connectionManager, null, webSocketHandshaker, null, null);
        }
    }

    private class OnUpgradeResourceCallableUnitCallback implements CallableUnitCallback {
        private final WebSocketHandshaker webSocketHandshaker;
        private final BWebSocketService wsService;

        public OnUpgradeResourceCallableUnitCallback(WebSocketHandshaker webSocketHandshaker,
                                                     BWebSocketService wsService) {
            this.webSocketHandshaker = webSocketHandshaker;
            this.wsService = wsService;
        }

        @Override
        public void notifySuccess() {
            if (!webSocketHandshaker.isCancelled() && !webSocketHandshaker.isHandshakeStarted()) {
                BWebSocketUtil.handleHandshake(wsService, connectionManager, null, webSocketHandshaker, null, null);
            } else {
                if (!webSocketHandshaker.isCancelled()) {
                    Resource onOpenResource = wsService.getResourceByName(
                            WebSocketConstants.RESOURCE_NAME_ON_OPEN);
                    BWebSocketOpenConnectionInfo connectionInfo =
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
                        BWebSocketUtil.executeOnOpenResource(onOpenResource, webSocketEndpoint,
                                                            webSocketConnection);
                    } else {
                        BWebSocketUtil.readFirstFrame(webSocketConnection, webSocketConnector);
                    }
                }
            }
        }

        @Override
        public void notifyFailure(BError error) {
            ErrorHandlerUtils.printError(BLangVMErrors.getPrintableStackTrace(error));
            BWebSocketOpenConnectionInfo connectionInfo =
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
            BWebSocketDispatcher.dispatchTextMessage(
                    connectionManager.getConnectionInfo(getConnectionId(webSocketTextMessage)), webSocketTextMessage);
        } catch (IllegalAccessException e) {
            // Ignore as it is not possible have an Illegal access
        }
    }

    @Override
    public void onMessage(WebSocketBinaryMessage webSocketBinaryMessage) {
        try {
            BWebSocketDispatcher.dispatchBinaryMessage(
                    connectionManager.getConnectionInfo(getConnectionId(webSocketBinaryMessage)),
                    webSocketBinaryMessage);
        } catch (IllegalAccessException e) {
            // Ignore as it is not possible have an Illegal access
        }
    }

    @Override
    public void onMessage(WebSocketControlMessage webSocketControlMessage) {
        try {
            BWebSocketDispatcher.dispatchControlMessage(
                    connectionManager.getConnectionInfo(getConnectionId(webSocketControlMessage)),
                    webSocketControlMessage);
        } catch (IllegalAccessException e) {
            // Ignore as it is not possible have an Illegal access
        }
    }

    @Override
    public void onMessage(WebSocketCloseMessage webSocketCloseMessage) {
        try {
            BWebSocketDispatcher.dispatchCloseMessage(
                    connectionManager.getConnectionInfo(getConnectionId(webSocketCloseMessage)), webSocketCloseMessage);
        } catch (IllegalAccessException e) {
            // Ignore as it is not possible have an Illegal access
        }
    }

    @Override
    public void onClose(WebSocketConnection webSocketConnection) {
        try {
            BWebSocketUtil.setListenerOpenField(
                    connectionManager.removeConnectionInfo(webSocketConnection.getChannelId()));
        } catch (IllegalAccessException e) {
            // Ignore as it is not possible have an Illegal access
        }
    }

    @Override
    public void onError(WebSocketConnection webSocketConnection, Throwable throwable) {
        BWebSocketDispatcher.dispatchError(
                connectionManager.getConnectionInfo(webSocketConnection.getChannelId()), throwable);
    }

    @Override
    public void onIdleTimeout(WebSocketControlMessage controlMessage) {
        try {
            BWebSocketDispatcher.dispatchIdleTimeout(
                    connectionManager.getConnectionInfo(getConnectionId(controlMessage)));
        } catch (IllegalAccessException e) {
            // Ignore as it is not possible have an Illegal access
        }
    }

    private String getConnectionId(WebSocketMessage webSocketMessage) {
        return webSocketMessage.getWebSocketConnection().getChannelId();
    }
}

