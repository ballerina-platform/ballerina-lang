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

import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.net.http.HttpService;
import org.ballerinalang.runtime.ServerConnectorMessageHandler;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketMessage;

/**
 * {@code WebSocketDispatcher} This is the web socket request dispatcher implementation which finds
 * best matching resource for incoming web socket request.
 *
 * @since 0.94
 */
public class WebSocketDispatcher {

    /**
     * This will find the best matching service for given web socket request.
     *
     * @param message incoming carbon message.
     * @param webSocketMessage incoming message.
     * @return matching service.
     */
    public static HttpService findService(CarbonMessage message, WebSocketMessage webSocketMessage) {
        if (!webSocketMessage.isServerMessage()) {
            String clientServiceName = webSocketMessage.getTarget();
            HttpService clientService = WebSocketServicesRegistry.getInstance().getClientService(clientServiceName);
            if (clientService == null) {
                throw new BallerinaConnectorException("no client service found to handle the service request");
            }
            return clientService;
        }
        try {
            String interfaceId = webSocketMessage.getListenerInterface();
            String serviceUri = webSocketMessage.getTarget();
            serviceUri = WebSocketServicesRegistry.getInstance().refactorUri(serviceUri);

            HttpService service =
                    WebSocketServicesRegistry.getInstance().getServiceEndpoint(interfaceId, serviceUri);

            if (service == null) {
                throw new BallerinaConnectorException("no Service found to handle the service request: " + serviceUri);
            }
            return service;
        } catch (Throwable throwable) {
            ServerConnectorMessageHandler.handleError(message, null, throwable);
            throw new BallerinaConnectorException("no Service found to handle the service request");
        }

    }

    /**
     * This method will find the best matching resource for the given web socket request.
     *
     * @param service matching service.
     * @param annotationName to be searched.
     * @return matching resource.
     */
    public static Resource getResource(HttpService service, String annotationName) {
        for (Resource resource : service.getResources()) {
            if (resource.getAnnotation(Constants.WEBSOCKET_PACKAGE_PATH, annotationName) != null) {
                return resource;
            }
        }
        return null;
    }
}
