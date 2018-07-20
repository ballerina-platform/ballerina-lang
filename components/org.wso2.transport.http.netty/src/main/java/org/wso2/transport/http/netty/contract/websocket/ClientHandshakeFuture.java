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
 *
 */

package org.wso2.transport.http.netty.contract.websocket;

import org.wso2.transport.http.netty.message.HttpCarbonResponse;

/**
 * Future for WebSocket handshake.
 */
public interface ClientHandshakeFuture extends WebSocketConnectorFuture {

    /**
     * Set the listener for WebSocket handshake.
     *
     * @param serverHandshakeListener Listener for WebSocket handshake.
     */
    void setClientHandshakeListener(ClientHandshakeListener serverHandshakeListener);

    /**
     * Notify the success of the WebSocket handshake.
     *
     * @param webSocketConnection {@link WebSocketConnection} for the successful connection.
     * @param response            {@link HttpCarbonResponse} received from server.
     */
    void notifySuccess(WebSocketConnection webSocketConnection, HttpCarbonResponse response);

    /**
     * Notify any error occurred during the handshake.
     *
     * @param throwable error occurred during handshake.
     * @param response  {@link HttpCarbonResponse} received from server or null if error occurred before response is
     *                                            received.
     */
    void notifyError(Throwable throwable, HttpCarbonResponse response);
}
