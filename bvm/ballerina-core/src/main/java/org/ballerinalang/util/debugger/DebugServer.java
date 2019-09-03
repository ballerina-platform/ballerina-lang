/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.util.debugger;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

import java.io.PrintStream;

import static org.ballerinalang.util.debugger.DebugConstants.SYSTEM_PROP_BAL_DEBUG;

/**
 * {@code VMDebugServer} will open a websocket server for external clients to connect.
 * The websocket server is implemented with netty websocket library.
 *
 * @since 0.88
 */
class DebugServer {

    private Debugger debugger;

    private Channel serverChannel;

    DebugServer(Debugger debugger) {
        this.debugger = debugger;
    }

    /**
     *  Debug server initializer class.
     */
    static class DebugServerInitializer extends ChannelInitializer<SocketChannel> {

        Debugger debugManager;

        DebugServerInitializer(Debugger debugManager) {
            this.debugManager = debugManager;
        }

        @Override
        public void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast(new HttpServerCodec());
            pipeline.addLast(new HttpObjectAggregator(65536));
            pipeline.addLast(new VMDebugServerHandler(debugManager));
        }
    }

    /**
     * Start the web socket server.
     */
    void startServer() {
        //lets start the server in a new thread.
        Runnable run = DebugServer.this::startListening;
        (new Thread(run)).start();
    }

    private void startListening() {
        int port = getDebugPort();
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    //todo activate debug logs once implemented.
                    //.handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new DebugServerInitializer(debugger));
            serverChannel = b.bind(port).sync().channel();

            PrintStream out = System.out;
            out.println(DebugConstants.DEBUG_MESSAGE + port);

            serverChannel.closeFuture().sync();
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        } catch (InterruptedException e) {
            // todo need to log the error.
            Thread.currentThread().interrupt();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private int getDebugPort() {
        String debugPort = System.getProperty(SYSTEM_PROP_BAL_DEBUG);
        if (debugPort == null || debugPort.equals("")) {
            debugPort = DebugConstants.DEFAULT_DEBUG_PORT;
        }
        return Integer.parseInt(debugPort);
    }

    void closeServerChannel() {
        serverChannel.close();
    }
}
