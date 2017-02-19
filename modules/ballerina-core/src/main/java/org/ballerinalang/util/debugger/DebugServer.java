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


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.ballerinalang.util.debugger.dto.MessageDTO;

import java.io.PrintStream;

import static org.ballerinalang.runtime.Constants.SYSTEM_PROP_BAL_DEBUG;

/**
 * {@code DebugServer} will open a websocket server for external clients to connect
 * The websocket server is implemented with netty websocket library.
 *
 * @since 0.8.0
 */
public class DebugServer {


    /**
     *  Debug server initializer class
     */
    static class DebugServerInitializer extends ChannelInitializer<SocketChannel> {

        @Override
        public void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast(new HttpServerCodec());
            pipeline.addLast(new HttpObjectAggregator(65536));
            pipeline.addLast(new DebugServerHandler());
        }
    }

    /**
     * Start the web socket server
     */
    public void startServer() {
        //lets start the server in a new thread.
        Runnable run = new Runnable() {
            public void run() {
                DebugServer.this.startListning();
            }
        };
        (new Thread(run)).start();
    }

    private void startListning() {
        int port = getDebugPort();
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    //todo activate debug logs once implemented.
                    //.handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new DebugServerInitializer());
            Channel ch = b.bind(port).sync().channel();

            PrintStream out = System.out;
            out.println(DebugConstants.DEBUG_MESSAGE + port);

            ch.closeFuture().sync();
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

    /**
     * Push message to client.
     *
     * @param debugSession
     * @param status      
     */
    public void pushMessageToClient(DebugSession debugSession, MessageDTO status) {
        ObjectMapper mapper = new ObjectMapper();
        String json = null;
        try {
            json = mapper.writeValueAsString(status);
        } catch (JsonProcessingException e) {
            json = DebugConstants.ERROR_JSON;
        }
        debugSession.getChannel().write(new TextWebSocketFrame(json));
        debugSession.getChannel().flush();
    }

    private int getDebugPort() {
        String debugPort = System.getProperty(SYSTEM_PROP_BAL_DEBUG);
        if (debugPort == null || debugPort.equals("")) {
            debugPort = DebugConstants.DEFAULT_DEBUG_PORT;
        }
        return Integer.parseInt(debugPort);
    }
}
