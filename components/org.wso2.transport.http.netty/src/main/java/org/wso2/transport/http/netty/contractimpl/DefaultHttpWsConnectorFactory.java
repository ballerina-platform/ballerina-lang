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
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.HttpClientConnector;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contract.ServerConnector;
import org.wso2.transport.http.netty.contract.config.ListenerConfiguration;
import org.wso2.transport.http.netty.contract.config.SenderConfiguration;
import org.wso2.transport.http.netty.contract.config.ServerBootstrapConfiguration;
import org.wso2.transport.http.netty.contract.websocket.WebSocketClientConnector;
import org.wso2.transport.http.netty.contract.websocket.WebSocketClientConnectorConfig;
import org.wso2.transport.http.netty.contractimpl.common.ssl.SSLConfig;
import org.wso2.transport.http.netty.contractimpl.common.ssl.SSLHandlerFactory;
import org.wso2.transport.http.netty.contractimpl.listener.ServerConnectorBootstrap;
import org.wso2.transport.http.netty.contractimpl.sender.channel.BootstrapConfiguration;
import org.wso2.transport.http.netty.contractimpl.sender.channel.pool.ConnectionManager;
import org.wso2.transport.http.netty.contractimpl.websocket.DefaultWebSocketClientConnector;

import java.util.Map;
import javax.net.ssl.SSLException;

/**
 * Implementation of HttpWsConnectorFactory interface.
 */
public class DefaultHttpWsConnectorFactory implements HttpWsConnectorFactory {

    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;
    private final EventLoopGroup clientGroup;
    private final ChannelGroup allChannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    public DefaultHttpWsConnectorFactory() {
        bossGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors());
        workerGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2);
        clientGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2);
    }

    public DefaultHttpWsConnectorFactory(int serverSocketThreads, int childSocketThreads, int clientThreads) {
        bossGroup = new NioEventLoopGroup(serverSocketThreads);
        workerGroup = new NioEventLoopGroup(childSocketThreads);
        clientGroup = new NioEventLoopGroup(clientThreads);
    }

    @Override
    public ServerConnector createServerConnector(ServerBootstrapConfiguration serverBootstrapConfiguration,
            ListenerConfiguration listenerConfig) {
        ServerConnectorBootstrap serverConnectorBootstrap = new ServerConnectorBootstrap(allChannels);
        serverConnectorBootstrap.addSocketConfiguration(serverBootstrapConfiguration);
        SSLConfig sslConfig = listenerConfig.getListenerSSLConfig();
        serverConnectorBootstrap.addSecurity(sslConfig);
        if (sslConfig != null) {
            setSslContext(serverConnectorBootstrap, sslConfig);
        }
        serverConnectorBootstrap.addIdleTimeout(listenerConfig.getSocketIdleTimeout());
        if (Constants.HTTP_2_0 == Float.valueOf(listenerConfig.getVersion())) {
            serverConnectorBootstrap.setHttp2Enabled(true);
        }
        serverConnectorBootstrap.addHttpTraceLogHandler(listenerConfig.isHttpTraceLogEnabled());
        serverConnectorBootstrap.addHttpAccessLogHandler(listenerConfig.isHttpAccessLogEnabled());
        serverConnectorBootstrap.addThreadPools(bossGroup, workerGroup);
        serverConnectorBootstrap.addHeaderAndEntitySizeValidation(listenerConfig.getRequestSizeValidationConfig());
        serverConnectorBootstrap.addChunkingBehaviour(listenerConfig.getChunkConfig());
        serverConnectorBootstrap.addKeepAliveBehaviour(listenerConfig.getKeepAliveConfig());
        serverConnectorBootstrap.addServerHeader(listenerConfig.getServerHeader());

        serverConnectorBootstrap.setPipeliningNeeded(listenerConfig.isPipeliningNeeded());
        serverConnectorBootstrap.setPipeliningLimit(listenerConfig.getPipeliningLimit());

        return serverConnectorBootstrap.getServerConnector(listenerConfig.getHost(), listenerConfig.getPort());
    }

    private void setSslContext(ServerConnectorBootstrap serverConnectorBootstrap, SSLConfig sslConfig) {
        try {
            SSLHandlerFactory sslHandlerFactory = new SSLHandlerFactory(sslConfig);
            serverConnectorBootstrap.addcertificateRevocationVerifier(sslConfig.isValidateCertEnabled());
            serverConnectorBootstrap.addCacheDelay(sslConfig.getCacheValidityPeriod());
            serverConnectorBootstrap.addCacheSize(sslConfig.getCacheSize());
            serverConnectorBootstrap.addOcspStapling(sslConfig.isOcspStaplingEnabled());
            serverConnectorBootstrap.addSslHandlerFactory(sslHandlerFactory);
            if (sslConfig.getKeyStore() != null) {
                serverConnectorBootstrap.addKeystoreSslContext(sslHandlerFactory.createSSLContextFromKeystores());
            } else {
                serverConnectorBootstrap.addCertAndKeySslContext(sslHandlerFactory.createHttpTLSContextForServer());
            }
        } catch (SSLException e) {
            throw new RuntimeException("Failed to create ssl context from given certs and key", e);
        }
    }

    @Override
    public HttpClientConnector createHttpClientConnector(
            Map<String, Object> transportProperties, SenderConfiguration senderConfiguration) {
        BootstrapConfiguration bootstrapConfig = new BootstrapConfiguration(transportProperties);
        ConnectionManager connectionManager = new ConnectionManager(senderConfiguration, bootstrapConfig, clientGroup);
        return new DefaultHttpClientConnector(connectionManager, senderConfiguration);
    }

    @Override
    public WebSocketClientConnector createWsClientConnector(WebSocketClientConnectorConfig clientConnectorConfig) {
        return new DefaultWebSocketClientConnector(clientConnectorConfig, clientGroup);
    }

    @Override
    public void shutdown() throws InterruptedException {
        this.allChannels.close().sync();
        this.workerGroup.shutdownGracefully().sync();
        this.bossGroup.shutdownGracefully().sync();
        this.clientGroup.shutdownGracefully().sync();
    }
}
