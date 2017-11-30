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
import org.ballerinalang.services.ErrorHandlerUtils;
import org.wso2.transport.http.netty.contract.websocket.WebSocketMessage;

/**
 * {@code WebSocketServerConnector} This is the web socket implementation for the {@code BallerinaServerConnector} API.
 *
 * @since 0.94
 */
@JavaSPIService("org.ballerinalang.connector.api.BallerinaServerConnector")
public class WebSocketServerConnector implements BallerinaServerConnector {

    private final WebSocketServicesRegistry webSocketServicesRegistry = new WebSocketServicesRegistry();
    private final WebSocketConnectionManager webSocketConnectionManager = new WebSocketConnectionManager();

    @Override
    public String getProtocolPackage() {
        return Constants.WEBSOCKET_PACKAGE_NAME;
    }

    @Override
    public void serviceRegistered(Service service) throws BallerinaConnectorException {
        WebSocketService wsService = new WebSocketService(service);
        webSocketServicesRegistry.registerService(wsService);
    }

    @Override
    public void serviceUnregistered(Service service) throws BallerinaConnectorException {
        WebSocketService webSocketService = new WebSocketService(service);
        webSocketServicesRegistry.unregisterService(webSocketService);
    }

    @Override
    public void deploymentComplete() throws BallerinaConnectorException {
        webSocketServicesRegistry.validateSeverEndpoints();
    }

    public WebSocketServicesRegistry getWebSocketServicesRegistry() {
        return webSocketServicesRegistry;
    }

    public WebSocketConnectionManager getWebSocketConnectionManager() {
        return webSocketConnectionManager;
    }

    /**
     * This will find the best matching service for given web socket request.
     *
     * @param webSocketMessage incoming message.
     * @return matching service.
     */
    public WebSocketService findService(WebSocketMessage webSocketMessage) {
        if (!webSocketMessage.isServerMessage()) {
            String clientServiceName = webSocketMessage.getTarget();
            WebSocketService clientService =
                    webSocketServicesRegistry.getClientService(clientServiceName);
            if (clientService == null) {
                throw new BallerinaConnectorException("no client service found to handle the service request");
            }
            return clientService;
        }
        try {
            String interfaceId = webSocketMessage.getListenerInterface();
            String serviceUri = webSocketMessage.getTarget();
            serviceUri = webSocketServicesRegistry.refactorUri(serviceUri);
            WebSocketService service = webSocketServicesRegistry.getServiceEndpoint(interfaceId, serviceUri);

            if (service == null) {
                throw new BallerinaConnectorException("no Service found to handle the service request: " + serviceUri);
            }
            return service;
        } catch (Throwable throwable) {
            ErrorHandlerUtils.printError(throwable);
            throw new BallerinaConnectorException("no Service found to handle the service request");
        }
    }
}
