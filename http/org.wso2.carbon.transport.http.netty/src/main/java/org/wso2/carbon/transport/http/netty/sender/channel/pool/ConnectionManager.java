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
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.timeout.IdleStateHandler;
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
import org.wso2.carbon.transport.http.netty.sender.TargetHandler;
import org.wso2.carbon.transport.http.netty.sender.channel.ChannelUtils;
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
    private PoolManagementPolicy poolManagementPolicy;
    private final List<Map<String, GenericObjectPool>> poolList;
    private AtomicInteger index = new AtomicInteger(1);
    private int poolCount;

    private ExecutorService executorService;
    private EventLoopGroup clientEventGroup;

    private ConnectionManager(PoolConfiguration poolConfiguration, Map<String, Object> transportProperties) {
        this.poolConfiguration = poolConfiguration;
        this.poolCount = poolConfiguration.getNumberOfPools();
        this.executorService = Executors.newFixedThreadPool(poolConfiguration.getExecutorServiceThreads());
        if (poolConfiguration.getNumberOfPools() == 0) {
            this.poolManagementPolicy = PoolManagementPolicy.LOCK_DEFAULT_POOLING;
        } else if (poolConfiguration.getNumberOfPools() == 1) {
            this.poolManagementPolicy = PoolManagementPolicy.GLOBAL_ENDPOINT_CONNECTION_CACHING;
        } else if (poolConfiguration.getNumberOfPools() == 2) {
            this.poolManagementPolicy = PoolManagementPolicy.LOCKLESS_DEFAULT_POOLING;
        }
//        else if (poolConfiguration.getNumberOfPools() == 2) {
//            this.poolManagementPolicy = PoolManagementPolicy.PER_SERVER_CHANNEL_ENDPOINT_CONNECTION_CACHING;
//        }

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
     * @throws Exception to notify any errors occur during retrieving the target channel
     */
    public void executeTargetChannel(HttpRoute httpRoute, SourceHandler sourceHandler,
            SenderConfiguration senderConfiguration, HttpRequest httpRequest, CarbonMessage carbonMessage,
            CarbonCallback carbonCallback) throws Exception {

        Class cl;
        EventLoopGroup group;
        if (sourceHandler != null) {
            ChannelHandlerContext ctx = sourceHandler.getInboundChannelContext();
            group = ctx.channel().eventLoop();
            cl = ctx.channel().getClass();
        } else {
            cl = NioSocketChannel.class;
            group = clientEventGroup;
            poolManagementPolicy = PoolManagementPolicy.LOCK_DEFAULT_POOLING;
        }

        // Take connections from Global connection pool
        if (poolManagementPolicy == PoolManagementPolicy.GLOBAL_ENDPOINT_CONNECTION_CACHING) {
            Map<String, GenericObjectPool> objectPoolMap = sourceHandler.getTargetChannelPool();
            synchronized (this) {
                GenericObjectPool pool = objectPoolMap.get(httpRoute.toString());
                if (pool == null) {
                    pool = createPoolForRoute(httpRoute, group, cl, senderConfiguration);
                    objectPoolMap.put(httpRoute.toString(), pool);
                }
            }
            try {
                acquireChannelAndDeliver(httpRoute, sourceHandler, senderConfiguration, httpRequest,
                                                 carbonMessage, carbonCallback, PoolManagementPolicy.
                                                 GLOBAL_ENDPOINT_CONNECTION_CACHING,
                                                 objectPoolMap.get(httpRoute.toString()), group, cl);
            } catch (Exception e) {
                String msg = "Cannot borrow free channel from pool";
                log.error(msg, e);
                MessagingException messagingException = new MessagingException(msg, e, 101500);
                carbonMessage.setMessagingException(messagingException);
                carbonCallback.done(carbonMessage);
            }
        } else if (poolManagementPolicy == PoolManagementPolicy.LOCK_DEFAULT_POOLING) {
            try {
                TargetChannel targetChannel;
                Map<String, GenericObjectPool> connPool = sourceHandler.getTargetChannelPool();
                GenericObjectPool genPool = connPool.get(httpRoute.toString());
                if (genPool == null) {
                    genPool = createPoolForRoute(httpRoute, group, cl, senderConfiguration);
                    connPool.put(httpRoute.toString(), genPool);
                }
                targetChannel = (TargetChannel) genPool.borrowObject();

                if (targetChannel.getChannel() != null) {
                    targetChannel.setTargetHandler(targetChannel.getHTTPClientInitializer().getTargetHandler());
                    targetChannel.setCorrelatedSource(sourceHandler);
                    targetChannel.setHttpRoute(httpRoute);
                    TargetHandler targetHandler = targetChannel.getTargetHandler();
                    targetHandler.setCallback(carbonCallback);
                    targetHandler.setIncomingMsg(carbonMessage);
                    targetHandler.setConnectionManager(connectionManager);
                    targetHandler.setTargetChannel(targetChannel);
                    if (ChannelUtils.writeContent(targetChannel.getChannel(), httpRequest, carbonMessage)) {
                        targetChannel.setRequestWritten(true); // If request written
                    }
                }
            } catch (Exception e) {
                String msg = "Failed to send the request through the default pooling";
                log.error(msg, e);
                MessagingException messagingException = new MessagingException(msg, e, 101500);
                carbonMessage.setMessagingException(messagingException);
                carbonCallback.done(carbonMessage);
            }
        } else if (poolManagementPolicy == PoolManagementPolicy.LOCKLESS_DEFAULT_POOLING) {
            // this is the lock less impl of LOCK_DEFAULT_POOLING. 90% of the
            // time this should work unless user decide to weird integrations.
            // just kept it here in case we need extreme level of per numbers.
            try {
                Map<String, TargetChannel> connPool = sourceHandler.getTargetChannelPerHostPool();
                TargetChannel targetChannel = connPool.remove(httpRoute.toString());
                if (targetChannel == null) {
                    targetChannel = new TargetChannel();
                    ChannelFuture channelFuture = ChannelUtils.getNewChannelFuture(targetChannel,
                            group, cl, httpRoute, senderConfiguration);
                    Channel channel = ChannelUtils.openChannel(channelFuture, httpRoute);
                    int socketIdleTimeout = senderConfiguration.getSocketIdleTimeout(60);
                    channel.pipeline().addLast("idleStateHandler",
                            new IdleStateHandler(socketIdleTimeout, socketIdleTimeout, socketIdleTimeout));
                    log.debug("Created channel: {}", channel);
                    targetChannel.setChannel(channel);
                }

                if (targetChannel.getChannel() != null) {
                    targetChannel.setTargetHandler(targetChannel.getHTTPClientInitializer().getTargetHandler());
                    targetChannel.setCorrelatedSource(sourceHandler);
                    targetChannel.setHttpRoute(httpRoute);
                    TargetHandler targetHandler = targetChannel.getTargetHandler();
                    targetHandler.setCallback(carbonCallback);
                    targetHandler.setIncomingMsg(carbonMessage);
                    targetHandler.setConnectionManager(connectionManager);
                    targetHandler.setTargetChannel(targetChannel);
                    if (ChannelUtils.writeContent(targetChannel.getChannel(), httpRequest, carbonMessage)) {
                        targetChannel.setRequestWritten(true); // If request written
                    }
                }
            } catch (Exception e) {
                String msg = "Failed to send the request through the default pooling";
                log.error(msg, e);
                MessagingException messagingException = new MessagingException(msg, e, 101500);
                carbonMessage.setMessagingException(messagingException);
                carbonCallback.done(carbonMessage);
            }
        }

//        TODO: Talk to others and remove it!!!
//        else if (poolManagementPolicy == PoolManagementPolicy.PER_SERVER_CHANNEL_ENDPOINT_CONNECTION_CACHING) {
            // manage connections according to per inbound channel caching method
//            if (!sourceHandler.isChannelFutureExists(httpRoute)) {
//                acquireChannelAndDeliver(httpRoute, sourceHandler, senderConfiguration, httpRequest,
//                                         carbonMessage, carbonCallback,
//                                         PoolManagementPolicy.PER_SERVER_CHANNEL_ENDPOINT_CONNECTION_CACHING,
//                                         null, group, cl);
//            } else {
//                synchronized (sourceHandler) {
//                    if (sourceHandler.isChannelFutureExists(httpRoute)) {
//                        targetChannel = sourceHandler.removeChannelFuture(httpRoute);
//                        Channel channel = targetChannel.getChannel();
//                        if (!channel.isActive()) {
//                            acquireChannelAndDeliver(httpRoute, sourceHandler, senderConfiguration, httpRequest,
//                                                     carbonMessage, carbonCallback,
//                                                     PoolManagementPolicy.
//                                                             PER_SERVER_CHANNEL_ENDPOINT_CONNECTION_CACHING,
//                                                     null, group, cl);
//                        }
//                    } else {
//                        acquireChannelAndDeliver(httpRoute, sourceHandler, senderConfiguration, httpRequest,
//                                                 carbonMessage, carbonCallback,
//                                                 PoolManagementPolicy.
//                                                         PER_SERVER_CHANNEL_ENDPOINT_CONNECTION_CACHING,
//                                                 null, group, cl);
//                    }
//                }
//            }
//        else if (poolManagementPolicy == PoolManagementPolicy.DEFAULT_POOLING) {
//            GenericObjectPool pool = localConnectionMap.get(httpRoute.toString());
//            if (pool == null) {
//                pool = createPoolForRoute(httpRoute, group, cl, senderConfiguration);
//                localConnectionMap.put(httpRoute.toString(), pool);
//            }
//            acquireChannelAndDeliver(httpRoute, sourceHandler, senderConfiguration, httpRequest,
//                                     carbonMessage, carbonCallback, PoolManagementPolicy.
//                                             DEFAULT_POOLING, pool, group, cl);
//        }
//        if (targetChannel != null) {
//            targetChannel.setHttpRoute(httpRoute);
//            targetChannel.setCorrelatedSource(sourceHandler);
//        }

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
        if ((poolManagementPolicy == PoolManagementPolicy.GLOBAL_ENDPOINT_CONNECTION_CACHING)
                || (poolManagementPolicy == PoolManagementPolicy.LOCK_DEFAULT_POOLING)) {
            Map<String, GenericObjectPool> objectPoolMap = targetChannel.getCorrelatedSource().getTargetChannelPool();
            releaseChannelToPool(targetChannel, objectPoolMap.get(targetChannel.getHttpRoute().toString()));
        } else if (poolManagementPolicy == PoolManagementPolicy.LOCKLESS_DEFAULT_POOLING) {
            Map<String, TargetChannel> objectPoolMap = targetChannel.getCorrelatedSource()
                    .getTargetChannelPerHostPool();
            objectPoolMap.put(targetChannel.getHttpRoute().toString(), targetChannel);
        }
//        else if (poolManagementPolicy == PoolManagementPolicy.PER_SERVER_CHANNEL_ENDPOINT_CONNECTION_CACHING) {
//            SourceHandler sourceHandler = targetChannel.getCorrelatedSource();
//            sourceHandler.addTargetChannel(targetChannel.getHttpRoute(), targetChannel);
//        }
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

    public PoolConfiguration getPoolConfiguration() {
        return poolConfiguration;
    }

    /**
     * Connection pool management policies for  target channels.
     */
    public enum PoolManagementPolicy {
        LOCK_DEFAULT_POOLING,
        GLOBAL_ENDPOINT_CONNECTION_CACHING,
        LOCKLESS_DEFAULT_POOLING
//        PER_SERVER_CHANNEL_ENDPOINT_CONNECTION_CACHING
    }

}
