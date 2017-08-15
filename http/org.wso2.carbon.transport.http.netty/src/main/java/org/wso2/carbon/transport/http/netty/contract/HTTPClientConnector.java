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

package org.wso2.carbon.transport.http.netty.contract;

import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;

/**
 * Allows to send outbound messages.
 */
public interface HTTPClientConnector {
    /**
     * Creates the connection to the back-end.
     * @return the future that can be used to get future events of the connection.
     */
    HTTPClientConnectorFuture connect();

    /**
     * Send httpMessages to the back-end in asynchronous manner.
     * @return returns the status of the asynchronous send action.
     */
    HTTPClientConnectorFuture send(HTTPCarbonMessage httpCarbonMessage) throws Exception;

    /**
     * Close the connection related to this connector.
     * @return return the status of the close action.
     */
    boolean close();
}
