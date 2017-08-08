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
 * Observable for WebSocket server connector.
 */
public interface WebSocketObservable {

    /**
     * Set the {@link WebSocketInboundObserver} for the Observable.
     *
     * @param observer for the given Observable.
     */
    void setObserver(WebSocketInboundObserver observer);

    /**
     * Remove the observer from the Observable.
     *
     * @param observer {@link WebSocketInboundObserver} which should be removed.
     */
    void removeObserver(WebSocketInboundObserver observer);

    /**
     * Notify WebSocket handshake. This will initialize a client connection for WebSocket
     * server connector.
     *
     * @param initMessage {@link WebSocketInitMessage} to initialize connection.
     */
    void notify(WebSocketInitMessage initMessage);

    /**
     * Notify incoming WebSocket text messages.
     *
     * @param textMessage {@link WebSocketTextMessage} to process text messages.
     */
    void notify(WebSocketTextMessage textMessage);

    /**
     * Notify incoming WebSocket binary messages.
     *
     * @param binaryMessage {@link WebSocketBinaryMessage} to process binary messages.
     */
    void notify(WebSocketBinaryMessage binaryMessage);

    /**
     * Notify incoming WebSocket control messages.
     *
     * @param controlMessage {@link WebSocketControlMessage} to indicate a incoming pong messages.
     */
    void notify(WebSocketControlMessage controlMessage);

    /**
     * Notify incoming WebSocket close messages.
     *
     * @param closeMessage {@link WebSocketCloseMessage} to indicate incoming close messages.
     */
    void notify(WebSocketCloseMessage closeMessage);

    /**
     * Notify any transport error.
     *
     * @param throwable error received from transport.
     */
    void notifyError(Throwable throwable);
}
