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
import org.wso2.transport.http.netty.util.server.initializers.HttpServerInitializer;

import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import static org.wso2.transport.http.netty.contract.Constants.JKS;
import static org.wso2.transport.http.netty.contract.Constants.TLS_PROTOCOL;

/**
 * A Simple HTTPS Server
 */
public class HttpsServer implements TestServer {

    private static final Logger LOG = LoggerFactory.getLogger(HttpsServer.class);

    private int port;
    private int bossGroupSize = Runtime.getRuntime().availableProcessors();
    private int workerGroupSize = Runtime.getRuntime().availableProcessors() * 2;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private SSLContext sslContext;
    private ChannelInitializer channelInitializer;

    char ksPass[] = "wso2carbon".toCharArray();
    char ctPass[] = "wso2carbon".toCharArray();

    public HttpsServer(int port, ChannelInitializer channelInitializer) {
        this.port = port;
        this.channelInitializer = channelInitializer;
    }

    /**
     * Start the HttpsServer
     */
    public void start() {
        bossGroup = new NioEventLoopGroup(this.bossGroupSize);
        workerGroup = new NioEventLoopGroup(this.workerGroupSize);
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 100);
            b.childOption(ChannelOption.TCP_NODELAY, true);
            b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 15000);

            KeyStore ks = KeyStore.getInstance(JKS);
            ks.load(new FileInputStream(TestUtil.getAbsolutePath(TestUtil.KEY_STORE_FILE_PATH)), ksPass);
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, ctPass);

            sslContext = SSLContext.getInstance(TLS_PROTOCOL);
            sslContext.init(kmf.getKeyManagers(), null, null);
            ((HttpServerInitializer) channelInitializer).setSslContext(sslContext);

            b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).childHandler(channelInitializer);
            ChannelFuture ch = b.bind(new InetSocketAddress(TestUtil.TEST_HOST, port));
            ch.sync();
            LOG.info("HttpServer started on port " + port);
        } catch (Exception e) {
            LOG.error("HTTP Server cannot start on port " + port);
        }
    }

    /**
     * Shutdown the HttpsServer
     */
    public void shutdown() throws InterruptedException {
        bossGroup.shutdownGracefully().sync();
        workerGroup.shutdownGracefully().sync();
        LOG.info("HttpsServer shutdown ");
    }

    public void setMessage(String message, String contentType) {
    }
}
