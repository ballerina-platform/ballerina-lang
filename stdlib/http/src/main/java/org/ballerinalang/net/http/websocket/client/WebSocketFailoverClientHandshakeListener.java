/*
 * Copyright (c) 2019, WSO2 Inc. (http:www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http:www.apache.orglicensesLICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.net.http.websocket.client;

import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.net.http.websocket.WebSocketConstants;
import org.ballerinalang.net.http.websocket.WebSocketResourceDispatcher;
import org.ballerinalang.net.http.websocket.WebSocketService;
import org.ballerinalang.net.http.websocket.WebSocketUtil;
import org.ballerinalang.net.http.websocket.server.WebSocketConnectionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.websocket.ClientHandshakeListener;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;
import org.wso2.transport.http.netty.message.HttpCarbonResponse;

import java.io.IOException;

/**
 * The handshake listener of the failover client.
 *
 * @since 1.1.0
 */
public class WebSocketFailoverClientHandshakeListener implements ClientHandshakeListener {

    private final WebSocketService wsService;
    private final WebSocketFailoverClientListener clientConnectorListener;
    private final boolean readyOnConnect;
    private final ObjectValue webSocketClient;

    private static final Logger logger = LoggerFactory.getLogger(WebSocketFailoverClientHandshakeListener.class);

    public WebSocketFailoverClientHandshakeListener(ObjectValue webSocketClient, WebSocketService wsService,
                                             WebSocketFailoverClientListener clientConnectorListener,
                                             boolean readyOnConnect) {
        this.webSocketClient = webSocketClient;
        this.wsService = wsService;
        this.clientConnectorListener = clientConnectorListener;
        this.readyOnConnect = readyOnConnect;
    }

    @Override
    public void onSuccess(WebSocketConnection webSocketConnection, HttpCarbonResponse carbonResponse) {
        WebSocketUtil.setWebSocketClient(webSocketClient, carbonResponse, webSocketConnection);
        clientConnectorListener.setConnectionInfo(WebSocketUtil.getWebSocketOpenConnectionInfo(
                webSocketConnection, wsService, webSocketClient));
        FailoverContext failoverConfig = (FailoverContext) webSocketClient.getNativeData(WebSocketConstants.
                FAILOVER_CONFIG);
        if (readyOnConnect || failoverConfig.isConnectionMade()) {
            webSocketConnection.readNextFrame();
        }
        WebSocketUtil.countDownForHandshake(webSocketClient);
        setFailoverWebSocketEndpoint(failoverConfig, webSocketClient, webSocketConnection);
        failoverConfig.setFailoverFinished(false);
        // Following these are created for future connection
        // Check whether the config has retry config or not
        // It has retry config, set these variables to default variable
        if (WebSocketUtil.hasRetryConfig(webSocketClient)) {
            ((RetryContext) webSocketClient.getNativeData(WebSocketConstants.RETRY_CONFIG)).setReconnectAttempts(0);
        }
        int currentIndex = failoverConfig.getCurrentIndex();
        logger.debug(WebSocketConstants.LOG_MESSAGE, WebSocketConstants.CONNECTED_TO ,
                failoverConfig.getTargetUrls().get(currentIndex));
        // Set failover context variable's value
        failoverConfig.setInitialIndex(currentIndex);
        failoverConfig.setConnectionMade();
    }

    @Override
    public void onError(Throwable throwable, HttpCarbonResponse response) {
        if (response != null) {
            webSocketClient.set(WebSocketConstants.CLIENT_RESPONSE_FIELD, HttpUtil.createResponseStruct(response));
        }
        WebSocketConnectionInfo connectionInfo = WebSocketUtil.getWebSocketOpenConnectionInfo(null,
                wsService, webSocketClient);
        if (throwable instanceof IOException) {
            WebSocketUtil.determineFailoverOrReconnect(connectionInfo, throwable, null);
        } else {
            logger.error("Error occurred: ", throwable);
            WebSocketUtil.countDownForHandshake(webSocketClient);
            WebSocketResourceDispatcher.dispatchOnError(connectionInfo, throwable);
        }
    }

    private void setFailoverWebSocketEndpoint(FailoverContext failoverConfig, ObjectValue webSocketClient,
                                              WebSocketConnection webSocketConnection) {
        if (failoverConfig.isConnectionMade()) {
            webSocketClient.set(WebSocketConstants.LISTENER_ID_FIELD, webSocketConnection.getChannelId());
        } else {
            WebSocketUtil.populateWebSocketEndpoint(webSocketConnection, webSocketClient);
        }
    }
}
