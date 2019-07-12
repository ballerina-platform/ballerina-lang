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

import org.wso2.transport.http.netty.contract.exceptions.ServerConnectorException;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnectorFuture;

/**
 * Allows to set listeners.
 */
public interface ServerConnectorFuture extends HttpConnectorFuture, WebSocketConnectorFuture {

    /**
     * Set life cycle event listener for the HTTP/WS_SCHEME connector
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
