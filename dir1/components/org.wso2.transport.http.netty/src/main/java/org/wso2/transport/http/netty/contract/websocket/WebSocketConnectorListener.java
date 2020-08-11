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
 *
 */

package org.wso2.transport.http.netty.contract.websocket;

/**
 * Message processor for WebSocket inbound messages.
 */
public interface WebSocketConnectorListener {

    /**
     * Trigger WebSocket handshake. This will initialize a client connection for WebSocket
     * server connector.
     *
     * @param webSocketHandshaker {@link WebSocketHandshaker} to initialize connection.
     */
    void onHandshake(WebSocketHandshaker webSocketHandshaker);

    /**
     * Trigger incoming WebSocket text messages.
     *
     * @param textMessage {@link WebSocketTextMessage} to process text messages.
     */
    void onMessage(WebSocketTextMessage textMessage);

    /**
     * Trigger incoming WebSocket binary messages.
     *
     * @param binaryMessage {@link WebSocketBinaryMessage} to process binary messages.
     */
    void onMessage(WebSocketBinaryMessage binaryMessage);

    /**
     * Trigger incoming WebSocket control messages.
     *
     * @param controlMessage {@link WebSocketControlMessage} to indicate a incoming pong messages.
     */
    void onMessage(WebSocketControlMessage controlMessage);

    /**
     * Trigger incoming WebSocket close messages.
     *
     * @param closeMessage {@link WebSocketCloseMessage} to indicate incoming close messages.
     */
    void onMessage(WebSocketCloseMessage closeMessage);

    /**
     * This lets the listener know when the connection closes. This method will be called only after the user is
     * notified of a close message or an error message if one is available. It is also possible that this method is
     * called without a close message or an error message. Also note that this method will be called only after the
     * futures of connection closure methods return.
     *
     * @param webSocketConnection {@link WebSocketConnection} which gets closed.
     */
    void onClose(WebSocketConnection webSocketConnection);

    /**
     * Trigger any transport error.
     *
     * @param webSocketConnection {@link WebSocketConnection} which causes the error.
     * @param throwable error received from transport.
     */
    void onError(WebSocketConnection webSocketConnection, Throwable throwable);

    /**
     * This is triggered in a IdleTimeout for WebSocket.
     *
     * @param controlMessage message which indicates idle timeout.
     */
    void onIdleTimeout(WebSocketControlMessage controlMessage);

}
