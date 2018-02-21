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

/**
 * Mange HTTP/2 connections
 */
public class Http2ConnectionManager {

    private static final Logger log = LoggerFactory.getLogger(Http2ConnectionManager.class);

    private static ConcurrentHashMap<String, TargetChannel> clientConnections = new ConcurrentHashMap<>();

    private static Http2ConnectionManager instance = new Http2ConnectionManager();

    private Http2ConnectionManager() {
    }

    public static Http2ConnectionManager getInstance() {
        return instance;
    }

    public synchronized TargetChannel borrowChannel(HttpRoute httpRoute, SenderConfiguration senderConfig) {

        String key = generateKey(httpRoute);
        TargetChannel channel = fetchConnectionFromPool(key);
        if (channel == null) {
            channel = createNewConnection(httpRoute, senderConfig);
        }
        return channel;
    }

    private TargetChannel createNewConnection(HttpRoute httpRoute, SenderConfiguration senderConfig) {

        Http2ClientInitializer initializer = new Http2ClientInitializer(senderConfig);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        Bootstrap clientBootstrap = new Bootstrap();
        clientBootstrap.group(workerGroup);
        clientBootstrap.channel(NioSocketChannel.class);
        clientBootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        clientBootstrap.remoteAddress(httpRoute.getHost(), httpRoute.getPort());
        clientBootstrap.handler(initializer);

        // Start the client.
        ChannelFuture channelFuture = clientBootstrap.connect();

        log.debug("Created channel: {}", httpRoute);
        TargetChannel targetChannel = new TargetChannel(initializer, channelFuture);
        Http2ClientHandler clientHandler = initializer.getHttp2ClientHandler();
        targetChannel.setClientHandler(clientHandler);
        clientHandler.setTargetChannel(targetChannel);

        String key = generateKey(httpRoute);
        clientConnections.put(key, targetChannel);

        channelFuture.channel().closeFuture().addListener(future -> clientConnections.remove(key));
        return targetChannel;
    }

    private TargetChannel fetchConnectionFromPool(String key) {

        TargetChannel targetChannel = null;
        if (clientConnections.containsKey(key)) {
            targetChannel = clientConnections.get(key);
            Channel channel = targetChannel.getChannel();
            Http2Connection.Endpoint localEndpoint = targetChannel.getConnection().local();
            if (!(channel.isActive() && localEndpoint.canOpenStream())) {
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
