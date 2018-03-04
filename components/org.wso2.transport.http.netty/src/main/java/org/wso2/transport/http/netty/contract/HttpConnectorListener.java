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
 * Allows to get notifications of connectors.
 */
public interface HttpConnectorListener {
    /**
     * Each http message event triggered by connector ends up here.
     *
     * @param httpMessage contains the state change information of the event.
     */
    default void onMessage(HTTPCarbonMessage httpMessage) {
    }

    /**
     * Each error event triggered by connector ends up here.
     *
     * @param throwable contains the error details of the event.
     */
    void onError(Throwable throwable);

    /**
     * Events on Push Promises ends up here
     *
     * @param pushPromise push promise
     */
    default void onPushPromise(Http2PushPromise pushPromise) {

    }

    /**
     * Events on promise availability ends up here
     *
     * @param isPromiseAvailable whether promise is available
     */
    default void onPushPromiseAvailability(boolean isPromiseAvailable){
    }

    /**
     * Events related to Response Handle will end up here
     *
     * @param responseHandle Response Handle
     */
    default void onResponseHandle(ResponseHandle responseHandle) {

    }

}
