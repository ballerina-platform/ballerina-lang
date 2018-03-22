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
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.net.http.WebSocketClientConnectorListener;
import org.ballerinalang.net.http.WebSocketConstants;
import org.ballerinalang.net.http.WebSocketService;
import org.ballerinalang.net.http.WebSocketUtil;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contract.websocket.HandshakeFuture;
import org.wso2.transport.http.netty.contract.websocket.HandshakeListener;
import org.wso2.transport.http.netty.contract.websocket.WebSocketClientConnector;
import org.wso2.transport.http.netty.contract.websocket.WsClientConnectorConfig;

import javax.websocket.Session;

/**
 * Get the ID of the connection.
 *
 * @since 0.966
 */

@BallerinaFunction(
        orgName = "ballerina", packageName = "net.http",
        functionName = "start",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "WebSocketClient",
                             structPackage = "ballerina.net.http"),
        isPublic = true
)
public class Start extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        HttpWsConnectorFactory connectorFactory = HttpUtil.createHttpWsConnectionFactory();
        Struct clientEndpoint = BLangConnectorSPIUtil.getConnectorEndpointStruct(context);
        Struct clientEndpointConfig = clientEndpoint.getStructField(HttpConstants.CLIENT_ENDPOINT_CONFIG);
        Object configs = clientEndpointConfig.getNativeData(WebSocketConstants.CLIENT_CONNECTOR_CONFIGS);
        if (configs == null || !(configs instanceof WsClientConnectorConfig)) {
            throw new BallerinaConnectorException("Initialize the service before starting it");
        }
        WebSocketClientConnector clientConnector =
                connectorFactory.createWsClientConnector((WsClientConnectorConfig) configs);
        WebSocketClientConnectorListener
                clientConnectorListener = new WebSocketClientConnectorListener();
        Object serviceConfig = clientEndpointConfig.getNativeData(WebSocketConstants.CLIENT_SERVICE_CONFIG);
        if (serviceConfig == null || !(serviceConfig instanceof WebSocketService)) {
            throw new BallerinaConnectorException("Initialize the service before starting it");
        }
        WebSocketService wsService = (WebSocketService) serviceConfig;
        HandshakeFuture handshakeFuture = clientConnector.connect(clientConnectorListener);
        handshakeFuture.setHandshakeListener(
                new WsHandshakeListener(context, wsService, clientConnectorListener));
        context.setReturnValues();
    }

    static class WsHandshakeListener implements HandshakeListener {

        Context context;
        WebSocketService wsService;
        WebSocketClientConnectorListener clientConnectorListener;

        WsHandshakeListener(Context context, WebSocketService wsService,
                            WebSocketClientConnectorListener clientConnectorListener) {
            this.context = context;
            this.wsService = wsService;
            this.clientConnectorListener = clientConnectorListener;
        }

        @Override
        public void onSuccess(Session session) {
            BStruct endpoint = (BStruct) context.getRefArgument(0);
            wsService.setServiceEndpoint(endpoint);
            BStruct wsConnection = WebSocketUtil.createAndGetBStruct(wsService.getResources()[0]);
            wsConnection.addNativeData(WebSocketConstants.NATIVE_DATA_WEBSOCKET_SESSION, session);
            WebSocketUtil.populateEndpoint(session, wsConnection);
            clientConnectorListener.setWebSocketService(wsService);
            endpoint.setRefField(0, wsConnection);
            context.setReturnValues();
        }

        @Override
        public void onError(Throwable t) {
            throw new BallerinaConnectorException("Initialize the service before starting it");
        }
    }
}
