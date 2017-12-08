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
import org.ballerinalang.connector.api.ParamDetail;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.services.ErrorHandlerUtils;
import org.wso2.transport.http.netty.contract.websocket.WebSocketBinaryMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketCloseMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketControlMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketControlSignal;
import org.wso2.transport.http.netty.contract.websocket.WebSocketMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketTextMessage;

import java.util.List;
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

    public static void dispatchTextMessage(WsOpenConnectionInfo connectionInfo, WebSocketTextMessage textMessage) {
        WebSocketService wsService = connectionInfo.getService();
        Resource onTextMessageResource = wsService.getResourceByName(Constants.RESOURCE_NAME_ON_TEXT_MESSAGE);
        if (onTextMessageResource == null) {
            return;
        }
        List<ParamDetail> paramDetails = onTextMessageResource.getParamDetails();
        BValue[] bValues = new BValue[paramDetails.size()];
        BStruct wsConnection = connectionInfo.getWsConnection();
        bValues[0] = wsConnection;
        BStruct wsTextFrame = wsService.createTextFrameStruct();
        wsTextFrame.setStringField(0, textMessage.getText());
        if (textMessage.isFinalFragment()) {
            wsTextFrame.setBooleanField(0, 1);
        } else {
            wsTextFrame.setBooleanField(0, 0);
        }
        bValues[1] = wsTextFrame;
        setPathParams(bValues, paramDetails, connectionInfo.getVarialbles(), 2);
        ConnectorFuture future = Executor.submit(onTextMessageResource, null, bValues);
        future.setConnectorFutureListener(new WebSocketEmptyConnFutureListener());
    }

    public static void dispatchBinaryMessage(WsOpenConnectionInfo connectionInfo,
                                             WebSocketBinaryMessage binaryMessage) {
        WebSocketService wsService = connectionInfo.getService();
        Resource onBinaryMessageResource = wsService.getResourceByName(Constants.RESOURCE_NAME_ON_BINARY_MESSAGE);
        if (onBinaryMessageResource == null) {
            return;
        }
        List<ParamDetail> paramDetails = onBinaryMessageResource.getParamDetails();
        BValue[] bValues = new BValue[paramDetails.size()];
        BStruct wsConnection = connectionInfo.getWsConnection();
        bValues[0] = wsConnection;
        BStruct wsBinaryFrame = wsService.createBinaryFrameStruct();
        byte[] data = binaryMessage.getByteArray();
        wsBinaryFrame.setBlobField(0, data);
        if (binaryMessage.isFinalFragment()) {
            wsBinaryFrame.setBooleanField(0, 1);
        } else {
            wsBinaryFrame.setBooleanField(0, 0);
        }
        bValues[1] = wsBinaryFrame;
        setPathParams(bValues, paramDetails, connectionInfo.getVarialbles(), 2);
        ConnectorFuture future = Executor.submit(onBinaryMessageResource, null, bValues);
        future.setConnectorFutureListener(new WebSocketEmptyConnFutureListener());
    }

    public static void dispatchControlMessage(WsOpenConnectionInfo connectionInfo,
                                              WebSocketControlMessage controlMessage) {
        if (controlMessage.getControlSignal() == WebSocketControlSignal.PING) {
            WebSocketDispatcher.dispatchPingMessage(connectionInfo, controlMessage);
        } else if (controlMessage.getControlSignal() == WebSocketControlSignal.PONG) {
            WebSocketDispatcher.dispatchPongMessage(connectionInfo, controlMessage);
        } else {
            throw new BallerinaConnectorException("Received unknown control signal");
        }
    }

    private static void dispatchPingMessage(WsOpenConnectionInfo connectionInfo,
                                            WebSocketControlMessage controlMessage) {
        WebSocketService wsService = connectionInfo.getService();
        Resource onPingMessageResource = wsService.getResourceByName(Constants.RESOURCE_NAME_ON_PING);
        if (onPingMessageResource == null) {
            return;
        }
        List<ParamDetail> paramDetails = onPingMessageResource.getParamDetails();
        BValue[] bValues = new BValue[paramDetails.size()];
        BStruct wsConnection = connectionInfo.getWsConnection();
        bValues[0] = wsConnection;
        BStruct wsPingFrame = wsService.createPingFrameStruct();
        byte[] data = controlMessage.getByteArray();
        wsPingFrame.setBlobField(0, data);
        bValues[1] = wsPingFrame;
        setPathParams(bValues, paramDetails, connectionInfo.getVarialbles(), 2);
        ConnectorFuture future = Executor.submit(onPingMessageResource, null, bValues);
        future.setConnectorFutureListener(new WebSocketEmptyConnFutureListener());
    }

    private static void dispatchPongMessage(WsOpenConnectionInfo connectionInfo,
                                            WebSocketControlMessage controlMessage) {
        WebSocketService wsService = connectionInfo.getService();
        Resource onPongMessageResource = wsService.getResourceByName(Constants.RESOURCE_NAME_ON_PONG);
        if (onPongMessageResource == null) {
            return;
        }
        List<ParamDetail> paramDetails = onPongMessageResource.getParamDetails();
        BValue[] bValues = new BValue[paramDetails.size()];
        BStruct wsConnection = connectionInfo.getWsConnection();
        bValues[0] = wsConnection;
        BStruct wsPongFrame = wsService.createPongFrameStruct();
        byte[] data = controlMessage.getByteArray();
        wsPongFrame.setBlobField(0, data);
        bValues[1] = wsPongFrame;
        setPathParams(bValues, paramDetails, connectionInfo.getVarialbles(), 2);
        ConnectorFuture future = Executor.submit(onPongMessageResource, null, bValues);
        future.setConnectorFutureListener(new WebSocketEmptyConnFutureListener());
    }

    public static void dispatchCloseMessage(WsOpenConnectionInfo connectionInfo, WebSocketCloseMessage closeMessage) {
        WebSocketService wsService = connectionInfo.getService();
        Resource onCloseResource = wsService.getResourceByName(Constants.RESOURCE_NAME_ON_CLOSE);
        if (onCloseResource == null) {
            return;
        }
        List<ParamDetail> paramDetails = onCloseResource.getParamDetails();
        BValue[] bValues = new BValue[paramDetails.size()];
        BStruct wsConnection = connectionInfo.getWsConnection();
        bValues[0] = wsConnection;
        BStruct wsCloseFrame = wsService.createCloseFrameStruct();
        wsCloseFrame.setIntField(0, closeMessage.getCloseCode());
        wsCloseFrame.setStringField(0, closeMessage.getCloseReason());
        bValues[1] = wsCloseFrame;
        setPathParams(bValues, paramDetails, connectionInfo.getVarialbles(), 2);
        ConnectorFuture future = Executor.submit(onCloseResource, null, bValues);
        future.setConnectorFutureListener(new WebSocketEmptyConnFutureListener());
    }

    public static void dispatchIdleTimeout(WsOpenConnectionInfo connectionInfo,
                                           WebSocketControlMessage controlMessage) {
        WebSocketService wsService = connectionInfo.getService();
        Resource onIdleTimeoutResource = wsService.getResourceByName(Constants.RESOURCE_NAME_ON_IDLE_TIMEOUT);
        if (onIdleTimeoutResource == null) {
            return;
        }
        List<ParamDetail> paramDetails = onIdleTimeoutResource.getParamDetails();
        BValue[] bValues = new BValue[paramDetails.size()];
        BStruct wsConnection = connectionInfo.getWsConnection();
        bValues[0] = wsConnection;
        setPathParams(bValues, paramDetails, connectionInfo.getVarialbles(), 1);
        ConnectorFuture future = Executor.submit(onIdleTimeoutResource, null, bValues);
        future.setConnectorFutureListener(new WebSocketEmptyConnFutureListener());
    }

    public static void setPathParams(BValue[] bValues, List<ParamDetail> paramDetails, Map<String, String> variables,
                                      int defaultArgSize) {
        int parameterDetailsSize = paramDetails.size();
        if (parameterDetailsSize > defaultArgSize) {
            for (int i = defaultArgSize; i < parameterDetailsSize; i++) {
                bValues[i]  = new BString(variables.get(paramDetails.get(i).getVarName()));
            }
        }
    }
}
