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
package org.ballerinalang.net.http;

import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.BallerinaServerConnector;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.net.ws.WebSocketService;
import org.ballerinalang.net.ws.WebSocketServicesRegistry;

import java.util.LinkedList;
import java.util.List;

import static org.ballerinalang.net.ws.Constants.PROTOCOL_PACKAGE_HTTP;
import static org.ballerinalang.net.ws.Constants.PROTOCOL_PACKAGE_WS;

/**
 * {@code HttpServerConnector} This is the http implementation for the {@code BallerinaServerConnector} API.
 *
 * @since 0.94
 */
@JavaSPIService("org.ballerinalang.connector.api.BallerinaServerConnector")
public class BallerinaHttpServerConnector implements BallerinaServerConnector {
    private final WebSocketServicesRegistry webSocketServicesRegistry = new WebSocketServicesRegistry();
    private final HTTPServicesRegistry httpServicesRegistry = new HTTPServicesRegistry(webSocketServicesRegistry);

    public BallerinaHttpServerConnector() {
    }

    @Override
    public List<String> getProtocolPackages() {
        List<String> protocolPackages = new LinkedList<>();
        protocolPackages.add(PROTOCOL_PACKAGE_HTTP);
        protocolPackages.add(PROTOCOL_PACKAGE_WS);
        return protocolPackages;
    }

    @Override
    public void serviceRegistered(Service service) throws BallerinaConnectorException {
        if (service.getProtocolPackage().equals(PROTOCOL_PACKAGE_HTTP)) {
            HttpService httpService = new HttpService(service);
            httpServicesRegistry.registerService(httpService);
        } else if (service.getProtocolPackage().equals(PROTOCOL_PACKAGE_WS)) {
            WebSocketService wsService = new WebSocketService(service);
            webSocketServicesRegistry.registerService(wsService);
        }
    }

    @Override
    public void deploymentComplete() throws BallerinaConnectorException {
        webSocketServicesRegistry.completeDeployment();
        HttpUtil.startPendingHttpConnectors(this);
    }

    public WebSocketServicesRegistry getWebSocketServicesRegistry() {
        return webSocketServicesRegistry;
    }

    public HTTPServicesRegistry getHttpServicesRegistry() {
        return httpServicesRegistry;
    }
}
