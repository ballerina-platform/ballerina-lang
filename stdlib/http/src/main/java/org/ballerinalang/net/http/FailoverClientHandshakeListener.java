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
 * specif ic language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.net.http;

import io.netty.util.concurrent.BlockingOperationException;
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.net.http.websocketclientendpoint.FailoverContext;
import org.ballerinalang.net.http.websocketclientendpoint.RetryContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;
import org.wso2.transport.http.netty.message.HttpCarbonResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import static org.ballerinalang.net.http.HttpConstants.PROTOCOL_PACKAGE_HTTP;
import static org.ballerinalang.net.http.WebSocketConstants.CONNECTED_TO;
import static org.ballerinalang.net.http.WebSocketConstants.FAILOVER_CONFIG;
import static org.ballerinalang.net.http.WebSocketConstants.RETRY_CONFIG;
import static org.ballerinalang.net.http.WebSocketUtil.doAction;
import static org.ballerinalang.net.http.WebSocketUtil.hasRetryConfig;

/**
 * The handshake listener for the failover client.
 */
public class FailoverClientHandshakeListener extends WebSocketClientHandshakeListener {
    private final WebSocketService wsService;
    private final WebSocketClientConnectorListener clientConnectorListener;
    private final boolean readyOnConnect;
    private final ObjectValue webSocketClient;
    private CountDownLatch countDownLatch;
    private static final Logger logger = LoggerFactory.getLogger(FailoverClientHandshakeListener.class);

    FailoverClientHandshakeListener(ObjectValue webSocketClient, WebSocketService wsService,
                                    WebSocketClientConnectorListener clientConnectorListener, boolean readyOnConnect,
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
        //using only one service endpoint in the client as there can be only one connection.
        webSocketClient.set(WebSocketConstants.CLIENT_RESPONSE_FIELD,
                HttpUtil.createResponseStruct(carbonResponse));
        ObjectValue webSocketConnector = BallerinaValues.createObjectValue(PROTOCOL_PACKAGE_HTTP,
                WebSocketConstants.WEBSOCKET_CONNECTOR);
        WebSocketOpenConnectionInfo connectionInfo = new WebSocketOpenConnectionInfo(
                wsService, webSocketConnection, webSocketClient);
        webSocketConnector.addNativeData(WebSocketConstants.NATIVE_DATA_WEBSOCKET_CONNECTION_INFO, connectionInfo);
        clientConnectorListener.setConnectionInfo(connectionInfo);
        webSocketClient.set(WebSocketConstants.CLIENT_CONNECTOR_FIELD, webSocketConnector);
        FailoverContext failoverConfig = (FailoverContext) webSocketClient.getNativeData(FAILOVER_CONFIG);
        if (failoverConfig.isConnectionMade()) {
            webSocketClient.set(WebSocketConstants.LISTENER_ID_FIELD, webSocketConnection.getChannelId());
        } else {
            WebSocketUtil.populateEndpoint(webSocketConnection, webSocketClient);
        }
        // Read the next frame when readyOnConnect is true in first connection or after the first connection
        if (failoverConfig.isConnectionMade() || readyOnConnect) {
            webSocketConnection.readNextFrame();
        }
        // Following these are created for future connection
        // Check whether the config has retry config or not
        // It has retry config, set these variable to default variable
        FailoverContext failoverContext = ((FailoverContext) webSocketClient.getNativeData(FAILOVER_CONFIG));
        if (hasRetryConfig(webSocketClient)) {
            ((RetryContext) webSocketClient.getNativeData(RETRY_CONFIG)).setReconnectAttempts(0);
            failoverContext.setFinishedFailover(false);
        }
        ArrayList targets = failoverConfig.getTargetUrls();
        int currentIndex = failoverConfig.getCurrentIndex();
        logger.info(CONNECTED_TO + targets.get(currentIndex).toString());
        // Set failover context variable's value
        failoverContext.setInitialIndex(currentIndex);
        failoverContext.setConnectionMade();
        countDownLatch.countDown();
    }

    @Override
    public void onError(Throwable throwable, HttpCarbonResponse response) {
        if (response != null) {
            webSocketClient.set(WebSocketConstants.CLIENT_RESPONSE_FIELD, HttpUtil.createResponseStruct(response));
        }
        ObjectValue webSocketConnector = BallerinaValues.createObjectValue(PROTOCOL_PACKAGE_HTTP,
                WebSocketConstants.WEBSOCKET_CONNECTOR);
        WebSocketOpenConnectionInfo connectionInfo = new WebSocketOpenConnectionInfo(
                wsService, null, webSocketClient);
        webSocketConnector.addNativeData(WebSocketConstants.NATIVE_DATA_WEBSOCKET_CONNECTION_INFO, connectionInfo);
        countDownLatch.countDown();
        if (throwable instanceof IOException || throwable instanceof BlockingOperationException) {
            doAction(connectionInfo, throwable, null);
        } else {
            logger.info("A connection has some issue that needs to fix.");
            WebSocketDispatcher.dispatchError(connectionInfo, throwable);
        }
    }
}
