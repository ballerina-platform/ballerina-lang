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

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.HttpRoute;
import org.wso2.transport.http.netty.config.SenderConfiguration;
import org.wso2.transport.http.netty.listener.SourceHandler;
import org.wso2.transport.http.netty.sender.channel.BootstrapConfiguration;
import org.wso2.transport.http.netty.sender.channel.TargetChannel;
import org.wso2.transport.http.netty.sender.http2.Http2ConnectionManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A class which handles connection pool management.
 */
public class ConnectionManager {

    private static final Logger log = LoggerFactory.getLogger(ConnectionManager.class);

    private EventLoopGroup clientEventGroup;
    private PoolConfiguration poolConfiguration;
    private PoolManagementPolicy poolManagementPolicy;
    private BootstrapConfiguration bootstrapConfig;
    private final Map<String, GenericObjectPool> connGlobalPool;
    private Http2ConnectionManager http2ConnectionManager;

    public ConnectionManager(SenderConfiguration senderConfig, BootstrapConfiguration bootstrapConfiguration,
                             EventLoopGroup clientEventGroup) {
        this.poolConfiguration = senderConfig.getPoolConfiguration();
        if (poolConfiguration.getNumberOfPools() == 1) {
            this.poolManagementPolicy = PoolManagementPolicy.LOCK_DEFAULT_POOLING;
        }
        connGlobalPool = new ConcurrentHashMap<>();
        this.clientEventGroup = clientEventGroup;

        this.bootstrapConfig = bootstrapConfiguration;
        this.http2ConnectionManager = new Http2ConnectionManager(senderConfig);
    }

    private GenericObjectPool createPoolForRoute(PoolableTargetChannelFactory poolableTargetChannelFactory) {
        return new GenericObjectPool(poolableTargetChannelFactory, instantiateAndConfigureConfig());
    }

    private GenericObjectPool createPoolForRoutePerSrcHndlr(GenericObjectPool genericObjectPool) {
        return new GenericObjectPool(new PoolableTargetChannelFactoryPerSrcHndlr(genericObjectPool),
                instantiateAndConfigureConfig());
    }

    /**
     * @param httpRoute BE address
     * @param sourceHandler Incoming channel
     * @param senderConfig The sender configuration instance
     * @return the target channel which is requested for given parameters.
     * @throws Exception to notify any errors occur during retrieving the target channel
     */
    public TargetChannel borrowTargetChannel(HttpRoute httpRoute, SourceHandler sourceHandler,
                                             SenderConfiguration senderConfig) throws Exception {
        GenericObjectPool trgHlrConnPool;

        if (sourceHandler != null) {
            EventLoopGroup group;
            ChannelHandlerContext ctx = sourceHandler.getInboundChannelContext();
            group = ctx.channel().eventLoop();
            Class eventLoopClass = ctx.channel().getClass();

            if (poolManagementPolicy == PoolManagementPolicy.LOCK_DEFAULT_POOLING) {
                // This is faster than the above one (about 2k difference)
                Map<String, GenericObjectPool> srcHlrConnPool = sourceHandler.getTargetChannelPool();
                trgHlrConnPool = srcHlrConnPool.get(httpRoute.toString());
                if (trgHlrConnPool == null) {
                    PoolableTargetChannelFactory poolableTargetChannelFactory =
                            new PoolableTargetChannelFactory(group, eventLoopClass, httpRoute, senderConfig,
                                    bootstrapConfig, this);
                    trgHlrConnPool = createPoolForRoute(poolableTargetChannelFactory);
                    srcHlrConnPool.put(httpRoute.toString(), trgHlrConnPool);
                }
            } else {
                Map<String, GenericObjectPool> srcHlrConnPool = sourceHandler.getTargetChannelPool();
                trgHlrConnPool = srcHlrConnPool.get(httpRoute.toString());
                if (trgHlrConnPool == null) {
                    synchronized (this) {
                        if (!this.connGlobalPool.containsKey(httpRoute.toString())) {
                            PoolableTargetChannelFactory poolableTargetChannelFactory =
                                    new PoolableTargetChannelFactory(group,
                                            eventLoopClass, httpRoute, senderConfig, bootstrapConfig, this);
                            trgHlrConnPool = createPoolForRoute(poolableTargetChannelFactory);
                            this.connGlobalPool.put(httpRoute.toString(), trgHlrConnPool);
                        }
                        trgHlrConnPool = this.connGlobalPool.get(httpRoute.toString());
                        trgHlrConnPool = createPoolForRoutePerSrcHndlr(trgHlrConnPool);
                    }
                    srcHlrConnPool.put(httpRoute.toString(), trgHlrConnPool);
                }
            }
        } else {
            Class cl = NioSocketChannel.class;
            EventLoopGroup group = clientEventGroup;
            synchronized (this) {
                if (!this.connGlobalPool.containsKey(httpRoute.toString())) {
                    PoolableTargetChannelFactory poolableTargetChannelFactory =
                            new PoolableTargetChannelFactory(group, cl,
                                    httpRoute, senderConfig, bootstrapConfig, this);
                    trgHlrConnPool = createPoolForRoute(poolableTargetChannelFactory);
                    this.connGlobalPool.put(httpRoute.toString(), trgHlrConnPool);
                }
                trgHlrConnPool = this.connGlobalPool.get(httpRoute.toString());
            }
        }

        TargetChannel targetChannel = (TargetChannel) trgHlrConnPool.borrowObject();
        targetChannel.setCorrelatedSource(sourceHandler);
        targetChannel.setConnectionManager(this);
        return targetChannel;
    }

    public void returnChannel(TargetChannel targetChannel) throws Exception {
        targetChannel.setRequestWritten(false);
        if (targetChannel.getCorrelatedSource() != null) {
            Map<String, GenericObjectPool> objectPoolMap = targetChannel.getCorrelatedSource().getTargetChannelPool();
            releaseChannelToPool(targetChannel, objectPoolMap.get(targetChannel.getHttpRoute().toString()));
        } else {
            releaseChannelToPool(targetChannel, this.connGlobalPool.get(targetChannel.getHttpRoute().toString()));
        }
    }

    private void releaseChannelToPool(TargetChannel targetChannel, GenericObjectPool pool) throws Exception {
        try {
            String channelID = targetChannel.getChannel().id().asShortText();
            if (targetChannel.getChannel().isActive() && pool != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Returning connection {} to the pool", channelID);
                }
                pool.returnObject(targetChannel);
            } else {
                log.warn("Channel {} is inactive hence not returning to connection pool", channelID);
            }
        } catch (Exception e) {
            throw new Exception("Couldn't return channel {} to pool", e);
        }
    }

    public void invalidateTargetChannel(TargetChannel targetChannel) throws Exception {
        targetChannel.setRequestWritten(false);
        if (targetChannel.getCorrelatedSource() != null) {
            Map<String, GenericObjectPool> objectPoolMap = targetChannel.getCorrelatedSource().getTargetChannelPool();
            try {
                // Need a null check because SourceHandler side could timeout before TargetHandler side.
                String httpRoute = targetChannel.getHttpRoute().toString();
                if (objectPoolMap.get(httpRoute) != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Invalidating connection {} to the pool",
                                targetChannel.getChannel().id().asShortText());
                    }
                    objectPoolMap.get(httpRoute).invalidateObject(targetChannel);
                }
            } catch (Exception e) {
                throw new Exception("Cannot invalidate channel from pool", e);
            }
        }
    }

    /**
     * Connection pool management policies for  target channels.
     */
    public enum PoolManagementPolicy {
        LOCK_DEFAULT_POOLING,
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

        if (log.isDebugEnabled()) {
            log.debug("Creating a pool with {}", config.toString());
        }
        return config;
    }

    public Http2ConnectionManager getHttp2ConnectionManager() {
        return http2ConnectionManager;
    }
}
