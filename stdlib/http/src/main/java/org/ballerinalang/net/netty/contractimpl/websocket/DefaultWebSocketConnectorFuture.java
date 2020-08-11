/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.net.netty.contractimpl.websocket;

import org.ballerinalang.net.netty.contract.websocket.WebSocketBinaryMessage;
import org.ballerinalang.net.netty.contract.websocket.WebSocketCloseMessage;
import org.ballerinalang.net.netty.contract.websocket.WebSocketConnection;
import org.ballerinalang.net.netty.contract.websocket.WebSocketConnectorException;
import org.ballerinalang.net.netty.contract.websocket.WebSocketConnectorFuture;
import org.ballerinalang.net.netty.contract.websocket.WebSocketConnectorListener;
import org.ballerinalang.net.netty.contract.websocket.WebSocketControlMessage;
import org.ballerinalang.net.netty.contract.websocket.WebSocketHandshaker;
import org.ballerinalang.net.netty.contract.websocket.WebSocketTextMessage;

/**
 * Default implementation of {@link org.ballerinalang.net.netty.contract.websocket.WebSocketConnectorFuture}.
 */
public class DefaultWebSocketConnectorFuture implements WebSocketConnectorFuture {

    private org.ballerinalang.net.netty.contract.websocket.WebSocketConnectorListener wsConnectorListener;

    @Override
    public void setWebSocketConnectorListener(WebSocketConnectorListener wsConnectorListener) {
        this.wsConnectorListener = wsConnectorListener;
    }

    @Override
    public void notifyWebSocketListener(WebSocketHandshaker webSocketHandshaker) throws
                                                                                 org.ballerinalang.net.netty.contract.websocket.WebSocketConnectorException {
        checkConnectorState();
        wsConnectorListener.onHandshake(webSocketHandshaker);
    }

    @Override
    public void notifyWebSocketListener(WebSocketTextMessage textMessage) throws
                                                                          org.ballerinalang.net.netty.contract.websocket.WebSocketConnectorException {
        checkConnectorState();
        wsConnectorListener.onMessage(textMessage);
    }

    @Override
    public void notifyWebSocketListener(WebSocketBinaryMessage binaryMessage) throws
                                                                              org.ballerinalang.net.netty.contract.websocket.WebSocketConnectorException {
        checkConnectorState();
        wsConnectorListener.onMessage(binaryMessage);
    }

    @Override
    public void notifyWebSocketListener(
            org.ballerinalang.net.netty.contract.websocket.WebSocketControlMessage controlMessage) throws
                                                                                                   org.ballerinalang.net.netty.contract.websocket.WebSocketConnectorException {
        checkConnectorState();
        wsConnectorListener.onMessage(controlMessage);
    }

    @Override
    public void notifyWebSocketListener(WebSocketCloseMessage closeMessage) throws
                                                                            org.ballerinalang.net.netty.contract.websocket.WebSocketConnectorException {
        checkConnectorState();
        wsConnectorListener.onMessage(closeMessage);
    }

    @Override
    public void notifyWebSocketListener(
            org.ballerinalang.net.netty.contract.websocket.WebSocketConnection webSocketConnection, Throwable throwable)
            throws org.ballerinalang.net.netty.contract.websocket.WebSocketConnectorException {
        checkConnectorState();
        wsConnectorListener.onError(webSocketConnection, throwable);
    }

    @Override
    public void notifyWebSocketIdleTimeout(WebSocketControlMessage controlMessage) throws
                                                                                   org.ballerinalang.net.netty.contract.websocket.WebSocketConnectorException {
        checkConnectorState();
        wsConnectorListener.onIdleTimeout(controlMessage);
    }

    @Override
    public void notifyWebSocketListener(WebSocketConnection webSocketConnection) throws
                                                                                 org.ballerinalang.net.netty.contract.websocket.WebSocketConnectorException {
        checkConnectorState();
        wsConnectorListener.onClose(webSocketConnection);
    }

    private void checkConnectorState() throws
                                       org.ballerinalang.net.netty.contract.websocket.WebSocketConnectorException {
        if (wsConnectorListener == null) {
            throw new WebSocketConnectorException("WebSocket connector listener is not set");
        }
    }
}
