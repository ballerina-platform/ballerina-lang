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

import org.ballerinalang.net.http.exception.WebSocketException;
import org.wso2.transport.http.netty.contract.websocket.WebSocketBinaryMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketCloseMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnectorListener;
import org.wso2.transport.http.netty.contract.websocket.WebSocketControlMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketHandshaker;
import org.wso2.transport.http.netty.contract.websocket.WebSocketTextMessage;

/**
 * Ballerina Connector listener for WebSocket.
 *
 * @since 0.93
 */
public class WebSocketClientConnectorListener implements WebSocketConnectorListener {
    private WebSocketOpenConnectionInfo connectionInfo;


    public void setConnectionInfo(WebSocketOpenConnectionInfo connectionInfo) {
        this.connectionInfo = connectionInfo;
    }

    @Override
    public void onHandshake(WebSocketHandshaker webSocketHandshaker) {
        throw new WebSocketException("onHandshake and onOpen is not supported for WebSocket client service");
    }

    @Override
    public void onMessage(WebSocketTextMessage webSocketTextMessage) {
        try {
            WebSocketDispatcher.dispatchTextMessage(connectionInfo, webSocketTextMessage);
        } catch (IllegalAccessException e) {
            // Ignore as it is not possible have an Illegal access
        }
    }

    @Override
    public void onMessage(WebSocketBinaryMessage webSocketBinaryMessage) {
        try {
            WebSocketDispatcher.dispatchBinaryMessage(connectionInfo, webSocketBinaryMessage);
        } catch (IllegalAccessException e) {
            // Ignore as it is not possible have an Illegal access
        }
    }

    @Override
    public void onMessage(WebSocketControlMessage webSocketControlMessage) {
        try {
            WebSocketDispatcher.dispatchControlMessage(connectionInfo, webSocketControlMessage);
        } catch (IllegalAccessException e) {
            // Ignore as it is not possible have an Illegal access
        }
    }

    @Override
    public void onMessage(WebSocketCloseMessage webSocketCloseMessage) {
        try {
            WebSocketDispatcher.dispatchCloseMessage(connectionInfo, webSocketCloseMessage);
        } catch (IllegalAccessException e) {
            // Ignore as it is not possible have an Illegal access
        }
    }

    @Override
    public void onError(WebSocketConnection webSocketConnection, Throwable throwable) {
        WebSocketDispatcher.dispatchError(connectionInfo, throwable);
    }

    @Override
    public void onIdleTimeout(WebSocketControlMessage controlMessage) {
        try {
            WebSocketDispatcher.dispatchIdleTimeout(connectionInfo);
        } catch (IllegalAccessException e) {
            // Ignore as it is not possible have an Illegal access
        }
    }

    @Override
    public void onClose(WebSocketConnection webSocketConnection) {
        try {
            WebSocketUtil.setListenerOpenField(connectionInfo);
        } catch (IllegalAccessException e) {
            // Ignore as it is not possible have an Illegal access
        }
    }
}
