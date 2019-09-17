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

package org.ballerinalang.net.http;

import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.net.http.websocketclientendpoint.FailoverContext;
import org.ballerinalang.net.http.websocketclientendpoint.RetryContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;
import org.wso2.transport.http.netty.message.HttpCarbonResponse;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import static org.ballerinalang.net.http.HttpConstants.PROTOCOL_HTTP_PKG_ID;
import static org.ballerinalang.net.http.WebSocketConstants.CONNECTED_TO;
import static org.ballerinalang.net.http.WebSocketConstants.FAILOVER_CONFIG;
import static org.ballerinalang.net.http.WebSocketConstants.RETRY_CONFIG;
import static org.ballerinalang.net.http.WebSocketUtil.determineAction;
import static org.ballerinalang.net.http.WebSocketUtil.hasRetryConfig;

/**
 * The handshake listener for the failover client.
 *
 * @since 1.0.0
 */
public class WebSocketFailoverClientHandshakeListener extends ClientHandshakeConnectorListener {

    private final WebSocketService wsService;
    private final WebSocketFailoverClientListener clientConnectorListener;
    private final boolean readyOnConnect;
    private final ObjectValue webSocketClient;
    private CountDownLatch countDownLatch;
    private static final Logger logger = LoggerFactory.getLogger(WebSocketFailoverClientHandshakeListener.class);

    WebSocketFailoverClientHandshakeListener(ObjectValue webSocketClient, WebSocketService wsService,
                                    WebSocketFailoverClientListener clientConnectorListener, boolean readyOnConnect,
                                    CountDownLatch countDownLatch) {
        super(webSocketClient, wsService, clientConnectorListener, readyOnConnect, countDownLatch);
        this.webSocketClient = webSocketClient;
        this.wsService = wsService;
        this.clientConnectorListener = clientConnectorListener;
        this.readyOnConnect = readyOnConnect;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void onSuccess(WebSocketConnection webSocketConnection, HttpCarbonResponse carbonResponse) {
        webSocketClient.set(WebSocketConstants.CLIENT_RESPONSE_FIELD,
                HttpUtil.createResponseStruct(carbonResponse));
        ObjectValue webSocketConnector = BallerinaValues.createObjectValue(PROTOCOL_HTTP_PKG_ID,
                WebSocketConstants.WEBSOCKET_CONNECTOR);
        WebSocketOpenConnectionInfo connectionInfo = getWebSocketOpenConnectionInfo(webSocketConnection,
                webSocketConnector);
        WebSocketUtil.populateEndpoint(webSocketConnection, webSocketClient);
        clientConnectorListener.setConnectionInfo(connectionInfo);
        FailoverContext failoverConfig = (FailoverContext) webSocketClient.getNativeData(FAILOVER_CONFIG);
        setFailoverWebSocketEndpoint(failoverConfig, webSocketClient, webSocketConnection);
        failoverConfig.setFailoverFinished(false);
        // Read the next frame when readyOnConnect is true in first connection or after the first connection
        if (failoverConfig.isConnectionMade() || readyOnConnect) {
            webSocketConnection.readNextFrame();
        }
        // Following these are created for future connection
        // Check whether the config has retry config or not
        // It has retry config, set these variable to default variable
        if (hasRetryConfig(webSocketClient)) {
            ((RetryContext) webSocketClient.getNativeData(RETRY_CONFIG)).setReconnectAttempts(0);
        }
        int currentIndex = failoverConfig.getCurrentIndex();
        logger.info(CONNECTED_TO + failoverConfig.getTargetUrls().get(currentIndex));
        // Set failover context variable's value
        failoverConfig.setInitialIndex(currentIndex);
        failoverConfig.setConnectionMade();
        countDownLatch.countDown();
    }

    @Override
    public void onError(Throwable throwable, HttpCarbonResponse response) {
        if (response != null) {
            webSocketClient.set(WebSocketConstants.CLIENT_RESPONSE_FIELD, HttpUtil.createResponseStruct(response));
        }
        ObjectValue webSocketConnector = BallerinaValues.createObjectValue(PROTOCOL_HTTP_PKG_ID,
                WebSocketConstants.WEBSOCKET_CONNECTOR);
        WebSocketOpenConnectionInfo connectionInfo = getWebSocketOpenConnectionInfo(null,
                webSocketConnector);
        countDownLatch.countDown();
        if (throwable instanceof IOException) {
            determineAction(connectionInfo, throwable, null);
        } else {
            logger.error("Error occurred: ", throwable);
            WebSocketDispatcher.dispatchError(connectionInfo, throwable);
        }
    }

    private void setFailoverWebSocketEndpoint(FailoverContext failoverConfig, ObjectValue webSocketClient,
                                                      WebSocketConnection webSocketConnection) {
        if (failoverConfig.isConnectionMade()) {
            webSocketClient.set(WebSocketConstants.LISTENER_ID_FIELD, webSocketConnection.getChannelId());
        } else {
            WebSocketUtil.populateEndpoint(webSocketConnection, webSocketClient);
        }
    }
}
