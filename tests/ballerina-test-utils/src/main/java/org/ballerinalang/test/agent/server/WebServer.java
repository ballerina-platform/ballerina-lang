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
 */
package org.ballerinalang.test.agent.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * The WebServer class is a convenience wrapper around the Netty HTTP server.
 *
 * @since 0.982.0
 */
public class WebServer {
    private static final String TYPE_PLAIN = "text/plain; charset=UTF-8";
    private static final String SERVER_NAME = "BallerinaAgent";
    private final RouteTable routeTable;
    private final String host;
    private final int port;

    /**
     * Creates a new WebServer.
     *
     * @param host hostname.
     * @param port port number.
     */
    public WebServer(String host, int port) {
        this.routeTable = new RouteTable();
        this.host = host;
        this.port = port;
    }

    /**
     * Adds a GET route.
     *
     * @param path The URL path.
     * @param handler The request handler.
     * @return This WebServer.
     */
    public WebServer get(final String path, final BHandler handler) {
        this.routeTable.addRoute(new Route(HttpMethod.GET, path, handler));
        return this;
    }

    /**
     * Adds a POST route.
     *
     * @param path The URL path.
     * @param handler The request handler.
     * @return This WebServer.
     */
    public WebServer post(final String path, final BHandler handler) {
        this.routeTable.addRoute(new Route(HttpMethod.POST, path, handler));
        return this;
    }

    /**
     * Starts the web server.
     *
     * @throws Exception in case anything fails
     */
    public void start() throws Exception {
        start(new NioEventLoopGroup(), NioServerSocketChannel.class);
    }

    /**
     * Initializes the server, socket, and channel.
     *
     * @param loopGroup The event loop group.
     * @param serverChannelClass The socket channel class.
     * @throws InterruptedException on interruption.
     */
    private void start(
            final EventLoopGroup loopGroup,
            final Class<? extends ServerChannel> serverChannelClass)
                    throws InterruptedException {
        try {
            final InetSocketAddress inet = new InetSocketAddress(host, port);

            final ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.option(ChannelOption.SO_BACKLOG, 1024);
            serverBootstrap.option(ChannelOption.SO_REUSEADDR, true);
            serverBootstrap.group(loopGroup).channel(serverChannelClass).childHandler(new WebServerInitializer());
            serverBootstrap.option(ChannelOption.MAX_MESSAGES_PER_READ, Integer.MAX_VALUE);
            serverBootstrap.childOption(ChannelOption.ALLOCATOR, new PooledByteBufAllocator(true));
            serverBootstrap.childOption(ChannelOption.SO_REUSEADDR, true);
            serverBootstrap.childOption(ChannelOption.MAX_MESSAGES_PER_READ, Integer.MAX_VALUE);

            final Channel ch = serverBootstrap.bind(inet).sync().channel();
            ch.closeFuture().sync();
        } finally {
            loopGroup.shutdownGracefully().sync();
        }
    }

    /**
     * The Initializer class initializes the HTTP channel.
     */
    private class WebServerInitializer extends ChannelInitializer<SocketChannel> {

        /**
         * Initializes the channel pipeline with the HTTP response handlers.
         *
         * @param ch The Channel which was registered.
         */
        @Override
        public void initChannel(SocketChannel ch) throws Exception {
            final ChannelPipeline p = ch.pipeline();
            p.addLast("decoder", new HttpRequestDecoder(4096, 8192, 8192, false));
            p.addLast("aggregator", new HttpObjectAggregator(100 * 1024 * 1024));
            p.addLast("encoder", new HttpResponseEncoder());
            p.addLast("handler", new WebServerHandler());
        }
    }

    /**
     * The Handler class handles all inbound channel messages.
     */
    private class WebServerHandler extends SimpleChannelInboundHandler<Object> {

        /**
         * Handles an exception caught.  Closes the context.
         *
         * @param ctx The channel context.
         * @param cause The exception.
         */
        @Override
        public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
            ctx.close();
        }

        /**
         * Handles read complete event.  Flushes the context.
         *
         * @param ctx The channel context.
         */
        @Override
        public void channelReadComplete(final ChannelHandlerContext ctx) {
            ctx.flush();
        }

        /**
         * Handles a new message.
         *
         * @param channelHandlerContext The channel context.
         * @param o The HTTP request message.
         */
        @Override
        protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
            if (!(o instanceof FullHttpRequest request)) {
                return;
            }

            final HttpMethod method = request.method();
            final String uri = request.uri();

            final Route route = WebServer.this.routeTable.findRoute(method, uri);
            if (route == null) {
                writeNotFound(channelHandlerContext);
                return;
            }

            try {
                //here we write the response back and then execute the handle method in the server
                //this is done because, within the handle method, server may get killed, so with this
                //we respond first and execute the method.
                final String content = "ok";
                writeResponse(channelHandlerContext, HttpResponseStatus.OK, TYPE_PLAIN, content);
                new Thread(() -> route.getHandler().handle()).start();
            } catch (final Exception ex) {
                ex.printStackTrace();
                writeInternalServerError(channelHandlerContext);
            }
        }
    }

    /**
     * Writes a 404 Not Found response.
     *
     * @param ctx The channel context.
     */
    private static void writeNotFound(ChannelHandlerContext ctx) {
        writeErrorResponse(ctx, HttpResponseStatus.NOT_FOUND);
    }

    /**
     * Writes a 500 Internal Server Error response.
     *
     * @param ctx The channel context.
     */
    private static void writeInternalServerError(ChannelHandlerContext ctx) {
        writeErrorResponse(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Writes a HTTP error response.
     *
     * @param ctx The channel context.
     * @param status The error status.
     */
    private static void writeErrorResponse(ChannelHandlerContext ctx, HttpResponseStatus status) {
        writeResponse(ctx, status, TYPE_PLAIN, status.reasonPhrase());
    }

    /**
     * Writes a HTTP response.
     *
     * @param ctx The channel context.
     * @param status The HTTP status code.
     * @param contentType The response content type.
     * @param content The response content.
     */
    private static void writeResponse(ChannelHandlerContext ctx, HttpResponseStatus status,
                                      CharSequence contentType, String content) {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        ByteBuf entity = Unpooled.wrappedBuffer(bytes);
        writeResponse(ctx, status, entity, contentType, bytes.length);
    }

    /**
     * Writes a HTTP response.
     *
     * @param ctx The channel context.
     * @param status The HTTP status code.
     * @param buf The response content buffer.
     * @param contentType The response content type.
     * @param contentLength The response content length;
     */
    private static void writeResponse(ChannelHandlerContext ctx, HttpResponseStatus status, ByteBuf buf,
                                      CharSequence contentType, int contentLength) {
        // Build the response object.
        final FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, buf, false);

        final ZonedDateTime dateTime = ZonedDateTime.now();
        final DateTimeFormatter formatter = DateTimeFormatter.RFC_1123_DATE_TIME;

        final DefaultHttpHeaders headers = (DefaultHttpHeaders) response.headers();
        headers.set(HttpHeaderNames.SERVER, SERVER_NAME);
        headers.set(HttpHeaderNames.DATE, dateTime.format(formatter));
        headers.set(HttpHeaderNames.CONTENT_TYPE, contentType);
        headers.set(HttpHeaderNames.CONTENT_LENGTH, Integer.toString(contentLength));

        // Close the non-keep-alive connection after the write operation is done.
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

}
