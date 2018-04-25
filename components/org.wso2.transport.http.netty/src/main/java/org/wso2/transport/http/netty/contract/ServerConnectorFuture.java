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

package org.wso2.transport.http.netty.contract;

import org.wso2.transport.http.netty.contract.websocket.WebSocketBinaryMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketCloseMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnectorListener;
import org.wso2.transport.http.netty.contract.websocket.WebSocketControlMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketInitMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketTextMessage;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.transport.http.netty.message.Http2PushPromise;

/**
 * Allows to set listeners.
 */
public interface ServerConnectorFuture {
    /**
     * Set Connector listener for HTTP.
     *
     * @param connectorListener Connector listener for HTTP.
     */
    void setHttpConnectorListener(HttpConnectorListener connectorListener);

    /**
     * Notify HTTP messages to the listener.
     *
     * @param httpMessage HTTP message.
     * @throws ServerConnectorException if any error occurred during the notification.
     */
    void notifyHttpListener(HTTPCarbonMessage httpMessage) throws ServerConnectorException;

    /**
     * Notifies HTTP Server Push messages to the listener.
     *
     * @param httpMessage the {@link HTTPCarbonMessage} receive as the push response
     * @param pushPromise the related {@link Http2PushPromise}
     * @throws ServerConnectorException if any error occurred during the notification
     */
    void notifyHttpListener(HTTPCarbonMessage httpMessage, Http2PushPromise pushPromise)
            throws ServerConnectorException;

    /**
     * Notifies {@link Http2PushPromise} to the listener.
     *
     * @param pushPromise the push promise message
     * @throws ServerConnectorException in case of failure
     */
    void notifyHttpListener(Http2PushPromise pushPromise) throws ServerConnectorException;

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

    /**
     * Notify idle timeout of WebSocket connection.
     *
     * @param controlMessage Indicate WebSocket connection timeout.
     * @throws ServerConnectorException if any error occurred during notification to the listener.
     */
    void notifyWSIdleTimeout(WebSocketControlMessage controlMessage) throws ServerConnectorException;

    /**
     * Notify error messages to the listener.
     *
     * @param cause Reason for the error.
     * @throws ServerConnectorException if any error occurred during the error notification.
     */
    void notifyErrorListener(Throwable cause) throws ServerConnectorException;;

    /**
     * Set life cycle event listener for the HTTP/WS connector
     *
     * @param portBindingEventListener The PortBindingEventListener implementation
     */
    void setPortBindingEventListener(PortBindingEventListener portBindingEventListener);

    /**
     * Notify the port binding listener of events related to connector start up
     *
     * @param serverConnectorId The ID of the server connected related to this port binding event
     * @param isHttps Specifies whether the server connector is using HTTPS.
     */
    void notifyPortBindingEvent(String serverConnectorId, boolean isHttps);

    /**
     * Notify the port binding listener of events related to connector termination
     *
     * @param serverConnectorId The ID of the server connected related to this port unbinding event
     * @param isHttps Specifies whether the server connector is using HTTPS.
     * @throws ServerConnectorException Thrown if there is an error in unbinding the port.
     */
    void notifyPortUnbindingEvent(String serverConnectorId, boolean isHttps) throws ServerConnectorException;

    /**
     * Notify the port binding listener of exceptions thrown during connector startup
     *
     * @param throwable Exception thrown during connector startup
     */
    void notifyPortBindingError(Throwable throwable);

    /**
     * Waits till the port binding is completed.
     *
     * @throws InterruptedException if any interrupt occurred while waiting for port binding.
     */
    void sync() throws InterruptedException;
}
