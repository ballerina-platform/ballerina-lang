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

/**
 * Inlet of inbound messages.
 */
public interface ServerConnector {
    /**
     * Start the server connector which actually opens the port.
     * @return events related to the server connector.
     */
    ServerConnectorFuture start();

    /**
     * Stop the server connector which actually closes the port.
     * @return state of action.
     */
    boolean stop();

    /**
     * Returns the unique ID of the server-connector.
     * @return the id.
     */
    String getConnectorID();
}
