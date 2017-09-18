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
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BConnector;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaAction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.ws.BallerinaWsServerConnectorListener;
import org.ballerinalang.net.ws.Constants;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.carbon.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.carbon.transport.http.netty.contract.websocket.HandshakeFuture;
import org.wso2.carbon.transport.http.netty.contract.websocket.HandshakeListener;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketClientConnector;
import org.wso2.carbon.transport.http.netty.contract.websocket.WsClientConnectorConfig;
import org.wso2.carbon.transport.http.netty.contractimpl.HttpWsConnectorFactoryImpl;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
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
                @Argument(name = "c", type = TypeEnum.CONNECTOR),
                @Argument(name = "clientConnectorConfig", type = TypeEnum.STRUCT, structType = "ClientConnectorConfig",
                          structPackage = Constants.WEBSOCKET_PACKAGE_NAME)
        },
        returnType = {@ReturnType(type = TypeEnum.STRUCT, structType = "Connection",
                                  structPackage = "ballerina.net.ws")}
)
public class Connect extends AbstractNativeWsAction {

    @Override
    public BValue execute(Context context) {
        BConnector bconnector = (BConnector) getRefArgument(context, 0);
        BStruct clientConfig = (BStruct) getRefArgument(context, 1);

        BRefType bSubProtocolsBRefType = clientConfig.getRefField(0);
        String wsParentConnectionID = clientConfig.getStringField(0);
        BRefType<BMap<BString, BString>> bCustomHeaders = clientConfig.getRefField(1);
        int idleTimeoutInSeconds =  new Long(clientConfig.getIntField(0)).intValue();

        WsClientConnectorConfig clientConnectorConfig =
                new WsClientConnectorConfig(getUrlFromConnector(bconnector));
        clientConnectorConfig.setTarget(getClientServiceFromConnector(bconnector));
        if (bSubProtocolsBRefType != null) {
            clientConnectorConfig.setSubProtocols(getSubProtocols(bSubProtocolsBRefType));
        }
        if (bCustomHeaders != null) {
            clientConnectorConfig.addHeaders(getCustomHeaders(bCustomHeaders));
        }
        if (idleTimeoutInSeconds > 0) {
            clientConnectorConfig.setIdleTimeoutInMillis(idleTimeoutInSeconds * 1000);
        }

        try {
            BlockingDeque<BStruct> structBlockingDeque = new LinkedBlockingDeque<>();
            HttpWsConnectorFactory connectorFactory = new HttpWsConnectorFactoryImpl();
            WebSocketClientConnector clientConnector =
                    connectorFactory.createWsClientConnector(clientConnectorConfig);
            HandshakeFuture handshakeFuture = clientConnector.connect(new BallerinaWsServerConnectorListener());
            handshakeFuture.setHandshakeListener(new HandshakeListener() {
                @Override
                public void onSuccess(Session session) {
                    BStruct wsConnection = createWSConnectionStruct(context, session, wsParentConnectionID);
                    context.getControlStackNew().currentFrame.returnValues[0] = wsConnection;
                    storeWsConnection(session.getId(), wsConnection);
                    try {
                        structBlockingDeque.put(wsConnection);
                    } catch (InterruptedException e) {
                        throw new BallerinaException("Error while connecting to the server: " + e.getMessage());
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    throw new BallerinaException("Error while connecting to the server: " + throwable.getMessage());
                }
            });
            return structBlockingDeque.take();
        } catch (InterruptedException e) {
            // TODO: should return error instead of null.
            throw new BallerinaException("Error while connecting to the server: " + e.getMessage());
        }
    }
}
