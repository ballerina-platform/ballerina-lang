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

package org.ballerinalang.net.http.websocket.client;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.net.http.websocket.WebSocketConstants;
import org.ballerinalang.net.http.websocket.server.WebSocketService;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;

import static org.ballerinalang.net.http.websocket.WebSocketConstants.CONNECTOR_FACTORY;
import static org.ballerinalang.net.http.websocket.WebSocketConstants.RETRY_CONFIG;
import static org.ballerinalang.net.http.websocket.WebSocketUtil.getWebSocketService;
import static org.ballerinalang.net.http.websocket.WebSocketUtil.hasRetryConfig;
import static org.ballerinalang.net.http.websocket.WebSocketUtil.initialiseWebSocketConnection;
import static org.ballerinalang.net.http.websocket.WebSocketUtil.populateRetryConnectorConfig;

/**
 * Initialize the WebSocket Client.
 *
 * @since 0.966
 */

@BallerinaFunction(
        orgName = WebSocketConstants.BALLERINA_ORG,
        packageName = WebSocketConstants.PACKAGE_HTTP,
        functionName = "initEndpoint",
        receiver = @Receiver(
                type = TypeKind.OBJECT,
                structType = WebSocketConstants.WEBSOCKET_CLIENT,
                structPackage = WebSocketConstants.FULL_PACKAGE_HTTP
        )
)
public class InitEndpoint {

    public static void initEndpoint(Strand strand, ObjectValue webSocketClient) {
        @SuppressWarnings(WebSocketConstants.UNCHECKED)
        MapValue<String, Object> clientEndpointConfig = (MapValue<String, Object>) webSocketClient.getMapValue(
                HttpConstants.CLIENT_ENDPOINT_CONFIG);
        if (hasRetryConfig(webSocketClient)) {
            @SuppressWarnings(WebSocketConstants.UNCHECKED)
            MapValue<String, Object> retryConfig = (MapValue<String, Object>) clientEndpointConfig.getMapValue(
                    RETRY_CONFIG);
            RetryContext retryConnectorConfig = new RetryContext();
            populateRetryConnectorConfig(retryConfig, retryConnectorConfig);
            webSocketClient.addNativeData(RETRY_CONFIG, retryConnectorConfig);
        }
        String remoteUrl = webSocketClient.getStringValue(WebSocketConstants.CLIENT_URL_CONFIG);
        WebSocketService wsService = getWebSocketService(clientEndpointConfig, strand);
        HttpWsConnectorFactory connectorFactory = HttpUtil.createHttpWsConnectionFactory();
        webSocketClient.addNativeData(CONNECTOR_FACTORY, connectorFactory);
        initialiseWebSocketConnection(remoteUrl, webSocketClient, wsService);
    }

    private InitEndpoint() {
    }
}
