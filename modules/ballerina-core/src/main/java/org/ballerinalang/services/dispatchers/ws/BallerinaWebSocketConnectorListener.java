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
 */

package org.ballerinalang.services.dispatchers.ws;

import org.ballerinalang.runtime.ServerConnectorMessageHandler;
import org.ballerinalang.util.codegen.ResourceInfo;
import org.ballerinalang.util.codegen.ServiceInfo;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketBinaryMessage;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketCloseMessage;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketConnectorListener;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketControlMessage;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketInitMessage;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketMessage;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketTextMessage;
import org.wso2.carbon.transport.http.netty.message.HTTPMessageUtil;

import java.net.ProtocolException;
import javax.websocket.Session;

/**
 * Ballerina Connector listener for WebSocket.
 *
 * @since 0.93
 */
public class BallerinaWebSocketConnectorListener implements WebSocketConnectorListener {

    @Override
    public void onMessage(WebSocketInitMessage webSocketInitMessage) {
        try {
            Session session = webSocketInitMessage.handshake();
            CarbonMessage carbonMessage = HTTPMessageUtil.convertWebSocketInitMessage(webSocketInitMessage);
            ServiceInfo service = findService(carbonMessage, webSocketInitMessage);
            WebSocketConnectionManager.getInstance().addServerSession(service, session, carbonMessage);
            ResourceInfo resource = getResource(service, Constants.ANNOTATION_NAME_ON_OPEN);
            dispatchMessage(carbonMessage, service, resource);
        } catch (ProtocolException e) {
            throw new BallerinaException("Error occurred during WebSocket handshake", e);
        }

    }

    @Override
    public void onMessage(WebSocketTextMessage webSocketTextMessage) {
        CarbonMessage carbonMessage = HTTPMessageUtil.convertWebSocketTextMessage(webSocketTextMessage);
        ServiceInfo service = findService(carbonMessage, webSocketTextMessage);
        ResourceInfo resource = getResource(service, Constants.ANNOTATION_NAME_ON_TEXT_MESSAGE);
        dispatchMessage(carbonMessage, service, resource);
    }

    @Override
    public void onMessage(WebSocketBinaryMessage webSocketBinaryMessage) {
        throw new BallerinaException("Binary messages are not supported!");
    }

    @Override
    public void onMessage(WebSocketControlMessage webSocketControlMessage) {
        throw new BallerinaException("Pong messages are not supported!");
    }

    @Override
    public void onMessage(WebSocketCloseMessage webSocketCloseMessage) {
        CarbonMessage carbonMessage = HTTPMessageUtil.convertWebSocketCloseMessage(webSocketCloseMessage);
        Session serverSession = webSocketCloseMessage.getChannelSession();
        WebSocketConnectionManager.getInstance().removeSessionFromAll(serverSession);
        ServiceInfo service = findService(carbonMessage, webSocketCloseMessage);
        ResourceInfo resource = getResource(service, Constants.ANNOTATION_NAME_ON_CLOSE);
        dispatchMessage(carbonMessage, service, resource);
    }

    @Override
    public void onError(Throwable throwable) {
        throw new BallerinaException("Unexpected error occurred in WebSocket transport", throwable);
    }

    private void dispatchMessage(CarbonMessage message, ServiceInfo service, ResourceInfo resource) {
        if (resource != null) {
            ServerConnectorMessageHandler.
                    invokeResource(message, null, Constants.PROTOCOL_WEBSOCKET, resource, service);
        }
    }

    private ServiceInfo findService(CarbonMessage message, WebSocketMessage webSocketMessage) {
        if (webSocketMessage.isServerMessage()) {
            try {
                String interfaceId = webSocketMessage.getListenerInterface();
                String serviceUri = webSocketMessage.getTarget();
                serviceUri = WebSocketServicesRegistry.getInstance().refactorUri(serviceUri);
                if (serviceUri == null) {
                    throw new BallerinaException("no Service found to handle the service request: " + serviceUri);
                }
                ServiceInfo service =
                        WebSocketServicesRegistry.getInstance().getServiceEndpoint(interfaceId, serviceUri);

                if (service == null) {
                    throw new BallerinaException("no Service found to handle the service request: " + serviceUri);
                }
                return service;
            } catch (Throwable throwable) {
                ServerConnectorMessageHandler.handleError(message, null, throwable);
                throw new BallerinaException("no Service found to handle the service request");
            }
        } else {
            String clientServiceName = webSocketMessage.getTarget();
            ServiceInfo clientService = WebSocketServicesRegistry.getInstance().getClientService(clientServiceName);
            if (clientService == null) {
                throw new BallerinaException("no client service found to handle the service request");
            }
            return clientService;
        }

    }

    private ResourceInfo getResource(ServiceInfo service, String annotationName) {
        for (ResourceInfo resource : service.getResourceInfoEntries()) {
            if (resource.getAnnotationAttachmentInfo(Constants.WEBSOCKET_PACKAGE_PATH, annotationName) != null) {
                return resource;
            }
        }
        return null;
    }
}
