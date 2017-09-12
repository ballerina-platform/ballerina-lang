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

import org.ballerinalang.model.values.BValue;
import org.wso2.carbon.messaging.CarbonMessage;

/**
 * {@code ConnectorFutureListener} API provides the functionality to listen to ballerina side events after
 * server connector invokes the resource in ballerina side.
 *
 * @since 0.94
 */
public interface ConnectorFutureListener {

    /**
     * Notify success event to the server connector implementation.
     * this will get invoked once resource finished execution.
     */
    void notifySuccess();

    /**
     * Notify success event to the server connector implementation.
     *
     * @param response success response.
     */
    void notifyReply(BValue response);

    /**
     * Notify failure event to the server connector implementation.
     *
     * @param ex failure cause.
     */
    void notifyFailure(BallerinaConnectorException ex);

}
