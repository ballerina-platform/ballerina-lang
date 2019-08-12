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
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.config.SenderConfiguration;
import org.wso2.transport.http.netty.contractimpl.common.HttpRoute;
import org.wso2.transport.http.netty.contractimpl.listener.SourceHandler;
import org.wso2.transport.http.netty.contractimpl.listener.http2.Http2SourceHandler;
import org.wso2.transport.http.netty.contractimpl.sender.channel.BootstrapConfiguration;
import org.wso2.transport.http.netty.contractimpl.sender.channel.TargetChannel;
import org.wso2.transport.http.netty.contractimpl.sender.http2.Http2ConnectionManager;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A class which handles connection pool management.
 */
public class ConnectionManager {

    private static final Logger LOG = LoggerFactory.getLogger(ConnectionManager.class);

    private final String connectionManagerId;
    private final PoolConfiguration poolConfiguration;
    private final Map<String, GenericObjectPool> globalConnPool;
    private final Map<String, PoolableTargetChannelFactory> globalFactoryObjects;
    private final Http2ConnectionManager http2ConnectionManager;

    public ConnectionManager(PoolConfiguration poolConfiguration) {
        this.poolConfiguration = poolConfiguration;
        globalConnPool = new ConcurrentHashMap<>();
        globalFactoryObjects = new ConcurrentHashMap<>();
        http2ConnectionManager = new Http2ConnectionManager(poolConfiguration);
        connectionManagerId = "-" + UUID.randomUUID().toString();
    }

    /**
     * Gets the client target channel pool.
     *
     * @param httpRoute          Represents the endpoint address
     * @param sourceHandler      Represents the HTTP/1.x source handler
     * @param http2SourceHandler Represents the HTTP/2 source handler
     * @param senderConfig       Represents the client configurations
     * @param bootstrapConfig    Represents the bootstrap info related to client connection creation
     * @param clientEventGroup   Represents the eventloop group that the client channel should be bound to
     * @return the target channel which is requested for given parameters.
     * @throws Exception to notify any errors occur during retrieving the target channel
     */
    public TargetChannel borrowTargetChannel(HttpRoute httpRoute, SourceHandler sourceHandler,
                                             Http2SourceHandler http2SourceHandler,
                                             SenderConfiguration senderConfig, BootstrapConfiguration bootstrapConfig,
                                             EventLoopGroup clientEventGroup) throws Exception {
        GenericObjectPool trgHlrConnPool;
        String trgHlrConnPoolId = httpRoute.toString() + connectionManagerId;

        if (sourceHandler != null) {
            ChannelHandlerContext inboundChannelContext = sourceHandler.getInboundChannelContext();


            trgHlrConnPool = getTrgHlrPoolFromGlobalPoolWithSrcPool(httpRoute, senderConfig, bootstrapConfig,
                                                                    trgHlrConnPoolId,
                                                                    inboundChannelContext.channel().eventLoop(),
                                                                    inboundChannelContext.channel().getClass(),
                                                                    sourceHandler.getTargetChannelPool());
        } else if (http2SourceHandler != null) {
            ChannelHandlerContext inboundChannelContext = http2SourceHandler.getInboundChannelContext();
            trgHlrConnPool = getTrgHlrPoolFromGlobalPoolWithSrcPool(httpRoute, senderConfig, bootstrapConfig,
                                                                    trgHlrConnPoolId,
                                                                    inboundChannelContext.channel().eventLoop(),
                                                                    inboundChannelContext.channel().getClass(),
                                                                    http2SourceHandler.getTargetChannelPool());

        } else {
            trgHlrConnPool = getTrgHlrPoolFromGlobalPool(httpRoute, senderConfig, bootstrapConfig, clientEventGroup);
        }

        return getTargetChannel(sourceHandler, http2SourceHandler, trgHlrConnPool, trgHlrConnPoolId);
    }

    private GenericObjectPool getTrgHlrPoolFromGlobalPool(HttpRoute httpRoute, SenderConfiguration senderConfig,
                                                          BootstrapConfiguration bootstrapConfig,
                                                          EventLoopGroup clientEventGroup) {
        GenericObjectPool trgHlrConnPool;
        Class eventLoopClass = NioSocketChannel.class;
        synchronized (this) {
            if (!globalConnPool.containsKey(httpRoute.toString())) {
                createTrgHlrPoolInGlobalPool(httpRoute, senderConfig, bootstrapConfig, clientEventGroup,
                                             eventLoopClass);
            }
            trgHlrConnPool = globalConnPool.get(httpRoute.toString());
        }
        return trgHlrConnPool;
    }

    private GenericObjectPool getTrgHlrPoolFromGlobalPoolWithSrcPool(HttpRoute httpRoute,
                                                                     SenderConfiguration senderConfig,
                                                                     BootstrapConfiguration bootstrapConfig,
                                                                     String trgHlrConnPoolId,
                                                                     EventLoopGroup clientEventGroup,
                                                                     Class eventLoopClass,
                                                                     Map<String, GenericObjectPool> srcHlrConnPool) {
        GenericObjectPool trgHlrConnPool = srcHlrConnPool.get(trgHlrConnPoolId);
        if (trgHlrConnPool == null) {
            synchronized (this) {
                if (!globalConnPool.containsKey(httpRoute.toString())) {
                    createTrgHlrPoolInGlobalPool(httpRoute, senderConfig, bootstrapConfig,
                                                 clientEventGroup, eventLoopClass);
                }
                trgHlrConnPool = globalConnPool.get(httpRoute.toString());
                PoolableTargetChannelFactory channelFactory = globalFactoryObjects.get(httpRoute.toString());
                trgHlrConnPool = createPoolForRoutePerSrcHndlr(trgHlrConnPool, channelFactory, clientEventGroup,
                                                               eventLoopClass);
            }
            srcHlrConnPool.put(trgHlrConnPoolId, trgHlrConnPool);
        }
        return trgHlrConnPool;
    }

    private void createTrgHlrPoolInGlobalPool(HttpRoute httpRoute, SenderConfiguration senderConfig,
                                              BootstrapConfiguration bootstrapConfig, EventLoopGroup clientEventGroup,
                                              Class eventLoopClass) {
        PoolableTargetChannelFactory poolableTargetChannelFactory =
                new PoolableTargetChannelFactory(clientEventGroup, eventLoopClass,
                                                 httpRoute, senderConfig, bootstrapConfig, this);
        GenericObjectPool trgHlrConnPool = createPoolForRoute(poolableTargetChannelFactory);
        globalConnPool.put(httpRoute.toString(), trgHlrConnPool);
        globalFactoryObjects.put(httpRoute.toString(), poolableTargetChannelFactory);
    }

    private TargetChannel getTargetChannel(SourceHandler sourceHandler, Http2SourceHandler http2SourceHandler,
                                           GenericObjectPool trgHlrConnPool,
                                           String trgHlrConnPoolId) throws Exception {
        TargetChannel targetChannel = (TargetChannel) trgHlrConnPool.borrowObject();
        if (sourceHandler != null) {
            targetChannel.setCorrelatedSource(sourceHandler);
        } else if (http2SourceHandler != null) {
            targetChannel.setCorrelatedSource(http2SourceHandler);
        } else {
            targetChannel.setCorrelatedSource(null);
        }
        targetChannel.setConnectionManager(this);
        targetChannel.trgHlrConnPoolId = trgHlrConnPoolId;
        return targetChannel;
    }

    public void returnChannel(TargetChannel targetChannel) throws Exception {
        if (targetChannel.getCorrelatedSource() != null) {
            Map<String, GenericObjectPool> objectPoolMap = getTargetPoolMap(targetChannel);
            if (objectPoolMap != null) {
                releaseChannelToPool(targetChannel, objectPoolMap.get(targetChannel.trgHlrConnPoolId));
            }
        } else {
            releaseChannelToPool(targetChannel, globalConnPool.get(targetChannel.getHttpRoute().toString()));
        }
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
        if (targetChannel.getCorrelatedSource() != null) {
            Map<String, GenericObjectPool> objectPoolMap = getTargetPoolMap(targetChannel);
            if (objectPoolMap != null) {
                try {
                    // Need a null check because SourceHandler side could timeout before TargetHandler side.
                    String poolId = targetChannel.trgHlrConnPoolId;
                    if (objectPoolMap.containsKey(poolId)) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Invalidating connection {} to the pool",
                                      targetChannel.getChannel().id().asShortText());
                        }
                        objectPoolMap.get(poolId).invalidateObject(targetChannel);
                    }
                } catch (Exception e) {
                    throw new Exception("Cannot invalidate channel from pool", e);
                }
            }
        }
    }

    private Map<String, GenericObjectPool> getTargetPoolMap(TargetChannel targetChannel) {
        Map<String, GenericObjectPool> objectPoolMap = null;
        ChannelInboundHandlerAdapter correlatedSource = targetChannel.getCorrelatedSource();
        if (correlatedSource instanceof SourceHandler) {
            SourceHandler h1SourceHandler = (SourceHandler) correlatedSource;
            objectPoolMap = h1SourceHandler.getTargetChannelPool();
        } else if (correlatedSource instanceof Http2SourceHandler) {
            Http2SourceHandler h2SourceHandler = (Http2SourceHandler) correlatedSource;
            objectPoolMap = h2SourceHandler.getTargetChannelPool();
        }
        return objectPoolMap;
    }

    public Http2ConnectionManager getHttp2ConnectionManager() {
        return http2ConnectionManager;
    }

    private GenericObjectPool createPoolForRoutePerSrcHndlr(GenericObjectPool genericObjectPool,
                                                            PoolableTargetChannelFactory channelFactory,
                                                            EventLoopGroup clientEventGroup,
                                                            Class eventLoopClass) {
        return new GenericObjectPool(
            new PoolableTargetChannelFactoryPerSrcHndlr(genericObjectPool, channelFactory, clientEventGroup,
                                                        eventLoopClass),
            instantiateAndConfigureConfig());
    }

    private GenericObjectPool createPoolForRoute(PoolableTargetChannelFactory poolableTargetChannelFactory) {
        return new GenericObjectPool(poolableTargetChannelFactory, instantiateAndConfigureConfig());
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
}
