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
import org.ballerinalang.net.http.WebSocketOpenConnectionInfo;
import org.ballerinalang.net.http.WebSocketService;
import org.ballerinalang.net.http.WebSocketUtil;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contract.websocket.HandshakeFuture;
import org.wso2.transport.http.netty.contract.websocket.HandshakeListener;
import org.wso2.transport.http.netty.contract.websocket.WebSocketClientConnector;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;
import org.wso2.transport.http.netty.contract.websocket.WsClientConnectorConfig;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.ballerinalang.net.http.HttpConstants.PROTOCOL_PACKAGE_HTTP;

/**
 * Get the ID of the connection.
 *
 * @since 0.966
 */

@BallerinaFunction(
        orgName = "ballerina", packageName = "http",
        functionName = "start",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "WebSocketClient",
                             structPackage = "ballerina.http"),
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
            throw new BallerinaConnectorException("Error in initializing: Missing client endpoint configurations");
        }
        WebSocketClientConnector clientConnector =
                connectorFactory.createWsClientConnector((WsClientConnectorConfig) configs);
        WebSocketClientConnectorListener
                clientConnectorListener = new WebSocketClientConnectorListener();
        Object serviceConfig = clientEndpointConfig.getNativeData(WebSocketConstants.CLIENT_SERVICE_CONFIG);
        if (serviceConfig == null || !(serviceConfig instanceof WebSocketService)) {
            throw new BallerinaConnectorException("Error in initializing: A callbackService is required");
        }
        WebSocketService wsService = (WebSocketService) serviceConfig;
        boolean readyOnConnect = clientEndpointConfig.getBooleanField(WebSocketConstants.CLIENT_READY_ON_CONNECT);
        HandshakeFuture handshakeFuture = clientConnector.connect(clientConnectorListener);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        handshakeFuture.setHandshakeListener(
                new WsHandshakeListener(context, wsService, clientConnectorListener, readyOnConnect, countDownLatch));
        try {
            countDownLatch.await(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new BallerinaConnectorException("Error occurred: " + e.getMessage());

        }
    }

    static class WsHandshakeListener implements HandshakeListener {

        private final Context context;
        private final WebSocketService wsService;
        private final WebSocketClientConnectorListener clientConnectorListener;
        private final boolean readyOnConnect;
        CountDownLatch countDownLatch;

        WsHandshakeListener(Context context, WebSocketService wsService,
                            WebSocketClientConnectorListener clientConnectorListener, boolean readyOnConnect, CountDownLatch countDownLatch) {
            this.context = context;
            this.wsService = wsService;
            this.clientConnectorListener = clientConnectorListener;
            this.readyOnConnect = readyOnConnect;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void onSuccess(WebSocketConnection webSocketConnection) {
            //using only one service endpoint in the client as there can be only one connection.
            BStruct webSocketClientEndpoint = ((BStruct) context.getRefArgument(0));
            BStruct webSocketConnector = BLangConnectorSPIUtil.createObject(
                    wsService.getResources()[0].getResourceInfo().getServiceInfo().getPackageInfo().getProgramFile(),
                    PROTOCOL_PACKAGE_HTTP, WebSocketConstants.WEBSOCKET_CONNECTOR);
            System.out.println("Setting connection ++++++++++++");
            webSocketConnector.addNativeData(WebSocketConstants.NATIVE_DATA_WEBSOCKET_CONNECTION, webSocketConnection);
            WebSocketUtil.populateEndpoint(webSocketConnection, webSocketClientEndpoint);
            WebSocketOpenConnectionInfo connectionInfo = new WebSocketOpenConnectionInfo(wsService,
                                                                                         webSocketClientEndpoint);
            clientConnectorListener.setConnectionInfo(connectionInfo);
            webSocketClientEndpoint.setRefField(1, webSocketConnector);
            context.setReturnValues();
            if (readyOnConnect) {
                webSocketConnection.readNextFrame();
            }
            countDownLatch.countDown();
        }

        @Override
        public void onError(Throwable t) {
            countDownLatch.countDown();
            throw new BallerinaConnectorException("Error occurred: " + t.getMessage());
        }
    }
}
