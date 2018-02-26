/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.transport.http.netty.sender.http2;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.HttpRoute;
import org.wso2.transport.http.netty.config.SenderConfiguration;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@code Http2ConnectionManager} Manages HTTP/2 connections
 */
public class ConnectionManager {

    private static final Logger log = LoggerFactory.getLogger(ConnectionManager.class);

    // Per route connection pools
    private static ConcurrentHashMap<String, PerRouteConnectionPool> connectionPools = new ConcurrentHashMap<>();
    // Lock for synchronizing access
    private Lock lock = new ReentrantLock();
    private SenderConfiguration senderConfig;

    public ConnectionManager(SenderConfiguration senderConfig) {
        this.senderConfig = senderConfig;
    }

    /**
     * Borrow a {@code TargetChannel} for a given http route
     *
     * @param httpRoute    http route
     * @return TargetChannel
     */
    public TargetChannel borrowChannel(HttpRoute httpRoute) {

        String key = generateKey(httpRoute);
        PerRouteConnectionPool perRouteConnectionPool = fetchConnectionPool(key);
        TargetChannel targetChannel = null;
        if (perRouteConnectionPool != null) {
            targetChannel = perRouteConnectionPool.fetchTargetChannel();
        }

        if (targetChannel == null) {
            // double locking is to prevent 2 connections get created for same host:port pair under concurrency
            lock.lock();
            try {
                perRouteConnectionPool = fetchConnectionPool(key);
                if (perRouteConnectionPool != null) {
                    targetChannel = perRouteConnectionPool.fetchTargetChannel();
                }

                if (targetChannel == null) {
                    targetChannel = createNewConnection(httpRoute);
                    if (perRouteConnectionPool == null) {
                        perRouteConnectionPool =
                                new PerRouteConnectionPool(targetChannel, senderConfig.getHttp2MaxActiveStreams());
                        registerConnectionPool(key, perRouteConnectionPool);
                    } else {
                        perRouteConnectionPool.addChannel(targetChannel);
                    }
                }
            } finally {
                lock.unlock();
            }
        }
        return targetChannel;
    }

    private TargetChannel createNewConnection(HttpRoute httpRoute) {

        ClientInitializer initializer = new ClientInitializer(senderConfig);

        // Bootstrapping
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        Bootstrap clientBootstrap = new Bootstrap();
        clientBootstrap.group(workerGroup);
        clientBootstrap.channel(NioSocketChannel.class);
        clientBootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        clientBootstrap.remoteAddress(httpRoute.getHost(), httpRoute.getPort());
        clientBootstrap.handler(initializer);

        // Start the client
        ChannelFuture channelFuture = clientBootstrap.connect();
        log.debug("Created channel: {}", httpRoute);

        // Create data holders which stores connection information
        ClientOutboundHandler clientHandler = initializer.getClientOutboundHandler();
        TargetChannel targetChannel =
                new TargetChannel(this, clientHandler.getConnection(), httpRoute, channelFuture);
        initializer.setTargetChannel(targetChannel);
        String key = generateKey(httpRoute);

        // Configure a listener to remove connection from pool when it is closed
        channelFuture.channel().closeFuture().
                addListener(future -> {
                                PerRouteConnectionPool perRouteConnectionPool = connectionPools.get(key);
                                if (perRouteConnectionPool != null) {
                                    perRouteConnectionPool.removeChannel(targetChannel);
                                }
                            }
                );
        return targetChannel;
    }

    private PerRouteConnectionPool fetchConnectionPool(String key) {
        return connectionPools.get(key);
    }

    private void registerConnectionPool(String key, PerRouteConnectionPool perRouteConnectionPool) {
        connectionPools.put(key, perRouteConnectionPool);
    }

    private String generateKey(HttpRoute httpRoute) {
        return httpRoute.getHost() + ":" + httpRoute.getPort();
    }

    /**
     * Return the previously exhausted {@code TargetChannel} back to the pool after
     *
     * @param httpRoute     http route
     * @param targetChannel previously exhausted TargetChannel
     */
    public void returnTargetChannel(HttpRoute httpRoute, TargetChannel targetChannel) {
        String key = generateKey(httpRoute);
        PerRouteConnectionPool perRouteConnectionPool = fetchConnectionPool(key);
        if (perRouteConnectionPool != null) {
            perRouteConnectionPool.addChannel(targetChannel);
        }
    }

    /**
     * Entity which holds the pool of connections for a given http route
     */
    private static class PerRouteConnectionPool {

        private BlockingQueue<TargetChannel> targetChannels = new LinkedBlockingQueue<>();

        // Maximum number of allowed active streams
        private int maxActiveStreams;

        public PerRouteConnectionPool(TargetChannel targetChannel) {
            targetChannels.add(targetChannel);
            maxActiveStreams = Integer.MAX_VALUE;
        }

        public PerRouteConnectionPool(TargetChannel targetChannel, int maxActiveStreams) {
            targetChannels.add(targetChannel);
            this.maxActiveStreams = maxActiveStreams;
        }

        /**
         * Fetch active {@code TargetChannel} from the pool
         *
         * @return active TargetChannel
         */
        TargetChannel fetchTargetChannel() {

            if (targetChannels.size() != 0) {
                TargetChannel targetChannel = targetChannels.peek();
                Channel channel = targetChannel.getChannel();
                if (!channel.isActive()) {  // if channel is not active, forget it and fetch next one
                    targetChannels.remove(targetChannel);
                    return fetchTargetChannel();
                }
                // increment and get active stream count
                int activeSteamCount = targetChannel.incrementActiveStreamCount();

                if (activeSteamCount < maxActiveStreams) {  // safe to fetch the Target Channel
                    return targetChannel;
                } else if (activeSteamCount == maxActiveStreams) {  // no more streams except this one can be opened
                    targetChannel.markAsExhausted();
                    targetChannels.remove(targetChannel);
                    return targetChannel;
                } else {
                    targetChannels.remove(targetChannel);
                    return fetchTargetChannel();    // fetch the next one from the queue
                }
            }
            return null;
        }

        void addChannel(TargetChannel targetChannel) {
            targetChannels.add(targetChannel);
        }

        void removeChannel(TargetChannel targetChannel) {
            targetChannels.remove(targetChannel);
        }
    }

}
