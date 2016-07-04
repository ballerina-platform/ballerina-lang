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

package org.wso2.carbon.transport.http.netty.util.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import javax.net.ssl.SSLContext;

/**
 * A Class which can be configured to use as HTTP Client
 */
public class HTTPClient {

    private static final Logger logger = LoggerFactory.getLogger(HTTPClient.class);

    private SSLContext sslContext;

    private Bootstrap bootstrap;

    private ResponseCallback responseCallback;

    private EventLoopGroup eventLoopGroup;

    private boolean keepAlive = true;

    private boolean tcpNoDelay = true;

    private int connectTimeOut = 15000;

    private boolean socketReUse = true;

    public HTTPClient(ResponseCallback responseCallback, SSLContext sslContext) {
        this.sslContext = sslContext;
        this.responseCallback = responseCallback;
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.option(ChannelOption.SO_KEEPALIVE, keepAlive);
        bootstrap.option(ChannelOption.TCP_NODELAY, tcpNoDelay);
        bootstrap.option(ChannelOption.SO_REUSEADDR, socketReUse);
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeOut);
        HTTPClientInitializer httpClientInitializer = new HTTPClientInitializer(responseCallback);
        httpClientInitializer.setSslContext(sslContext);
        bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class).handler(httpClientInitializer);
    }

    /**
     * Send Request
     *
     * @param request
     */
    public void send(Request request) {
        String scheme = request.getUri().getScheme() == null ? "http" : request.getUri().getScheme();
        String host = request.getUri().getHost() == null ? "localhost" : request.getUri().getHost();
        int port = request.getUri().getPort();
        if (port == -1) {
            if ("http".equalsIgnoreCase(scheme)) {
                port = 80;
            } else if ("https".equalsIgnoreCase(scheme)) {
                port = 443;
            }
        }

        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            logger.info("Only HTTP(S) is supported.");
            return;
        }
        try {
            ChannelFuture channelFuture = bootstrap.connect(new InetSocketAddress(host, port)).await();
            if (channelFuture.isSuccess()) {
                channelFuture.channel().writeAndFlush(request.getHTTPRequest());
            } else {
                logger.error("Connect failed  for host " + host + " port " + port);
                eventLoopGroup.shutdownGracefully();
            }
        } catch (Exception exception) {
            eventLoopGroup.shutdownGracefully();
        }
    }

}
