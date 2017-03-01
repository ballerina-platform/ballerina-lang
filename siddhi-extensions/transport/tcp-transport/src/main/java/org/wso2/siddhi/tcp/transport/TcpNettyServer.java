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
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.log4j.Logger;
import org.wso2.siddhi.tcp.transport.config.ServerConfig;
import org.wso2.siddhi.tcp.transport.handlers.ServerChannelInitializer;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class TcpNettyServer {
    private static final Logger log = Logger.getLogger(TcpNettyServer.class);
    private PrintWriter writer = null;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public static void main(String[] args) {
        StreamDefinition stockStream = StreamDefinition.id("StockStream").attribute("symbol", Attribute.Type.STRING)
                .attribute("price", Attribute.Type.INT).attribute("volume", Attribute.Type.INT);
        TransportStreamManager.getInstance().addStreamDefinition(stockStream);
        TcpNettyServer tcpNettyServer= new TcpNettyServer();
        tcpNettyServer.bootServer(new ServerConfig());
        tcpNettyServer.shutdownGracefully();
    }

    public static void slog(Object msg) {
        log.info("[Server]:" + msg);
    }

    public void bootServer(ServerConfig serverConfig) {
        bossGroup = new NioEventLoopGroup(serverConfig.getReceiverThreads());
        workerGroup = new NioEventLoopGroup(serverConfig.getWorkerThreads());
        try {
            // More terse code to setup the server
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ServerChannelInitializer());

            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind(serverConfig.getHost(), serverConfig.getPort()).sync();
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("Error when booting up binary server " + e.getMessage(), e);
        }
    }

    public void shutdownGracefully() {
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }

    public void writeResult(String data) {
        try {
            if (writer == null) {
                writer = new PrintWriter("results.csv");
            }
        } catch (FileNotFoundException ex) {
            log.error("File not found......");
        }

        writer.println(data);
        writer.flush();
    }
}



