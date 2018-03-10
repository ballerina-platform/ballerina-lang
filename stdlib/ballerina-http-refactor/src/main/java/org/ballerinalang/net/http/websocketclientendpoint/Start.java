/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.net.http.websocketclientendpoint;

import org.ballerinalang.bre.Context;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.http.BallerinaWebSocketClientConnectorListener;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.net.http.WebSocketConstants;
import org.ballerinalang.net.http.WebSocketOpenConnectionInfo;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contract.websocket.HandshakeFuture;
import org.wso2.transport.http.netty.contract.websocket.HandshakeListener;
import org.wso2.transport.http.netty.contract.websocket.WebSocketClientConnector;
import org.wso2.transport.http.netty.contract.websocket.WsClientConnectorConfig;

import java.util.HashMap;
import javax.websocket.Session;

/**
 * Get the ID of the connection.
 *
 * @since 0.966
 */

@BallerinaFunction(
        packageName = "ballerina.net.http",
        functionName = "start",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "WebSocketClientEndpoint",
                             structPackage = "ballerina.net.http"),
        isPublic = true
)
public class Start extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
//        HttpWsConnectorFactory connectorFactory = HttpUtil.createHttpWsConnectionFactory();
//        Struct clientEndpointConfig = BLangConnectorSPIUtil.getConnectorEndpointStruct(context);
//        Object configs = clientEndpointConfig.getNativeData(WebSocketConstants.CLIENT_CONNECTOR_CONFIGS);
//        if (configs == null || !(configs instanceof WsClientConnectorConfig)) {
//            throw new BallerinaConnectorException("Initialize the service before starting it");
//        }
//        WebSocketClientConnector clientConnector =
//                connectorFactory.createWsClientConnector((WsClientConnectorConfig) configs);
//        BallerinaWebSocketClientConnectorListener
//                clientConnectorListener = new BallerinaWebSocketClientConnectorListener();
//        HandshakeFuture handshakeFuture = clientConnector.connect(clientConnectorListener);
//        handshakeFuture.setHandshakeListener(new HandshakeListener() {
//            @Override
//            public void onSuccess(Session session) {
//                BStruct wsConnection = createWsConnectionStruct(wsService, session, wsParentConnectionID);
//                context.getControlStack().currentFrame.returnValues[0] = wsConnection;
//                WebSocketOpenConnectionInfo connectionInfo =
//                        new WebSocketOpenConnectionInfo(wsService, wsConnection, new HashMap<>());
//                clientConnectorListener.setConnectionInfo(connectionInfo);
//                connectorFuture.notifySuccess();
//            }
//
//            @Override
//            public void onError(Throwable t) {
//                BStruct wsError = createWsErrorStruct(context, t);
//                context.getControlStack().currentFrame.returnValues[1] = wsError;
//                connectorFuture.notifySuccess();
//            }
//        });
        return VOID_RETURN;
    }

}
