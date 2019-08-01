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
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.ObjectValue;
import org.wso2.transport.http.netty.contract.websocket.ClientHandshakeListener;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;
import org.wso2.transport.http.netty.message.HttpCarbonResponse;

import java.io.PrintStream;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.ballerinalang.net.http.HttpConstants.PROTOCOL_PACKAGE_HTTP;
import static org.ballerinalang.net.http.WebSocketConstants.CLIENT_ENDPOINT_CONFIG;
import static org.ballerinalang.net.http.WebSocketConstants.CONNECTED_SERVER_URLS_INDEX;
import static org.ballerinalang.net.http.WebSocketConstants.CONNECTED_TO;
import static org.ballerinalang.net.http.WebSocketConstants.FAILOVER_INTEVAL;
import static org.ballerinalang.net.http.WebSocketConstants.FAILOVER_WEBSOCKET_CLIENT;
import static org.ballerinalang.net.http.WebSocketConstants.IS_CONNECTION_MADE;
import static org.ballerinalang.net.http.WebSocketConstants.NO_OF_RECONNECT_ATTEMPTS;
import static org.ballerinalang.net.http.WebSocketConstants.RETRY_CONFIG;
import static org.ballerinalang.net.http.WebSocketConstants.STATEMENT_FOR_FAILOVDER_RECONNECT;
import static org.ballerinalang.net.http.WebSocketConstants.STATEMENT_FOR_FAILOVER;
import static org.ballerinalang.net.http.WebSocketConstants.STATEMENT_FOR_RECONNECT;
import static org.ballerinalang.net.http.WebSocketConstants.SUB_TARGET_URLS;
import static org.ballerinalang.net.http.WebSocketConstants.SUB_TARGET_URLS_INDEX;
import static org.ballerinalang.net.http.WebSocketConstants.TARGET_URLS;
import static org.ballerinalang.net.http.WebSocketConstants.TARGET_URL_INDEX;
import static org.ballerinalang.net.http.WebSocketUtil.getIntegerValue;
import static org.ballerinalang.net.http.WebSocketUtil.initialiseWebSocketConnection;
import static org.ballerinalang.net.http.WebSocketUtil.reconnect;
import static org.ballerinalang.net.http.WebSocketUtil.reconnectForFailoverClient;
import static org.ballerinalang.net.http.WebSocketUtil.setSubTargetUrlList;
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
        int targetUrlsIndex = 0;
        ArrayValue targetUrls = null;
        if (webSocketClient.getType().getName().equalsIgnoreCase(FAILOVER_WEBSOCKET_CLIENT)) {
            int index = (int) webSocketClient.getNativeData(TARGET_URL_INDEX);
            if (index > 0) {
                targetUrlsIndex = (int) webSocketClient.getNativeData(TARGET_URL_INDEX) - 1;
            }
            targetUrls = (ArrayValue) webSocketClient.getNativeData(TARGET_URLS);
            if (webSocketClient.getNativeData(SUB_TARGET_URLS) != null) {
                List subTargetUrls = (List) webSocketClient.getNativeData(SUB_TARGET_URLS);
                int subtargetUrlsIndex = (int) webSocketClient.getNativeData(SUB_TARGET_URLS_INDEX);
                String url = subTargetUrls.get(subtargetUrlsIndex - 1).toString();
                for (int i = 0; i < targetUrls.size(); i++) {
                    if (targetUrls.get(i) == url) {
                        targetUrlsIndex = i;
                    }
                }
            }
            webSocketClient.addNativeData(CONNECTED_SERVER_URLS_INDEX, targetUrlsIndex);
            setSubTargetUrlList(webSocketClient, targetUrlsIndex);
        }
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
        if (webSocketClient.getNativeData(IS_CONNECTION_MADE) == null) {
            if (readyOnConnect) {
                webSocketConnection.readNextFrame();
            }
        } else {
            webSocketConnection.readNextFrame();
        }
        webSocketClient.addNativeData(IS_CONNECTION_MADE, true);
        countDownLatch.countDown();
        if (!webSocketClient.getType().getName().equalsIgnoreCase(FAILOVER_WEBSOCKET_CLIENT) &&
                webSocketClient.getMapValue(CLIENT_ENDPOINT_CONFIG).getMapValue(RETRY_CONFIG) != null) {
            webSocketClient.addNativeData(NO_OF_RECONNECT_ATTEMPTS, 0);
            console.println(CONNECTED_TO + webSocketClient.
                    getStringValue(WebSocketConstants.CLIENT_URL_CONFIG));
        }
        if (webSocketClient.getType().getName().equalsIgnoreCase(FAILOVER_WEBSOCKET_CLIENT)) {
            if (targetUrls != null) {
                console.println(CONNECTED_TO + targetUrls.get(targetUrlsIndex).toString());
            }
            if (webSocketClient.getMapValue(CLIENT_ENDPOINT_CONFIG).getMapValue(RETRY_CONFIG) != null) {
                webSocketClient.addNativeData(NO_OF_RECONNECT_ATTEMPTS, 0);
            }
        }
    }

    @Override
    public void onError(Throwable throwable, HttpCarbonResponse response) {
        console.println("HAnshake" + throwable.getMessage());
        if (response != null) {
            webSocketClient.set(WebSocketConstants.CLIENT_RESPONSE_FIELD, HttpUtil.createResponseStruct(response));
        }
        ObjectValue webSocketConnector = BallerinaValues.createObjectValue(PROTOCOL_PACKAGE_HTTP,
                                                                           WebSocketConstants.WEBSOCKET_CONNECTOR);
        WebSocketOpenConnectionInfo connectionInfo = new WebSocketOpenConnectionInfo(
                wsService, null, webSocketClient);
        webSocketConnector.addNativeData(WebSocketConstants.NATIVE_DATA_WEBSOCKET_CONNECTION_INFO, connectionInfo);
        countDownLatch.countDown();
        boolean isConnectionMade;
        if (webSocketClient.getNativeData(IS_CONNECTION_MADE) == null) {
            isConnectionMade = false;
        } else {
            isConnectionMade = (boolean) webSocketClient.getNativeData(IS_CONNECTION_MADE);
        }
        MapValueImpl clientConfig = webSocketClient.getMapValue(CLIENT_ENDPOINT_CONFIG);
        ArrayValue targets = (ArrayValue) webSocketClient.getNativeData(TARGET_URLS);
        if (webSocketClient.getType().getName().equalsIgnoreCase(FAILOVER_WEBSOCKET_CLIENT) &&
                !isConnectionMade) {
            int failoverInterval = getIntegerValue(Long.valueOf(clientConfig.get(FAILOVER_INTEVAL).
                    toString()));
            int targetsUrlIndex = getIntegerValue(Long.valueOf(webSocketClient.getNativeData(TARGET_URL_INDEX).
                    toString()));
            int size = targets.size();
            if (targetsUrlIndex < size) {
                webSocketClient.addNativeData(TARGET_URL_INDEX, targetsUrlIndex + 1);
                setcountDownLatch(failoverInterval);
                initialiseWebSocketConnection(targets.get(targetsUrlIndex).toString(), webSocketClient, wsService);
            } else {
                console.println(STATEMENT_FOR_FAILOVER + targets);
                WebSocketDispatcher.dispatchError(connectionInfo, throwable);
            }
        } else if (webSocketClient.getType().getName().equalsIgnoreCase(FAILOVER_WEBSOCKET_CLIENT) &&
                clientConfig.getMapValue(RETRY_CONFIG) != null && isConnectionMade) {
            if (!reconnectForFailoverClient(connectionInfo)) {
                console.println(STATEMENT_FOR_FAILOVDER_RECONNECT + webSocketClient.getNativeData(SUB_TARGET_URLS));
                WebSocketDispatcher.dispatchError(connectionInfo, throwable);
            }
        } else if (webSocketClient.getType().getName().equalsIgnoreCase(FAILOVER_WEBSOCKET_CLIENT) &&
                clientConfig.getMapValue(RETRY_CONFIG) == null && isConnectionMade) {
            int failoverInterval = getIntegerValue(Long.valueOf(clientConfig.get(FAILOVER_INTEVAL).
                    toString()));
            int subTargetsUrlIndex = (int) webSocketClient.getNativeData(SUB_TARGET_URLS_INDEX);
            List subTargetUrls = (List) webSocketClient.getNativeData(SUB_TARGET_URLS);
            if (subTargetsUrlIndex < subTargetUrls.size()) {
                webSocketClient.addNativeData(SUB_TARGET_URLS_INDEX, subTargetsUrlIndex + 1);
                setcountDownLatch(failoverInterval);
                initialiseWebSocketConnection(subTargetUrls.get(subTargetsUrlIndex).toString(), webSocketClient,
                        wsService);
            } else {
                WebSocketDispatcher.dispatchError(connectionInfo, throwable);
                console.println(STATEMENT_FOR_FAILOVER + targets);
            }
        } else if (!webSocketClient.getType().getName().equalsIgnoreCase(FAILOVER_WEBSOCKET_CLIENT) &&
                clientConfig.getMapValue(RETRY_CONFIG) != null) {
            if (!reconnect(connectionInfo)) {
                WebSocketDispatcher.dispatchError(connectionInfo, throwable);
                console.println(STATEMENT_FOR_RECONNECT +
                        webSocketClient.getStringValue(WebSocketConstants.CLIENT_URL_CONFIG));
            }
        } else {
            WebSocketDispatcher.dispatchError(connectionInfo, throwable);
        }
    }
}
