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

package org.wso2.carbon.transport.http.netty.sender.websocket;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.Headers;
import org.wso2.carbon.transport.http.netty.listener.WebSocketSourceHandler;

import java.net.URI;
import java.net.URISyntaxException;
import javax.net.ssl.SSLException;
import javax.websocket.Session;

/**
 * WebSocket client for sending and receiving messages in WebSocket as a client.
 */
public class WebSocketClient {

    private static final Logger log = LoggerFactory.getLogger(WebSocketClient.class);

    private Channel channel;
    private WebSocketClientHandler handler;
    private EventLoopGroup group;
    private boolean handshakeDone = false;

    private final String url;
    private final String subprotocol;
    private final boolean allowExtensions;
    private final Headers headers;
    private final WebSocketSourceHandler sourceHandler;

    /**
     * @param url url of the remote endpoint.
     * @param subprotocol the negotiable sub-protocol if server is asking for it.
     * @param allowExtensions true is extensions are allowed.
     * @param headers any specific headers which need to send to the server.
     */
    public WebSocketClient(String url, String subprotocol, boolean allowExtensions,
                           Headers headers, WebSocketSourceHandler sourceHandler) {
        this.url = url;
        this.subprotocol = subprotocol;
        this.allowExtensions = allowExtensions;
        this.headers = headers;
        this.sourceHandler = sourceHandler;
    }

    /**
     * Handle the handshake with the server.
     *
     * @throws URISyntaxException throws if there is an error in the URI syntax.
     * @throws InterruptedException throws if the connecting the server is interrupted.
     * @throws SSLException throws if SSL exception occurred during protocol negotiation.
     */
    public void handshake() throws InterruptedException, URISyntaxException, SSLException {
        URI uri = new URI(url);
        String scheme = uri.getScheme() == null ? "ws" : uri.getScheme();
        final String host = uri.getHost() == null ? "127.0.0.1" : uri.getHost();
        final int port;
        if (uri.getPort() == -1) {
            if ("ws".equalsIgnoreCase(scheme)) {
                port = 80;
            } else if ("wss".equalsIgnoreCase(scheme)) {
                port = 443;
            } else {
                port = -1;
            }
        } else {
            port = uri.getPort();
        }

        if (!"ws".equalsIgnoreCase(scheme) && !"wss".equalsIgnoreCase(scheme)) {
            log.error("Only WS(S) is supported.");
            throw new SSLException("");
        }

        final boolean ssl = "wss".equalsIgnoreCase(scheme);
        final SslContext sslCtx;
        if (ssl) {
            sslCtx = SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        } else {
            sslCtx = null;
        }

        group = new NioEventLoopGroup();
        HttpHeaders httpHeaders = new DefaultHttpHeaders();

        // Adding custom headers to the handshake request.
        headers.getAll().forEach(
                header -> httpHeaders.add(header.getName(), header.getValue())
        );

        WebSocketClientHandshaker websocketHandshaker = WebSocketClientHandshakerFactory.newHandshaker(
                uri, WebSocketVersion.V13, subprotocol, allowExtensions, httpHeaders);
        handler = new WebSocketClientHandler(websocketHandshaker, sourceHandler, ssl, url);

        Bootstrap b = new Bootstrap();
        b.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        if (sslCtx != null) {
                            p.addLast(sslCtx.newHandler(ch.alloc(), host, port));
                        }
                        p.addLast(
                                new HttpClientCodec(),
                                new HttpObjectAggregator(8192),
                                WebSocketClientCompressionHandler.INSTANCE,
                                handler);
                    }
                });

        channel = b.connect(uri.getHost(), port).sync().channel();
        handler.handshakeFuture().sync();
        handshakeDone = true;
    }

    /**
     * Retrieve the relevant session of the client.
     *
     * @return Session of the client.
     */
    public Session getSession() {
        return handler.getClientSession();
    }

}
