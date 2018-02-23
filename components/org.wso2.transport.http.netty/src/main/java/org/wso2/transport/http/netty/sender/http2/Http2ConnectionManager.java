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
import io.netty.handler.codec.http2.Http2Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.HttpRoute;
import org.wso2.transport.http.netty.config.SenderConfiguration;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@code Http2ConnectionManager} Manages HTTP/2 connections
 */
public class Http2ConnectionManager {

    private static final Logger log = LoggerFactory.getLogger(Http2ConnectionManager.class);
    private static ConcurrentHashMap<String, TargetChannel> clientConnections = new ConcurrentHashMap<>();
    private static Http2ConnectionManager instance = new Http2ConnectionManager();
    /* Lock for synchronizing access */
    private Lock lock = new ReentrantLock();

    private Http2ConnectionManager() {
    }

    public static Http2ConnectionManager getInstance() {
        return instance;
    }

    public synchronized TargetChannel borrowChannel(HttpRoute httpRoute, SenderConfiguration senderConfig) {

        String key = generateKey(httpRoute);
        TargetChannel channel = fetchConnectionFromPool(key);
        if (channel == null) {
            // double locking is to prevent 2 connections get created for same host:port pair under concurrency
            lock.lock();
            try {
                channel = fetchConnectionFromPool(key);
                if (channel == null) {
                    channel = createNewConnection(httpRoute, senderConfig);
                }
            } finally {
                lock.unlock();
            }
        }
        return channel;
    }

    private TargetChannel createNewConnection(HttpRoute httpRoute, SenderConfiguration senderConfig) {

        Http2ClientInitializer initializer = new Http2ClientInitializer(senderConfig);

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
        Http2ClientHandler clientHandler = initializer.getHttp2ClientHandler();
        TargetChannel targetChannel = new TargetChannel(clientHandler.getConnection(), channelFuture);
        clientHandler.setTargetChannel(targetChannel);
        String key = generateKey(httpRoute);
        clientConnections.put(key, targetChannel);

        // Configure a listener to remove connection from pool when it is closed
        channelFuture.channel().closeFuture().addListener(future -> clientConnections.remove(key));
        return targetChannel;
    }

    /**
     * Fetch a connection from the pool
     * <p>
     * No need to remove from the pool when fetching as same connection can be shared across multiple requests
     * (HTTP/2 Multiplexing)
     *
     * @param key host:port combination key
     * @return TargetChannel already available in the pool
     */
    private TargetChannel fetchConnectionFromPool(String key) {

        TargetChannel targetChannel = null;
        if (clientConnections.containsKey(key)) {
            log.debug("Fetched connection for : {} route from the pool", key);
            targetChannel = clientConnections.get(key);
            Channel channel = targetChannel.getChannel();
            Http2Connection.Endpoint localEndpoint = targetChannel.getConnection().local();
            if (!channel.isActive()) {
                log.debug("Channel available for : {} route in the pool is not active, hence removing", key);
                clientConnections.remove(key);
                return null;
            }

            if (!localEndpoint.canOpenStream()) {
                log.debug("Channel available for : {} route in the pool cannot have more streams, hence removing", key);
                clientConnections.remove(key);
                return null;
            }
        }
        return targetChannel;
    }

    private String generateKey(HttpRoute httpRoute) {
        return httpRoute.getHost() + ":" + httpRoute.getPort();
    }

}
