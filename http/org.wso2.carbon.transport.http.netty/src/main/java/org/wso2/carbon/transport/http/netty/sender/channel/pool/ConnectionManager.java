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
import org.wso2.carbon.transport.http.netty.sender.TargetHandler;
import org.wso2.carbon.transport.http.netty.sender.channel.ChannelUtils;
import org.wso2.carbon.transport.http.netty.sender.channel.TargetChannel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A class which handles connection pool management.
 */
public class ConnectionManager {

    private static final Logger log = LoggerFactory.getLogger(ConnectionManager.class);

    private EventLoopGroup clientEventGroup;
    private PoolConfiguration poolConfiguration;
    private PoolManagementPolicy poolManagementPolicy;
    private final Map<String, GenericObjectPool> connGlobalPool;
    private AtomicInteger index = new AtomicInteger(1);
    private static volatile ConnectionManager connectionManager;

    private ConnectionManager(PoolConfiguration poolConfiguration, Map<String, Object> transportProperties) {
        this.poolConfiguration = poolConfiguration;
        if (poolConfiguration.getNumberOfPools() == 1) {
            this.poolManagementPolicy = PoolManagementPolicy.LOCK_DEFAULT_POOLING;
        }
        connGlobalPool = new ConcurrentHashMap<>();
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
        config.maxWait = poolConfiguration.getMaxWait();
        return new GenericObjectPool(
                new PoolableTargetChannelFactory(httpRoute, eventLoopGroup, eventLoopClass, senderConfiguration),
                config);

    }

    private GenericObjectPool createPoolForRoutePerSrcHndlr(GenericObjectPool genericObjectPool) {
        GenericObjectPool.Config config = new GenericObjectPool.Config();
        config.maxActive = poolConfiguration.getMaxActivePerPool();
        config.maxIdle = poolConfiguration.getMaxIdlePerPool();
        config.minIdle = poolConfiguration.getMinIdlePerPool();
        config.testOnBorrow = poolConfiguration.isTestOnBorrow();
        config.testWhileIdle = poolConfiguration.isTestWhileIdle();
        config.timeBetweenEvictionRunsMillis = poolConfiguration.getTimeBetweenEvictionRuns();
        config.minEvictableIdleTimeMillis = poolConfiguration.getMinEvictableIdleTime();
        config.whenExhaustedAction = poolConfiguration.getExhaustedAction();
        return new GenericObjectPool(new PoolableTargetChannelFactoryPerSrcHndlr(genericObjectPool), config);
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

        // Take connections from Global connection pool
        try {
            GenericObjectPool trgHlrConnPool;
            TargetChannel targetChannel;

            if (sourceHandler != null) {
                EventLoopGroup group;
                ChannelHandlerContext ctx = sourceHandler.getInboundChannelContext();
                group = ctx.channel().eventLoop();
                Class cl = ctx.channel().getClass();

                if (poolManagementPolicy == PoolManagementPolicy.LOCK_DEFAULT_POOLING) {
                    // This is faster than the above one (about 2k difference)
                    Map<String, GenericObjectPool> srcHlrConnPool = sourceHandler.getTargetChannelPool();
                    trgHlrConnPool = srcHlrConnPool.get(httpRoute.toString());
                    if (trgHlrConnPool == null) {
                        trgHlrConnPool = createPoolForRoute(httpRoute, group, cl, senderConfiguration);
                        srcHlrConnPool.put(httpRoute.toString(), trgHlrConnPool);
                    }
                } else {
                    Map<String, GenericObjectPool> srcHlrConnPool = sourceHandler.getTargetChannelPool();
                    trgHlrConnPool = srcHlrConnPool.get(httpRoute.toString());
                    if (trgHlrConnPool == null) {
                        synchronized (this) {
                            if (!this.connGlobalPool.containsKey(httpRoute.toString())) {
                                trgHlrConnPool = createPoolForRoute(httpRoute, group, cl, senderConfiguration);
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
                        trgHlrConnPool = createPoolForRoute(httpRoute, group, cl, senderConfiguration);
                        this.connGlobalPool.put(httpRoute.toString(), trgHlrConnPool);
                    }
                    trgHlrConnPool = this.connGlobalPool.get(httpRoute.toString());
                }
            }

            targetChannel = (TargetChannel) trgHlrConnPool.borrowObject();

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

    // TODO: Additional thread pool is not needed. Finalize it and remove it.
//    private void acquireChannelAndDeliver(HttpRoute httpRoute, SourceHandler sourceHandler,
//                                          SenderConfiguration senderConfig,
//                                          HttpRequest httpRequest, CarbonMessage carbonMessage,
//                                          CarbonCallback carbonCallback,
//                                          PoolManagementPolicy poolManagementPolicy,
//                                          GenericObjectPool genericObjectPool,
//                                          EventLoopGroup eventLoopGroup,
//                                          Class aClass) {
//        executorService.execute(
//                new ClientRequestWorker(httpRoute, sourceHandler, senderConfig, httpRequest,
//                                        carbonMessage, carbonCallback,
//                                        poolManagementPolicy,
//                                        genericObjectPool, this, eventLoopGroup, aClass));
//    }

    //Add connection to Pool back
    public void returnChannel(TargetChannel targetChannel) throws Exception {
        Map<String, GenericObjectPool> objectPoolMap = targetChannel.getCorrelatedSource().getTargetChannelPool();
        releaseChannelToPool(targetChannel, objectPoolMap.get(targetChannel.getHttpRoute().toString()));
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

    public void invalidateTargetChannel(TargetChannel targetChannel) throws Exception {
        Map<String, GenericObjectPool> objectPoolMap = targetChannel.getCorrelatedSource().getTargetChannelPool();
        try {
            objectPoolMap.get(targetChannel.getHttpRoute().toString()).invalidateObject(targetChannel);
        } catch (Exception e) {
            throw new Exception("Cannot invalidate channel from pool", e);
        }
    }

    /**
     * Provide specific target channel map.
     *
     * @return Map contains pools for each route
     */
    public Map<String, GenericObjectPool> getTargetChannelPool() {
        return this.connGlobalPool;
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
        GLOBAL_ENDPOINT_CONNECTION_CACHING,
        LOCK_DEFAULT_POOLING,
    }

}
