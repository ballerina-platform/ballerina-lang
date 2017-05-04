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
import io.netty.buffer.Unpooled;
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
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.Headers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import javax.net.ssl.SSLException;

/**
 * WebSocket client for sending and receiving messages in WebSocket as a client.
 */
public class WebSocketClient {

    private static final Logger log = LoggerFactory.getLogger(WebSocketClient.class);

    private Channel channel;
    private WebSocketClientHandler handler;
    private EventLoopGroup group;

    private final String clientId;
    private final String url;
    private final String subprotocol;
    private final boolean allowExtensions;
    private final Headers headers;

    /**
     * @param clientId unique id of the client given by the application.
     * @param url url of the remote endpoint.
     */
    public WebSocketClient(String clientId, String url) {
        this.clientId = clientId;
        this.url = url;
        this.subprotocol = null;
        this.allowExtensions = true;
        this.headers = new Headers();
    }

    /**
     * @param clientId unique id of the client given by the application.
     * @param url url of the remote endpoint.
     * @param subprotocol the negotiable sub-protocol if server is asking for it.
     */
    public WebSocketClient(String clientId, String url, String subprotocol) {
        this.clientId = clientId;
        this.url = url;
        this.subprotocol = subprotocol;
        this.allowExtensions = true;
        this.headers = new Headers();
    }

    /**
     * @param clientId unique id of the client given by the application.
     * @param url url of the remote endpoint.
     * @param subprotocol the negotiable sub-protocol if server is asking for it.
     * @param allowExtensions true is extensions are allowed.
     */
    public WebSocketClient(String clientId, String url, String subprotocol, boolean allowExtensions) {
        this.clientId = clientId;
        this.url = url;
        this.subprotocol = subprotocol;
        this.allowExtensions = allowExtensions;
        this.headers = new Headers();
    }

    /**
     * @param clientId unique id of the client given by the application.
     * @param url url of the remote endpoint.
     * @param subprotocol the negotiable sub-protocol if server is asking for it.
     * @param allowExtensions true is extensions are allowed.
     * @param headers any specific headers which need to send to the server.
     */
    public WebSocketClient(String clientId, String url, String subprotocol, boolean allowExtensions,
                           Headers headers) {
        this.clientId = clientId;
        this.url = url;
        this.subprotocol = subprotocol;
        this.allowExtensions = allowExtensions;
        this.headers = headers;
    }

    /**
     * Handle the handshake with the server.
     *
     * @return true if the handshake is done properly.
     * @throws URISyntaxException throws if there is an error in the URI syntax.
     * @throws InterruptedException throws if the connecting the server is interrupted.
     * @throws SSLException throws if SSL exception occurred during protocol negotiation.
     */
    public boolean handhshake() throws InterruptedException, URISyntaxException, SSLException {
        boolean isDone;
        URI uri = new URI(url);
        String scheme = uri.getScheme() == null ? "ws" : uri.getScheme();
        final String host = uri.getHost() == null ? "127.0.0.1" : uri.getHost();
        final int port;
        if (uri.getPort() == -1) {
            throw new URISyntaxException(url, "Cannot find a valid port");
        } else {
            port = uri.getPort();
        }

        if (!"ws".equalsIgnoreCase(scheme) && !"wss".equalsIgnoreCase(scheme)) {
            log.error("Only WS(S) is supported.");
            return false;
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
        handler =
                new WebSocketClientHandler(clientId,
                                           WebSocketClientHandshakerFactory.newHandshaker(
                                                   uri, WebSocketVersion.V13, subprotocol,
                                                   allowExtensions, httpHeaders
                                           ));
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
        isDone = handler.handshakeFuture().sync().isSuccess();
        return isDone;
    }

    /**
     * Send text to the server.
     *
     * @param text text need to be sent.
     * @throws IOException if I/O exception occurred during the writing data to the channel.
     */
    public void sendText(String text) throws IOException {
        if (channel == null) {
            log.error("Channel is null. Cannot send text.");
            throw new IOException("Cannot find the channel to write");
        }
        channel.writeAndFlush(new TextWebSocketFrame(text));
    }

    /**
     * Send binary data to server.
     *
     * @param buf buffer containing the data need to be sent.
     * @throws IOException if I/O exception occurred during the writing data to the channel.
     */
    public void sendBinary(ByteBuffer buf) throws IOException {
        if (channel == null) {
            log.error("Channel is null. Cannot send text.");
            throw new IOException("Cannot find the channel to write");
        }
        channel.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(buf)));
    }

    /**
     * Send a ping message to the server.
     * @param buf content of the pong message to be sent.
     * @throws IOException if I/O exception occurred during the writing data to the channel.
     */
    public void sendPing(ByteBuffer buf) throws IOException {
        if (channel == null) {
            log.error("Channel is null. Cannot send text.");
            throw new IOException("Cannot find the channel to write");
        }
        channel.writeAndFlush(new PingWebSocketFrame(Unpooled.wrappedBuffer(buf)));
    }

    /**
     * Shutdown the WebSocket Client.
     *
     * @param statusCode code for the reason for closure according to https://tools.ietf.org/html/rfc6455#page-45.
     * @param reasonText String explaining the reason for closing the connection.
     * @see <a href="http://google.com">http://google.com</a>
     * @throws InterruptedException if any interruption occurred during connection closure.
     */
    public void shutDown(int statusCode, String reasonText) throws InterruptedException {
        channel.writeAndFlush(new CloseWebSocketFrame(statusCode, reasonText));
        channel.closeFuture().sync();
        group.shutdownGracefully();
    }

}
