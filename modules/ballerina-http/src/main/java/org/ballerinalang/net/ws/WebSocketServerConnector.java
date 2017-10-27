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
package org.ballerinalang.net.ws;

import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.BallerinaServerConnector;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.net.http.HttpUtil;

/**
 * {@code WebSocketServerConnector} This is the web socket implementation for the {@code BallerinaServerConnector} API.
 *
 * @since 0.94
 */
@JavaSPIService("org.ballerinalang.connector.api.BallerinaServerConnector")
public class WebSocketServerConnector implements BallerinaServerConnector {
    @Override
    public String getProtocolPackage() {
        return Constants.WEBSOCKET_PACKAGE_NAME;
    }

    @Override
    public void serviceRegistered(Service service) throws BallerinaConnectorException {
        WebSocketService wsService = new WebSocketService(service);
        WebSocketServicesRegistry.getInstance().registerService(wsService);
    }

    @Override
    public void serviceUnregistered(Service service) throws BallerinaConnectorException {
        WebSocketService webSocketService = new WebSocketService(service);
        WebSocketServicesRegistry.getInstance().unregisterService(webSocketService);
    }

    @Override
    public void deploymentComplete() throws BallerinaConnectorException {
        WebSocketServicesRegistry.getInstance().validateSeverEndpoints();
        HttpUtil.startPendingHttpConnectors();
    }
}
