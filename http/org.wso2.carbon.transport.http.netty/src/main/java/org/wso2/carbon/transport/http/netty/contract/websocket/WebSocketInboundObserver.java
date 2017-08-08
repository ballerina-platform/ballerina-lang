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

package org.wso2.carbon.transport.http.netty.contract.websocket;

/**
 * Message processor for WebSocket inbound messages.
 */
public interface WebSocketInboundObserver {

    /**
     * Update WebSocket handshake. This will initialize a client connection for WebSocket
     * server connector.
     *
     * @param initMessage {@link WebSocketInitMessage} to initialize connection.
     */
    void update(WebSocketInitMessage initMessage);

    /**
     * Update incoming WebSocket text messages.
     *
     * @param textMessage {@link WebSocketTextMessage} to process text messages.
     */
    void update(WebSocketTextMessage textMessage);

    /**
     * Update incoming WebSocket binary messages.
     *
     * @param binaryMessage {@link WebSocketBinaryMessage} to process binary messages.
     */
    void update(WebSocketBinaryMessage binaryMessage);

    /**
     * Update incoming WebSocket control messages.
     *
     * @param controlMessage {@link WebSocketControlMessage} to indicate a incoming pong messages.
     */
    void update(WebSocketControlMessage controlMessage);

    /**
     * Update incoming WebSocket close messages.
     *
     * @param closeMessage {@link WebSocketCloseMessage} to indicate incoming close messages.
     */
    void update(WebSocketCloseMessage closeMessage);

    /**
     * Update any transport error.
     *
     * @param throwable error received from transport.
     */
    void handleError(Throwable throwable);

}
