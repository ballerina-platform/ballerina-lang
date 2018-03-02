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
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.nativeimpl.actions.ClientConnectorFuture;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaAction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.http.BallerinaHttpServerConnector;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.net.ws.BallerinaWsClientConnectorListener;
import org.ballerinalang.net.ws.WebSocketConstants;
import org.ballerinalang.net.ws.WebSocketService;
import org.ballerinalang.net.ws.WsOpenConnectionInfo;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contract.websocket.HandshakeFuture;
import org.wso2.transport.http.netty.contract.websocket.HandshakeListener;
import org.wso2.transport.http.netty.contract.websocket.WebSocketClientConnector;
import org.wso2.transport.http.netty.contract.websocket.WsClientConnectorConfig;

import java.util.HashMap;
import javax.websocket.Session;

/**
 * Connect to the remote endpoint.
 *
 * @since 0.94
 */
@BallerinaAction(
        packageName = "ballerina.net.ws",
        actionName = "connectWithDefault",
        connectorName = WebSocketConstants.CONNECTOR_NAME,
        args = {
                @Argument(name = "c", type = TypeKind.CONNECTOR)
        },
        returnType = {@ReturnType(type = TypeKind.STRUCT, structType = "Connection",
                                  structPackage = "ballerina.net.ws")}
)
public class ConnectWithDefault extends AbstractNativeWsAction {
    @Override
    public ConnectorFuture execute(Context context) {
        BConnector bconnector = (BConnector) getRefArgument(context, 0);
        String remoteUrl = getUrlFromConnector(bconnector);
        String clientServiceName = getClientServiceNameFromConnector(bconnector);
        BallerinaHttpServerConnector httpServerConnector = (BallerinaHttpServerConnector) ConnectorUtils.
                getBallerinaServerConnector(context, HttpConstants.HTTP_PACKAGE_PATH);
        final WebSocketService wsService =
                httpServerConnector.getWebSocketServicesRegistry().getClientService(clientServiceName);
        if (wsService == null) {
            throw new BallerinaConnectorException("Cannot find client service: " + clientServiceName);
        }

        WsClientConnectorConfig clientConnectorConfig = new WsClientConnectorConfig(remoteUrl);
        clientConnectorConfig.setTarget(clientServiceName);
        HttpWsConnectorFactory connectorFactory = HttpUtil.createHttpWsConnectionFactory();

        ClientConnectorFuture connectorFuture = new ClientConnectorFuture();
        WebSocketClientConnector clientConnector =
                connectorFactory.createWsClientConnector(clientConnectorConfig);
        BallerinaWsClientConnectorListener clientConnectorListener = new BallerinaWsClientConnectorListener();
        HandshakeFuture handshakeFuture = clientConnector.connect(clientConnectorListener);
        handshakeFuture.setHandshakeListener(new HandshakeListener() {
            @Override
            public void onSuccess(Session session) {
                BStruct wsConnection = createWsConnectionStruct(wsService, session, null);
                context.getControlStack().currentFrame.returnValues[0] = wsConnection;
                WsOpenConnectionInfo connectionInfo =
                        new WsOpenConnectionInfo(wsService, wsConnection, new HashMap<>());
                clientConnectorListener.setConnectionInfo(connectionInfo);
                connectorFuture.notifySuccess();
            }

            @Override
            public void onError(Throwable t) {
                BStruct wsError = createWsErrorStruct(context, t);
                context.getControlStack().currentFrame.returnValues[1] = wsError;
                connectorFuture.notifySuccess();
            }
        });
        return connectorFuture;
    }
}
