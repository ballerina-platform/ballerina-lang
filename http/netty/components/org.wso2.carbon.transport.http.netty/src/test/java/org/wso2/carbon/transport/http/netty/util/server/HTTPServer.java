/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.transport.http.netty.util.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.transport.http.netty.util.TestUtil;

import java.net.InetSocketAddress;
import javax.net.ssl.SSLContext;

/**
 * A Simple HTTP Server
 */
public class HTTPServer {

    private static final Logger logger = LoggerFactory.getLogger(HTTPServer.class);

    private int port = 9000;
    private int bossGroupSize = Runtime.getRuntime().availableProcessors();
    private int workerGroupSize = Runtime.getRuntime().availableProcessors() * 2;
    private int backLog = 100;
    private int connectionTimeOut = 15000;
    private boolean tcpNoDelay = true;
    private boolean isKeepAlive = true;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private SSLContext sslContext;
    private HTTPServerInitializer httpServerInitializer;

    public HTTPServer(int port) {
        this.port = port;
    }

    /**
     * Start the HTTPServer
     */
    public void start() {
        bossGroup = new NioEventLoopGroup(this.bossGroupSize);
        workerGroup = new NioEventLoopGroup(this.workerGroupSize);
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, backLog);
            b.childOption(ChannelOption.TCP_NODELAY, tcpNoDelay);
            b.option(ChannelOption.SO_KEEPALIVE, isKeepAlive);
            b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeOut);
            httpServerInitializer = new HTTPServerInitializer();
            httpServerInitializer.setSslContext(sslContext);
            b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).childHandler(httpServerInitializer);
            ChannelFuture ch = b.bind(new InetSocketAddress(TestUtil.TEST_HOST, port)).sync();
            logger.info("HTTPServer starting on port " + port);
            if (ch.isSuccess()) {
                logger.info("HTTPServer started on port " + port);
            }
        } catch (InterruptedException e) {
            logger.error("HTTP Server cannot start on port " + port);
        }
    }

    /**
     * Shutdown the HTTPServer
     */
    public void shutdown() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        logger.info("HTTPServer shutdown ");
    }

    public void setBossGroupSize(int bossGroupSize) {
        this.bossGroupSize = bossGroupSize;
    }

    public void setWorkerGroupSize(int workerGroupSize) {
        this.workerGroupSize = workerGroupSize;
    }

    public void setBackLog(int backLog) {
        this.backLog = backLog;
    }

    public void setConnectionTimeOut(int connectionTimeOut) {
        this.connectionTimeOut = connectionTimeOut;
    }

    public void setTcpNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
    }

    public void setKeepAlive(boolean isKeepAlive) {
        this.isKeepAlive = isKeepAlive;
    }

    public void setSslContext(SSLContext sslContext) {
        this.sslContext = sslContext;
    }

    public void setMessage(String message, String contentType) {
        httpServerInitializer.setMessage(message, contentType);

    }

    public void setResponseCode(int responseCode) {
        httpServerInitializer.setResponseCode(responseCode);
    }

}
