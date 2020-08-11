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

import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.ResponseHandle;

/**
 * Represents the future events and results of connectors.
 */
public interface HttpResponseFuture {
    /**
     * Sets listener for the connector future.
     *
     * @param connectorListener the listener that receive http message events related to the connector
     */
    void setHttpConnectorListener(HttpConnectorListener connectorListener);

    /**
     * Removes the message listener set to the future.
     */
    void removeHttpListener();

    /**
     * Notifies the http message listener when there is an event.
     *
     * @param httpMessage the entity which contains the data related to the event
     */
    void notifyHttpListener(HttpCarbonMessage httpMessage);

    /**
     * Notifies the listener when there is an event.
     *
     * @param throwable the entity which contains the data related to the error
     */
    void notifyHttpListener(Throwable throwable);

    /**
     * Reruns the status of the future.
     *
     * @return the status of the {@code HttpResponseFuture}
     */
    OperationStatus getStatus();

    /**
     * Reset the states associated with the future.
     *
     * Status need to be reset if we are to reuse the future for more than once with sync operation.
     */
    void resetStatus();

    /**
     * Makes the async operation sync.
     *
     * @return Status future
     * @throws InterruptedException if the connection is interrupted
     */
    HttpResponseFuture sync() throws InterruptedException;

    /**
     * Sets response handle listener.
     *
     * @param responseHandleListener the listener that receives events related to the handle
     */
    void setResponseHandleListener(HttpClientConnectorListener responseHandleListener);

    /**
     * Removes the handle listener set to the future.
     */
    void removeResponseHandleListener();

    /**
     * Notifies the response handle listener when there is an event.
     *
     * @param responseHandle the entity which contains the data related to the event
     */
    void notifyResponseHandle(ResponseHandle responseHandle);

    /**
     * Notifies the response handle listener when there is an error.
     *
     * @param throwable the error related to the error
     */
    void notifyResponseHandle(Throwable throwable);

    /**
     * Sets the listener for the promise availability future.
     *
     * @param promiseAvailabilityListener the listener that receives events related to the promise availability.
     */
    void setPromiseAvailabilityListener(HttpClientConnectorListener promiseAvailabilityListener);

    /**
     * Removes the promise availability listener.
     */
    void removePromiseAvailabilityListener();

    /**
     * Notifies the promise listener when there is an event.
     */
    void notifyPromiseAvailability();

    /**
     * Sets listener for the push promise future.
     *
     * @param pushPromiseListener the listener that receives events related to the connector
     */
    void setPushPromiseListener(HttpConnectorListener pushPromiseListener);

    /**
     * Removes the push promise listener set to the future.
     */
    void removePushPromiseListener();

    /**
     * Notifies the push promise listener when there is an event.
     */
    void notifyPushPromise();

    /**
     * Sets the Push Response listener.
     *
     * @param pushResponseListener the push response listener
     * @param promiseId            the promised stream id which the push response is suppose to arrive
     */
    void setPushResponseListener(HttpConnectorListener pushResponseListener, int promiseId);

    /**
     * Removes the Push Response listener.
     *
     * @param promisedId the promised stream id which the push response is suppose to arrive
     */
    void removePushResponseListener(int promisedId);

    /**
     * Notifies push response listener when there is a push response.
     *
     * @param streamId     stream id of the received push response
     * @param pushResponse push response message
     */
    void notifyPushResponse(int streamId, HttpCarbonMessage pushResponse);

    /**
     * Notifies the Push Response Listener when there is an error.
     *
     * @param streamId  stream id of the received or to be received push response
     * @param throwable data related to the error
     */
    void notifyPushResponse(int streamId, Throwable throwable);

}
