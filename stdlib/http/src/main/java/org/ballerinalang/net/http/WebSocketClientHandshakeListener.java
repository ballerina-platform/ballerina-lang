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

import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.wso2.transport.http.netty.contract.websocket.ClientHandshakeListener;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;
import org.wso2.transport.http.netty.message.HttpCarbonResponse;

import java.io.PrintStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.ballerinalang.net.http.HttpConstants.PROTOCOL_PACKAGE_HTTP;
import static org.ballerinalang.net.http.WebSocketConstants.CLIENT_ENDPOINT_CONFIG;
import static org.ballerinalang.net.http.WebSocketConstants.FAILOVER_INTEVAL;
import static org.ballerinalang.net.http.WebSocketConstants.FAILOVER_WEBSOCKET_CLIENT;
import static org.ballerinalang.net.http.WebSocketConstants.RECONNECT_ATTEMPTS;
import static org.ballerinalang.net.http.WebSocketConstants.RETRY_CONFIG;
import static org.ballerinalang.net.http.WebSocketConstants.TARGETS_URLS;
import static org.ballerinalang.net.http.WebSocketConstants.TARGET_URL_INDEX;
import static org.ballerinalang.net.http.WebSocketUtil.getIntegerValue;
import static org.ballerinalang.net.http.WebSocketUtil.getRemoteUrl;
import static org.ballerinalang.net.http.WebSocketUtil.initialiseWebSocketConnection;
import static org.ballerinalang.net.http.WebSocketUtil.reconnect;
import static org.ballerinalang.net.http.WebSocketUtil.waitForTime;

/**
 * The handshake listener for the client.
 *
 * @since 0.983.1
 */
public class WebSocketClientHandshakeListener implements ClientHandshakeListener {

    private final WebSocketService wsService;
    private final WebSocketClientConnectorListener clientConnectorListener;
    private final boolean readyOnConnect;
    private final ObjectValue webSocketClient;
    private CountDownLatch countDownLatch;
    private static final PrintStream console = System.out;

    public WebSocketClientHandshakeListener(ObjectValue webSocketClient,
                                            WebSocketService wsService,
                                            WebSocketClientConnectorListener clientConnectorListener,
                                            boolean readyOnConnect, CountDownLatch countDownLatch) {
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
        WebSocketUtil.populateEndpoint(webSocketConnection, webSocketClient);
        clientConnectorListener.setConnectionInfo(connectionInfo);
        webSocketClient.set(WebSocketConstants.CLIENT_CONNECTOR_FIELD, webSocketConnector);
        if (readyOnConnect) {
            webSocketConnection.readNextFrame();
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
        MapValue<String, Object> clientEndpointConfig = (MapValue<String, Object>) webSocketClient.getMapValue(
                HttpConstants.CLIENT_ENDPOINT_CONFIG);
        int reconnetAttemptCount = 0;
        if (clientEndpointConfig.get(RETRY_CONFIG) != null) {
            reconnetAttemptCount = getIntegerValue(Long.valueOf(webSocketClient.
                    getNativeData(RECONNECT_ATTEMPTS).toString()));
        }
        if (webSocketClient.getType().getName().equalsIgnoreCase(FAILOVER_WEBSOCKET_CLIENT) &&
                reconnetAttemptCount == 0) {
            ArrayValue targets = clientEndpointConfig.getArrayValue(TARGETS_URLS);
            int failoverInterval =  getIntegerValue(Long.valueOf(clientEndpointConfig.get(FAILOVER_INTEVAL).
                    toString()));
            int targetsUrlIndex = getIntegerValue(Long.valueOf(webSocketClient.getNativeData(TARGET_URL_INDEX).
                    toString()));
            int size = targets.size();
            if (targetsUrlIndex < size) {
                webSocketClient.addNativeData(TARGET_URL_INDEX, targetsUrlIndex + 1);
                waitForTime(countDownLatch, failoverInterval, TimeUnit.MILLISECONDS);
                initialiseWebSocketConnection(targets.get(targetsUrlIndex).toString(), webSocketClient, wsService);
                countDownLatch.countDown();
            } else {
                console.println("Couldn't connect to the one of the server in the targets: " + targets);
                WebSocketDispatcher.dispatchError(connectionInfo, throwable);
            }
        } else if (webSocketClient.getMapValue(CLIENT_ENDPOINT_CONFIG).getMapValue(RETRY_CONFIG).size() > 0) {
            if (!reconnect(connectionInfo)) {
                console.println("Attempt maximum retry but couldn't connect to the server: " +
                        getRemoteUrl(connectionInfo));
                WebSocketDispatcher.dispatchError(connectionInfo, throwable);
            } else {
                WebSocketDispatcher.dispatchError(connectionInfo, throwable);
            }
        } else {
            WebSocketDispatcher.dispatchError(connectionInfo, throwable);
        }
    }
}
