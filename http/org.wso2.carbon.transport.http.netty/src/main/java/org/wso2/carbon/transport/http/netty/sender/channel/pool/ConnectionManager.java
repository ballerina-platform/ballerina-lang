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
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpRequest;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.exceptions.MessagingException;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.common.HttpRoute;
import org.wso2.carbon.transport.http.netty.common.Util;
import org.wso2.carbon.transport.http.netty.config.SenderConfiguration;
import org.wso2.carbon.transport.http.netty.listener.SourceHandler;
import org.wso2.carbon.transport.http.netty.sender.ClientRequestWorker;
import org.wso2.carbon.transport.http.netty.sender.channel.TargetChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A class which handles connection pool management.
 */
public class ConnectionManager {

    private static final Logger log = LoggerFactory.getLogger(ConnectionManager.class);

    private static volatile ConnectionManager connectionManager;

    private PoolConfiguration poolConfiguration;

    private int poolCount;

    private final List<Map<String, GenericObjectPool>> poolList;

    //Connection Pool to be used  when Carbon transport HTTP Listeners are not used.
    private final Map<String, GenericObjectPool> localConnectionMap;

    private PoolManagementPolicy poolManagementPolicy;

    private AtomicInteger index = new AtomicInteger(1);

    private ExecutorService executorService;

    private EventLoopGroup clientEventGroup;

    private ConnectionManager(PoolConfiguration poolConfiguration, Map<String, Object> transportProperties) {
        this.poolConfiguration = poolConfiguration;
        this.poolCount = poolConfiguration.getNumberOfPools();
        this.executorService = Executors.newFixedThreadPool(poolConfiguration.getExecutorServiceThreads());
        localConnectionMap = new ConcurrentHashMap<>();
        if (poolConfiguration.getNumberOfPools() == 0) {
            this.poolManagementPolicy = PoolManagementPolicy.PER_SERVER_CHANNEL_ENDPOINT_CONNECTION_CACHING;
        } else {
            this.poolManagementPolicy = PoolManagementPolicy.GLOBAL_ENDPOINT_CONNECTION_CACHING;
        }

        poolList = new ArrayList<>();
        for (int i = 0; i < poolCount; i++) {
            Map<String, GenericObjectPool> map = new ConcurrentHashMap<>();
            poolList.add(map);
        }

        clientEventGroup = new NioEventLoopGroup(
                Util.getIntProperty(transportProperties, Constants.CLIENT_BOOTSTRAP_WORKER_GROUP_SIZE, 4));
    }

    private GenericObjectPool createPoolForRoute(HttpRoute httpRoute, EventLoopGroup eventLoopGroup,
            Class eventLoopClass, SenderConfiguration senderConfiguration) {
        GenericObjectPool.Config config = new GenericObjectPool.Config();
        config.maxActive = poolConfiguration.getMaxActivePerPool();
        config.maxIdle = poolConfiguration.getMaxIdlePerPool();
        config.minIdle = poolConfiguration.getMinIdlePerPool();
        config.testOnBorrow = poolConfiguration.isTestOnBorrow();
        config.testWhileIdle = poolConfiguration.isTestWhileIdle();
        config.timeBetweenEvictionRunsMillis = poolConfiguration.getTimeBetweenEvictionRuns();
        config.minEvictableIdleTimeMillis = poolConfiguration.getMinEvictableIdleTime();
        config.whenExhaustedAction = poolConfiguration.getExhaustedAction();
        return new GenericObjectPool(
                new PoolableTargetChannelFactory(httpRoute, eventLoopGroup, eventLoopClass, senderConfiguration),
                config);

    }

    public static ConnectionManager getInstance(Map<String, Object> transportProperties) {
        if (connectionManager == null) {
            synchronized (ConnectionManager.class) {
                if (connectionManager == null) {
                    PoolConfiguration poolConfiguration = PoolConfiguration.getInstance();
                    if (poolConfiguration == null) {
                        PoolConfiguration.createPoolConfiguration(transportProperties);
                        poolConfiguration = PoolConfiguration.getInstance();
                    }
                    connectionManager = new ConnectionManager(poolConfiguration, transportProperties);
                }
            }

        }
        return connectionManager;
    }

    /**
     * Provide target channel for given http route.
     *
     * @param httpRoute           BE address
     * @param sourceHandler       Incoming channel
     * @param senderConfiguration netty sender config
     * @param httpRequest         http request
     * @param carbonMessage       carbon message
     * @param carbonCallback      carbon call back
     * @return TargetChannel
     * @throws Exception to notify any errors occur during retrieving the target channel
     */
    public TargetChannel getTargetChannel(HttpRoute httpRoute, SourceHandler sourceHandler,
            SenderConfiguration senderConfiguration, HttpRequest httpRequest, CarbonMessage carbonMessage,
            CarbonCallback carbonCallback) throws Exception {
        TargetChannel targetChannel = null;

        Class cl;
        EventLoopGroup group;

        if (sourceHandler != null) {
            ChannelHandlerContext ctx = sourceHandler.getInboundChannelContext();
            group = ctx.channel().eventLoop();
            cl = ctx.channel().getClass();
        } else {
            cl = NioSocketChannel.class;
            group = clientEventGroup;
            poolManagementPolicy = PoolManagementPolicy.DEFAULT_POOLING;
        }

        // Take connections from Global connection pool
        if (poolManagementPolicy == PoolManagementPolicy.GLOBAL_ENDPOINT_CONNECTION_CACHING) {
            Map<String, GenericObjectPool> objectPoolMap = sourceHandler.getTargetChannelPool();
            GenericObjectPool pool = objectPoolMap.get(httpRoute.toString());
            if (pool == null) {
                pool = createPoolForRoute(httpRoute, group, cl, senderConfiguration);
                objectPoolMap.put(httpRoute.toString(), pool);
            }
            try {
                acquireChannelAndDeliver(httpRoute, sourceHandler, senderConfiguration, httpRequest,
                                         carbonMessage, carbonCallback, PoolManagementPolicy.
                                                 GLOBAL_ENDPOINT_CONNECTION_CACHING, pool, group, cl);
            } catch (Exception e) {
                String msg = "Cannot borrow free channel from pool ";
                log.error(msg, e);
                MessagingException messagingException = new MessagingException(msg, e, 101500);
                carbonMessage.setMessagingException(messagingException);
                carbonCallback.done(carbonMessage);
            }
        } else if (poolManagementPolicy == PoolManagementPolicy.PER_SERVER_CHANNEL_ENDPOINT_CONNECTION_CACHING) {
            // manage connections according to per inbound channel caching method
            if (!sourceHandler.isChannelFutureExists(httpRoute)) {
                acquireChannelAndDeliver(httpRoute, sourceHandler, senderConfiguration, httpRequest,
                                         carbonMessage, carbonCallback,
                                         PoolManagementPolicy.PER_SERVER_CHANNEL_ENDPOINT_CONNECTION_CACHING,
                                         null, group, cl);
            } else {
                synchronized (sourceHandler) {
                    if (sourceHandler.isChannelFutureExists(httpRoute)) {
                        targetChannel = sourceHandler.getChannelFuture(httpRoute);
                        Channel channel = targetChannel.getChannel();
                        if (!channel.isActive()) {
                            targetChannel = null;
                            acquireChannelAndDeliver(httpRoute, sourceHandler, senderConfiguration, httpRequest,
                                                     carbonMessage, carbonCallback,
                                                     PoolManagementPolicy.
                                                             PER_SERVER_CHANNEL_ENDPOINT_CONNECTION_CACHING,
                                                     null, group, cl);
                        }
                    } else {
                        acquireChannelAndDeliver(httpRoute, sourceHandler, senderConfiguration, httpRequest,
                                                 carbonMessage, carbonCallback,
                                                 PoolManagementPolicy.
                                                         PER_SERVER_CHANNEL_ENDPOINT_CONNECTION_CACHING,
                                                 null, group, cl);
                    }
                }
            }
        } else if (poolManagementPolicy == PoolManagementPolicy.DEFAULT_POOLING) {
            GenericObjectPool pool = localConnectionMap.get(httpRoute.toString());
            if (pool == null) {
                pool = createPoolForRoute(httpRoute, group, cl, senderConfiguration);
                localConnectionMap.put(httpRoute.toString(), pool);
            }
            acquireChannelAndDeliver(httpRoute, sourceHandler, senderConfiguration, httpRequest,
                                     carbonMessage, carbonCallback, PoolManagementPolicy.
                                             DEFAULT_POOLING, pool, group, cl);
        }

        if (targetChannel != null) {
            targetChannel.setHttpRoute(httpRoute);
            if (sourceHandler != null) {
                targetChannel.setCorrelatedSource(sourceHandler);
            }
        }
        return targetChannel;
    }

    private void acquireChannelAndDeliver(HttpRoute httpRoute, SourceHandler sourceHandler,
                                          SenderConfiguration senderConfig,
                                          HttpRequest httpRequest, CarbonMessage carbonMessage,
                                          CarbonCallback carbonCallback,
                                          PoolManagementPolicy poolManagementPolicy,
                                          GenericObjectPool genericObjectPool,
                                          EventLoopGroup eventLoopGroup,
                                          Class aClass) {
        executorService.execute(
                new ClientRequestWorker(httpRoute, sourceHandler, senderConfig, httpRequest,
                                        carbonMessage, carbonCallback,
                                        poolManagementPolicy,
                                        genericObjectPool, this, eventLoopGroup, aClass));
    }

    //Add connection to Pool back
    public void returnChannel(TargetChannel targetChannel) throws Exception {
        if (poolManagementPolicy == PoolManagementPolicy.PER_SERVER_CHANNEL_ENDPOINT_CONNECTION_CACHING) {
            SourceHandler sourceHandler = targetChannel.getCorrelatedSource();
            sourceHandler.addTargetChannel(targetChannel.getHttpRoute(), targetChannel);
        } else if (poolManagementPolicy == PoolManagementPolicy.GLOBAL_ENDPOINT_CONNECTION_CACHING) {
            Map<String, GenericObjectPool> objectPoolMap = targetChannel.getCorrelatedSource().getTargetChannelPool();
            releaseChannelToPool(targetChannel, objectPoolMap.get(targetChannel.getHttpRoute().toString()));
        } else if (poolManagementPolicy == PoolManagementPolicy.DEFAULT_POOLING) {
            GenericObjectPool pool = localConnectionMap.get(targetChannel.getHttpRoute().toString());
            releaseChannelToPool(targetChannel, pool);
        }
    }

    private void releaseChannelToPool(TargetChannel targetChannel, GenericObjectPool pool) throws Exception {
        try {
            if (targetChannel.getChannel().isActive()) {
                pool.returnObject(targetChannel);
            }
        } catch (Exception e) {
            throw new Exception("Cannot return channel to pool", e);
        }
    }

    /**
     * Provide specific target channel map.
     *
     * @return Map contains pools for each route
     */
    public Map<String, GenericObjectPool> getTargetChannelPool() {
        if (poolManagementPolicy == PoolManagementPolicy.GLOBAL_ENDPOINT_CONNECTION_CACHING) {
            int ind = index.getAndIncrement() % poolCount;
            return poolList.get(ind);
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
        GLOBAL_ENDPOINT_CONNECTION_CACHING,
        DEFAULT_POOLING
    }

}
