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

package org.wso2.transport.http.netty.contract;

import org.wso2.transport.http.netty.contract.exceptions.ServerConnectorException;
import org.wso2.transport.http.netty.message.Http2PushPromise;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

/**
 * Connector Future for HTTP events.
 */
public interface HttpConnectorFuture {

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
    void notifyHttpListener(HttpCarbonMessage httpMessage) throws ServerConnectorException;

    /**
     * Notifies HTTP Server Push messages to the listener.
     *
     * @param httpMessage the {@link HttpCarbonMessage} receive as the push response
     * @param pushPromise the related {@link Http2PushPromise}
     * @throws ServerConnectorException if any error occurred during the notification
     */
    void notifyHttpListener(HttpCarbonMessage httpMessage, Http2PushPromise pushPromise)
            throws ServerConnectorException;

    /**
     * Notifies {@link Http2PushPromise} to the listener.
     *
     * @param pushPromise the push promise message
     * @throws ServerConnectorException in case of failure
     */
    void notifyHttpListener(Http2PushPromise pushPromise) throws ServerConnectorException;

    /**
     * Notify error messages to the listener.
     *
     * @param cause Reason for the error.
     * @throws ServerConnectorException if any error occurred during the error notification.
     */
    void notifyErrorListener(Throwable cause) throws ServerConnectorException;
}
