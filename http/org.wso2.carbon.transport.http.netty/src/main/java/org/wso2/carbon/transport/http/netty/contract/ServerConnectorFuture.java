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

package org.wso2.carbon.transport.http.netty.contract;

import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketBinaryMessage;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketCloseMessage;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketConnectorListener;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketControlMessage;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketInitMessage;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketTextMessage;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;

/**
 * Allows to set listeners.
 */
public interface ServerConnectorFuture {
    /**
     * Set Connector listener for HTTP.
     *
     * @param connectorListener Connector listener for HTTP.
     */
    void setHTTPConnectorListener(HTTPConnectorListener connectorListener);

    /**
     * Notify HTTP messages to the listener.
     *
     * @param httpMessage HTTP message.
     * @throws ServerConnectorException if any error occurred during the notification.
     */
    void notifyHTTPListener(HTTPCarbonMessage httpMessage) throws ServerConnectorException;

    /**
     *  Set Connector listener for WebSocket.
     * @param connectorListener Connector listener for WebSocket.
     */
    void setWSConnectorListener(WebSocketConnectorListener connectorListener);

    /**
     * Notify WebSocket connection initialization for the listener.
     *
     * @param initMessage {@link WebSocketInitMessage} to notify connection initialization.
     * @throws ServerConnectorException if any error occurred during the notification.
     */
    void notifyWSListener(WebSocketInitMessage initMessage) throws ServerConnectorException;

    /**
     * Notify incoming WebSocket text message for the listener.
     *
     * @param textMessage {@link WebSocketTextMessage} to notify incoming WebSocket text message.
     * @throws ServerConnectorException if any error occurred during the notification.
     */
    void notifyWSListener(WebSocketTextMessage textMessage) throws ServerConnectorException;

    /**
     * Notify incoming WebSocket binary message for the listener.
     *
     * @param binaryMessage {@link WebSocketBinaryMessage} to notify incoming WebSocket binary message.
     * @throws ServerConnectorException if any error occurred during the notification.
     */
    void notifyWSListener(WebSocketBinaryMessage binaryMessage) throws ServerConnectorException;

    /**
     * Notify incoming WebSocket pong message for the listener.
     *
     * @param controlMessage {@link WebSocketControlMessage} to Notify incoming WebSocket pong message.
     * @throws ServerConnectorException if any error occurred during the notification.
     */
    void notifyWSListener(WebSocketControlMessage controlMessage) throws ServerConnectorException;

    /**
     * Notify incoming WebSocket connection closure for the listener.
     *
     * @param closeMessage {@link WebSocketCloseMessage} to notify incoming WebSocket connection closure.
     * @throws ServerConnectorException if any error occurred during the notification.
     */
    void notifyWSListener(WebSocketCloseMessage closeMessage) throws ServerConnectorException;

    /**
     * Notify any error occurred in transport for the listener.
     *
     * @param throwable {@link Throwable} error occurred.
     * @throws ServerConnectorException if any error occurred during the notification.
     */
    void notifyWSListener(Throwable throwable) throws ServerConnectorException;
}
