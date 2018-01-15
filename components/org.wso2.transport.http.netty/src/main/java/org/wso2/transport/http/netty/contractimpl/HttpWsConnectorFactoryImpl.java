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

package org.wso2.transport.http.netty.contractimpl;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.wso2.transport.http.netty.config.ListenerConfiguration;
import org.wso2.transport.http.netty.config.SenderConfiguration;
import org.wso2.transport.http.netty.contract.HttpClientConnector;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contract.ServerConnector;
import org.wso2.transport.http.netty.contract.websocket.WebSocketClientConnector;
import org.wso2.transport.http.netty.contract.websocket.WsClientConnectorConfig;
import org.wso2.transport.http.netty.contractimpl.websocket.WebSocketClientConnectorImpl;
import org.wso2.transport.http.netty.listener.ServerBootstrapConfiguration;
import org.wso2.transport.http.netty.listener.ServerConnectorBootstrap;
import org.wso2.transport.http.netty.sender.channel.BootstrapConfiguration;
import org.wso2.transport.http.netty.sender.channel.pool.ConnectionManager;
import org.wso2.transport.http.netty.sender.channel.pool.PoolConfiguration;

import java.util.Map;

/**
 * Implementation of HttpWsConnectorFactory interface
 */
public class HttpWsConnectorFactoryImpl implements HttpWsConnectorFactory {

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public HttpWsConnectorFactoryImpl() {
        bossGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors());
        workerGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2);
    }

    public HttpWsConnectorFactoryImpl(int serverSocketThreads, int childSocketThreads) {
        bossGroup = new NioEventLoopGroup(serverSocketThreads);
        workerGroup = new NioEventLoopGroup(childSocketThreads);
    }

    @Override
    public ServerConnector createServerConnector(ServerBootstrapConfiguration serverBootstrapConfiguration,
            ListenerConfiguration listenerConfig) {
        ServerConnectorBootstrap serverConnectorBootstrap = new ServerConnectorBootstrap();
        serverConnectorBootstrap.addSocketConfiguration(serverBootstrapConfiguration);
        serverConnectorBootstrap.addSecurity(listenerConfig.getSSLConfig());
        serverConnectorBootstrap.addIdleTimeout(listenerConfig.getSocketIdleTimeout(120000));
        serverConnectorBootstrap.addHttpTraceLogHandler(listenerConfig.isHttpTraceLogEnabled());
        serverConnectorBootstrap.addThreadPools(bossGroup, workerGroup);
        serverConnectorBootstrap.addHeaderAndEntitySizeValidation(listenerConfig.getRequestSizeValidationConfig());
        serverConnectorBootstrap.addChunkingBehaviour(listenerConfig.getChunkConfig());

        return serverConnectorBootstrap.getServerConnector(listenerConfig.getHost(), listenerConfig.getPort());
    }

    @Override
    public HttpClientConnector createHttpClientConnector(
            Map<String, Object> transportProperties, SenderConfiguration senderConfiguration) {
        PoolConfiguration poolConfiguration = new PoolConfiguration(transportProperties);
        BootstrapConfiguration bootstrapConfig = new BootstrapConfiguration(transportProperties);
        ConnectionManager connectionManager =
                new ConnectionManager(poolConfiguration, transportProperties, bootstrapConfig);
        return new HttpClientConnectorImpl(connectionManager, senderConfiguration);
    }

    @Override
    public WebSocketClientConnector createWsClientConnector(WsClientConnectorConfig clientConnectorConfig) {
        return new WebSocketClientConnectorImpl(clientConnectorConfig);
    }
}
