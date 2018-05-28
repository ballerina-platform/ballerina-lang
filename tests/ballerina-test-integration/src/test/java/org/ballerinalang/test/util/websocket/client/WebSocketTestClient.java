/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.util.websocket.client;

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
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * WebSocket client class for test.
 */
public class WebSocketTestClient {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketTestClient.class);

    private Channel channel = null;
    private WebSocketTestClientHandler webSocketHandler;
    private final URI uri;
    private EventLoopGroup group;

    public WebSocketTestClient(String url) throws URISyntaxException {
        this(url, new HashMap<>());
    }

    public WebSocketTestClient(String url, Map<String, String> headers) throws URISyntaxException {
        this.uri = new URI(url);
        // Creating webSocketHandler
        URI uri = new URI(url);
        DefaultHttpHeaders httpHeaders = new DefaultHttpHeaders();
        headers.forEach(httpHeaders::add);
        webSocketHandler = new WebSocketTestClientHandler(WebSocketClientHandshakerFactory.
                newHandshaker(uri, WebSocketVersion.V13, null, true, httpHeaders));
    }

    public void setCountDownLatch(CountDownLatch countdownLatch) {
        webSocketHandler.setCountDownLatch(countdownLatch);
    }

    /**
     * Handshake with the remote server.
     */
    public void handshake() throws InterruptedException {
        group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast(new HttpClientCodec(), new HttpObjectAggregator(8192),
                                 WebSocketClientCompressionHandler.INSTANCE, webSocketHandler);
            }
        });
        channel = bootstrap.connect(uri.getHost(), uri.getPort()).sync().channel();
        webSocketHandler.handshakeFuture().sync();
    }

    /**
     * Send text to the server.
     *
     * @param text text need to be sent.
     */
    public void sendText(String text) throws InterruptedException {
        if (channel == null) {
            logger.error("Channel is null. Cannot send text.");
            throw new IllegalArgumentException("Cannot find the channel to write");
        }
        channel.writeAndFlush(new TextWebSocketFrame(text)).sync();
    }

    /**
     * Send binary data to server.
     *
     * @param buf buffer containing the data need to be sent.
     */
    public void sendBinary(ByteBuffer buf) throws InterruptedException {
        if (channel == null) {
            logger.error("Channel is null. Cannot send text.");
            throw new IllegalArgumentException("Cannot find the channel to write");
        }
        channel.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(buf))).sync();
    }

    /**
     * Send a ping message to the server.
     *
     * @param buf content of the ping message to be sent.
     */
    public void sendPing(ByteBuffer buf) throws InterruptedException {
        if (channel == null) {
            logger.error("Channel is null. Cannot send text.");
            throw new IllegalArgumentException("Cannot find the channel to write");
        }
        channel.writeAndFlush(new PingWebSocketFrame(Unpooled.wrappedBuffer(buf))).sync();
    }

    /**
     * @return the text received from the server.
     */
    public String getTextReceived() {
        return webSocketHandler.getTextReceived();
    }

    /**
     * @return the binary data received from the server.
     */
    public ByteBuffer getBufferReceived() {
        return webSocketHandler.getBufferReceived();
    }

    /**
     * Check whether the connection is opened or not.
     *
     * @return true if the connection is open.
     */
    public boolean isOpen() {
        return webSocketHandler.isOpen();
    }

    /**
     * Check whether webSocketHandler receives a ping.
     *
     * @return true if a ping is received.
     */
    public boolean isPing() {
        return webSocketHandler.isPing();
    }

    /**
     * Check whether webSocketHandler receives a pong.
     *
     * @return true if a pong is received.
     */
    public boolean isPong() {
        return webSocketHandler.isPong();
    }

    /**
     * Gets the header value from the response headers from the WebSocket handler.
     *
     * @param headerName the header name
     * @return the header value from the response headers.
     */
    public String getHeader(String headerName) {
        return webSocketHandler.getHeader(headerName);
    }

    /**
     * Shutdown the WebSocket Client.
     */
    public void shutDown() throws InterruptedException {
        channel.writeAndFlush(new CloseWebSocketFrame());
        channel.closeFuture().sync();
        group.shutdownGracefully();
    }

}
