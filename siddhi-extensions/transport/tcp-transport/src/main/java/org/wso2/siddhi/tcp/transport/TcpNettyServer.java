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
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.log4j.Logger;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.tcp.transport.callback.LogStreamListener;
import org.wso2.siddhi.tcp.transport.callback.StatisticsStreamListener;
import org.wso2.siddhi.tcp.transport.callback.StreamListener;
import org.wso2.siddhi.tcp.transport.config.ServerConfig;
import org.wso2.siddhi.tcp.transport.utils.StreamTypeHolder;
import org.wso2.siddhi.tcp.transport.handlers.EventDecoder;

public class TcpNettyServer {
    private static final Logger log = Logger.getLogger(TcpNettyServer.class);
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private StreamTypeHolder streamInfoHolder = new StreamTypeHolder();

    public static void main(String[] args) {
        StreamDefinition streamDefinition = StreamDefinition.id("StockStream").attribute("symbol", Attribute.Type.STRING)
                .attribute("price", Attribute.Type.INT).attribute("volume", Attribute.Type.INT);

        TcpNettyServer tcpNettyServer = new TcpNettyServer();
        tcpNettyServer.addStreamListener(new LogStreamListener(streamDefinition));
//        tcpNettyServer.addStreamListener(new StatisticsStreamListener(streamDefinition));

        tcpNettyServer.bootServer(new ServerConfig());
        tcpNettyServer.shutdownGracefully();
    }

    public void bootServer(ServerConfig serverConfig) {
        bossGroup = new NioEventLoopGroup(serverConfig.getReceiverThreads());
        workerGroup = new NioEventLoopGroup(serverConfig.getWorkerThreads());
        try {
            // More terse code to setup the server
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer() {

                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            ChannelPipeline p = channel.pipeline();
                            p.addLast(
                                    new EventDecoder(streamInfoHolder));
                        }
                    });

            // Bind and start to accept incoming connections.
            ChannelFuture channelFuture = bootstrap.bind(serverConfig.getHost(), serverConfig.getPort()).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("Error when booting up binary server " + e.getMessage(), e);
        }
    }

    public void shutdownGracefully() {
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
        workerGroup = null;
        bossGroup = null;
    }

    public void addStreamListener(StreamListener streamListener) {
        streamInfoHolder.putStreamCallback(streamListener);
    }

    public void removeStreamListener(String streamId) {
        streamInfoHolder.removeStreamCallback(streamId);

    }

}



