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

package org.wso2.carbon.transport.http.netty.sender.channel.pool;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import org.apache.commons.pool.PoolableObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.transport.http.netty.common.HttpRoute;
import org.wso2.carbon.transport.http.netty.common.ProxyServerConfiguration;
import org.wso2.carbon.transport.http.netty.common.ssl.SSLConfig;
import org.wso2.carbon.transport.http.netty.common.ssl.SSLHandlerFactory;
import org.wso2.carbon.transport.http.netty.sender.HTTPClientInitializer;
import org.wso2.carbon.transport.http.netty.sender.channel.BootstrapConfiguration;
import org.wso2.carbon.transport.http.netty.sender.channel.TargetChannel;

import java.net.InetSocketAddress;
import javax.net.ssl.SSLEngine;

/**
 * A class which creates a TargetChannel pool for each route.
 */
public class PoolableTargetChannelFactory implements PoolableObjectFactory {

    private static final Logger log = LoggerFactory.getLogger(PoolableTargetChannelFactory.class);

    private EventLoopGroup eventLoopGroup;
    private Class eventLoopClass;
    private HttpRoute httpRoute;
    private SSLConfig sslConfig;
    private boolean httpTraceLogEnabled;
    private boolean followRedirect;
    private int maxRedirectCount;
    private boolean chunkDisabled;
    private ProxyServerConfiguration proxyServerConfiguration;

    public PoolableTargetChannelFactory(HttpRoute httpRoute, EventLoopGroup eventLoopGroup, Class eventLoopClass
            , SSLConfig sslConfig, boolean httpTraceLogEnabled, boolean chunkDisabled, boolean followRedirect
            , int maxRedirectCount, ProxyServerConfiguration proxyServerConfiguration) {
        this.eventLoopGroup = eventLoopGroup;
        this.eventLoopClass = eventLoopClass;
        this.httpRoute = httpRoute;
        this.sslConfig = sslConfig;
        this.httpTraceLogEnabled = httpTraceLogEnabled;
        this.followRedirect = followRedirect;
        this.maxRedirectCount = maxRedirectCount;
        this.chunkDisabled = chunkDisabled;
        this.proxyServerConfiguration = proxyServerConfiguration;
    }


    @Override
    public Object makeObject() throws Exception {
        Bootstrap clientBootstrap = instantiateAndConfigBootStrap(eventLoopGroup,
                eventLoopClass, BootstrapConfiguration.getInstance());
        SSLEngine clientSslEngine = instantiateAndConfigSSL(sslConfig);
        HTTPClientInitializer httpClientInitializer = instantiateAndConfigClientInitializer(clientBootstrap,
                clientSslEngine);
        clientBootstrap.handler(httpClientInitializer);
        ChannelFuture channelFuture = clientBootstrap
                .connect(new InetSocketAddress(httpRoute.getHost(), httpRoute.getPort()));
        TargetChannel targetChannel = new TargetChannel(httpClientInitializer, channelFuture);
        targetChannel.setHttpRoute(httpRoute);
        log.debug("Created channel: {}", httpRoute);
        return targetChannel;
    }

    @Override
    public void destroyObject(Object o) throws Exception {
        log.debug("Destroying channel: {}", o);
        if (((TargetChannel) o).getChannel().isOpen()) {
            if (log.isDebugEnabled()) {
                log.debug("And channel id is : " + ((TargetChannel) o).getChannel().id());
            }
            ((TargetChannel) o).getChannel().close();
        }
    }

    @Override
    public boolean validateObject(Object o) {
        if (((TargetChannel) o).getChannel() != null) {
            boolean answer = ((TargetChannel) o).getChannel().isActive();
            log.debug("Validating channel: {} -> {}", o, answer);
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

    private SSLEngine instantiateAndConfigSSL(SSLConfig sslConfig) {
        // set the pipeline factory, which creates the pipeline for each newly created channels
        SSLEngine sslEngine = null;
        if (sslConfig != null) {
            SSLHandlerFactory sslHandlerFactory = new SSLHandlerFactory(sslConfig);
            sslEngine = sslHandlerFactory.build();
            sslEngine.setUseClientMode(true);
            sslHandlerFactory.setSNIServerNames(sslEngine, httpRoute.getHost());
        }

        return sslEngine;
    }

    private HTTPClientInitializer instantiateAndConfigClientInitializer(Bootstrap clientBootstrap,
            SSLEngine sslEngine) {
        HTTPClientInitializer httpClientInitializer = new HTTPClientInitializer(sslEngine, httpTraceLogEnabled
                , chunkDisabled, followRedirect, maxRedirectCount, proxyServerConfiguration);
        if (log.isDebugEnabled()) {
            log.debug("Created new TCP client bootstrap connecting to {}:{} with options: {}", httpRoute.getHost(),
                    httpRoute.getPort(), clientBootstrap);
        }
        return httpClientInitializer;
    }
}
