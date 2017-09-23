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
import org.apache.commons.pool.impl.GenericObjectPool;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.common.HttpRoute;
import org.wso2.carbon.transport.http.netty.common.Util;
import org.wso2.carbon.transport.http.netty.common.ssl.SSLConfig;
import org.wso2.carbon.transport.http.netty.listener.SourceHandler;
import org.wso2.carbon.transport.http.netty.sender.channel.TargetChannel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A class which handles connection pool management.
 */
public class ConnectionManager {

    private EventLoopGroup clientEventGroup;
    private PoolConfiguration poolConfiguration;
    private PoolManagementPolicy poolManagementPolicy;
    private final Map<String, GenericObjectPool> connGlobalPool;
    private EventLoopGroup targetEventLoopGroup;
    private static volatile ConnectionManager connectionManager;

    private ConnectionManager(PoolConfiguration poolConfiguration, Map<String, Object> transportProperties) {
        this.poolConfiguration = poolConfiguration;
        if (poolConfiguration.getNumberOfPools() == 1) {
            this.poolManagementPolicy = PoolManagementPolicy.LOCK_DEFAULT_POOLING;
        }
        connGlobalPool = new ConcurrentHashMap<>();
        clientEventGroup = new NioEventLoopGroup(
                Util.getIntProperty(transportProperties, Constants.CLIENT_BOOTSTRAP_WORKER_GROUP_SIZE, 4));
        targetEventLoopGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2);
    }

    private GenericObjectPool createPoolForRoute(PoolableTargetChannelFactory poolableTargetChannelFactory) {
        return new GenericObjectPool(poolableTargetChannelFactory, instantiateAndConfigureConfig());
    }

    private GenericObjectPool createPoolForRoutePerSrcHndlr(GenericObjectPool genericObjectPool) {
        return new GenericObjectPool(new PoolableTargetChannelFactoryPerSrcHndlr(genericObjectPool),
                instantiateAndConfigureConfig());
    }

    public static ConnectionManager getInstance() {
        return connectionManager;
    }

    public static void init(Map<String, Object> transportProperties) {
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
    }

    /**
     *
     * @param httpRoute           BE address
     * @param sourceHandler       Incoming channel
     * @param sslConfig           netty sender config
     * @param httpTraceLogEnabled Indicates whether HTTP trace logs are enabled
     * @return the target channel which is requested for given parameters.
     * @throws Exception    to notify any errors occur during retrieving the target channel
     */
    public TargetChannel borrowTargetChannel(HttpRoute httpRoute, SourceHandler sourceHandler, SSLConfig sslConfig,
                                             boolean httpTraceLogEnabled)
            throws Exception {
        GenericObjectPool trgHlrConnPool;

        if (sourceHandler != null) {
            EventLoopGroup group;
            ChannelHandlerContext ctx = sourceHandler.getInboundChannelContext();
            // TODO: use the same event loop group once pass-through service detection is complete.
            group = ctx.channel().eventLoop();
//            group = targetEventLoopGroup;
            Class cl = ctx.channel().getClass();

            if (poolManagementPolicy == PoolManagementPolicy.LOCK_DEFAULT_POOLING) {
                // This is faster than the above one (about 2k difference)
                Map<String, GenericObjectPool> srcHlrConnPool = sourceHandler.getTargetChannelPool();
                trgHlrConnPool = srcHlrConnPool.get(httpRoute.toString());
                if (trgHlrConnPool == null) {
                    PoolableTargetChannelFactory poolableTargetChannelFactory =
                            new PoolableTargetChannelFactory(httpRoute, group, cl, sslConfig, httpTraceLogEnabled);
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
                                    new PoolableTargetChannelFactory(
                                            httpRoute, group, cl, sslConfig, httpTraceLogEnabled);
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
                            new PoolableTargetChannelFactory(httpRoute, group, cl, sslConfig, httpTraceLogEnabled);
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

    //Add connection to Pool back
    public void returnChannel(TargetChannel targetChannel) throws Exception {
        targetChannel.setRequestWritten(false);
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
        targetChannel.setRequestWritten(false);
        Map<String, GenericObjectPool> objectPoolMap = targetChannel.getCorrelatedSource().getTargetChannelPool();
        try {
            // Need a null check because SourceHandler side could timeout before TargetHandler side.
            if (objectPoolMap.get(targetChannel.getHttpRoute().toString()) != null) {
                objectPoolMap.get(targetChannel.getHttpRoute().toString()).invalidateObject(targetChannel);
            }
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
        config.maxWait = poolConfiguration.getMaxWait();

        return config;
    }

}
