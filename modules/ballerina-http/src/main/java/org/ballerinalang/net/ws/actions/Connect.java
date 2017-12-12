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

package org.ballerinalang.net.ws.actions;

import org.ballerinalang.bre.Context;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.ConnectorFuture;
import org.ballerinalang.connector.api.ConnectorUtils;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BConnector;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.nativeimpl.actions.ClientConnectorFuture;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaAction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.http.BallerinaHttpServerConnector;
import org.ballerinalang.net.ws.BallerinaWsClientConnectorListener;
import org.ballerinalang.net.ws.Constants;
import org.ballerinalang.net.ws.WebSocketService;
import org.ballerinalang.net.ws.WsOpenConnectionInfo;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contract.websocket.HandshakeFuture;
import org.wso2.transport.http.netty.contract.websocket.HandshakeListener;
import org.wso2.transport.http.netty.contract.websocket.WebSocketClientConnector;
import org.wso2.transport.http.netty.contract.websocket.WsClientConnectorConfig;
import org.wso2.transport.http.netty.contractimpl.HttpWsConnectorFactoryImpl;

import java.util.HashMap;
import javax.websocket.Session;

/**
 * Connect to the remote endpoint.
 *
 * @since 0.94
 */
@BallerinaAction(
        packageName = "ballerina.net.ws",
        actionName = "connect",
        connectorName = Constants.CONNECTOR_NAME,
        args = {
                @Argument(name = "c", type = TypeKind.CONNECTOR),
                @Argument(name = "clientConnectorConfig", type = TypeKind.STRUCT, structType = "ClientConnectorConfig",
                          structPackage = Constants.PROTOCOL_PACKAGE_WS)
        },
        returnType = {@ReturnType(type = TypeKind.STRUCT, structType = "Connection",
                                  structPackage = "ballerina.net.ws")}
)
public class Connect extends AbstractNativeWsAction {

    @Override
    public ConnectorFuture execute(Context context) {
        BConnector bconnector = (BConnector) getRefArgument(context, 0);
        BStruct clientConfig = (BStruct) getRefArgument(context, 1);
        String remoteUrl = getUrlFromConnector(bconnector);
        String clientServiceName = getClientServiceNameFromConnector(bconnector);
        BallerinaHttpServerConnector httpServerConnector = (BallerinaHttpServerConnector) ConnectorUtils.
                getBallerinaServerConnector(context, Constants.HTTP_PACKAGE_PATH);
        final WebSocketService wsService =
                httpServerConnector.getWebSocketServicesRegistry().getClientService(clientServiceName);
        if (wsService == null) {
            throw new BallerinaConnectorException("Cannot find client service: " + clientServiceName);
        }

        BRefType bSubProtocolsBRefType = clientConfig.getRefField(0);
        String wsParentConnectionID = clientConfig.getStringField(0);
        BRefType<BMap<BString, BString>> bCustomHeaders = clientConfig.getRefField(1);
        int idleTimeoutInSeconds =  (int) clientConfig.getIntField(0);
        WsClientConnectorConfig clientConnectorConfig = new WsClientConnectorConfig(remoteUrl);
        clientConnectorConfig.setTarget(clientServiceName);
        if (bSubProtocolsBRefType != null) {
            clientConnectorConfig.setSubProtocols(getSubProtocols(bSubProtocolsBRefType));
        }
        if (bCustomHeaders != null) {
            clientConnectorConfig.addHeaders(getCustomHeaders(bCustomHeaders));
        }
        if (idleTimeoutInSeconds > 0) {
            clientConnectorConfig.setIdleTimeoutInMillis(idleTimeoutInSeconds * 1000);
        }

        ClientConnectorFuture connectorFuture = new ClientConnectorFuture();
        HttpWsConnectorFactory connectorFactory = new HttpWsConnectorFactoryImpl();
        WebSocketClientConnector clientConnector =
                connectorFactory.createWsClientConnector(clientConnectorConfig);
        BallerinaWsClientConnectorListener clientConnectorListener = new BallerinaWsClientConnectorListener();
        HandshakeFuture handshakeFuture = clientConnector.connect(clientConnectorListener);
        handshakeFuture.setHandshakeListener(new HandshakeListener() {
            @Override
            public void onSuccess(Session session) {
                BStruct wsConnection = createWsConnectionStruct(wsService, session, wsParentConnectionID);
                context.getControlStackNew().currentFrame.returnValues[0] = wsConnection;
                WsOpenConnectionInfo connectionInfo =
                        new WsOpenConnectionInfo(wsService, wsConnection, new HashMap<>());
                clientConnectorListener.setConnectionInfo(connectionInfo);
                connectorFuture.notifySuccess();
            }

            @Override
            public void onError(Throwable t) {
                BStruct wsError = createWsErrorStruct(context, t);
                context.getControlStackNew().currentFrame.returnValues[1] = wsError;
                connectorFuture.notifySuccess();
            }
        });
        return connectorFuture;
    }
}
