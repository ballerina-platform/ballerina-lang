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
import org.ballerinalang.net.http.exception.WebSocketException;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contract.websocket.ClientHandshakeFuture;
import org.wso2.transport.http.netty.contract.websocket.WebSocketClientConnector;
import org.wso2.transport.http.netty.contract.websocket.WebSocketClientConnectorConfig;

import static org.ballerinalang.net.http.WebSocketConstants.RECONNECT_ATTEMPTS;
import static org.ballerinalang.net.http.WebSocketConstants.RETRY_CONFIG;
import static org.ballerinalang.net.http.WebSocketUtil.getWebSocketService;
import static org.ballerinalang.net.http.WebSocketUtil.initialiseWebSocketConnection;

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
        ),
        args = {@Argument(name = "epName", type = TypeKind.STRING),
                @Argument(name = "config", type = TypeKind.RECORD, structType = "WebSocketClientEndpointConfig")},
        isPublic = true
)
public class InitEndpoint extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
    }

    public static void initEndpoint(Strand strand, ObjectValue webSocketClient) {
        @SuppressWarnings(WebSocketConstants.UNCHECKED)
        MapValue<String, Object> clientEndpointConfig = (MapValue<String, Object>) webSocketClient.getMapValue(
                HttpConstants.CLIENT_ENDPOINT_CONFIG);
        String remoteUrl = webSocketClient.getStringValue(WebSocketConstants.CLIENT_URL_CONFIG);
        if (clientEndpointConfig.get(RETRY_CONFIG) != null) {
            webSocketClient.addNativeData(RECONNECT_ATTEMPTS, 0);
        }
        initialiseWebSocketConnection(remoteUrl, webSocketClient, getWebSocketService(clientEndpointConfig, strand));
    }
}
