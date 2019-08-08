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
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.net.http.websocketclientendpoint.FailoverClientConnectorConfig;
import org.ballerinalang.net.http.websocketclientendpoint.RetryConnectorConfig;
import org.wso2.transport.http.netty.contract.websocket.ClientHandshakeListener;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;
import org.wso2.transport.http.netty.message.HttpCarbonResponse;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import static org.ballerinalang.net.http.HttpConstants.PROTOCOL_PACKAGE_HTTP;
import static org.ballerinalang.net.http.WebSocketConstants.CLIENT_ENDPOINT_CONFIG;
import static org.ballerinalang.net.http.WebSocketConstants.CONNECTED_TO;
import static org.ballerinalang.net.http.WebSocketConstants.FAILOVER_CONFIG;
import static org.ballerinalang.net.http.WebSocketConstants.FAILOVER_WEBSOCKET_CLIENT;
import static org.ballerinalang.net.http.WebSocketConstants.RETRY_CONFIG;
import static org.ballerinalang.net.http.WebSocketConstants.STATEMENT_FOR_FAILOVDER_RECONNECT;
import static org.ballerinalang.net.http.WebSocketConstants.STATEMENT_FOR_FAILOVER;
import static org.ballerinalang.net.http.WebSocketConstants.STATEMENT_FOR_RECONNECT;
import static org.ballerinalang.net.http.WebSocketUtil.initialiseWebSocketConnection;
import static org.ballerinalang.net.http.WebSocketUtil.reconnect;
import static org.ballerinalang.net.http.WebSocketUtil.reconnectForFailoverClient;
import static org.ballerinalang.net.http.WebSocketUtil.setcountDownLatch;

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
        RetryConnectorConfig retryConnectorConfig = (RetryConnectorConfig) webSocketClient.
                getNativeData(RETRY_CONFIG);
        FailoverClientConnectorConfig failoverClientConnectorConfig = (FailoverClientConnectorConfig)
                webSocketClient.getNativeData(FAILOVER_CONFIG);;
        webSocketClient.set(WebSocketConstants.CLIENT_RESPONSE_FIELD,
                HttpUtil.createResponseStruct(carbonResponse));
        ObjectValue webSocketConnector = BallerinaValues.createObjectValue(PROTOCOL_PACKAGE_HTTP,
                WebSocketConstants.WEBSOCKET_CONNECTOR);
        WebSocketOpenConnectionInfo connectionInfo = new WebSocketOpenConnectionInfo(
                wsService, webSocketConnection, webSocketClient);
        webSocketConnector.addNativeData(WebSocketConstants.NATIVE_DATA_WEBSOCKET_CONNECTION_INFO,
                connectionInfo);
        clientConnectorListener.setConnectionInfo(connectionInfo);
        webSocketClient.set(WebSocketConstants.CLIENT_CONNECTOR_FIELD, webSocketConnector);
        if (webSocketClient.getMapValue(CLIENT_ENDPOINT_CONFIG).getMapValue(RETRY_CONFIG) == null) {
            if (webSocketClient.getType().getName().equalsIgnoreCase(FAILOVER_WEBSOCKET_CLIENT)) {
                if (!failoverClientConnectorConfig.getConnectionState()) {
                    WebSocketUtil.populateEndpoint(webSocketConnection, webSocketClient);
                } else {
                    webSocketClient.set(WebSocketConstants.LISTENER_ID_FIELD, webSocketConnection.getChannelId());
                }
                if (failoverClientConnectorConfig.getConnectionState() && readyOnConnect) {
                    webSocketConnection.readNextFrame();
                }
                ArrayList targets = failoverClientConnectorConfig.getTargetUrls();
                int currentIndex = failoverClientConnectorConfig.getCurrentIndex();
                console.println(CONNECTED_TO + targets.get(currentIndex).toString());
                failoverClientConnectorConfig.setConnectionState();
                if (((FailoverClientConnectorConfig) webSocketClient.getNativeData(FAILOVER_CONFIG)).
                        getOmittedUrlIndex() != -1) {
                    ((FailoverClientConnectorConfig) webSocketClient.getNativeData(FAILOVER_CONFIG)).
                            setOmittedUrlIndex(-1);
                }
                ((FailoverClientConnectorConfig) webSocketClient.getNativeData(FAILOVER_CONFIG)).
                        setInitialIndex(currentIndex);
            } else {
                WebSocketUtil.populateEndpoint(webSocketConnection, webSocketClient);
                console.println(CONNECTED_TO + webSocketClient.getStringValue(WebSocketConstants.
                        CLIENT_URL_CONFIG));
            }
            ((FailoverClientConnectorConfig) webSocketClient.getNativeData(FAILOVER_CONFIG)).
                    setConnectionState();
        } else {
            if (webSocketClient.getType().getName().equalsIgnoreCase(FAILOVER_WEBSOCKET_CLIENT)) {
                int currentIndex = retryConnectorConfig.getCurrentIndex();
                ArrayList targets = retryConnectorConfig.getTargetUrls();
                ((RetryConnectorConfig) webSocketClient.getNativeData(RETRY_CONFIG)).
                        setInitialIndex(currentIndex);
                console.println(CONNECTED_TO + targets.get(currentIndex).toString());
            } else {
                console.println(CONNECTED_TO + webSocketClient.getStringValue(WebSocketConstants.
                        CLIENT_URL_CONFIG));
            }
            if (!retryConnectorConfig.getConnectionState()) {
                WebSocketUtil.populateEndpoint(webSocketConnection, webSocketClient);
            } else {
                webSocketClient.set(WebSocketConstants.LISTENER_ID_FIELD, webSocketConnection.getChannelId());
            }
            if (retryConnectorConfig.getConnectionState() && readyOnConnect) {
                webSocketConnection.readNextFrame();
            }
            ((RetryConnectorConfig) webSocketClient.getNativeData(RETRY_CONFIG)).setConnectionState();
            ((RetryConnectorConfig) webSocketClient.getNativeData(RETRY_CONFIG)).setReconnectAttempts(0);
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
        MapValueImpl clientConfig = webSocketClient.getMapValue(CLIENT_ENDPOINT_CONFIG);
        if (throwable.getMessage().contains("Connection refused") || throwable.getMessage().
                contains("Unexpected error") || throwable.getMessage().contains("incomplete")) {
            if (webSocketClient.getType().getName().equalsIgnoreCase(FAILOVER_WEBSOCKET_CLIENT)) {
                FailoverClientConnectorConfig failoverClientConnectorConfig = (FailoverClientConnectorConfig)
                        webSocketClient.getNativeData(FAILOVER_CONFIG);
                ArrayList targets = failoverClientConnectorConfig.getTargetUrls();
                if (clientConfig.getMapValue(RETRY_CONFIG) == null) {
                    int failoverInterval = failoverClientConnectorConfig.getFailoverInterval();
                    int currentIndex = failoverClientConnectorConfig.getCurrentIndex();
                    currentIndex++;
                    if (currentIndex == targets.size()) {
                        currentIndex = 0;
                    }
                    if (currentIndex != ((FailoverClientConnectorConfig) webSocketClient.
                            getNativeData(FAILOVER_CONFIG)).getInitialIndex()) {
                        ((FailoverClientConnectorConfig) webSocketClient.getNativeData(FAILOVER_CONFIG)).
                                setCurrentIndex(currentIndex);
                        setcountDownLatch(failoverInterval);
                        initialiseWebSocketConnection(targets.get(currentIndex).toString(), webSocketClient,
                                wsService);
                    } else {
                        ((FailoverClientConnectorConfig) webSocketClient.getNativeData(FAILOVER_CONFIG)).
                                setCurrentIndex(0);
                        console.println("\n" + STATEMENT_FOR_FAILOVER + targets + "\n");
                        WebSocketDispatcher.dispatchError(connectionInfo, throwable);
                    }
                } else {
                    if (!reconnectForFailoverClient(connectionInfo)) {
                        console.println("\n" + STATEMENT_FOR_FAILOVDER_RECONNECT + targets + "\n");
                        WebSocketDispatcher.dispatchError(connectionInfo, throwable);
                    }
                }
            } else if (!webSocketClient.getType().getName().equalsIgnoreCase(FAILOVER_WEBSOCKET_CLIENT) &&
                    clientConfig.getMapValue(RETRY_CONFIG) != null) {
                if (!reconnect(connectionInfo)) {
                    WebSocketDispatcher.dispatchError(connectionInfo, throwable);
                    console.println("\n" + STATEMENT_FOR_RECONNECT +
                            webSocketClient.getStringValue(WebSocketConstants.CLIENT_URL_CONFIG) + "\n");
                }
            } else {
                WebSocketDispatcher.dispatchError(connectionInfo, throwable);
            }
        } else {
            WebSocketDispatcher.dispatchError(connectionInfo, throwable);
        }
    }
}
