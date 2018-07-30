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

/**
 * Allows to get notifications of connectors.
 */
public interface HttpConnectorListener {
    /**
     * Gets notified for events on a http message.
     *
     * @param httpMessage contains the state change information of the event.
     */
    void onMessage(HttpCarbonMessage httpMessage);

    /**
     * Each error event triggered by connector ends up here.
     * @param throwable contains the error details of the event.
     */
    void onError(Throwable throwable);

    /**
     * Gets notified for an event on a {@link Http2PushPromise}.
     *
     * @param pushPromise the push promise message
     */
    default void onPushPromise(Http2PushPromise pushPromise) {
    }

    /**
     * Gets notified for events on Push responses.
     *
     * @param promiseId the promise id of the push response
     * @param  httpMessage the push response message
     */
    default void onPushResponse(int promiseId, HttpCarbonMessage httpMessage) {
    }
}
