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

import org.ballerinalang.bre.Context;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.wso2.transport.http.netty.contract.websocket.ClientHandshakeListener;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;
import org.wso2.transport.http.netty.message.HttpCarbonResponse;

import java.util.concurrent.CountDownLatch;

import static org.ballerinalang.net.http.HttpConstants.PROTOCOL_PACKAGE_HTTP;

/**
 * The handshake listener for the client.
 *
 * @since 0.983.1
 */
public class WebSocketClientHandshakeListener implements ClientHandshakeListener {

    private final Context context;
    private final WebSocketService wsService;
    private final WebSocketClientConnectorListener clientConnectorListener;
    private final boolean readyOnConnect;
    private CountDownLatch countDownLatch;

    public WebSocketClientHandshakeListener(Context context, WebSocketService wsService,
                                            WebSocketClientConnectorListener clientConnectorListener,
                                            boolean readyOnConnect, CountDownLatch countDownLatch) {
        this.context = context;
        this.wsService = wsService;
        this.clientConnectorListener = clientConnectorListener;
        this.readyOnConnect = readyOnConnect;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void onSuccess(WebSocketConnection webSocketConnection, HttpCarbonResponse carbonResponse) {
        //using only one service endpoint in the client as there can be only one connection.
        BMap<String, BValue> webSocketClientEndpoint = ((BMap<String, BValue>) context.getRefArgument(0));
        webSocketClientEndpoint.put(WebSocketConstants.CLIENT_RESPONSE_FIELD,
                                    HttpUtil.createResponseStruct(context, carbonResponse));
        BMap<String, BValue> webSocketConnector = BLangConnectorSPIUtil.createObject(
                context, PROTOCOL_PACKAGE_HTTP, WebSocketConstants.WEBSOCKET_CONNECTOR);
        WebSocketOpenConnectionInfo connectionInfo = new WebSocketOpenConnectionInfo(
                wsService, webSocketConnection, webSocketClientEndpoint, context);
        webSocketConnector.addNativeData(WebSocketConstants.NATIVE_DATA_WEBSOCKET_CONNECTION_INFO, connectionInfo);
        WebSocketUtil.populateEndpoint(webSocketConnection, webSocketClientEndpoint);
        clientConnectorListener.setConnectionInfo(connectionInfo);
        webSocketClientEndpoint.put(WebSocketConstants.CLIENT_CONNECTOR_FIELD, webSocketConnector);
        context.setReturnValues();
        if (readyOnConnect) {
            webSocketConnection.readNextFrame();
        }
        countDownLatch.countDown();
    }

    @Override
    public void onError(Throwable throwable, HttpCarbonResponse response) {
        BMap<String, BValue> webSocketClientEndpoint = ((BMap<String, BValue>) context.getRefArgument(0));
        if (response != null) {
            webSocketClientEndpoint.put(WebSocketConstants.CLIENT_RESPONSE_FIELD,
                                        HttpUtil.createResponseStruct(context, response));
        }
        BMap<String, BValue> webSocketConnector = BLangConnectorSPIUtil.createObject(
                context, PROTOCOL_PACKAGE_HTTP, WebSocketConstants.WEBSOCKET_CONNECTOR);
        WebSocketOpenConnectionInfo connectionInfo = new WebSocketOpenConnectionInfo(
                wsService, null, webSocketClientEndpoint, context);
        webSocketConnector.addNativeData(WebSocketConstants.NATIVE_DATA_WEBSOCKET_CONNECTION_INFO, connectionInfo);
        countDownLatch.countDown();
        WebSocketDispatcher.dispatchError(connectionInfo, throwable);
    }
}
