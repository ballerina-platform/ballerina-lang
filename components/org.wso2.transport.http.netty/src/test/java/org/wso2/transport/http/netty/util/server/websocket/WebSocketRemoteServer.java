/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.transport.http.netty.util.server.websocket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple WebSocket server for Test cases.
 */
public final class WebSocketRemoteServer {

    private static final Logger LOG = LoggerFactory.getLogger(WebSocketRemoteServer.class);

    private final int port;
    private final String subProtocols;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public WebSocketRemoteServer(int port) {
        this(port, null);
    }

    public WebSocketRemoteServer(int port, String subProtocols) {
        this.port = port;
        this.subProtocols = subProtocols;
    }

    public void run() throws InterruptedException {
        final SslContext sslCtx = null;
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
         .channel(NioServerSocketChannel.class)
         .childHandler(new WebSocketRemoteServerInitializer(sslCtx, subProtocols));

        serverBootstrap.bind(port).sync();
        LOG.info("WebSocket remote server started listening on port " + port);
    }

    public void stop() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        LOG.info("WebSocket remote server stopped listening  on port " + port);
    }
}
