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
import org.ballerinalang.connector.api.ConnectorFuture;
import org.ballerinalang.connector.api.Executor;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.services.ErrorHandlerUtils;
import org.wso2.transport.http.netty.contract.websocket.WebSocketBinaryMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketCloseMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketControlMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketControlSignal;
import org.wso2.transport.http.netty.contract.websocket.WebSocketMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketTextMessage;

import java.util.Map;

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
     * @param webSocketMessage incoming message.
     * @return matching service.
     */
    public static WebSocketService findService(WebSocketServicesRegistry servicesRegistry,
                                               Map<String, String> variables, WebSocketMessage webSocketMessage) {
        if (!webSocketMessage.isServerMessage()) {
            String clientServiceName = webSocketMessage.getTarget();
            WebSocketService clientService = servicesRegistry.getClientService(clientServiceName);
            if (clientService == null) {
                throw new BallerinaConnectorException("no client service found to handle the service request");
            }
            return clientService;
        }
        try {
            String interfaceId = webSocketMessage.getListenerInterface();
            String serviceUri = webSocketMessage.getTarget();
            serviceUri = servicesRegistry.refactorUri(serviceUri);

            WebSocketService service = servicesRegistry.matchServiceEndpoint(interfaceId, serviceUri, variables);

            if (service == null) {
                throw new BallerinaConnectorException("no Service found to handle the service request: " + serviceUri);
            }
            return service;
        } catch (Throwable throwable) {
            ErrorHandlerUtils.printError(throwable);
            throw new BallerinaConnectorException("no Service found to handle the service request");
        }
    }

    public static void dispatchTextMessage(WebSocketService wsService, WebSocketTextMessage textMessage) {
        Resource onTextMessageResource = wsService.getResourceByName(Constants.RESOURCE_NAME_ON_TEXT_MESSAGE);
        if (onTextMessageResource == null) {
            return;
        }
        BStruct wsConnection = getWSConnection(textMessage);

        BStruct wsTextFrame = wsService.createTextFrameStruct();
        wsTextFrame.setStringField(0, textMessage.getText());
        if (textMessage.isFinalFragment()) {
            wsTextFrame.setBooleanField(0, 1);
        } else {
            wsTextFrame.setBooleanField(0, 0);
        }
        BValue[] bValues = {wsConnection, wsTextFrame};
        ConnectorFuture future = Executor.submit(onTextMessageResource, null, bValues);
        future.setConnectorFutureListener(new WebSocketEmptyConnFutureListener());
    }

    public static void dispatchBinaryMessage(WebSocketService wsService, WebSocketBinaryMessage binaryMessage) {
        Resource onBinaryMessageResource = wsService.getResourceByName(Constants.RESOURCE_NAME_ON_BINARY_MESSAGE);
        if (onBinaryMessageResource == null) {
            return;
        }
        BStruct wsConnection = getWSConnection(binaryMessage);
        BStruct wsBinaryFrame = wsService.createBinaryFrameStruct();
        byte[] data = binaryMessage.getByteArray();
        wsBinaryFrame.setBlobField(0, data);
        if (binaryMessage.isFinalFragment()) {
            wsBinaryFrame.setBooleanField(0, 1);
        } else {
            wsBinaryFrame.setBooleanField(0, 0);
        }
        BValue[] bValues = {wsConnection, wsBinaryFrame};
        ConnectorFuture future = Executor.submit(onBinaryMessageResource, null, bValues);
        future.setConnectorFutureListener(new WebSocketEmptyConnFutureListener());
    }

    public static void dispatchControlMessage(WebSocketService wsService, WebSocketControlMessage controlMessage) {
        if (controlMessage.getControlSignal() == WebSocketControlSignal.PING) {
            WebSocketDispatcher.dispatchPingMessage(wsService, controlMessage);
        } else if (controlMessage.getControlSignal() == WebSocketControlSignal.PONG) {
            WebSocketDispatcher.dispatchPongMessage(wsService, controlMessage);
        } else {
            throw new BallerinaConnectorException("Received unknown control signal");
        }
    }

    private static void dispatchPingMessage(WebSocketService wsService, WebSocketControlMessage controlMessage) {
        Resource onPingMessageResource = wsService.getResourceByName(Constants.RESOURCE_NAME_ON_PING);
        if (onPingMessageResource == null) {
            return;
        }
        BStruct wsConnection = getWSConnection(controlMessage);
        BStruct wsPingFrame = wsService.createPingFrameStruct();
        byte[] data = controlMessage.getByteArray();
        wsPingFrame.setBlobField(0, data);
        BValue[] bValues = {wsConnection, wsPingFrame};
        ConnectorFuture future = Executor.submit(onPingMessageResource, null, bValues);
        future.setConnectorFutureListener(new WebSocketEmptyConnFutureListener());
    }

    private static void dispatchPongMessage(WebSocketService wsService, WebSocketControlMessage controlMessage) {
        Resource onPongMessageResource = wsService.getResourceByName(Constants.RESOURCE_NAME_ON_PONG);
        if (onPongMessageResource == null) {
            return;
        }
        BStruct wsConnection = getWSConnection(controlMessage);
        BStruct wsPongFrame = wsService.createPongFrameStruct();
        byte[] data = controlMessage.getByteArray();
        wsPongFrame.setBlobField(0, data);
        BValue[] bValues = {wsConnection, wsPongFrame};
        ConnectorFuture future = Executor.submit(onPongMessageResource, null, bValues);
        future.setConnectorFutureListener(new WebSocketEmptyConnFutureListener());
    }

    public static void dispatchCloseMessage(WebSocketService wsService, WebSocketCloseMessage closeMessage) {
        Resource onCloseResource = wsService.getResourceByName(Constants.RESOURCE_NAME_ON_CLOSE);
        if (onCloseResource == null) {
            return;
        }
        BStruct wsConnection = removeConnection(closeMessage);
        BStruct wsCloseFrame = wsService.createCloseFrameStruct();
        wsCloseFrame.setIntField(0, closeMessage.getCloseCode());
        wsCloseFrame.setStringField(0, closeMessage.getCloseReason());

        BValue[] bValues = {wsConnection, wsCloseFrame};
        ConnectorFuture future = Executor.submit(onCloseResource, null, bValues);
        future.setConnectorFutureListener(new WebSocketEmptyConnFutureListener());
    }

    public static void dispatchIdleTimeout(WebSocketService wsService, WebSocketControlMessage controlMessage) {
        Resource onIdleTimeoutResource = wsService.getResourceByName(Constants.RESOURCE_NAME_ON_IDLE_TIMEOUT);
        if (onIdleTimeoutResource == null) {
            return;
        }
        BStruct wsConnection = getWSConnection(controlMessage);
        BValue[] bValues = {wsConnection};
        ConnectorFuture future = Executor.submit(onIdleTimeoutResource, null, bValues);
        future.setConnectorFutureListener(new WebSocketEmptyConnFutureListener());
    }

    private static BStruct getWSConnection(WebSocketMessage webSocketMessage) {
        return WebSocketConnectionManager.getInstance().getConnection(webSocketMessage.getChannelSession().getId());
    }

    private static BStruct removeConnection(WebSocketMessage webSocketMessage) {
        return WebSocketConnectionManager.getInstance().removeConnection(webSocketMessage.getChannelSession().getId());
    }
}
