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

package org.wso2.transport.http.netty.contract;

import org.wso2.transport.http.netty.contract.config.ListenerConfiguration;
import org.wso2.transport.http.netty.contract.config.SenderConfiguration;
import org.wso2.transport.http.netty.contract.config.ServerBootstrapConfiguration;
import org.wso2.transport.http.netty.contract.websocket.WebSocketClientConnector;
import org.wso2.transport.http.netty.contract.websocket.WebSocketClientConnectorConfig;
import org.wso2.transport.http.netty.contractimpl.sender.channel.pool.ConnectionManager;

import java.util.Map;

/**
 * Allows you create server and client connectors.
 */
public interface HttpWsConnectorFactory {
    /**
     * This method can be used to get new server connectors.
     *
     * @param serverBootstrapConfiguration configTargetHandler socket related stuff.
     * @param listenerConfiguration contains SSL and socket bindings.
     * @return connector that represents the server socket and additional details.
     */
    ServerConnector createServerConnector(ServerBootstrapConfiguration serverBootstrapConfiguration,
            ListenerConfiguration listenerConfiguration);

    /**
     * This method can be used to get http client connectors.
     *
     * @param transportProperties configTargetHandler stuff like global timeout, number of outbound connections, etc.
     * @param senderConfiguration contains SSL configuration and endpoint details.
     * @return HttpClientConnector.
     */
    HttpClientConnector createHttpClientConnector(Map<String, Object> transportProperties,
            SenderConfiguration senderConfiguration);

    /**
     * Creates a client connector with a given connection manager.
     *
     * @param transportProperties Represents the configurations related to HTTP client
     * @param senderConfiguration Represents the configurations related to client channel creation
     * @param connectionManager   Manages the client pool
     * @return the HttpClientConnector
     */
    HttpClientConnector createHttpClientConnector(Map<String, Object> transportProperties,
        SenderConfiguration senderConfiguration, ConnectionManager connectionManager);

    /**
     * This method is used to get WebSocket client connector.
     *
     * @param clientConnectorConfig Properties to create a client connector.
     * @return WebSocketClientConnector.
     */
    WebSocketClientConnector createWsClientConnector(WebSocketClientConnectorConfig clientConnectorConfig);

    /**
     * Shutdown all the server channels and the accepted channels. It also shutdown all the eventloop groups.
     * @throws InterruptedException when interrupted by some other event
     */
    void shutdown() throws InterruptedException;
}
