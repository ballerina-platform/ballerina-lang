/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.connector.api;

/**
 * {@code ConnectorFuture} object will be returned to the server connector implementation, which can be used to
 * listen to future events in ballerina side.
 * This will also be used in client connector side to notify Ballerina about client responses in asynchronous manner.
 *
 * @since 0.94
 */
public interface ConnectorFuture {

    /**
     * If server connector implementation is interested in future events from ballerina, it needs to set this
     * {@code ConnectorFutureListener} object.
     * This will also be used in Ballerina side, if it is interested in client side events.
     *
     * @param futureListener to listen to future events.
     */
    void registerConnectorFutureListener(ConnectorFutureListener futureListener);
}
