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
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.SupportedCipherSuiteFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.util.TestUtil;
import org.wso2.transport.http.netty.util.server.initializers.HttpServerInitializer;
import org.wso2.transport.http.netty.util.server.initializers.http2.Http2ServerInitializer;

import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
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
    private SslContext nettySSLCtx;
    private ChannelInitializer channelInitializer;
    private String httpVersion = "1.1";
    private static final String HTTP2_VERSION = "2.0";

    char ksPass[] = "wso2carbon".toCharArray();
    char ctPass[] = "wso2carbon".toCharArray();

    public HttpsServer(int port, ChannelInitializer channelInitializer) {
        this.port = port;
        this.channelInitializer = channelInitializer;
    }

    public HttpsServer(int port, ChannelInitializer channelInitializer, int bossGroupSize, int workerGroupSize,
                       String httpVersion) {
        this.port = port;
        this.channelInitializer = channelInitializer;
        this.bossGroupSize = bossGroupSize;
        this.workerGroupSize = workerGroupSize;
        this.httpVersion = httpVersion;
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

            if (HTTP2_VERSION.equals(httpVersion)) {
                List<String> ciphers = new ArrayList<>();
                ciphers.add("TLS_RSA_WITH_AES_128_CBC_SHA");
                nettySSLCtx = SslContextBuilder.forServer(kmf).applicationProtocolConfig(
                    new ApplicationProtocolConfig(ApplicationProtocolConfig.Protocol.ALPN,
                                                  ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
                                                  ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
                                                  ApplicationProtocolNames.HTTP_2, ApplicationProtocolNames.HTTP_1_1))
                    .sslProvider(SslProvider.OPENSSL)
                    .ciphers(ciphers, SupportedCipherSuiteFilter.INSTANCE).build();
                ((Http2ServerInitializer) channelInitializer).setSslContext(nettySSLCtx);
            } else {
                sslContext = SSLContext.getInstance(TLS_PROTOCOL);
                sslContext.init(kmf.getKeyManagers(), null, null);
                ((HttpServerInitializer) channelInitializer).setSslContext(sslContext);
            }
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
