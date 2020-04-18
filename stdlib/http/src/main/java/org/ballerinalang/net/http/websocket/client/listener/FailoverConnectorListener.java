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

package org.ballerinalang.net.http.websocket.client.listener;

import org.ballerinalang.net.http.websocket.WebSocketConstants;
import org.ballerinalang.net.http.websocket.WebSocketUtil;
import org.ballerinalang.net.http.websocket.server.WebSocketConnectionInfo;
import org.wso2.transport.http.netty.contract.websocket.WebSocketBinaryMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketCloseMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;
import org.wso2.transport.http.netty.contract.websocket.WebSocketControlMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketHandshaker;
import org.wso2.transport.http.netty.contract.websocket.WebSocketTextMessage;

import java.io.IOException;

/**
 * Ballerina Connector listener of WebSocket failover.
 *
 * @since 1.2.0
 */
public class FailoverConnectorListener implements ExtendedConnectorListener {

    private WebSocketConnectionInfo connectionInfo = null;
    private ExtendedConnectorListener connectorListener;

    public FailoverConnectorListener(ExtendedConnectorListener connectorListener) {
        this.connectorListener = connectorListener;
    }

    @Override
    public void setConnectionInfo(WebSocketConnectionInfo connectionInfo) {
        connectorListener.setConnectionInfo(connectionInfo);
        this.connectionInfo = connectionInfo;
    }

    @Override
    public void onHandshake(WebSocketHandshaker webSocketHandshaker) {
        // Cannot reach this point as `onHandshake` and `onOpen` are not supported by WebSocket client services.
    }

    @Override
    public void onMessage(WebSocketTextMessage textMessage) {
        connectorListener.onMessage(textMessage);
    }

    @Override
    public void onMessage(WebSocketBinaryMessage binaryMessage) {
        connectorListener.onMessage(binaryMessage);
    }

    @Override
    public void onMessage(WebSocketControlMessage controlMessage) {
        connectorListener.onMessage(controlMessage);
    }

    @Override
    public void onMessage(WebSocketCloseMessage webSocketCloseMessage) {
        int statusCode = webSocketCloseMessage.getCloseCode();
        if (!(statusCode == WebSocketConstants.STATUS_CODE_ABNORMAL_CLOSURE &&
                WebSocketUtil.failover(connectionInfo.getWebSocketEndpoint(), connectionInfo.getService()))) {
            connectorListener.onMessage(webSocketCloseMessage);
        }
    }

    @Override
    public void onClose(WebSocketConnection webSocketConnection) {
        connectorListener.onClose(webSocketConnection);
    }

    @Override
    public void onError(WebSocketConnection webSocketConnection, Throwable throwable) {
        // When the connection is lost, do the failover to the remaining server URLs.
        if (!(throwable instanceof IOException && WebSocketUtil.failover(connectionInfo.getWebSocketEndpoint(),
                connectionInfo.getService()))) {
            connectorListener.onError(webSocketConnection, throwable);
        }
    }

    @Override
    public void onIdleTimeout(WebSocketControlMessage controlMessage) {
        connectorListener.onIdleTimeout(controlMessage);
    }
}
