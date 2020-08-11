/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.transport.http.netty.util.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.util.TestUtil;

import java.net.InetSocketAddress;

/**
 * A Simple HTTP Server
 */
public class HttpServer implements TestServer {

    private static final Logger LOG = LoggerFactory.getLogger(HttpServer.class);

    private int port = TestUtil.HTTP_SERVER_PORT;
    private int bossGroupSize = Runtime.getRuntime().availableProcessors();
    private int workerGroupSize = Runtime.getRuntime().availableProcessors() * 2;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ChannelInitializer channelInitializer;

    public HttpServer(int port, ChannelInitializer channelInitializer) {
        this.port = port;
        this.channelInitializer = channelInitializer;
    }

    public HttpServer(int port, ChannelInitializer channelInitializer, int bossGroupSize, int workerGroupSize) {
        this.port = port;
        this.channelInitializer = channelInitializer;
        this.bossGroupSize = bossGroupSize;
        this.workerGroupSize = workerGroupSize;
    }

    /**
     * Start the HttpServer
     */
    public void start() {
        bossGroup = new NioEventLoopGroup(this.bossGroupSize);
        workerGroup = new NioEventLoopGroup(this.workerGroupSize);
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.option(ChannelOption.SO_BACKLOG, 100);
            serverBootstrap.option(ChannelOption.SO_REUSEADDR, true);
            serverBootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 15000);
            serverBootstrap.childOption(ChannelOption.TCP_NODELAY, true);
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class).childHandler(channelInitializer);
            ChannelFuture ch = serverBootstrap.bind(new InetSocketAddress(TestUtil.TEST_HOST, port));
            ch.sync();
            LOG.info("HttpServer started on port " + port);
        } catch (InterruptedException e) {
            LOG.error("HTTP Server cannot start on port " + port);
        }
    }

    /**
     * Shutdown the HttpServer
     */
    public void shutdown() throws InterruptedException {
        bossGroup.shutdownGracefully().sync();
        workerGroup.shutdownGracefully().sync();
        LOG.info("HttpServer shutdown");
    }

    public void setMessage(String message, String contentType) {}
}
