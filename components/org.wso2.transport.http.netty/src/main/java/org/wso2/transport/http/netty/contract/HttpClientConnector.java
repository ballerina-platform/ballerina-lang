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

import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.transport.http.netty.message.Http2PushPromise;
import org.wso2.transport.http.netty.message.ResponseHandle;

/**
 * Allows to send outbound messages.
 */
public interface HttpClientConnector {
    /**
     * Creates the connection to the back-end.
     *
     * @return the future that can be used to get future events of the connection.
     */
    HttpResponseFuture connect();

    /**
     * Send httpMessages to the back-end in asynchronous manner.
     *
     * @param httpCarbonMessage {@link HTTPCarbonMessage} which should be sent to the remote server.
     * @return returns the status of the asynchronous send action.
     */
    HttpResponseFuture send(HTTPCarbonMessage httpCarbonMessage);

    /**
     * Close the connection related to this connector.
     *
     * @return return the status of the close action.
     */
    boolean close();

    /**
     * Submit httpMessages to the back-end in asynchronous manner
     *
     * @param httpCarbonMessage httpCarbonMessage {@link HTTPCarbonMessage} which should be sent to the remote server.
     * @return returns the status of the asynchronous submit action.
     */
    HttpResponseFuture submit(HTTPCarbonMessage httpCarbonMessage);

    /**
     * Fetch response related to the {@code ResponseHandle} in asynchronous manner.
     *
     * @param responseHandle Response Handle
     * @return returns the status of the asynchronous response fetch action
     */
    HttpResponseFuture getResponse(ResponseHandle responseHandle);

    /**
     * Get the next available push promise related to the {@code ResponseHandle} in asynchronous manner.
     *
     * @param responseHandle Response Handle
     * @return returns the status of the asynchronous push promise fetch action
     */
    HttpResponseFuture getNextPushPromise(ResponseHandle responseHandle);

    /**
     * Check whether a push promise exists in asynchronous manner.
     *
     * @param responseHandle Response Handle
     * @return returns the status of the asynchronous push promise check action
     */
    HttpResponseFuture hasPushPromise(ResponseHandle responseHandle);

    /**
     * Get the push response in asynchronous manner.
     *
     * @param responseHandle Response Handle
     * @param pushPromise    push promise related to the push response
     * @return returns the status of the asynchronous push response fetch action
     */
    HttpResponseFuture getPushResponse(ResponseHandle responseHandle, Http2PushPromise pushPromise);
}
