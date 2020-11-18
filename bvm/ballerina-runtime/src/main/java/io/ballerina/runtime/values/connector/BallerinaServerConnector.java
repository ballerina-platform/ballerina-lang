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
package io.ballerina.runtime.values.connector;

import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.util.exceptions.BallerinaConnectorException;

import java.util.List;

/**
 * {@code ServerConnector} This API provides the functionality to register ballerina servers in respective server
 * connector.
 *
 * @since 0.94
 */
public interface BallerinaServerConnector {

    /**
     * This should return relevant protocol package paths.
     *
     * @return relevant protocol package paths.
     */
    List<String> getProtocolPackages();

    /**
     * This will fire a service registration event to the server connector implementation.
     *
     * @param service to be registered.
     * @throws BallerinaConnectorException if an error occurs
     */
    void serviceRegistered(BObject service) throws BallerinaConnectorException;

    /**
     * This will fire a deployment complete event so to the server connector implementation.
     *
     * @throws BallerinaConnectorException if an error occurs
     */
    void deploymentComplete() throws BallerinaConnectorException;

}
