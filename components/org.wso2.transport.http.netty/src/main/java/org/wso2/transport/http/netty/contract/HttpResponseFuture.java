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
import org.wso2.transport.http.netty.message.ResponseHandle;

/**
 * Represents the future events and results of connectors.
 */
public interface HttpResponseFuture {
    /**
     * Set listener for the connector future.
     * @param connectorListener that receive http message events related to the connector.
     */
    void setHttpConnectorListener(HttpConnectorListener connectorListener);

    /**
     * Remove the message listener set to the future.
     */
    void removeHttpListener();

    /**
     * Notify the http message listener when there is an event
     * @param httpMessage contains the data related to the event.
     */
    void notifyHttpListener(HTTPCarbonMessage httpMessage);

    /**
     * Notify the listener when there is an event
     * @param throwable contains the data related to the error.
     */
    void notifyHttpListener(Throwable throwable);

    /**
     * Rerun the status of the future.
     * @return status
     */
    OperationStatus getStatus();

    /**
     * Let make the async operation sync.
     * @return Status future
     * @throws InterruptedException if the connection is interrupted
     */
    HttpResponseFuture sync() throws InterruptedException;

    /**
     * Set listener for the promise availability future
     *
     * @param promiseAvailabilityListener listener that receives events related to the promise availability.
     */
    void setPromiseAvailabilityListener(HttpConnectorListener promiseAvailabilityListener);

    /**
     * Notify the promise listener when there is an event
     */
    void notifyPromiseAvailability();

    /**
     * Remove the promise availability listener
     */
    void removePromiseAvailabilityListener();

    /**
     * Set listener for the push promise future.
     *
     * @param pushPromiseListener that receives events related to the connector.
     */
    void setPushPromiseListener(HttpConnectorListener pushPromiseListener);

    /**
     * Remove the push promise listener set to the future.
     */
    void removePushPromiseListener();

    /**
     * Notify the push promise listener when there is an event
     */
    void notifyPushPromise();

    /**
     * Set response handle listener
     *
     * @param responseHandleListener listener that receives events related to the handle.
     */
    void setResponseHandleListener(HttpConnectorListener responseHandleListener);

    /**
     * Remove the handle listener set to the future.
     */
    void removeResponseHandleListener();

    /**
     * Notify the response handle listener when there is an event
     *
     * @param responseHandle contains the data related to the event.
     */
    void notifyResponseHandle(ResponseHandle responseHandle);


}
