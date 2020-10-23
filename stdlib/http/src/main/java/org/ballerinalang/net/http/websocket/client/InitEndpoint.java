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

import io.ballerina.runtime.api.BalEnv;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.net.http.websocket.WebSocketConstants;
import org.ballerinalang.net.http.websocket.WebSocketService;
import org.ballerinalang.net.http.websocket.WebSocketUtil;
import org.ballerinalang.net.http.websocket.client.listener.ClientConnectorListener;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contract.websocket.WebSocketClientConnector;
import org.wso2.transport.http.netty.contract.websocket.WebSocketClientConnectorConfig;

import java.net.URI;
import java.util.concurrent.CountDownLatch;

/**
 * Initialize the WebSocket Client.
 *
 * @since 0.966
 */
public class InitEndpoint {

    public static void initEndpoint(BalEnv env, BObject webSocketClient) {
        @SuppressWarnings(WebSocketConstants.UNCHECKED)
        BMap<BString, Object> clientEndpointConfig =  webSocketClient.getMapValue(
                WebSocketConstants.CLIENT_ENDPOINT_CONFIG);
        String remoteUrl = webSocketClient.getStringValue(WebSocketConstants.CLIENT_URL_CONFIG).getValue();
        WebSocketService wsService = WebSocketUtil.validateAndCreateWebSocketService(env.getRuntime(),
                                                                                     clientEndpointConfig);
        HttpWsConnectorFactory connectorFactory = HttpUtil.createHttpWsConnectionFactory();
        WebSocketClientConnectorConfig clientConnectorConfig = new WebSocketClientConnectorConfig(remoteUrl);
        String scheme = URI.create(remoteUrl).getScheme();
        WebSocketUtil.populateClientConnectorConfig(clientEndpointConfig, clientConnectorConfig, scheme);
        // Creates the client connector.
        WebSocketClientConnector clientConnector = connectorFactory.createWsClientConnector(clientConnectorConfig);
        webSocketClient.addNativeData(WebSocketConstants.CONNECTOR_FACTORY, connectorFactory);
        // Add the client connector as a native data
        // because there is no need to create the client connector again when using one URL.
        webSocketClient.addNativeData(WebSocketConstants.CLIENT_CONNECTOR, clientConnector);
        if (webSocketClient.getNativeData(WebSocketConstants.CLIENT_LISTENER) == null) {
            webSocketClient.addNativeData(WebSocketConstants.CLIENT_LISTENER, new ClientConnectorListener());
        }
        CountDownLatch countDownLatch = new CountDownLatch(1);
        webSocketClient.addNativeData(WebSocketConstants.COUNT_DOWN_LATCH, countDownLatch);
        WebSocketUtil.establishWebSocketConnection(clientConnector, webSocketClient, wsService);
        // Sets the count down latch for the initial connection.
        WebSocketUtil.waitForHandshake(countDownLatch);
    }

    private InitEndpoint() {
    }
}
