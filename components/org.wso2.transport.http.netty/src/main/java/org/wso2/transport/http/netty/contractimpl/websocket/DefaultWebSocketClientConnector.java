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
 *
 */

package org.wso2.transport.http.netty.contractimpl.websocket;

import io.netty.channel.EventLoopGroup;
import org.wso2.transport.http.netty.contract.websocket.ClientHandshakeFuture;
import org.wso2.transport.http.netty.contract.websocket.WebSocketClientConnector;
import org.wso2.transport.http.netty.contract.websocket.WebSocketClientConnectorConfig;
import org.wso2.transport.http.netty.contractimpl.sender.websocket.WebSocketClient;

/**
 * Implementation of WebSocket client connector.
 */
public class DefaultWebSocketClientConnector implements WebSocketClientConnector {

    private final WebSocketClient webSocketClient;

    public DefaultWebSocketClientConnector(WebSocketClientConnectorConfig clientConnectorConfig,
            EventLoopGroup wsClientEventLoopGroup) {
        this.webSocketClient = new WebSocketClient(wsClientEventLoopGroup, clientConnectorConfig);
    }

    @Override
    public ClientHandshakeFuture connect() {
        return webSocketClient.handshake();
    }
}
