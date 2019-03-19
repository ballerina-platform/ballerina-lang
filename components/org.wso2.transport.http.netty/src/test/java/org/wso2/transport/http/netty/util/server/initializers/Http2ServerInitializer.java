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

package org.wso2.transport.http.netty.util.server.initializers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerUpgradeHandler;
import io.netty.handler.codec.http.HttpServerUpgradeHandler.UpgradeCodecFactory;
import io.netty.handler.codec.http2.CleartextHttp2ServerUpgradeHandler;
import io.netty.handler.codec.http2.Http2CodecUtil;
import io.netty.handler.codec.http2.Http2ConnectionHandler;
import io.netty.handler.codec.http2.Http2ServerUpgradeCodec;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.ApplicationProtocolNegotiationHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.util.AsciiString;

import static io.netty.buffer.Unpooled.unreleasableBuffer;
import static io.netty.handler.codec.http2.Http2CodecUtil.connectionPrefaceBuf;


/**
 * An initializer class for a Http2 Server.
 */
public abstract class Http2ServerInitializer extends ChannelInitializer<SocketChannel> {

    private SslContext sslContext;

  /*  private final UpgradeCodecFactory upgradeCodecFactory = protocol -> {
        if (AsciiString.contentEquals(Http2CodecUtil.HTTP_UPGRADE_PROTOCOL_NAME, protocol)) {
            return new Http2ServerUpgradeCodec(
                Http2FrameCodecBuilder.forServer().build(), getBusinessLogicHandler());
        } else {
            return null;
        }
    };*/

/*
    private static final UpgradeCodecFactory upgradeCodecFactory = new UpgradeCodecFactory() {
        @Override
        public HttpServerUpgradeHandler.UpgradeCodec newUpgradeCodec(CharSequence protocol) {
            if (AsciiString.contentEquals(Http2CodecUtil.HTTP_UPGRADE_PROTOCOL_NAME, protocol)) {
                return new Http2ServerUpgradeCodec(Http2FrameCodecBuilder.forServer().build(),
                getBusinessLogicHandler());
            } else {
                return null;
            }
        }
    };*/

    private final UpgradeCodecFactory upgradeCodecFactory = protocol -> {
        if (AsciiString.contentEquals(Http2CodecUtil.HTTP_UPGRADE_PROTOCOL_NAME, protocol)) {
            return new Http2ServerUpgradeCodec(getH2BusinessLogicHandler());
        } else {
            return null;
        }
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
       /* final ChannelPipeline p = ch.pipeline();
        final HttpServerCodec sourceCodec = new HttpServerCodec();
        p.addLast(new PriorKnowledgeHandler());
        p.addLast(sourceCodec);
        p.addLast(new HttpServerUpgradeHandler(sourceCodec, upgradeCodecFactory, Integer.MAX_VALUE));*/

        final HttpServerCodec sourceCodec = new HttpServerCodec();
        final HttpServerUpgradeHandler upgradeHandler = new HttpServerUpgradeHandler(sourceCodec, upgradeCodecFactory);
        final CleartextHttp2ServerUpgradeHandler cleartextHttp2ServerUpgradeHandler =
            new CleartextHttp2ServerUpgradeHandler(sourceCodec, upgradeHandler,
                                                   getBusinessLogicHandler());
        final ChannelPipeline p = ch.pipeline();
        p.addLast(cleartextHttp2ServerUpgradeHandler);
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

    protected abstract Http2ConnectionHandler getH2BusinessLogicHandler();

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
//                ctx.pipeline().addLast(Http2FrameCodecBuilder.forServer().build(), getBusinessLogicHandler());
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

    /**
     * Peek inbound message to determine current connection wants to start HTTP/2 by HTTP upgrade or prior knowledge
     */
    private final class PriorKnowledgeHandler extends ChannelInboundHandlerAdapter {
        private final ByteBuf CONNECTION_PREFACE = unreleasableBuffer(connectionPrefaceBuf());

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            if (msg instanceof ByteBuf) {
                ByteBuf inputData = (ByteBuf) msg;
                int prefaceLength = CONNECTION_PREFACE.readableBytes();
                int bytesRead = Math.min(inputData.readableBytes(), prefaceLength);

                if (!ByteBufUtil.equals(CONNECTION_PREFACE, CONNECTION_PREFACE.readerIndex(),
                                        inputData, inputData.readerIndex(), bytesRead)) {
                    ctx.pipeline().remove(this);
                } else if (bytesRead == prefaceLength) {
                    // Full h2 preface match, removed source codec, using http2 codec to handle
                    // following network traffic
//                safelyRemoveHandlers(ctx.pipeline(), Constants.HTTP_SERVER_CODEC, "HttpServerUpgradeHandler");
                /*ctx.pipeline()
                    .remove(httpServerCodec)
                    .remove(httpServerUpgradeHandler);

                ctx.pipeline().addAfter(ctx.name(), null, http2ServerHandler);*/
                    ctx.pipeline()
                        .remove("HttpServerCodec#0");
                    ctx.pipeline()
                        .remove("HttpServerUpgradeHandler#0");
//                    ctx.pipeline().addAfter();
                    ctx.pipeline().addAfter(ctx.name(), null, getBusinessLogicHandler());
//                ctx.pipeline().addLast(getBusinessLogicHandler());
                    ctx.pipeline().remove(this);
//                ReferenceCountUtil.release(in);
                }
            }

        }
    }
}
