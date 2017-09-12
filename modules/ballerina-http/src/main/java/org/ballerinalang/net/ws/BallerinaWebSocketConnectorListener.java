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

package org.ballerinalang.net.ws;

import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.ConnectorFuture;
import org.ballerinalang.connector.api.ConnectorFutureListener;
import org.ballerinalang.connector.api.Executor;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.http.HttpService;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketBinaryMessage;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketCloseMessage;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketConnectorListener;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketControlMessage;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketInitMessage;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketMessage;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketTextMessage;

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
        WebSocketService wsService = WebSocketDispatcher.findService(webSocketInitMessage);
        Resource onHandshakeResource = wsService.getResourceByName(Constants.RESOURCE_NAME_ON_HANDSHAKE);
        if (onHandshakeResource != null) {
            BValue[] bValues = new BValue[0];
            ConnectorFuture future = Executor.submit(onHandshakeResource, bValues);
            future.setConnectorFutureListener(new ConnectorFutureListener() {
                @Override
                public void notifySuccess(BValue response) {
                   handleHandshake(webSocketInitMessage, wsService);
                }

                @Override
                public void notifyFailure(BallerinaConnectorException ex) {
                    throw new BallerinaException("Error: " + ex.getMessage());
                }
            });

        } else {
            handleHandshake(webSocketInitMessage, wsService);
        }
    }

    @Override
    public void onMessage(WebSocketTextMessage webSocketTextMessage) {
        WebSocketService wsService = WebSocketDispatcher.findService(webSocketTextMessage);
        Resource onTextMessageResource =
                WebSocketDispatcher.getResource(wsService, Constants.RESOURCE_NAME_ON_TEXT_MESSAGE);
        if (onTextMessageResource == null) {
            return;
        }
        BStruct wsConnection = getWSConnection(webSocketTextMessage);

        BStruct wsTextFrame = wsService.createTextFrameStruct();
        wsTextFrame.setStringField(0, webSocketTextMessage.getText());
        if (webSocketTextMessage.isFinalFragment()) {
            wsTextFrame.setBooleanField(0, 1);
        } else {
            wsTextFrame.setBooleanField(0, 0);
        }
        BValue[] bValues = {wsConnection, wsTextFrame};
        Executor.submit(onTextMessageResource, bValues);
    }

    @Override
    public void onMessage(WebSocketBinaryMessage webSocketBinaryMessage) {
        WebSocketService wsService = WebSocketDispatcher.findService(webSocketBinaryMessage);
        Resource onBinaryMessageResource =
                WebSocketDispatcher.getResource(wsService, Constants.RESOURCE_NAME_ON_BINARY_MESSAGE);
        if (onBinaryMessageResource == null) {
            return;
        }
        BStruct wsConnection = getWSConnection(webSocketBinaryMessage);
        BStruct wsBinaryFrame = wsService.createBinaryFrameStruct();
        wsBinaryFrame.setBlobField(0, webSocketBinaryMessage.getByteArray());
        if (webSocketBinaryMessage.isFinalFragment()) {
            wsBinaryFrame.setBooleanField(0, 1);
        } else {
            wsBinaryFrame.setBooleanField(0, 0);
        }
        BValue[] bValues = {wsConnection, wsBinaryFrame};
        Executor.execute(onBinaryMessageResource, bValues);
    }

    @Override
    public void onMessage(WebSocketControlMessage webSocketControlMessage) {
        throw new BallerinaConnectorException("Pong messages are not supported!");
    }

    @Override
    public void onMessage(WebSocketCloseMessage webSocketCloseMessage) {
        WebSocketService wsService = WebSocketDispatcher.findService(webSocketCloseMessage);
        Resource onCloseResource = WebSocketDispatcher.getResource(wsService, Constants.RESOURCE_NAME_ON_CLOSE);
        if (onCloseResource == null) {
            return;
        }
        BStruct wsConnection = getWSConnection(webSocketCloseMessage);
        BStruct wsCloseFrame = wsService.createCloseFrameStruct();
        wsCloseFrame.setIntField(0, webSocketCloseMessage.getCloseCode());
        wsCloseFrame.setStringField(0, webSocketCloseMessage.getCloseReason());

        BValue[] bValues = {wsConnection, wsCloseFrame};
        Executor.submit(onCloseResource, bValues);
    }

    @Override
    public void onError(Throwable throwable) {
        throw new BallerinaConnectorException("Unexpected error occurred in WebSocket transport", throwable);
    }

    @Override
    public void onIdleTimeout(WebSocketControlMessage controlMessage) {
        WebSocketService wsService = WebSocketDispatcher.findService(controlMessage);
        Resource onIdleTimeoutResource =
                WebSocketDispatcher.getResource(wsService, Constants.RESOURCE_NAME_ON_IDLE_TIMEOUT);
        if (onIdleTimeoutResource == null) {
            return;
        }
        BStruct wsConnection = getWSConnection(controlMessage);
        BValue[] bValues = {wsConnection};
        Executor.execute(onIdleTimeoutResource, bValues);
    }


    private void handleHandshake(WebSocketInitMessage initMessage, WebSocketService wsService) {
        try {
            Session session = initMessage.handshake();
            BStruct wsConnection = wsService.createConnectionStruct();
            wsConnection.addNativeData(Constants.NATIVE_DATA_WEBSOCKET_SESSION, session);
            wsConnection.addNativeData(Constants.WEBSOCKET_MESSAGE, initMessage);

            WebSocketConnectionManager.getInstance().addConnection(session.getId(), wsConnection);

            Resource onOpenResource = wsService.getResourceByName(Constants.RESOURCE_NAME_ON_OPEN);
            BValue[] bValues = {wsConnection};
            if (onOpenResource == null) {
                return;
            }
            Executor.submit(onOpenResource, bValues);
        } catch (ProtocolException e) {
            throw new BallerinaException("Error occurred during handshake");
        }
    }

    private BStruct getWSConnection(WebSocketMessage webSocketMessage) {
        return WebSocketConnectionManager.getInstance().getConnection(webSocketMessage.getChannelSession().getId());
    }
















    ////////////////////////////


    @Override
    public void onMessage(WebSocketInitMessage webSocketInitMessage) {
        try {
            Session session = webSocketInitMessage.handshake();
            CarbonMessage carbonMessage = HTTPMessageUtil.convertWebSocketInitMessage(webSocketInitMessage);
            HttpService service = WebSocketDispatcher.findService(carbonMessage, webSocketInitMessage);
            WebSocketConnectionManager.getInstance().addServerSession(service, session, carbonMessage);
            Resource resource = WebSocketDispatcher.getResource(service, Constants.ANNOTATION_NAME_ON_OPEN);
            submitResource(resource, carbonMessage);
        } catch (ProtocolException e) {
            throw new BallerinaConnectorException("Error occurred during WebSocket handshake", e);
        }
    }

    @Override
    public void onMessage(WebSocketTextMessage webSocketTextMessage) {
        CarbonMessage carbonMessage = HTTPMessageUtil.convertWebSocketTextMessage(webSocketTextMessage);
        HttpService service = WebSocketDispatcher.findService(carbonMessage, webSocketTextMessage);
        Resource resource = WebSocketDispatcher.getResource(service, Constants.ANNOTATION_NAME_ON_TEXT_MESSAGE);
        submitResource(resource, carbonMessage);
    }

    @Override
    public void onMessage(WebSocketBinaryMessage webSocketBinaryMessage) {
        throw new BallerinaConnectorException("Binary messages are not supported!");
    }

    @Override
    public void onMessage(WebSocketControlMessage webSocketControlMessage) {
        throw new BallerinaConnectorException("Pong messages are not supported!");
    }

    @Override
    public void onMessage(WebSocketCloseMessage webSocketCloseMessage) {
        CarbonMessage carbonMessage = HTTPMessageUtil.convertWebSocketCloseMessage(webSocketCloseMessage);
        Session serverSession = webSocketCloseMessage.getChannelSession();
        WebSocketConnectionManager.getInstance().removeSessionFromAll(serverSession);
        HttpService service = WebSocketDispatcher.findService(carbonMessage, webSocketCloseMessage);
        Resource resource = WebSocketDispatcher.getResource(service, Constants.ANNOTATION_NAME_ON_CLOSE);
        submitResource(resource, carbonMessage);
    }

    @Override
    public void onError(Throwable throwable) {
        throw new BallerinaConnectorException("Unexpected error occurred in WebSocket transport", throwable);
    }

    private void submitResource(Resource resource, CarbonMessage carbonMessage) {
        ConnectorFuture connectorFuture = Executor.submit(resource, carbonMessage);
        ConnectorFutureListener futureListener = new WebSocketConnectorFutureListener();
        connectorFuture.setConnectorFutureListener(futureListener);
    }
}
