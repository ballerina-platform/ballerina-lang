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

package org.wso2.transport.http.netty.contractimpl.sender.channel.pool;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.config.SenderConfiguration;
import org.wso2.transport.http.netty.contractimpl.common.HttpRoute;
import org.wso2.transport.http.netty.contractimpl.listener.SourceHandler;
import org.wso2.transport.http.netty.contractimpl.sender.channel.BootstrapConfiguration;
import org.wso2.transport.http.netty.contractimpl.sender.channel.TargetChannel;
import org.wso2.transport.http.netty.contractimpl.sender.http2.Http2ConnectionManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A class which handles connection pool management.
 */
public class ConnectionManager {

    private static final Logger LOG = LoggerFactory.getLogger(ConnectionManager.class);

    private PoolConfiguration poolConfiguration;
    private final Map<String, GenericObjectPool> connGlobalPool;
    private Http2ConnectionManager http2ConnectionManager;

    public ConnectionManager(PoolConfiguration poolConfiguration) {
        this.poolConfiguration = poolConfiguration;
        connGlobalPool = new ConcurrentHashMap<>();
        this.http2ConnectionManager = new Http2ConnectionManager(poolConfiguration);
    }

    private GenericObjectPool createPoolForRoute(PoolableTargetChannelFactory poolableTargetChannelFactory) {
        return new GenericObjectPool(poolableTargetChannelFactory, instantiateAndConfigureConfig());
    }

    public void returnChannel(TargetChannel targetChannel) throws Exception {
        releaseChannelToPool(targetChannel, this.connGlobalPool.get(targetChannel.getHttpRoute().toString()));
    }

    private void releaseChannelToPool(TargetChannel targetChannel, GenericObjectPool pool) throws Exception {
        try {
            String channelID = targetChannel.getChannel().id().asShortText();
            if (targetChannel.getChannel().isActive() && pool != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Returning connection {} to the pool", channelID);
                }
                pool.returnObject(targetChannel);
            } else {
                LOG.debug("Channel {} is inactive hence not returning to connection pool", channelID);
            }
        } catch (Exception e) {
            throw new Exception("Couldn't return channel to pool", e);
        }
    }

    public void invalidateTargetChannel(TargetChannel targetChannel) throws Exception {
        this.connGlobalPool.get(targetChannel.getHttpRoute().toString()).invalidateObject(targetChannel);
    }

    private GenericObjectPool.Config instantiateAndConfigureConfig() {
        GenericObjectPool.Config config = new GenericObjectPool.Config();
        config.maxActive = poolConfiguration.getMaxActivePerPool();
        config.maxIdle = poolConfiguration.getMaxIdlePerPool();
        config.minIdle = poolConfiguration.getMinIdlePerPool();
        config.testOnBorrow = poolConfiguration.isTestOnBorrow();
        config.testWhileIdle = poolConfiguration.isTestWhileIdle();
        config.timeBetweenEvictionRunsMillis = poolConfiguration.getTimeBetweenEvictionRuns();
        config.minEvictableIdleTimeMillis = poolConfiguration.getMinEvictableIdleTime();
        config.whenExhaustedAction = poolConfiguration.getExhaustedAction();
        config.maxWait = poolConfiguration.getMaxWaitTime();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating a pool with {}", config);
        }
        return config;
    }

    public Http2ConnectionManager getHttp2ConnectionManager() {
        return http2ConnectionManager;
    }

    public GenericObjectPool getClientPool(HttpRoute httpRoute, SourceHandler sourceHandler,
        SenderConfiguration senderConfig, BootstrapConfiguration bootstrapConfig, EventLoopGroup clientEventGroup) {
        GenericObjectPool clientPool;
        EventLoopGroup group;
        Class eventLoopClass;
        if (sourceHandler != null) {
            ChannelHandlerContext ctx = sourceHandler.getInboundChannelContext();
            group = ctx.channel().eventLoop();
            eventLoopClass = ctx.channel().getClass();
            clientPool = getGenericObjectPool(httpRoute, senderConfig, bootstrapConfig, eventLoopClass, group);
        } else {
            eventLoopClass = NioSocketChannel.class;
            group = clientEventGroup;
            clientPool = getGenericObjectPool(httpRoute, senderConfig, bootstrapConfig, eventLoopClass, group);
        }
        return clientPool;
    }

    private GenericObjectPool getGenericObjectPool(HttpRoute httpRoute, SenderConfiguration senderConfig,
        BootstrapConfiguration bootstrapConfig, Class eventLoopClass, EventLoopGroup group) {
        GenericObjectPool clientPool;
        synchronized (this) {
            if (!this.connGlobalPool.containsKey(httpRoute.toString())) {
                PoolableTargetChannelFactory poolableTargetChannelFactory = new PoolableTargetChannelFactory(group,
                    eventLoopClass, httpRoute, senderConfig, bootstrapConfig, this);
                clientPool = createPoolForRoute(poolableTargetChannelFactory);
                this.connGlobalPool.put(httpRoute.toString(), clientPool);
            } else {
                clientPool = this.connGlobalPool.get(httpRoute.toString());
            }
        }
        return clientPool;
    }
}
