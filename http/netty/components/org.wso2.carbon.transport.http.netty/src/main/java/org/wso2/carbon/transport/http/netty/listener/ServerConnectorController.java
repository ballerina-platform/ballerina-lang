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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.config.TransportProperty;
import org.wso2.carbon.transport.http.netty.config.TransportsConfiguration;
import org.wso2.carbon.transport.http.netty.internal.HTTPTransportContextHolder;

import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.util.Set;

/**
 * {@code ServerConnectorController} is the heart of the HTTP Server Connector.
 * <p>
 * This is responsible for creating the bootstrap and allow bind/unbind to interfaces
 */
public class ServerConnectorController {

    private static final Logger log = LoggerFactory.getLogger(ServerConnectorController.class);

    private ServerBootstrap bootstrap;

    private HTTPServerChannelInitializer handler;

    private boolean initialized = false;

    private static PrintStream outStream = System.out;

    public ServerConnectorController (TransportsConfiguration trpConfig) {

        Set<TransportProperty> transportProperties = trpConfig.getTransportProperties();

        // Thread Pool configurations
        int bossGroupSize = 0, workerGroupSize = 0;
        for (TransportProperty property : transportProperties) {
            if (property.getName().equals(Constants.SERVER_BOOTSTRAP_BOSS_GROUP_SIZE)) {
                bossGroupSize = (Integer) property.getValue();
            } else if (property.getName().equals(Constants.SERVER_BOOTSTRAP_WORKER_GROUP_SIZE)) {
                workerGroupSize = (Integer) property.getValue();
            }
        }

        // Create Bootstrap Configuration from listener parameters
        ServerBootstrapConfiguration.createBootStrapConfiguration(transportProperties);
        ServerBootstrapConfiguration serverBootstrapConfiguration = ServerBootstrapConfiguration.getInstance();

        // Create Boss Group - boss group is for accepting channels
        EventLoopGroup bossGroup = HTTPTransportContextHolder.getInstance().getBossGroup();
        if (bossGroup == null) {
            bossGroup = new NioEventLoopGroup(
                    bossGroupSize != 0 ? bossGroupSize : Runtime.getRuntime().availableProcessors());
            HTTPTransportContextHolder.getInstance().setBossGroup(bossGroup);
        }

        // Create Worker Group - worker group is for processing IO
        EventLoopGroup workerGroup = HTTPTransportContextHolder.getInstance().getWorkerGroup();
        if (workerGroup == null) {
            workerGroup = new NioEventLoopGroup(
                    workerGroupSize != 0 ? workerGroupSize : Runtime.getRuntime().availableProcessors() * 2);
            HTTPTransportContextHolder.getInstance().setWorkerGroup(workerGroup);
        }

        bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class);

        // Register Channel initializer
        handler = new HTTPServerChannelInitializer();
        handler.setup(null);
        handler.setupConnectionManager(transportProperties);
        bootstrap.childHandler(handler);


        // Set other bootstrap parameters
        bootstrap.option(ChannelOption.SO_BACKLOG, serverBootstrapConfiguration.getSoBackLog());
        log.debug("Netty Server Socket BACKLOG " + serverBootstrapConfiguration.getSoBackLog());
        bootstrap.childOption(ChannelOption.TCP_NODELAY, serverBootstrapConfiguration.isTcpNoDelay());
        log.debug("Netty Server Socket TCP_NODELAY " + serverBootstrapConfiguration.isTcpNoDelay());
        bootstrap.option(ChannelOption.SO_KEEPALIVE, serverBootstrapConfiguration.isKeepAlive());
        log.debug("Netty Server Socket SO_KEEPALIVE " + serverBootstrapConfiguration.isKeepAlive());
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, serverBootstrapConfiguration.getConnectTimeOut());
        log.debug(" Netty Server Socket CONNECT_TIMEOUT_MILLIS " + serverBootstrapConfiguration.getConnectTimeOut());
        bootstrap.option(ChannelOption.SO_SNDBUF, serverBootstrapConfiguration.getSendBufferSize());
        log.debug("Netty Server Socket SO_SNDBUF " + serverBootstrapConfiguration.getSendBufferSize());
        bootstrap.option(ChannelOption.SO_RCVBUF, serverBootstrapConfiguration.getReciveBufferSize());
        log.debug("Netty Server Socket SO_RCVBUF " + serverBootstrapConfiguration.getReciveBufferSize());
        bootstrap.childOption(ChannelOption.SO_RCVBUF, serverBootstrapConfiguration.getReciveBufferSize());
        log.debug("Netty Server Socket SO_RCVBUF " + serverBootstrapConfiguration.getReciveBufferSize());
        bootstrap.childOption(ChannelOption.SO_SNDBUF, serverBootstrapConfiguration.getSendBufferSize());
        log.debug("Netty Server Socket SO_SNDBUF " + serverBootstrapConfiguration.getSendBufferSize());

        initialized = true;

    }

    public boolean bindInterface(HTTPServerConnector serverConnector) {

        if (!initialized) {
            log.error("ServerConnectorController is not initialized");
            return false;
        }

        try {
            ListenerConfiguration listenerConfiguration = serverConnector.getListenerConfiguration();

            handler.registerListenerConfig(listenerConfiguration);

            ChannelFuture future = bootstrap.bind(new InetSocketAddress(listenerConfiguration.getHost(),
                                                                        listenerConfiguration.getPort())).sync();
            serverConnector.setChannelFuture(future);

            if (future.isSuccess()) {

                String msg = "Started listener " +
                             listenerConfiguration.getScheme() + "-" + listenerConfiguration.getPort();

                outStream.println(msg);
                log.info(msg);

                if (listenerConfiguration.getSslConfig() == null) {
                    log.info("HTTP Interface " + listenerConfiguration.getId() + " starting on host  " +
                             listenerConfiguration.getHost() + " and port " + listenerConfiguration.getPort());
                } else {
                    log.info("HTTPS Interface " + listenerConfiguration.getId() + " starting on host  " +
                             listenerConfiguration.getHost() + " and port " + listenerConfiguration.getPort());
                }
                return true;
            } else {
                log.error("Cannot bind port for host " + listenerConfiguration.getHost() + " port "
                          + listenerConfiguration.getPort());
            }

        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    public boolean unBindInterface(HTTPServerConnector serverConnector) {

        if (!initialized) {
            log.error("ServerConnectorController is not initialized");
            return false;
        }

        ListenerConfiguration listenerConfiguration = serverConnector.getListenerConfiguration();

        handler.unRegisterListenerConfig(listenerConfiguration);

        //Remove cached channels and close them.
        ChannelFuture future = serverConnector.getChannelFuture();
        if (future != null) {
            future.channel().close();
            if (listenerConfiguration.getSslConfig() == null) {
                log.info("HTTP Listener stopped on listening interface " +
                         listenerConfiguration.getId() + " attached to host " +
                         listenerConfiguration.getHost() + " and port " + listenerConfiguration.getPort());
            } else {
                log.info("HTTPS Listener stopped on listening interface " +
                         listenerConfiguration.getId() + " attached to host " + listenerConfiguration.getHost() +
                         " and port " + listenerConfiguration.getPort());
            }
            return true;
        }
        return false;
    }

}
