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

package org.wso2.transport.http.netty.contractimpl.websocket;

import org.wso2.transport.http.netty.contract.websocket.WebSocketBinaryMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketCloseMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnectorException;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnectorFuture;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnectorListener;
import org.wso2.transport.http.netty.contract.websocket.WebSocketControlMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketHandshaker;
import org.wso2.transport.http.netty.contract.websocket.WebSocketTextMessage;

/**
 * Default implementation of {@link WebSocketConnectorFuture}.
 */
public class DefaultWebSocketConnectorFuture implements WebSocketConnectorFuture {

    private WebSocketConnectorListener wsConnectorListener;

    @Override
    public void setWebSocketConnectorListener(WebSocketConnectorListener wsConnectorListener) {
        this.wsConnectorListener = wsConnectorListener;
    }

    @Override
    public void notifyWebSocketListener(WebSocketHandshaker webSocketHandshaker) throws WebSocketConnectorException {
        checkConnectorState();
        wsConnectorListener.onHandshake(webSocketHandshaker);
    }

    @Override
    public void notifyWebSocketListener(WebSocketTextMessage textMessage) throws WebSocketConnectorException {
        checkConnectorState();
        wsConnectorListener.onMessage(textMessage);
    }

    @Override
    public void notifyWebSocketListener(WebSocketBinaryMessage binaryMessage) throws WebSocketConnectorException {
        checkConnectorState();
        wsConnectorListener.onMessage(binaryMessage);
    }

    @Override
    public void notifyWebSocketListener(WebSocketControlMessage controlMessage) throws WebSocketConnectorException {
        checkConnectorState();
        wsConnectorListener.onMessage(controlMessage);
    }

    @Override
    public void notifyWebSocketListener(WebSocketCloseMessage closeMessage) throws WebSocketConnectorException {
        checkConnectorState();
        wsConnectorListener.onMessage(closeMessage);
    }

    @Override
    public void notifyWebSocketListener(WebSocketConnection webSocketConnection, Throwable throwable)
            throws WebSocketConnectorException {
        checkConnectorState();
        wsConnectorListener.onError(webSocketConnection, throwable);
    }

    @Override
    public void notifyWebSocketIdleTimeout(WebSocketControlMessage controlMessage) throws WebSocketConnectorException {
        checkConnectorState();
        wsConnectorListener.onIdleTimeout(controlMessage);
    }

    @Override
    public void notifyWebSocketListener(WebSocketConnection webSocketConnection) throws WebSocketConnectorException {
        checkConnectorState();
        wsConnectorListener.onClose(webSocketConnection);
    }

    private void checkConnectorState() throws WebSocketConnectorException {
        if (wsConnectorListener == null) {
            throw new WebSocketConnectorException("WebSocket connector listener is not set");
        }
    }
}
