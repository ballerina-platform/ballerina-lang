/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.siddhi.tcp.transport;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.EmptyByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.log4j.Logger;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.tcp.transport.callback.LogStreamListener;
import org.wso2.siddhi.tcp.transport.callback.StreamListener;
import org.wso2.siddhi.tcp.transport.config.ServerConfig;
import org.wso2.siddhi.tcp.transport.handlers.EventDecoder;
import org.wso2.siddhi.tcp.transport.utils.StreamTypeHolder;

public class TCPNettyServer {
    private static final Logger log = Logger.getLogger(TCPNettyServer.class);
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private StreamTypeHolder streamInfoHolder = new StreamTypeHolder();
    private ChannelFuture channelFuture;
    private String hostAndPort;
    private FlowController flowController;

    public static void main(String[] args) {
        StreamDefinition streamDefinition = StreamDefinition.id("StockStream").attribute("symbol", Attribute.Type
                .STRING)
                .attribute("price", Attribute.Type.INT).attribute("volume", Attribute.Type.INT);

        TCPNettyServer tcpNettyServer = new TCPNettyServer();
        tcpNettyServer.addStreamListener(new LogStreamListener(streamDefinition));
//        tcpNettyServer.addStreamListener(new StatisticsStreamListener(streamDefinition));

        tcpNettyServer.bootServer(new ServerConfig());
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
        } finally {
            tcpNettyServer.shutdownGracefully();
        }
    }

    public void bootServer(ServerConfig serverConfig) {
        bossGroup = new NioEventLoopGroup(serverConfig.getReceiverThreads());
        workerGroup = new NioEventLoopGroup(serverConfig.getWorkerThreads());
        hostAndPort = serverConfig.getHost() + ":" + serverConfig.getPort();
        try {
            flowController = new FlowController(serverConfig.getQueueSizeOfTcpTransport());
            // More terse code to setup the server
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childOption(ChannelOption.AUTO_READ, false)
                    .childHandler(new ChannelInitializer() {

                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            ChannelPipeline p = channel.pipeline();
                            p.addLast(flowController);
                            p.addLast(new EventDecoder(streamInfoHolder));
                        }
                    });

            // Bind and start to accept incoming connections.
            channelFuture = bootstrap.bind(serverConfig.getHost(), serverConfig.getPort()).sync();

            log.info("Tcp Server started in " + hostAndPort + "");
        } catch (InterruptedException e) {
            log.error("Error when booting up tcp server on '" + hostAndPort + "' " + e.getMessage(), e);
        }
    }

    public void shutdownGracefully() {
        channelFuture.channel().close();
        try {
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("Error when shutdowning the tcp server " + e.getMessage(), e);
        }
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
        log.info("Tcp Server running on '" + hostAndPort + "' stopped.");
        workerGroup = null;
        bossGroup = null;

    }

    public synchronized void addStreamListener(StreamListener streamListener) {
        streamInfoHolder.putStreamCallback(streamListener);
    }

    public synchronized void removeStreamListener(String streamId) {
        streamInfoHolder.removeStreamCallback(streamId);
    }

    public synchronized int getNoOfRegisteredStreamListeners() {
        return streamInfoHolder.getNoOfRegisteredStreamListeners();
    }

    public void isPaused(boolean paused) {
        flowController.isPaused(paused);
    }
}

/**
 * This {@link io.netty.channel.ChannelInboundHandlerAdapter} implementation is used to control the flow when the
 * transport is needed to be paused or resumed. When the transport is paused, this would keep the read messages in
 * an internal {@link CircularFifoQueue} with a user defined size (default is
 * {@link org.wso2.siddhi.tcp.transport.utils.Constant#DEFAULT_QUEUE_SIZE_OF_TCP_TRANSPORT}).
 */

class FlowController extends ChannelInboundHandlerAdapter {
    private ChannelHandlerContext channelHandlerContext;
    private final CircularFifoQueue<Object> queue;
    private boolean paused;

    FlowController(int size) {
        queue = new CircularFifoQueue<>(size);
    }

    void isPaused(boolean paused) {
        this.paused = paused;
        channelRead(channelHandlerContext, null);
    }

    public void channelActive(ChannelHandlerContext ctx) {
        channelHandlerContext = ctx;
        // since auto-read is set to false, we have to trigger the read
        ctx.channel().read();
    }

    public void channelRead(final ChannelHandlerContext ctx, Object msg) {
        // since auto-read is set to false, we have to trigger the read
        ctx.channel().read();

        if (msg != null) {
            // queue the message
            queue.add(msg);
        }

        if (!paused) {
            // deque the messages if the transport is not paused
            queue.forEach(e -> {
                if (!(e instanceof EmptyByteBuf)) {
                    ctx.fireChannelRead(e);
                }
            });
            queue.clear();
        }
    }

    @Override
    public boolean isSharable() {
        return true;
    }
}