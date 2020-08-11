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

import org.wso2.transport.http.netty.message.Http2PushPromise;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
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
     * @param httpCarbonMessage {@link HttpCarbonMessage} which should be sent to the remote server.
     * @return the status of the asynchronous send action.
     */
    HttpResponseFuture send(HttpCarbonMessage httpCarbonMessage);

    /**
     * Close the connection related to this connector.
     *
     * @return return the status of the close action.
     */
    boolean close();

    /**
     * Fetches the response related to the {@link ResponseHandle} in asynchronous manner.
     *
     * @param responseHandle the Response Handle which represent the asynchronous service invocation
     * @return the status of the asynchronous response fetch action
     */
    HttpResponseFuture getResponse(ResponseHandle responseHandle);

    /**
     * Gets the next available {@link Http2PushPromise} related to the {@link ResponseHandle} in asynchronous manner.
     *
     * @param responseHandle the Response Handle which represent the asynchronous service invocation
     * @return the status of the asynchronous push promise fetch action
     */
    HttpResponseFuture getNextPushPromise(ResponseHandle responseHandle);

    /**
     * Checks whether a {@link Http2PushPromise} exists in asynchronous manner.
     *
     * @param responseHandle the Response Handle which represent the asynchronous service invocation
     * @return the status of the asynchronous push promise check action
     */
    HttpResponseFuture hasPushPromise(ResponseHandle responseHandle);

    /**
     * Rejects a server push response which is expected to receive over a promised stream.
     * This method will do the best to prevent receiving a server push which is promised by a particular
     * PUSH_PROMISE frame.
     * This basically sends a RST_STREAM referring the promised stream to reject the server push message.
     * However invoking this does not guarantee that the server will not start sending the push message.
     * As per the spec, server is allowed to start sending the push response without waiting for an acknowledgement
     * from the client.
     *
     * @param pushPromise    push promise related to the server push which is need to be rejected
     */
    void rejectPushResponse(Http2PushPromise pushPromise);

    /**
     * Gets the push response in asynchronous manner.
     *
     * @param pushPromise    push promise related to the server push
     * @return returns the status of the asynchronous push response fetch action
     */
    HttpResponseFuture getPushResponse(Http2PushPromise pushPromise);
}
