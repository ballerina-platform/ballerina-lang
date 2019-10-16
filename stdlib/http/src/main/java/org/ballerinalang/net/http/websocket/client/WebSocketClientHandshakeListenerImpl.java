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

package org.ballerinalang.net.http.websocket.client;

import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.net.http.websocket.WebSocketConstants;
import org.ballerinalang.net.http.websocket.WebSocketResourceDispatcher;
import org.ballerinalang.net.http.websocket.WebSocketUtil;
import org.ballerinalang.net.http.websocket.server.WebSocketOpenConnectionInfo;
import org.ballerinalang.net.http.websocket.server.WebSocketService;
import org.wso2.transport.http.netty.contract.websocket.ClientHandshakeListener;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;
import org.wso2.transport.http.netty.message.HttpCarbonResponse;

import static org.ballerinalang.net.http.HttpConstants.PROTOCOL_HTTP_PKG_ID;
import static org.ballerinalang.net.http.websocket.WebSocketUtil.countDownForHandshake;

/**
 * The handshake listener for the client.
 *
 * @since 0.983.1
 */
public class WebSocketClientHandshakeListenerImpl implements ClientHandshakeListener {

    private final WebSocketService wsService;
    private final WebSocketClientListenerImpl clientConnectorListener;
    private final boolean readyOnConnect;
    private final ObjectValue webSocketClient;

    public WebSocketClientHandshakeListenerImpl(ObjectValue webSocketClient,
                                                WebSocketService wsService,
                                                WebSocketClientListenerImpl clientConnectorListener,
                                                boolean readyOnConnect) {
        this.webSocketClient = webSocketClient;
        this.wsService = wsService;
        this.clientConnectorListener = clientConnectorListener;
        this.readyOnConnect = readyOnConnect;
    }

    @Override
    public void onSuccess(WebSocketConnection webSocketConnection, HttpCarbonResponse carbonResponse) {
        //using only one service endpoint in the client as there can be only one connection.
        webSocketClient.set(WebSocketConstants.CLIENT_RESPONSE_FIELD,
                HttpUtil.createResponseStruct(carbonResponse));
        ObjectValue webSocketConnector = BallerinaValues.createObjectValue(PROTOCOL_HTTP_PKG_ID,
                WebSocketConstants.WEBSOCKET_CONNECTOR);
        WebSocketOpenConnectionInfo connectionInfo = getWebSocketOpenConnectionInfo(webSocketConnection,
                webSocketConnector);
        WebSocketUtil.populateWebSocketCaller(webSocketConnection, webSocketClient);
        clientConnectorListener.setConnectionInfo(connectionInfo);
        if (readyOnConnect) {
            webSocketConnection.readNextFrame();
        }
        countDownForHandshake(webSocketClient);
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
        countDownForHandshake(webSocketClient);
        WebSocketResourceDispatcher.dispatchOnError(connectionInfo, throwable);
    }

    WebSocketOpenConnectionInfo getWebSocketOpenConnectionInfo(WebSocketConnection webSocketConnection,
                                                               ObjectValue webSocketConnector) {
        WebSocketOpenConnectionInfo connectionInfo = new WebSocketOpenConnectionInfo(
                wsService, webSocketConnection, webSocketClient);
        webSocketConnector.addNativeData(WebSocketConstants.NATIVE_DATA_WEBSOCKET_CONNECTION_INFO, connectionInfo);
        webSocketClient.set(WebSocketConstants.CLIENT_CONNECTOR_FIELD, webSocketConnector);
        return connectionInfo;
    }
}
