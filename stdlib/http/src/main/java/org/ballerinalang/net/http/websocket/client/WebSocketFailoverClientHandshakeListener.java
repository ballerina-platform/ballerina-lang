/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.net.http.websocket.client;

import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.net.http.websocket.WebSocketConstants;
import org.ballerinalang.net.http.websocket.WebSocketService;
import org.ballerinalang.net.http.websocket.WebSocketUtil;
import org.ballerinalang.net.http.websocket.observability.WebSocketObservabilityUtil;
import org.ballerinalang.net.http.websocket.server.WebSocketConnectionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.websocket.ClientHandshakeListener;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;
import org.wso2.transport.http.netty.message.HttpCarbonResponse;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * The handshake listener of the failover client.
 *
 * @since 1.2.0
 */
public class WebSocketFailoverClientHandshakeListener implements ClientHandshakeListener {

    private final WebSocketService wsService;
    private final WebSocketFailoverClientListener clientConnectorListener;
    private final boolean readyOnConnect;
    private final ObjectValue webSocketClient;
    private final CountDownLatch countDownLatch;
    private final FailoverContext failoverConfig;

    private static final Logger logger = LoggerFactory.getLogger(WebSocketFailoverClientHandshakeListener.class);

    public WebSocketFailoverClientHandshakeListener(ObjectValue webSocketClient, WebSocketService wsService,
                                                    WebSocketFailoverClientListener clientConnectorListener,
                                                    boolean readyOnConnect, CountDownLatch countDownLatch,
                                                    FailoverContext failoverConfig) {
        this.webSocketClient = webSocketClient;
        this.wsService = wsService;
        this.clientConnectorListener = clientConnectorListener;
        this.readyOnConnect = readyOnConnect;
        this.countDownLatch = countDownLatch;
        this.failoverConfig = failoverConfig;
    }

    @Override
    public void onSuccess(WebSocketConnection webSocketConnection, HttpCarbonResponse carbonResponse) {
        ObjectValue webSocketConnector;
        if (!failoverConfig.isFirstConnectionMadeSuccessfully()) {
            webSocketConnector = WebSocketUtil.createWebSocketConnector(readyOnConnect);
        } else {
            webSocketConnector = (ObjectValue) webSocketClient.get(WebSocketConstants.CLIENT_CONNECTOR_FIELD);

        }
        setFailoverWebSocketEndpoint(carbonResponse, webSocketClient, webSocketConnection, failoverConfig);
        WebSocketConnectionInfo connectionInfo = WebSocketUtil.getWebSocketOpenConnectionInfo(webSocketConnection,
                webSocketConnector,
                webSocketClient, wsService);
        clientConnectorListener.setConnectionInfo(connectionInfo);
        // Reads the next frame when readyOnConnect is true or isReady is true
        WebSocketUtil.readNextFrame(readyOnConnect, webSocketClient, webSocketConnection);
        countDownLatch.countDown();
        // Calls the countDown() to initial connection's countDown latch
        WebSocketUtil.countDownForHandshake(webSocketClient);
        WebSocketObservabilityUtil.observeConnection(connectionInfo);
        // Following these are created for future connection
        int currentIndex = failoverConfig.getCurrentIndex();
        logger.debug(WebSocketConstants.LOG_MESSAGE, WebSocketConstants.CONNECTED_TO,
                failoverConfig.getTargetUrls().get(currentIndex));
        // Sets failover context variable's value
        failoverConfig.setInitialIndex(currentIndex);
        if (!failoverConfig.isFirstConnectionMadeSuccessfully()) {
            failoverConfig.setFirstConnectionMadeSuccessfully();
        }
    }

    @Override
    public void onError(Throwable throwable, HttpCarbonResponse response) {
        WebSocketConnectionInfo connectionInfo = WebSocketUtil.getConnectionInfoForOnError(response, webSocketClient,
                wsService, countDownLatch);
        // When the connection is lost, do the failover to the remaining server URLs
        if (!(throwable instanceof IOException && WebSocketUtil.failover(connectionInfo))) {
            WebSocketUtil.dispatchOnError(connectionInfo, throwable);
        }
    }

    private void setFailoverWebSocketEndpoint(HttpCarbonResponse carbonResponse, ObjectValue webSocketClient,
                                              WebSocketConnection webSocketConnection,
                                              FailoverContext failoverConfig) {
        webSocketClient.set(WebSocketConstants.CLIENT_RESPONSE_FIELD, HttpUtil.createResponseStruct(carbonResponse));
        if (failoverConfig.isFirstConnectionMadeSuccessfully()) {
            webSocketClient.set(WebSocketConstants.LISTENER_ID_FIELD, webSocketConnection.getChannelId());
        } else {
            WebSocketUtil.populateWebSocketEndpoint(webSocketConnection, webSocketClient);
        }
    }
}
