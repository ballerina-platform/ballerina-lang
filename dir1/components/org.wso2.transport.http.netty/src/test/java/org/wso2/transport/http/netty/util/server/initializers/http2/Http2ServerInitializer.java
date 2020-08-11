/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.transport.http.netty.util.server.initializers.http2;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerUpgradeHandler;
import io.netty.handler.codec.http.HttpServerUpgradeHandler.UpgradeCodecFactory;
import io.netty.handler.codec.http2.CleartextHttp2ServerUpgradeHandler;
import io.netty.handler.codec.http2.Http2CodecUtil;
import io.netty.handler.codec.http2.Http2ConnectionHandler;
import io.netty.handler.codec.http2.Http2FrameCodecBuilder;
import io.netty.handler.codec.http2.Http2ServerUpgradeCodec;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.ApplicationProtocolNegotiationHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.util.AsciiString;


/**
 * An initializer class for a Http2 Server.
 */
public abstract class Http2ServerInitializer extends ChannelInitializer<SocketChannel> {

    private SslContext sslContext;

    private final UpgradeCodecFactory upgradeCodecFactory = protocol -> {
        if (AsciiString.contentEquals(Http2CodecUtil.HTTP_UPGRADE_PROTOCOL_NAME, protocol)) {
            if (getBusinessLogicHandlerViaBuiler() != null) {
                return new Http2ServerUpgradeCodec(getBusinessLogicHandlerViaBuiler());
            }
            return new Http2ServerUpgradeCodec(Http2FrameCodecBuilder.forServer().build(),
                                               getBusinessLogicHandler());

        }
        return null;
    };

    @Override
    public void initChannel(SocketChannel ch) {
        if (sslContext != null) {
            configureSsl(ch);
        } else {
            configureClearText(ch);
        }
    }

    /**
     * Configure the pipeline for a cleartext upgrade from HTTP to HTTP/2.0
     *
     * @param ch represents the socket channel
     */
    private void configureClearText(SocketChannel ch) {
        final HttpServerCodec sourceCodec = new HttpServerCodec();
        final HttpServerUpgradeHandler upgradeHandler = new HttpServerUpgradeHandler(sourceCodec, upgradeCodecFactory,
                                                                                     Integer.MAX_VALUE);
        final CleartextHttp2ServerUpgradeHandler cleartextHttp2ServerUpgradeHandler =
                new CleartextHttp2ServerUpgradeHandler(sourceCodec, upgradeHandler,
                                                       getBusinessLogicHandler());
        final ChannelPipeline channelPipeline = ch.pipeline();
        channelPipeline.addLast(cleartextHttp2ServerUpgradeHandler);
    }

    /**
     * Configure the pipeline for TLS NPN negotiation to HTTP/2.
     *
     * @param ch represents the socket channel
     */
    private void configureSsl(SocketChannel ch) {
        ch.pipeline().addLast(sslContext.newHandler(ch.alloc()), new Http2PipelineConfiguratorForServer());
    }

    public void setSslContext(SslContext sslContext) {
        this.sslContext = sslContext;
    }

    protected abstract ChannelHandler getBusinessLogicHandler();

    protected abstract Http2ConnectionHandler getBusinessLogicHandlerViaBuiler();

    /**
     * Handler which handles ALPN.
     */
    class Http2PipelineConfiguratorForServer extends ApplicationProtocolNegotiationHandler {

        Http2PipelineConfiguratorForServer() {
            super(ApplicationProtocolNames.HTTP_1_1);
        }

        /**
         * Configure pipeline after SSL handshake.
         *
         * @param ctx      the channel handler context
         * @param protocol the negotiated protocol
         */
        @Override
        protected void configurePipeline(ChannelHandlerContext ctx, String protocol) {
            if (ApplicationProtocolNames.HTTP_2.equals(protocol)) {
                // handles pipeline for HTTP/2 requests after SSL handshake
                ctx.pipeline().addLast(getBusinessLogicHandler());
            } else {
                throw new IllegalStateException("unknown protocol: " + protocol);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            if (ctx != null && ctx.channel().isActive()) {
                ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
            }
        }
    }
}
