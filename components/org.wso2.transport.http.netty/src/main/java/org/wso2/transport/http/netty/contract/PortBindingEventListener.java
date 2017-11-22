/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.transport.http.netty.contract;

/**
 * An event listener to listen to port binding/unbinding events.
 */
public interface PortBindingEventListener {

    /**
     * Trigger an onOpen event when a connector is successfully up and running.
     *
     * @param serverConnectorId The ID of the server connecter which just started.
     * @param isHttps Specifies whether the server connector is using HTTPS.
     */
    void onOpen(String serverConnectorId, boolean isHttps);

    /**
     * Trigger an onClose event when a connector has successfully stopped.
     *
     * @param serverConnectorId The ID of the server connecter which just stopped.
     * @param isHttps Specifies whether the server connector is using HTTPS.
     */
    void onClose(String serverConnectorId, boolean isHttps);

    /**
     * Trigger an onError event when there is an error in starting the connector. This is usually a BindException.
     *
     * @param throwable The exception thrown when attempting to start the connector.
     */
    void onError(Throwable throwable);
}
