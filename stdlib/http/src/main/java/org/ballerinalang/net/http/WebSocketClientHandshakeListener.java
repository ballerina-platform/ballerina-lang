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

import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.values.BString;
import org.wso2.transport.http.netty.contract.websocket.ClientHandshakeListener;
import org.wso2.transport.http.netty.contract.websocket.WebSocketClientConnector;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;
import org.wso2.transport.http.netty.message.HttpCarbonResponse;

import java.io.PrintStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.ballerinalang.net.http.HttpConstants.PROTOCOL_PACKAGE_HTTP;
import static org.ballerinalang.net.http.WebSocketConstants.CLIENT_CONNECTOR;
import static org.ballerinalang.net.http.WebSocketConstants.FAILOVER_INTEVAL;
import static org.ballerinalang.net.http.WebSocketConstants.FAILOVER_WEBSOCKET_CLIENT;
import static org.ballerinalang.net.http.WebSocketConstants.INDEX;
import static org.ballerinalang.net.http.WebSocketConstants.MAX_RETRY_COUNT;
import static org.ballerinalang.net.http.WebSocketConstants.MAX_RETRY_INTERVAL;
import static org.ballerinalang.net.http.WebSocketConstants.RECONNECTING;
import static org.ballerinalang.net.http.WebSocketConstants.RECONNECT_ATTEMPTS;
import static org.ballerinalang.net.http.WebSocketConstants.RECONNECT_INTERVAL;
import static org.ballerinalang.net.http.WebSocketConstants.RETRY_CONFIG;
import static org.ballerinalang.net.http.WebSocketConstants.RETRY_DECAY;
import static org.ballerinalang.net.http.WebSocketConstants.TARGETS_URLS;
import static org.ballerinalang.net.http.WebSocketConstants.WEBSOCKET_CLIENT;
import static org.ballerinalang.net.http.WebSocketUtil.reconnect;
import static org.ballerinalang.net.http.WebSocketUtil.setWebSocketConnection;

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
        if (webSocketClient.getType().getName().equalsIgnoreCase(FAILOVER_WEBSOCKET_CLIENT)) {
            ArrayValue targets = clientEndpointConfig.getArrayValue(TARGETS_URLS);
            long failoverInterval =  Long.valueOf(clientEndpointConfig.get(FAILOVER_INTEVAL).toString());
            long index = Long.valueOf(clientEndpointConfig.get(INDEX).toString());
            long size = targets.size();
            if (index < size) {
                ((MapValue<String, Object>) webSocketClient.getMapValue(HttpConstants.CLIENT_ENDPOINT_CONFIG))
                        .put(INDEX, index + 1);
                try {
                    countDownLatch.await(failoverInterval, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new BallerinaConnectorException("Error occurred: " + e.getMessage());
                }
                setWebSocketConnection(targets.get(index).toString(), webSocketClient, wsService);
                countDownLatch.countDown();
            } else {
                console.println("Couldn't connect to the one of the server in the targets: " + targets);
                WebSocketDispatcher.dispatchError(connectionInfo, throwable);
            }
        } else if (webSocketClient.getType().getName().equalsIgnoreCase(WEBSOCKET_CLIENT)) {
            MapValue<String, Object> reconnectConfig = (MapValue<String, Object>) clientEndpointConfig
                    .getMapValue(RETRY_CONFIG);
            WebSocketClientConnector clientConnector = (WebSocketClientConnector) clientEndpointConfig
                    .get(CLIENT_CONNECTOR);
            int reconnectInterval = Math.toIntExact(reconnectConfig.getIntValue(RECONNECT_INTERVAL));
            Double reconnectDecay = reconnectConfig.getFloatValue(RETRY_DECAY);
            int maxReconnectInterval = Math.toIntExact(reconnectConfig.getIntValue(MAX_RETRY_INTERVAL));
            int maxReconnectAttempts = Math.toIntExact(reconnectConfig.getIntValue(MAX_RETRY_COUNT));
            int reconnectAttempts = Integer.valueOf(reconnectConfig.get(RECONNECT_ATTEMPTS).toString());
            int timeout = (int) (reconnectInterval * Math.pow(reconnectDecay, reconnectAttempts));
            if (timeout > maxReconnectInterval) {
                maxReconnectInterval = timeout;
            }
            if (reconnectAttempts < maxReconnectAttempts) {
                console.println(RECONNECTING);
                ((MapValue<String, Object>) clientEndpointConfig.getMapValue(
                        HttpConstants.CLIENT_ENDPOINT_CONFIG)).getMapValue(RETRY_CONFIG).
                        addNativeData(RECONNECT_ATTEMPTS, new BString(String.valueOf(reconnectAttempts + 1)));
                reconnect(clientConnector, webSocketClient, maxReconnectInterval, wsService);
            } else if (reconnectAttempts == 0 && maxReconnectAttempts == 0) {
                console.println(RECONNECTING);
                reconnect(clientConnector, webSocketClient, maxReconnectInterval, wsService);
            } else {
                console.println("Couldn't connect to the server");
                WebSocketDispatcher.dispatchError(connectionInfo, throwable);
            }
        } else {
            WebSocketDispatcher.dispatchError(connectionInfo, throwable);
        }
    }
}
