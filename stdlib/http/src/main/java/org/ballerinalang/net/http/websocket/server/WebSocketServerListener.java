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

import io.ballerina.runtime.api.values.BMap;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpResource;
import org.ballerinalang.net.http.HttpResourceArguments;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.net.http.websocket.WebSocketResourceDispatcher;
import org.ballerinalang.net.http.websocket.WebSocketUtil;
import org.ballerinalang.net.http.websocket.observability.WebSocketObservabilityConstants;
import org.ballerinalang.net.http.websocket.observability.WebSocketObservabilityUtil;
import org.wso2.transport.http.netty.contract.websocket.ServerHandshakeFuture;
import org.wso2.transport.http.netty.contract.websocket.WebSocketBinaryMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketCloseMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnectorListener;
import org.wso2.transport.http.netty.contract.websocket.WebSocketControlMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketHandshaker;
import org.wso2.transport.http.netty.contract.websocket.WebSocketMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketTextMessage;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.net.URI;

/**
 * Ballerina Connector listener for WebSocket.
 *
 * @since 0.93
 */
public class WebSocketServerListener implements WebSocketConnectorListener {

    private final WebSocketServicesRegistry servicesRegistry;
    private final WebSocketConnectionManager connectionManager;
    private final BMap httpEndpointConfig;

    public WebSocketServerListener(WebSocketServicesRegistry servicesRegistry, BMap httpEndpointConfig) {
        this.servicesRegistry = servicesRegistry;
        this.connectionManager = new WebSocketConnectionManager();
        this.httpEndpointConfig = httpEndpointConfig;
    }

    @Override
    public void onHandshake(WebSocketHandshaker webSocketHandshaker) {
        HttpResourceArguments pathParams = new HttpResourceArguments();
        URI requestUri = createRequestUri(webSocketHandshaker);
        WebSocketServerService wsService = servicesRegistry.findMatching(requestUri.getPath(), pathParams,
                                                                         webSocketHandshaker);
        if (wsService == null) {
            String errMsg = "No service found to handle the service request";
            webSocketHandshaker.cancelHandshake(404, errMsg);
            WebSocketObservabilityUtil.observeError(WebSocketObservabilityConstants.ERROR_TYPE_CONNECTION,
                                                    errMsg, requestUri.getPath(),
                                                    WebSocketObservabilityConstants.CONTEXT_SERVER);
            return;
        }
        setCarbonMessageProperties(pathParams, requestUri, webSocketHandshaker.getHttpCarbonRequest());

        HttpResource onUpgradeResource = wsService.getUpgradeResource();
        if (onUpgradeResource != null) {
            WebSocketResourceDispatcher.dispatchUpgrade(webSocketHandshaker, wsService, httpEndpointConfig,
                                                        connectionManager);
        } else {
            ServerHandshakeFuture future = webSocketHandshaker.handshake(
                    wsService.getNegotiableSubProtocols(), wsService.getIdleTimeoutInSeconds() * 1000, null,
                    wsService.getMaxFrameSize());
            future.setHandshakeListener(new UpgradeListener(wsService, connectionManager));
        }
    }

    private URI createRequestUri(WebSocketHandshaker webSocketHandshaker) {
        String serviceUri = webSocketHandshaker.getTarget();
        serviceUri = HttpUtil.sanitizeBasePath(serviceUri);
        return URI.create(serviceUri);
    }

    private void setCarbonMessageProperties(HttpResourceArguments pathParams, URI requestUri, HttpCarbonMessage msg) {
        msg.setProperty(HttpConstants.QUERY_STR, requestUri.getRawQuery());
        msg.setProperty(HttpConstants.RAW_QUERY_STR, requestUri.getRawQuery());
        msg.setProperty(HttpConstants.RESOURCE_ARGS, pathParams);
    }

    @Override
    public void onMessage(WebSocketTextMessage webSocketTextMessage) {
        WebSocketResourceDispatcher.dispatchOnText(
                getConnectionInfo(webSocketTextMessage), webSocketTextMessage);
    }

    @Override
    public void onMessage(WebSocketBinaryMessage webSocketBinaryMessage) {
        WebSocketResourceDispatcher.dispatchOnBinary(
                getConnectionInfo(webSocketBinaryMessage), webSocketBinaryMessage);
    }

    @Override
    public void onMessage(WebSocketControlMessage webSocketControlMessage) {
        WebSocketResourceDispatcher.dispatchOnPingOnPong(
                getConnectionInfo(webSocketControlMessage), webSocketControlMessage);
    }

    @Override
    public void onMessage(WebSocketCloseMessage webSocketCloseMessage) {
        WebSocketResourceDispatcher.dispatchOnClose(
                getConnectionInfo(webSocketCloseMessage), webSocketCloseMessage);
    }

    @Override
    public void onClose(WebSocketConnection webSocketConnection) {
        WebSocketObservabilityUtil.observeClose(getConnectionInfo(webSocketConnection));
        try {
            WebSocketUtil.setListenerOpenField(
                    connectionManager.removeConnectionInfo(webSocketConnection.getChannelId()));
        } catch (IllegalAccessException e) {
            // Ignore as it is not possible have an Illegal access
        }
    }

    @Override
    public void onError(WebSocketConnection webSocketConnection, Throwable throwable) {
        WebSocketResourceDispatcher.dispatchOnError(
                getConnectionInfo(webSocketConnection), throwable);
    }

    @Override
    public void onIdleTimeout(WebSocketControlMessage controlMessage) {
        WebSocketResourceDispatcher.dispatchOnIdleTimeout(getConnectionInfo(controlMessage));
    }

    private String getConnectionId(WebSocketMessage webSocketMessage) {
        return webSocketMessage.getWebSocketConnection().getChannelId();
    }

    private WebSocketConnectionInfo getConnectionInfo(WebSocketMessage webSocketMessage) {
        return connectionManager.getConnectionInfo(getConnectionId(webSocketMessage));
    }

    private WebSocketConnectionInfo getConnectionInfo(WebSocketConnection webSocketConnection) {
        return connectionManager.getConnectionInfo(webSocketConnection.getChannelId());
    }

}

