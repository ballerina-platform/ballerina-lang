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

package org.wso2.transport.http.netty.contract.websocket;

/**
 * Connector Future for WebSocket events.
 */
public interface WebSocketConnectorFuture {

    /**
     * Set Connector listener for WebSocket.
     *
     * @param connectorListener Connector listener for WebSocket
     */
    void setWebSocketConnectorListener(WebSocketConnectorListener connectorListener);

    /**
     * Notify WebSocket connection initialization for the listener.
     *
     * @param webSocketHandshaker {@link WebSocketHandshaker} to notify connection initialization
     * @throws WebSocketConnectorException if any error occurred during the notification
     */
    void notifyWebSocketListener(WebSocketHandshaker webSocketHandshaker) throws WebSocketConnectorException;

    /**
     * Notify incoming WebSocket text message for the listener.
     *
     * @param textMessage {@link WebSocketTextMessage} to notify incoming WebSocket text message
     * @throws WebSocketConnectorException if any error occurred during the notification
     */
    void notifyWebSocketListener(WebSocketTextMessage textMessage) throws WebSocketConnectorException;

    /**
     * Notify incoming WebSocket binary message for the listener.
     *
     * @param binaryMessage {@link WebSocketBinaryMessage} to notify incoming WebSocket binary message
     * @throws WebSocketConnectorException if any error occurred during the notification
     */
    void notifyWebSocketListener(WebSocketBinaryMessage binaryMessage)throws WebSocketConnectorException;

    /**
     * Notify incoming WebSocket pong message for the listener.
     *
     * @param controlMessage {@link WebSocketControlMessage} to Notify incoming WebSocket pong message
     * @throws WebSocketConnectorException if any error occurred during the notification
     */
    void notifyWebSocketListener(WebSocketControlMessage controlMessage) throws WebSocketConnectorException;

    /**
     * Notify WebSocket close message to the user.
     *
     * @param closeMessage {@link WebSocketCloseMessage} to notify incoming WebSocket connection closure
     * @throws WebSocketConnectorException if any error occurred during the notification
     */
    void notifyWebSocketListener(WebSocketCloseMessage closeMessage) throws WebSocketConnectorException;

    /**
     * Notify incoming WebSocket connection closure for the listener.
     *
     * @param webSocketConnection {@link WebSocketConnection} to notify incoming WebSocket connection closure
     * @throws WebSocketConnectorException if any error occurred during the notification
     */
    void notifyWebSocketListener(WebSocketConnection webSocketConnection) throws WebSocketConnectorException;

    /**
     * Notify any error occurred in transport for the listener.
     *
     * @param webSocketConnection {@link WebSocketConnection} which causes the error
     * @param throwable {@link Throwable} error occurred
     * @throws WebSocketConnectorException if any error occurred during the notification
     */
    void notifyWebSocketListener(WebSocketConnection webSocketConnection, Throwable throwable)
            throws WebSocketConnectorException;

    /**
     * Notify idle timeout of WebSocket connection.
     *
     * @param controlMessage Indicate WebSocket connection timeout
     * @throws WebSocketConnectorException if any error occurred during notification to the listener
     */
    void notifyWebSocketIdleTimeout(WebSocketControlMessage controlMessage) throws WebSocketConnectorException;
}
