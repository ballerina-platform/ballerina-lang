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

import io.netty.handler.codec.http.HttpHeaders;

/**
 * This Message is used to handle WebSocket handshake.
 */
public interface WebSocketInitMessage extends WebSocketMessage {

    /**
     * Complete the handshake of a given request. There will not be a idle timeout for the connection if this
     * method is used.
     *
     * @return the Server session for the newly created WebSocket connection.
     */
    HandshakeFuture handshake();

    /**
     * Complete the handshake of a given request. There will not be a idle timeout for the connection if this
     * method is used.
     *
     * @param subProtocols Sub-Protocols which are allowed by the service.
     * @param allowExtensions whether the extensions are allowed or not.
     * @return the Server session for the newly created WebSocket connection.
     */
    HandshakeFuture handshake(String[] subProtocols, boolean allowExtensions);

    /**
     * Complete the handshake of a given request. The connection will be timed out if the connection is idle for
     * given time period.
     *
     * @param subProtocols Sub-Protocols which are allowed by the service.
     * @param allowExtensions whether the extensions are allowed or not.
     * @param idleTimeout Idle timeout in milli-seconds for WebSocket connection.
     * @return the handshake future.
     */
    HandshakeFuture handshake(String[] subProtocols, boolean allowExtensions, int idleTimeout);

    /**
     * Complete the handshake of a given request. The connection will be timed out if the connection is idle for given
     * time period.
     *
     * @param subProtocols Sub-Protocols which are allowed by the service.
     * @param allowExtensions whether the extensions are allowed or not.
     * @param idleTimeout Idle timeout in milli-seconds for WebSocket connection.
     * @param responseHeaders Extra headers to add to the handshake response or {@code null} if no extra headers should
     *                        be added
     * @return the handshake future.
     */
    HandshakeFuture handshake(String[] subProtocols, boolean allowExtensions, int idleTimeout,
                              HttpHeaders responseHeaders);

    /**
     * Complete the handshake of a given request. The connection will be timed out if the connection is idle for given
     * time period.
     *
     * @param subProtocols Sub-Protocols which are allowed by the service.
     * @param allowExtensions whether the extensions are allowed or not.
     * @param idleTimeout Idle timeout in milli-seconds for WebSocket connection.
     * @param responseHeaders Extra headers to add to the handshake response or {@code null} if no extra headers should
     *                        be added.
     * @param maxFramePayloadLength Maximum allowable frame payload length. Setting this value to your application's
     *            requirement may reduce denial of service attacks using long data frames.
     * @return the handshake future.
     */
    HandshakeFuture handshake(String[] subProtocols, boolean allowExtensions, int idleTimeout,
                              HttpHeaders responseHeaders, int maxFramePayloadLength);

    /**
     * Cancel the handshake.
     *
     * @param closeCode close code for cancelling the handshake.
     * @param closeReason reason for canceling the handshake.
     */
    void cancelHandShake(int closeCode, String closeReason);

    /**
     * Check whether the handshake is cancelled in someplace or not.
     *
     * @return true if the handshake is cancelled.
     */
    boolean isCancelled();

    /**
     * Check whether the handshake has been started or not.
     *
     * @return true if the handshake has been started.
     */
    boolean isHandshakeStarted();
}
