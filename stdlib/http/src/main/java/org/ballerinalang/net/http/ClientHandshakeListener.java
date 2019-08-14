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

import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.net.http.websocketclientendpoint.RetryContext;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;
import org.wso2.transport.http.netty.message.HttpCarbonResponse;

import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.CountDownLatch;

import static org.ballerinalang.net.http.HttpConstants.PROTOCOL_PACKAGE_HTTP;
import static org.ballerinalang.net.http.WebSocketConstants.CLIENT_ENDPOINT_CONFIG;
import static org.ballerinalang.net.http.WebSocketConstants.CONNECTED_TO;
import static org.ballerinalang.net.http.WebSocketConstants.RETRY_CONFIG;
import static org.ballerinalang.net.http.WebSocketConstants.STATEMENT_FOR_RECONNECT;
import static org.ballerinalang.net.http.WebSocketUtil.doReconnect;
import static org.ballerinalang.net.http.WebSocketUtil.hasRetryConfig;

/**
 * The handshake listener for the client.
 */
public class ClientHandshakeListener extends WebSocketClientHandshakeListener {
    private final WebSocketService wsService;
    private final WebSocketClientConnectorListener clientConnectorListener;
    private final boolean readyOnConnect;
    private final ObjectValue webSocketClient;
    private CountDownLatch countDownLatch;
    private static final PrintStream console = System.out;

    public ClientHandshakeListener(ObjectValue webSocketClient, WebSocketService wsService,
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
        if (webSocketClient.getMapValue(CLIENT_ENDPOINT_CONFIG).getMapValue(RETRY_CONFIG) == null) {
            if (readyOnConnect) {
                webSocketConnection.readNextFrame();
            }
            WebSocketUtil.populateEndpoint(webSocketConnection, webSocketClient);
            console.println(CONNECTED_TO + webSocketClient.getStringValue(WebSocketConstants.
                    CLIENT_URL_CONFIG));
        } else {
            RetryContext retryConfig = (RetryContext) webSocketClient.getNativeData(RETRY_CONFIG);
            console.println(CONNECTED_TO + webSocketClient.getStringValue(WebSocketConstants.
                    CLIENT_URL_CONFIG));
            if (retryConfig.getConnectionState()) {
                webSocketClient.set(WebSocketConstants.LISTENER_ID_FIELD, webSocketConnection.getChannelId());
            } else {
                WebSocketUtil.populateEndpoint(webSocketConnection, webSocketClient);
            }
            if (retryConfig.getConnectionState() || readyOnConnect) {
                webSocketConnection.readNextFrame();
            }
            ((RetryContext) webSocketClient.getNativeData(RETRY_CONFIG)).setConnectionState();
            ((RetryContext) webSocketClient.getNativeData(RETRY_CONFIG)).setReconnectAttempts(0);
        }
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
        if (throwable instanceof IOException) {
            if (hasRetryConfig(webSocketClient)) {
                if (!doReconnect(connectionInfo)) {
                    WebSocketDispatcher.dispatchError(connectionInfo, throwable);
                    console.println(STATEMENT_FOR_RECONNECT +
                            webSocketClient.getStringValue(WebSocketConstants.CLIENT_URL_CONFIG));
                }
            } else {
                WebSocketDispatcher.dispatchError(connectionInfo, throwable);
            }
        } else {
            console.println("A connection has some issue that needs to fix.");
            WebSocketDispatcher.dispatchError(connectionInfo, throwable);
        }
    }
}
