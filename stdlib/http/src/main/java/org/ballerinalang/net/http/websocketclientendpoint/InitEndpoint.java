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
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.util.exceptions.BallerinaConnectorException;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.net.http.WebSocketClientConnectorListener;
import org.ballerinalang.net.http.WebSocketClientHandshakeListener;
import org.ballerinalang.net.http.WebSocketConstants;
import org.ballerinalang.net.http.WebSocketService;
import org.ballerinalang.net.http.WebSocketUtil;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contract.websocket.ClientHandshakeFuture;
import org.wso2.transport.http.netty.contract.websocket.WebSocketClientConnector;
import org.wso2.transport.http.netty.contract.websocket.WebSocketClientConnectorConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Initialize the WebSocket Client.
 *
 * @since 0.966
 */

@BallerinaFunction(
        orgName = "ballerina", packageName = "http",
        functionName = "initEndpoint",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "WebSocketClient",
                             structPackage = "ballerina/http"),
        args = {@Argument(name = "epName", type = TypeKind.STRING),
                @Argument(name = "config", type = TypeKind.RECORD, structType = "WebSocketClientEndpointConfig")},
        isPublic = true
)
public class InitEndpoint extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
//        Struct clientEndpoint = BLangConnectorSPIUtil.getConnectorEndpointStruct(context);
//        Struct clientEndpointConfig = clientEndpoint.getStructField(HttpConstants.CLIENT_ENDPOINT_CONFIG);
//
//        String remoteUrl = clientEndpoint.getStringField(WebSocketConstants.CLIENT_URL_CONFIG);
//        BMap clientService = (BMap) clientEndpointConfig.getServiceField(WebSocketConstants.CLIENT_SERVICE_CONFIG);
//        WebSocketService wsService;
//        if (clientService != null) {
//            Service service = BLangConnectorSPIUtil.getBalService(context.getProgramFile(), clientService);
//            ParamDetail param = service.getResources()[0].getParamDetails().get(0);
//            if (param == null || !WebSocketConstants.WEBSOCKET_CLIENT_ENDPOINT_NAME.equals(
//                    param.getVarType().toString())) {
//                throw new BallerinaConnectorException("The callback service should be a WebSocket Client Service");
//            }
//            wsService = new WebSocketService(service);
//        } else {
//            wsService = new WebSocketService();
//        }
//        WebSocketClientConnectorConfig clientConnectorConfig = new WebSocketClientConnectorConfig(remoteUrl);
//        populateClientConnectorConfig(clientEndpointConfig, clientConnectorConfig);
//
//        HttpWsConnectorFactory connectorFactory = HttpUtil.createHttpWsConnectionFactory();
//        WebSocketClientConnector clientConnector = connectorFactory.createWsClientConnector(
//                clientConnectorConfig);
//        WebSocketClientConnectorListener clientConnectorListener = new WebSocketClientConnectorListener();
//        boolean readyOnConnect = clientEndpointConfig.getBooleanField(WebSocketConstants.CLIENT_READY_ON_CONNECT);
//        ClientHandshakeFuture handshakeFuture = clientConnector.connect();
//        handshakeFuture.setWebSocketConnectorListener(clientConnectorListener);
//        CountDownLatch countDownLatch = new CountDownLatch(1);
//        handshakeFuture.setClientHandshakeListener(
//                new WebSocketClientHandshakeListener(context, wsService, clientConnectorListener,
//                                                     readyOnConnect, countDownLatch));
//        try {
//            if (!countDownLatch.await(60, TimeUnit.SECONDS)) {
//                throw new BallerinaConnectorException("Waiting for WebSocket handshake has not been successful");
//            }
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            throw new BallerinaConnectorException("Error occurred: " + e.getMessage());
//
//        }
//        context.setReturnValues();
    }

    @SuppressWarnings("unchecked")
    public static void initEndpoint(Strand strand, ObjectValue webSocketClient) {
        MapValue<String, Object> clientEndpointConfig = webSocketClient.getMapValue(
                HttpConstants.CLIENT_ENDPOINT_CONFIG);

        String remoteUrl = webSocketClient.getStringValue(WebSocketConstants.CLIENT_URL_CONFIG);
        ObjectValue clientService = (ObjectValue) clientEndpointConfig.get(WebSocketConstants.CLIENT_SERVICE_CONFIG);
        WebSocketService wsService;
        if (clientService != null) {
            //TODO Check whether following wrapper is needed
//            Service service = BLangConnectorSPIUtil.getBalService(context.getProgramFile(), clientService);
            BType param = clientService.getType().getAttachedFunctions()[0].getParameterType()[0];
            if (param == null || !WebSocketConstants.WEBSOCKET_CLIENT_ENDPOINT_NAME.equals(
                    param.toString())) {
                throw new BallerinaConnectorException("The callback service should be a WebSocket Client Service");
            }
            wsService = new WebSocketService(clientService);
        } else {
            wsService = new WebSocketService();
        }
        WebSocketClientConnectorConfig clientConnectorConfig = new WebSocketClientConnectorConfig(remoteUrl);
        populateClientConnectorConfig(clientEndpointConfig, clientConnectorConfig);

        HttpWsConnectorFactory connectorFactory = HttpUtil.createHttpWsConnectionFactory();
        WebSocketClientConnector clientConnector = connectorFactory.createWsClientConnector(
                clientConnectorConfig);
        WebSocketClientConnectorListener clientConnectorListener = new WebSocketClientConnectorListener();
        boolean readyOnConnect = clientEndpointConfig.getBooleanValue(WebSocketConstants.CLIENT_READY_ON_CONNECT);
        ClientHandshakeFuture handshakeFuture = clientConnector.connect();
        handshakeFuture.setWebSocketConnectorListener(clientConnectorListener);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        handshakeFuture.setClientHandshakeListener(
                new WebSocketClientHandshakeListener(webSocketClient, wsService, clientConnectorListener, readyOnConnect,
                                                     countDownLatch));
        try {
            if (!countDownLatch.await(60, TimeUnit.SECONDS)) {
                throw new BallerinaConnectorException("Waiting for WebSocket handshake has not been successful");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BallerinaConnectorException("Error occurred: " + e.getMessage());

        }
    }

    @SuppressWarnings("unchecked")
    private static void populateClientConnectorConfig(MapValue<String, Object> clientEndpointConfig,
                                               WebSocketClientConnectorConfig clientConnectorConfig) {
        clientConnectorConfig.setAutoRead(false); // Frames are read sequentially in ballerina.
        clientConnectorConfig.setSubProtocols(WebSocketUtil.findNegotiableSubProtocols(clientEndpointConfig));
        MapValue<String, Object> headerValues = (MapValue<String, Object>) clientEndpointConfig.getMapValue(
                WebSocketConstants.CLIENT_CUSTOM_HEADERS_CONFIG);
        if (headerValues != null) {
            clientConnectorConfig.addHeaders(getCustomHeaders(headerValues));
        }

        long idleTimeoutInSeconds = WebSocketUtil.findIdleTimeoutInSeconds(clientEndpointConfig);
        if (idleTimeoutInSeconds > 0) {
            clientConnectorConfig.setIdleTimeoutInMillis((int) (idleTimeoutInSeconds * 1000));
        }

        clientConnectorConfig.setMaxFrameSize(WebSocketUtil.findMaxFrameSize(clientEndpointConfig));

        MapValue secureSocket = clientEndpointConfig.getMapValue(HttpConstants.ENDPOINT_CONFIG_SECURE_SOCKET);
        if (secureSocket != null) {
            HttpUtil.populateSSLConfiguration(clientConnectorConfig, secureSocket);
        } else {
            HttpUtil.setDefaultTrustStore(clientConnectorConfig);
        }
    }

    private static Map<String, String> getCustomHeaders(MapValue<String, Object> headers) {
        Map<String, String> customHeaders = new HashMap<>();
        headers.keySet().forEach(
                key -> customHeaders.put(key, headers.get(key).toString())
        );
        return customHeaders;
    }
}
