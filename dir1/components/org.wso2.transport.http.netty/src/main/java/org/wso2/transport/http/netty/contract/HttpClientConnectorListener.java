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

package org.wso2.transport.http.netty.contract;

import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.ResponseHandle;

/**
 * {@code HttpClientConnectorListener} is the interface for Http ClientConnector event listeners.
 *
 * This is an extension of {@link HttpConnectorListener} contains additional capability which are specific only for
 * {@link HttpClientConnector}.
 * <p>
 * Implementors of this interface can implement only what they prefer to listen for.
 */
public interface HttpClientConnectorListener extends HttpConnectorListener {

    @Override
    default void onMessage(HttpCarbonMessage httpMessage) {
    }

    @Override
    default void onError(Throwable throwable) {
    }

    /**
     * Gets notified for events related to promise availability.
     *
     * @param isPromiseAvailable the parameter which tells whether a promise is available
     */
    default void onPushPromiseAvailability(boolean isPromiseAvailable) {
    }

    /**
     * Gets notified for events related to {@link ResponseHandle}.
     *
     * @param responseHandle the Response Handle
     */
    default void onResponseHandle(ResponseHandle responseHandle) {
    }
}
