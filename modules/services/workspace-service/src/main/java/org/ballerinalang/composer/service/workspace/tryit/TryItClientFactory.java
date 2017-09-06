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

package org.ballerinalang.composer.service.workspace.tryit;

/**
 * A factory class for try it clients based on protocol.
 */
public class TryItClientFactory {
    
    /**
     * Generates the client based on a protocol.
     *
     * @param protocol   The protocol. {@link TryItConstants}.
     * @param serviceUrl The service url. Example: hostname:port.
     * @return The client.
     */
    public TryItClient getClient(String protocol, String serviceUrl, String clientArgs) {
        if (TryItConstants.HTTP_PROTOCOL.equalsIgnoreCase(protocol)) {
            return new HttpTryItClient(serviceUrl, clientArgs);
        } else {
            return null;
        }
    }
}
