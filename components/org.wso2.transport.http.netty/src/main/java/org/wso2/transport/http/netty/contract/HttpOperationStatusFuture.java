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

/**
 * Represents the future events and results of connectors.
 */
public interface HttpOperationStatusFuture {
    /**
     * Set listener for the connector future.
     * @param operationStatusListener that receives events related to the connector.
     */
    void setHttpConnectorListener(HttpOperationStatusListener operationStatusListener);

    /**
     * Remove the listener set to the future.
     */
    void removeHttpOperationListener();

    /**
     * Notify the listeners when there is an event
     * @param operationStatus contains the data related to the error.
     */
    void notifyHttpOperationListener(OperationStatus operationStatus);
}
