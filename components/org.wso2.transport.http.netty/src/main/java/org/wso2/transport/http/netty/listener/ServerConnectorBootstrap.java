/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.transport.http.netty.listener;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.Util;
import org.wso2.transport.http.netty.common.ssl.SSLConfig;
import org.wso2.transport.http.netty.config.ChunkConfig;
import org.wso2.transport.http.netty.config.KeepAliveConfig;
import org.wso2.transport.http.netty.config.RequestSizeValidationConfig;
import org.wso2.transport.http.netty.contract.ServerConnector;
import org.wso2.transport.http.netty.contract.ServerConnectorException;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contractimpl.HttpWsServerConnectorFuture;
import org.wso2.transport.http.netty.internal.HTTPTransportContextHolder;
import org.wso2.transport.http.netty.internal.HandlerExecutor;

import java.net.InetSocketAddress;

/**
 * {@code ServerConnectorBootstrap} is the heart of the HTTP Server Connector.
 * <p>
 * This is responsible for creating the serverBootstrap and allow bind/unbind to interfaces
 */
public class ServerConnectorBootstrap {

    private static final Logger log = LoggerFactory.getLogger(ServerConnectorBootstrap.class);

    private ServerBootstrap serverBootstrap;
    private HttpServerChannelInitializer httpServerChannelInitializer;
    private boolean initialized = false;
    private boolean isHttps = false;
    private ChannelGroup allChannels;

    public ServerConnectorBootstrap(ChannelGroup allChannels) {
        serverBootstrap = new ServerBootstrap();
        httpServerChannelInitializer = new HttpServerChannelInitializer();
        httpServerChannelInitializer.setAllChannels(allChannels);
        serverBootstrap.childHandler(httpServerChannelInitializer);
        HTTPTransportContextHolder.getInstance().setHandlerExecutor(new HandlerExecutor());
        initialized = true;
        this.allChannels = allChannels;
    }

    private ChannelFuture bindInterface(HTTPServerConnector serverConnector) {

        if (!initialized) {
            log.error("ServerConnectorBootstrap is not initialized");
            return null;
        }

        return serverBootstrap
                .bind(new InetSocketAddress(serverConnector.getHost(), serverConnector.getPort()));
    }

    private boolean unBindInterface(HTTPServerConnector serverConnector) throws InterruptedException {
        if (!initialized) {
            log.error("ServerConnectorBootstrap is not initialized");
            return false;
        }

        //Remove cached channels and close them.
        ChannelFuture future = serverConnector.getChannelFuture();
        if (future != null) {
            ChannelFuture channelFuture = future.channel().close();
            channelFuture.sync();
            log.info("HttpConnectorListener stopped listening on host " + serverConnector.getHost()
                    + " and port " + serverConnector.getPort());
            return true;
        }
        return false;
    }

    public ServerConnector getServerConnector(String host, int port) {
        String serverConnectorId = Util.createServerConnectorID(host, port);
        return new HTTPServerConnector(serverConnectorId, this, host, port);
    }

    public void addSocketConfiguration(ServerBootstrapConfiguration serverBootstrapConfiguration) {
        // Set other serverBootstrap parameters
        serverBootstrap.option(ChannelOption.SO_BACKLOG, serverBootstrapConfiguration.getSoBackLog());
        serverBootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, serverBootstrapConfiguration.getConnectTimeOut());
        serverBootstrap.option(ChannelOption.SO_RCVBUF, serverBootstrapConfiguration.getReceiveBufferSize());

        serverBootstrap.childOption(ChannelOption.TCP_NODELAY, serverBootstrapConfiguration.isTcpNoDelay());
        serverBootstrap.childOption(ChannelOption.SO_RCVBUF, serverBootstrapConfiguration.getReceiveBufferSize());
        serverBootstrap.childOption(ChannelOption.SO_SNDBUF, serverBootstrapConfiguration.getSendBufferSize());

        if (log.isDebugEnabled()) {
            log.debug(String.format("Netty Server Socket BACKLOG %d", serverBootstrapConfiguration.getSoBackLog()));
            log.debug(String.format("Netty Server Socket TCP_NODELAY %s", serverBootstrapConfiguration.isTcpNoDelay()));
            log.debug(String.format("Netty Server Socket CONNECT_TIMEOUT_MILLIS %d",
                                    serverBootstrapConfiguration.getConnectTimeOut()));
            log.debug(String.format("Netty Server Socket SO_RCVBUF %d",
                                    serverBootstrapConfiguration.getReceiveBufferSize()));
            log.debug(String.format("Netty Server Socket SO_SNDBUF %d",
                                    serverBootstrapConfiguration.getSendBufferSize()));
        }
    }

    public void addSecurity(SSLConfig sslConfig) {
        if (sslConfig != null) {
            httpServerChannelInitializer.setSslConfig(sslConfig);
            isHttps = true;
        }
    }

    public void addIdleTimeout(int socketIdleTimeout) {
        httpServerChannelInitializer.setIdleTimeout(socketIdleTimeout);
    }

    public void setHttp2Enabled(boolean isHttp2Enabled) {
        httpServerChannelInitializer.setHttp2Enabled(isHttp2Enabled);
    }

    public void addThreadPools(EventLoopGroup bossGroup, EventLoopGroup workerGroup) {
        serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class);
    }

    public void addHttpTraceLogHandler(Boolean isHttpTraceLogEnabled) {
        httpServerChannelInitializer.setHttpTraceLogEnabled(isHttpTraceLogEnabled);
    }

    public void addHttpAccessLogHandler(Boolean isHttpAccessLogEnabled) {
        httpServerChannelInitializer.setHttpAccessLogEnabled(isHttpAccessLogEnabled);
    }
    public void addHeaderAndEntitySizeValidation(RequestSizeValidationConfig requestSizeValidationConfig) {
        httpServerChannelInitializer.setReqSizeValidationConfig(requestSizeValidationConfig);
    }

    public void addcertificateRevocationVerifier(Boolean validateCertEnabled) {
        httpServerChannelInitializer.setValidateCertEnabled(validateCertEnabled);
    }

    public void addCacheDelay(int cacheDelay) {
        httpServerChannelInitializer.setCacheDelay(cacheDelay);
    }

    public void addCacheSize(int cacheSize) {
        httpServerChannelInitializer.setCacheSize(cacheSize);
    }

    public void addOcspStapling(boolean ocspStapling) {
        httpServerChannelInitializer.setOcspStaplingEnabled(ocspStapling);
    }

    public void addChunkingBehaviour(ChunkConfig chunkConfig) {
        httpServerChannelInitializer.setChunkingConfig(chunkConfig);
    }

    public void addKeepAliveBehaviour(KeepAliveConfig keepAliveConfig) {
        httpServerChannelInitializer.setKeepAliveConfig(keepAliveConfig);
    }

    public void addServerHeader(String serverName) {
        httpServerChannelInitializer.setServerName(serverName);
    }

    class HTTPServerConnector implements ServerConnector {

       private final Logger log = LoggerFactory.getLogger(HTTPServerConnector.class);

        private ChannelFuture channelFuture;
        private ServerConnectorFuture serverConnectorFuture;
        private ServerConnectorBootstrap serverConnectorBootstrap;
        private String host;
        private int port;
        private String connectorID;

        HTTPServerConnector(String id, ServerConnectorBootstrap serverConnectorBootstrap, String host, int port) {
            this.serverConnectorBootstrap = serverConnectorBootstrap;
            this.host = host;
            this.port = port;
            this.connectorID =  id;
            httpServerChannelInitializer.setInterfaceId(id);
        }

        @Override
        public ServerConnectorFuture start() {
            channelFuture = bindInterface(this);
            serverConnectorFuture = new HttpWsServerConnectorFuture(channelFuture, allChannels);
            channelFuture.addListener(channelFuture -> {
                if (channelFuture.isSuccess()) {
                    log.info("HTTP(S) Interface starting on host " + this.getHost() + " and port " + this.getPort());
                    serverConnectorFuture.notifyPortBindingEvent(this.connectorID, isHttps);
                } else {
                    serverConnectorFuture.notifyPortBindingError(channelFuture.cause());
                }
            });
            httpServerChannelInitializer.setServerConnectorFuture(serverConnectorFuture);
            return serverConnectorFuture;
        }

        @Override
        public boolean stop() {
            boolean connectorStopped = false;
            
            try {
                connectorStopped = serverConnectorBootstrap.unBindInterface(this);
                if (connectorStopped) {
                    serverConnectorFuture.notifyPortUnbindingEvent(this.connectorID, isHttps);
                }
            } catch (InterruptedException e) {
                log.error("Couldn't close the port", e);
                return false;
            } catch (ServerConnectorException e) {
                log.error("Error in notifying life cycle event listener", e);
            }

            return connectorStopped;
        }

        @Override
        public String getConnectorID() {
            return this.connectorID;
        }

        private ChannelFuture getChannelFuture() {
            return channelFuture;
        }

        @Override
        public String toString() {
            return this.host + "-" + this.port;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }
    }
}
