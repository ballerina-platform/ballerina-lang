/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package org.wso2.transport.http.netty.sender.channel.pool;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import org.apache.commons.pool.PoolableObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.HttpRoute;
import org.wso2.transport.http.netty.config.SenderConfiguration;
import org.wso2.transport.http.netty.sender.ConnectionAvailabilityFuture;
import org.wso2.transport.http.netty.sender.HttpClientChannelInitializer;
import org.wso2.transport.http.netty.sender.channel.BootstrapConfiguration;
import org.wso2.transport.http.netty.sender.channel.TargetChannel;

import java.net.InetSocketAddress;

/**
 * A class which creates a TargetChannel pool for each route.
 */
public class PoolableTargetChannelFactory implements PoolableObjectFactory {

    private static final Logger log = LoggerFactory.getLogger(PoolableTargetChannelFactory.class);

    private EventLoopGroup eventLoopGroup;
    private Class eventLoopClass;
    private HttpRoute httpRoute;
    private SenderConfiguration senderConfiguration;
    private BootstrapConfiguration bootstrapConfiguration;
    private ConnectionManager connectionManager;

    PoolableTargetChannelFactory(EventLoopGroup eventLoopGroup, Class eventLoopClass, HttpRoute httpRoute,
                                        SenderConfiguration senderConfiguration,
                                        BootstrapConfiguration bootstrapConfiguration,
                                        ConnectionManager connectionManager) {
        this.eventLoopGroup = eventLoopGroup;
        this.eventLoopClass = eventLoopClass;
        this.httpRoute = httpRoute;
        this.senderConfiguration = senderConfiguration;
        this.bootstrapConfiguration = bootstrapConfiguration;
        this.connectionManager = connectionManager;
    }


    @Override
    public Object makeObject() throws Exception {
        Bootstrap clientBootstrap = instantiateAndConfigBootStrap(eventLoopGroup,
                eventLoopClass, bootstrapConfiguration);
        ConnectionAvailabilityFuture connectionAvailabilityFuture = new ConnectionAvailabilityFuture();

        HttpClientChannelInitializer httpClientChannelInitializer = instantiateAndConfigClientInitializer(
                senderConfiguration, clientBootstrap, httpRoute, connectionManager, connectionAvailabilityFuture);
        clientBootstrap.handler(httpClientChannelInitializer);

        ChannelFuture channelFuture = clientBootstrap
                .connect(new InetSocketAddress(httpRoute.getHost(), httpRoute.getPort()));
        connectionAvailabilityFuture.setSocketAvailabilityFuture(channelFuture);
        connectionAvailabilityFuture.setForceHttp2(senderConfiguration.isForceHttp2());

        TargetChannel targetChannel =
                new TargetChannel(httpClientChannelInitializer, channelFuture, httpRoute, connectionAvailabilityFuture);
        httpClientChannelInitializer.setHttp2ClientChannel(targetChannel.getHttp2ClientChannel());

        log.debug("Created channel: {}", httpRoute);

        return targetChannel;
    }

    @Override
    public void destroyObject(Object o) throws Exception {
        TargetChannel targetChannel = (TargetChannel) o;
        if (log.isDebugEnabled()) {
            log.debug("Destroying channel: {}", targetChannel.getChannel().id());
        }
        if (targetChannel.getChannel().isOpen()) {
            targetChannel.getChannel().close();
        }
    }

    @Override
    public boolean validateObject(Object o) {
        TargetChannel targetChannel = (TargetChannel) o;
        if (targetChannel.getChannel() != null) {
            boolean answer = targetChannel.getChannel().isActive();
            log.debug("Validating channel: {} -> {}", targetChannel.getChannel().id(), answer);
            return answer;
        }
        return true;
    }

    @Override
    public void activateObject(Object o) throws Exception {

    }

    @Override
    public void passivateObject(Object o) throws Exception {

    }


    private Bootstrap instantiateAndConfigBootStrap(EventLoopGroup eventLoopGroup, Class eventLoopClass,
            BootstrapConfiguration bootstrapConfiguration) {
        Bootstrap clientBootstrap = new Bootstrap();
        clientBootstrap.channel(eventLoopClass);
        clientBootstrap.group(eventLoopGroup);
        clientBootstrap.option(ChannelOption.SO_KEEPALIVE, bootstrapConfiguration.isKeepAlive());
        clientBootstrap.option(ChannelOption.TCP_NODELAY, bootstrapConfiguration.isTcpNoDelay());
        clientBootstrap.option(ChannelOption.SO_REUSEADDR, bootstrapConfiguration.isSocketReuse());
        clientBootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, bootstrapConfiguration.getConnectTimeOut());
        return clientBootstrap;
    }

    private HttpClientChannelInitializer instantiateAndConfigClientInitializer(SenderConfiguration senderConfiguration,
            Bootstrap clientBootstrap, HttpRoute httpRoute, ConnectionManager connectionManager,
            ConnectionAvailabilityFuture connectionAvailabilityFuture) {
        HttpClientChannelInitializer httpClientChannelInitializer = new HttpClientChannelInitializer(
                senderConfiguration, httpRoute, connectionManager, connectionAvailabilityFuture);
        if (log.isDebugEnabled()) {
            log.debug("Created new TCP client bootstrap connecting to {}:{} with options: {}", httpRoute.getHost(),
                    httpRoute.getPort(), clientBootstrap);
        }
        return httpClientChannelInitializer;
    }
}
