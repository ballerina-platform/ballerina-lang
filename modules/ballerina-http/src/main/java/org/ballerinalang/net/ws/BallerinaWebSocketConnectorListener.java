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

package org.ballerinalang.net.ws;

import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.Dispatcher;
import org.ballerinalang.connector.api.Executor;
import org.ballerinalang.net.ws.dispatchers.WebSocketCloseMsgDispatcher;
import org.ballerinalang.net.ws.dispatchers.WebSocketInitMsgDispatcher;
import org.ballerinalang.net.ws.dispatchers.WebSocketTextMsgDispatcher;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketBinaryMessage;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketCloseMessage;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketConnectorListener;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketControlMessage;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketInitMessage;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketTextMessage;

import java.net.ProtocolException;
import javax.websocket.Session;

/**
 * Ballerina Connector listener for WebSocket.
 *
 * @since 0.93
 */
public class BallerinaWebSocketConnectorListener implements WebSocketConnectorListener {

    @Override
    public void onMessage(WebSocketInitMessage webSocketInitMessage) {
        try {
            Session session = webSocketInitMessage.handshake();
            Dispatcher dispatcher = new WebSocketInitMsgDispatcher(webSocketInitMessage, session);
            Executor.execute(dispatcher);
        } catch (ProtocolException e) {
            throw new BallerinaConnectorException("Error occurred during WebSocket handshake", e);
        }
    }

    @Override
    public void onMessage(WebSocketTextMessage webSocketTextMessage) {
        Dispatcher dispatcher = new WebSocketTextMsgDispatcher(webSocketTextMessage);
        Executor.execute(dispatcher);
    }

    @Override
    public void onMessage(WebSocketBinaryMessage webSocketBinaryMessage) {
        throw new BallerinaConnectorException("Binary messages are not supported!");
    }

    @Override
    public void onMessage(WebSocketControlMessage webSocketControlMessage) {
        throw new BallerinaConnectorException("Pong messages are not supported!");
    }

    @Override
    public void onMessage(WebSocketCloseMessage webSocketCloseMessage) {
        Dispatcher dispatcher = new WebSocketCloseMsgDispatcher(webSocketCloseMessage);
        Executor.execute(dispatcher);
    }

    @Override
    public void onError(Throwable throwable) {
        throw new BallerinaConnectorException("Unexpected error occurred in WebSocket transport", throwable);
    }
}
