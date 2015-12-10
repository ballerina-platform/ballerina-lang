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


import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.transport.http.netty.common.HttpRoute;
import org.wso2.carbon.transport.http.netty.listener.SourceHandler;
import org.wso2.carbon.transport.http.netty.sender.channel.ChannelUtils;
import org.wso2.carbon.transport.http.netty.sender.channel.TargetChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A class which handles connection pool management.
 */
public class ConnectionManager {

    private static final Logger log = LoggerFactory.getLogger(ConnectionManager.class);

    private static volatile ConnectionManager connectionManager;

    private PoolConfiguration poolConfiguration;

    private int poolCount;

    private final List<Map<String, GenericObjectPool>> mapList;

    private PoolManagementPolicy poolManagementPolicy;

    private AtomicInteger index = new AtomicInteger(1);


    private ConnectionManager(PoolConfiguration poolConfiguration) {
        this.poolConfiguration = poolConfiguration;
        this.poolCount = poolConfiguration.getNumberOfPools();
        if (poolConfiguration.getNumberOfPools() == 0) {
            this.poolManagementPolicy = PoolManagementPolicy.PER_SERVER_CHANNEL_ENDPOINT_CONNECTION_CACHING;
        } else {
            this.poolManagementPolicy = PoolManagementPolicy.GLOBAL_ENDPOINT_CONNECTION_CACHING;
        }

        mapList = new ArrayList<>();
        for (int i = 0; i < poolCount; i++) {
            Map<String, GenericObjectPool> map = new ConcurrentHashMap<>();
            mapList.add(map);
        }

    }


    private GenericObjectPool createPoolForRoute(HttpRoute httpRoute, EventLoopGroup eventLoopGroup,
                                                 Class eventLoopClass) {
        GenericObjectPool.Config config = new GenericObjectPool.Config();
        config.maxActive = poolConfiguration.getMaxActivePerPool();
        config.maxIdle = poolConfiguration.getMaxIdlePerPool();
        config.minIdle = poolConfiguration.getMinIdlePerPool();
        config.testOnBorrow = poolConfiguration.isTestOnBorrow();
        config.testWhileIdle = poolConfiguration.isTestWhileIdle();
        config.timeBetweenEvictionRunsMillis = poolConfiguration.getTimeBetweenEvictionRuns();
        config.minEvictableIdleTimeMillis = poolConfiguration.getMinEvictableIdleTime();
        config.whenExhaustedAction = poolConfiguration.getExhaustedAction();
        return new GenericObjectPool(new PoolableTargetChannelFactory(httpRoute, eventLoopGroup, eventLoopClass),
                                     config);


    }


    public static ConnectionManager getInstance() {
        if (connectionManager == null) {
            synchronized (ConnectionManager.class) {
                if (connectionManager == null) {
                    PoolConfiguration poolConfiguration = PoolConfiguration.getInstance();
                    connectionManager = new ConnectionManager(poolConfiguration);
                }
            }

        }
        return connectionManager;
    }

    /**
     * Provide target channel for given http route.
     *
     * @param httpRoute     BE address
     * @param sourceHandler Incoming channel
     * @return TargetChannel
     * @throws Exception   Exception to notify any errors occur during retrieving the target channel
     */
    public TargetChannel getTargetChannel(HttpRoute httpRoute, SourceHandler sourceHandler)
               throws Exception {
        Channel channel = null;
        TargetChannel targetChannel = null;
        ChannelHandlerContext ctx = sourceHandler.getInboundChannelContext();
        EventLoopGroup group = ctx.channel().eventLoop();
        Class cl = ctx.channel().getClass();

        // Take connections from Global connection pool
        if (poolManagementPolicy == PoolManagementPolicy.GLOBAL_ENDPOINT_CONNECTION_CACHING) {
            Map<String, GenericObjectPool> objectPoolMap = sourceHandler.getTargetChannelPool();
            GenericObjectPool pool = objectPoolMap.get(httpRoute.toString());
            if (pool == null) {
                pool = createPoolForRoute(httpRoute, group, cl);
                objectPoolMap.put(httpRoute.toString(), pool);
            }
            try {
                Object obj = pool.borrowObject();
                if (obj != null) {
                    targetChannel = (TargetChannel) obj;
                    targetChannel.setTargetHandler(targetChannel.getTargetInitializer().getTargetHandler());
                }
            } catch (Exception e) {
                log.error("Cannot borrow free channel from pool ", e);
            }
        } else {
            // manage connections according to per inbound channel caching method
            if (!isRouteExists(httpRoute, sourceHandler)) {
                targetChannel = new TargetChannel();
                ChannelFuture future = ChannelUtils.getNewChannelFuture(targetChannel, group, cl, httpRoute);

                try {
                    channel = ChannelUtils.openChannel(future, httpRoute);
                } catch (Exception failedCause) {
                    throw failedCause;
                } finally {
                    if (channel != null) {
                        targetChannel.setChannel(channel);
                        targetChannel.setTargetHandler(targetChannel.getTargetInitializer().getTargetHandler());
                        sourceHandler.addTargetChannel(httpRoute, targetChannel);
                    }
                }
            } else {
                targetChannel = sourceHandler.getChannel(httpRoute);
                Channel tempc = targetChannel.getChannel();
                if (tempc.isActive()) {
                    channel = tempc;
                } else {
                    ChannelFuture future = ChannelUtils.getNewChannelFuture(targetChannel, group, cl, httpRoute);
                    channel = ChannelUtils.openChannel(future, httpRoute);
                    targetChannel.setChannel(channel);
                }
            }
        }
        if (targetChannel != null) {
            targetChannel.setHttpRoute(httpRoute);
            targetChannel.setCorrelatedSource(sourceHandler);
        }
        return targetChannel;
    }


    //Add connection to Pool back
    public void returnChannel(TargetChannel targetChannel) {
        if (poolManagementPolicy == PoolManagementPolicy.GLOBAL_ENDPOINT_CONNECTION_CACHING) {
            Map<String, GenericObjectPool> objectPoolMap = targetChannel.getCorrelatedSource().getTargetChannelPool();
            GenericObjectPool pool = objectPoolMap.get(targetChannel.getHttpRoute().toString());
            try {
                if (targetChannel.getChannel().isActive()) {
                    pool.returnObject(targetChannel);
                }
            } catch (Exception e) {
                log.error("Cannot return channel to pool", e);
            }

        }
    }


    private boolean isRouteExists(HttpRoute httpRoute, SourceHandler srcHandler) {
        return srcHandler.getChannel(httpRoute) != null;
    }


    /**
     * Provide specific target channel map.
     *
     * @return Map contains pools for each route
     */
    public Map<String, GenericObjectPool> getTargetChannelPool() {
        if (poolManagementPolicy == PoolManagementPolicy.GLOBAL_ENDPOINT_CONNECTION_CACHING) {
            int ind = index.getAndIncrement() % poolCount;
                return mapList.get(ind);
        }
        return null;
    }


    public void notifyChannelInactive() {
        if (poolManagementPolicy == PoolManagementPolicy.GLOBAL_ENDPOINT_CONNECTION_CACHING) {
            index.getAndDecrement();
        }
    }


    /**
     * Connection pool management policies for  target channels.
     */
    public enum PoolManagementPolicy {
        PER_SERVER_CHANNEL_ENDPOINT_CONNECTION_CACHING,
        GLOBAL_ENDPOINT_CONNECTION_CACHING
    }

}
