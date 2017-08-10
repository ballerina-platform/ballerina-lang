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

package org.wso2.carbon.transport.http.netty.listener;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.carbon.transport.http.netty.contract.ServerConnector;
import org.wso2.carbon.messaging.handler.HandlerExecutor;
import org.wso2.carbon.transport.http.netty.common.ssl.SSLHandlerFactory;
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.contractImpl.HTTPServerConnectorFuture;
import org.wso2.carbon.transport.http.netty.internal.HTTPTransportContextHolder;

import java.net.InetSocketAddress;
import javax.net.ssl.SSLEngine;

/**
 * {@code ServerConnectorBootstrap} is the heart of the HTTP Server Connector.
 * <p>
 * This is responsible for creating the serverBootstrap and allow bind/unbind to interfaces
 */
public class ServerConnectorBootstrap {

    private static final Logger log = LoggerFactory.getLogger(ServerConnectorBootstrap.class);

    private ServerBootstrap serverBootstrap;
    private HTTPServerChannelInitializer httpServerChannelInitializer;
    private ListenerConfiguration listenerConfiguration;
    private boolean initialized = false;

    public ServerConnectorBootstrap(ListenerConfiguration listenerConfiguration) {
        this.listenerConfiguration = listenerConfiguration;
        serverBootstrap = new ServerBootstrap();
        httpServerChannelInitializer = new HTTPServerChannelInitializer(this.listenerConfiguration);
        serverBootstrap.childHandler(httpServerChannelInitializer);
        HTTPTransportContextHolder.getInstance().setHandlerExecutor(new HandlerExecutor());
        initialized = true;
    }

    public void stop() {
        httpServerChannelInitializer.getConnectionManager().getTargetChannelPool().clear();
        shutdownEventLoops();
    }

    private void shutdownEventLoops() {
        try {
            EventLoopGroup bossGroup = HTTPTransportContextHolder.getInstance().getBossGroup();
            if (bossGroup != null) {
                bossGroup.shutdownGracefully().sync();
                HTTPTransportContextHolder.getInstance().setBossGroup(null);
            }
            EventLoopGroup workerGroup = HTTPTransportContextHolder.getInstance().getWorkerGroup();
            if (workerGroup != null) {
                workerGroup.shutdownGracefully().sync();
                HTTPTransportContextHolder.getInstance().setWorkerGroup(null);
            }
            log.info("HTTP transport event loops stopped successfully");
        } catch (InterruptedException e) {
            log.error("Error while shutting down event loops " + e.getMessage());
        }
    }

    private ChannelFuture bindInterface(HTTPServerConnector serverConnector) {

        if (!initialized) {
            log.error("ServerConnectorBootstrap is not initialized");
            return null;
        }

        ChannelFuture future = serverBootstrap.bind(
                new InetSocketAddress(serverConnector.getHost(), serverConnector.getPort()));
        future.addListener(future1 -> {
            if (future1.isSuccess()) {
                log.info("Started listener " +
                        listenerConfiguration.getScheme() + "-" + listenerConfiguration.getPort());
                log.info("HTTP(S) Interface " + listenerConfiguration.getId() + " starting on host  " +
                        listenerConfiguration.getHost() + " and port " + listenerConfiguration.getPort());
            } else {
                log.error("Cannot bind port for host " + listenerConfiguration.getHost() + " port "
                        + listenerConfiguration.getPort());
            }
        });

        return future;

        // TODO: Fix this with HTTP2
//            ListenerConfiguration listenerConfiguration = serverConnector.getListenerConfiguration();
//            SslContext http2sslContext = null;
//            // Create HTTP/2 ssl context during interface binding.
//            if (listenerConfiguration.isHttp2() && listenerConfiguration.getSslConfig() != null) {
//                http2sslContext = new SSLHandlerFactory(listenerConfiguration.getSslConfig())
//                        .createHttp2TLSContext();
//            }

//            httpServerChannelInitializer.registerListenerConfig(listenerConfiguration, http2sslContext);
//            httpServerChannelInitializer.setSslContext(http2sslContext);
//            httpServerChannelInitializer.setServerConnectorFuture(serverConnector.getServerConnectorFuture());
    }

    public boolean unBindInterface(HTTPServerConnector serverConnector) throws InterruptedException {

        if (!initialized) {
            log.error("ServerConnectorBootstrap is not initialized");
            return false;
        }

        ListenerConfiguration listenerConfiguration = serverConnector.getListenerConfiguration();

        //Remove cached channels and close them.
        ChannelFuture future = serverConnector.getChannelFuture();
        if (future != null) {
            ChannelFuture channelFuture = future.channel().close();
            channelFuture.sync();
            if (listenerConfiguration.getSslConfig() == null) {
                log.info("HTTP HTTPConnectorListener stopped on listening interface " +
                         listenerConfiguration.getId() + " attached to host " +
                         listenerConfiguration.getHost() + " and port " + listenerConfiguration.getPort());
            } else {
                log.info("HTTPS HTTPConnectorListener stopped on listening interface " +
                         listenerConfiguration.getId() + " attached to host " + listenerConfiguration.getHost() +
                         " and port " + listenerConfiguration.getPort());
            }
            return true;
        }
        return false;
    }

//    public TransportsConfiguration getTransportsConfiguration() {
//        return transportsConfiguration;
//    }

    public void setChannelInitializer(ListenerConfiguration listenerConfiguration) {
        httpServerChannelInitializer = new HTTPServerChannelInitializer(listenerConfiguration);
        serverBootstrap.childHandler(httpServerChannelInitializer);
        this.listenerConfiguration = listenerConfiguration;
    }

    public ServerConnector getServerConnector(ListenerConfiguration listenerConfiguration) {
        HTTPServerConnector httpServerConnector = new HTTPServerConnector("serviceID", this,
                listenerConfiguration.getHost(), listenerConfiguration.getPort());
        httpServerChannelInitializer.setServerConnectorFuture(httpServerConnector.getServerConnectorFuture());
        return httpServerConnector;
    }

    public void addSocketConfiguration(ServerBootstrapConfiguration serverBootstrapConfiguration) {
        // Set other serverBootstrap parameters
        serverBootstrap.option(ChannelOption.SO_BACKLOG, serverBootstrapConfiguration.getSoBackLog());
        serverBootstrap.childOption(ChannelOption.TCP_NODELAY, serverBootstrapConfiguration.isTcpNoDelay());
        serverBootstrap.option(ChannelOption.SO_KEEPALIVE, serverBootstrapConfiguration.isKeepAlive());
        serverBootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, serverBootstrapConfiguration.getConnectTimeOut());
        serverBootstrap.option(ChannelOption.SO_SNDBUF, serverBootstrapConfiguration.getSendBufferSize());
        serverBootstrap.option(ChannelOption.SO_RCVBUF, serverBootstrapConfiguration.getReceiveBufferSize());
        serverBootstrap.childOption(ChannelOption.SO_RCVBUF, serverBootstrapConfiguration.getReceiveBufferSize());
        serverBootstrap.childOption(ChannelOption.SO_SNDBUF, serverBootstrapConfiguration.getSendBufferSize());

        log.debug("Netty Server Socket BACKLOG " + serverBootstrapConfiguration.getSoBackLog());
        log.debug("Netty Server Socket TCP_NODELAY " + serverBootstrapConfiguration.isTcpNoDelay());
        log.debug("Netty Server Socket SO_KEEPALIVE " + serverBootstrapConfiguration.isKeepAlive());
        log.debug("Netty Server Socket CONNECT_TIMEOUT_MILLIS " + serverBootstrapConfiguration.getConnectTimeOut());
        log.debug("Netty Server Socket SO_SNDBUF " + serverBootstrapConfiguration.getSendBufferSize());
        log.debug("Netty Server Socket SO_RCVBUF " + serverBootstrapConfiguration.getReceiveBufferSize());
        log.debug("Netty Server Socket SO_RCVBUF " + serverBootstrapConfiguration.getReceiveBufferSize());
        log.debug("Netty Server Socket SO_SNDBUF " + serverBootstrapConfiguration.getSendBufferSize());
    }

    public void addSecurity(ListenerConfiguration listenerConfiguration) {
        if (listenerConfiguration.getSslConfig() != null) {
            SSLEngine sslEngine = new SSLHandlerFactory(listenerConfiguration.getSslConfig()).build();
            httpServerChannelInitializer.setSslEngine(sslEngine);
        }
    }

    public void addIdleTimeout(ListenerConfiguration listenerConfiguration) {
        int socketIdleTimeout = listenerConfiguration.getSocketIdleTimeout(120000);
        httpServerChannelInitializer.setIdleTimeout(socketIdleTimeout);
    }

    public void addThreadPools(int parentSize, int childSize) {
        EventLoopGroup bossGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors());
        EventLoopGroup workerGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2);
        serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class);
    }

    class HTTPServerConnector implements ServerConnector {

       private final Logger log = LoggerFactory.getLogger(HTTPServerConnector.class);

        private ChannelFuture channelFuture;
        private ServerConnectorFuture serverConnectorFuture;
        private ServerConnectorBootstrap serverConnectorBootstrap;
        private String host;
        private int port;

        public HTTPServerConnector(String id, ServerConnectorBootstrap serverConnectorBootstrap,
                String host, int port) {
            serverConnectorFuture = new HTTPServerConnectorFuture();
            this.serverConnectorBootstrap = serverConnectorBootstrap;
            this.host = host;
            this.port = port;
        }

        @Override
        public ServerConnectorFuture start() {
            //            if (listenerConfiguration.isBindOnStartup()) { // Already bind at the startup, hence skipping
            //                return null;
            //            }
            channelFuture = serverConnectorBootstrap.bindInterface(this);
            return getServerConnectorFuture();
        }

        @Override
        public boolean stop() {
            try {
                return serverConnectorBootstrap.unBindInterface(this);
            } catch (InterruptedException e) {
                log.error("Couldn't close the port", e);
                return false;
            }
        }

        //    @Override
        //    public void beginMaintenance() {
        //    }

        //    @Override
        //    public void endMaintenance() {
        //    }

        //    @Override
        //    public void setMessageProcessor(CarbonMessageProcessor carbonMessageProcessor) {
        //        HTTPTransportContextHolder.getInstance().setMessageProcessor(carbonMessageProcessor);
        //    }

        //    @Override
        //    public void init() throws ServerConnectorException {
        //        log.info("Initializing  HTTP Transport HTTPConnectorListener");
        //    }

        //    @Override
        //    protected void destroy() throws ServerConnectorException {
        //        log.info("Destroying  HTTP Transport HTTPConnectorListener");
        //    }

        public ChannelFuture getChannelFuture() {
            return channelFuture;
        }

        //    public void setChannelFuture(ChannelFuture channelFuture) {
        //        this.channelFuture = channelFuture;
        //    }

        public ListenerConfiguration getListenerConfiguration() {
            return listenerConfiguration;
        }

        //        public void setListenerConfiguration(ListenerConfiguration listenerConfiguration) {
        //            this.listenerConfiguration = listenerConfiguration;
        //        }

        //    public void setServerConnectorBootstrap(
        //            ServerConnectorBootstrap serverConnectorBootstrap) {
        //        this.serverConnectorBootstrap = serverConnectorBootstrap;
        //    }

        //    public ServerConnectorBootstrap getServerConnectorBootstrap() {
        //        return serverConnectorBootstrap;
        //    }
        //
        @Override
        public String toString() {
            return listenerConfiguration.getScheme() + "-" + listenerConfiguration.getPort();
        }

        public ServerConnectorFuture getServerConnectorFuture() {
            return serverConnectorFuture;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }
    }
}
