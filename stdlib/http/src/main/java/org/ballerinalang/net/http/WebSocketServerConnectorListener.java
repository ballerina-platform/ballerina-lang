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
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.services.ErrorHandlerUtils;
import org.ballerinalang.util.observability.ObservabilityUtils;
import org.ballerinalang.util.observability.ObserverContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.websocket.WebSocketBinaryMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketCloseMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnectorListener;
import org.wso2.transport.http.netty.contract.websocket.WebSocketControlMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketInitMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketTextMessage;

import java.util.Optional;

import static org.ballerinalang.net.http.HttpConstants.SERVICE_ENDPOINT_CONNECTION_FIELD;
import static org.ballerinalang.util.observability.ObservabilityConstants.SERVER_CONNECTOR_WEBSOCKET;

/**
 * Ballerina Connector listener for WebSocket.
 *
 * @since 0.93
 */
public class WebSocketServerConnectorListener implements WebSocketConnectorListener {

    private static final Logger log = LoggerFactory.getLogger(WebSocketServerConnectorListener.class);
    private final WebSocketServicesRegistry servicesRegistry;
    private final WebSocketConnectionManager connectionManager;
    private final Struct httpEndpointConfig;

    public WebSocketServerConnectorListener(WebSocketServicesRegistry servicesRegistry, Struct httpEndpointConfig) {
        this.servicesRegistry = servicesRegistry;
        this.connectionManager = new WebSocketConnectionManager();
        this.httpEndpointConfig = httpEndpointConfig;
    }

    @Override
    public void onMessage(WebSocketInitMessage webSocketInitMessage) {
        WebSocketService wsService = WebSocketDispatcher.findService(servicesRegistry, webSocketInitMessage);

        HttpResource onUpgradeResource = wsService.getUpgradeResource();
        if (onUpgradeResource != null) {
            webSocketInitMessage.getHttpCarbonRequest().setProperty(HttpConstants.RESOURCES_CORS,
                    onUpgradeResource.getCorsHeaders());
            Resource balResource = onUpgradeResource.getBalResource();
            BValue[] signatureParams = HttpDispatcher.getSignatureParameters(onUpgradeResource, webSocketInitMessage
                    .getHttpCarbonRequest(), httpEndpointConfig);

            BMap<String, BValue> httpServiceEndpoint = (BMap<String, BValue>) signatureParams[0];
            BMap<String, BValue> httpConnection =
                    (BMap<String, BValue>) httpServiceEndpoint.get(SERVICE_ENDPOINT_CONNECTION_FIELD);
            httpConnection.addNativeData(WebSocketConstants.WEBSOCKET_MESSAGE, webSocketInitMessage);
            httpConnection.addNativeData(WebSocketConstants.WEBSOCKET_SERVICE, wsService);
            httpConnection.addNativeData(HttpConstants.NATIVE_DATA_WEBSOCKET_CONNECTION_MANAGER, connectionManager);

            // TODO: Need to revisit this code of observation.
            Optional<ObserverContext> observerContext = ObservabilityUtils.startServerObservation(
                    SERVER_CONNECTOR_WEBSOCKET, wsService.getServiceInfo(), balResource.getName(),
                    null);

            Executor.submit(balResource, new CallableUnitCallback() {
                @Override
                public void notifySuccess() {
                    if (!webSocketInitMessage.isCancelled() && !webSocketInitMessage.isHandshakeStarted()) {
                        WebSocketUtil.handleHandshake(wsService, connectionManager, null, webSocketInitMessage, null,
                                null);
                        // TODO: Change this to readNextFrame
                    } else {
                        if (!webSocketInitMessage.isCancelled()) {
                            Resource onOpenResource = wsService.getResourceByName(
                                    WebSocketConstants.RESOURCE_NAME_ON_OPEN);
                            WebSocketOpenConnectionInfo connectionInfo =
                                    connectionManager.getConnectionInfo(webSocketInitMessage.getSessionID());
                            WebSocketConnection webSocketConnection = connectionInfo.getWebSocketConnection();
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
                public void notifyFailure(BMap<String, BValue> error) {
                    ErrorHandlerUtils.printError("error: " + BLangVMErrors.getPrintableStackTrace(error));
                    WebSocketOpenConnectionInfo connectionInfo =
                            connectionManager.getConnectionInfo(webSocketInitMessage.getSessionID());
                    if (connectionInfo != null) {
                        WebSocketUtil.closeDuringUnexpectedCondition(connectionInfo.getWebSocketConnection());
                    }
                }
            }, null, observerContext.orElse(null), signatureParams);

        } else {
            WebSocketUtil.handleHandshake(wsService, connectionManager, null, webSocketInitMessage, null, null);
        }
    }

    @Override
    public void onMessage(WebSocketTextMessage webSocketTextMessage) {
        WebSocketDispatcher.dispatchTextMessage(
                connectionManager.getConnectionInfo(webSocketTextMessage.getSessionID()), webSocketTextMessage);
    }

    @Override
    public void onMessage(WebSocketBinaryMessage webSocketBinaryMessage) {
        WebSocketDispatcher.dispatchBinaryMessage(
                connectionManager.getConnectionInfo(webSocketBinaryMessage.getSessionID()), webSocketBinaryMessage);
    }

    @Override
    public void onMessage(WebSocketControlMessage webSocketControlMessage) {
        WebSocketDispatcher.dispatchControlMessage(
                connectionManager.getConnectionInfo(webSocketControlMessage.getSessionID()), webSocketControlMessage);
    }

    @Override
    public void onMessage(WebSocketCloseMessage webSocketCloseMessage) {
        WebSocketDispatcher.dispatchCloseMessage(
                connectionManager.removeConnectionInfo(webSocketCloseMessage.getSessionID()), webSocketCloseMessage);
    }

    @Override
    public void onError(WebSocketConnection webSocketConnection, Throwable throwable) {
        WebSocketDispatcher.dispatchError(
                connectionManager.removeConnectionInfo(webSocketConnection.getId()), throwable);
    }

    @Override
    public void onIdleTimeout(WebSocketControlMessage controlMessage) {
        WebSocketDispatcher.dispatchIdleTimeout(connectionManager.getConnectionInfo(controlMessage.getSessionID()),
                controlMessage);
    }

}

