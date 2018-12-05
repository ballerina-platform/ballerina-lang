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
package org.wso2.transport.http.netty.contractimpl.listener.http2;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http2.Http2ConnectionDecoder;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;
import io.netty.handler.codec.http2.Http2ConnectionHandler;
import io.netty.handler.codec.http2.Http2EventAdapter;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.Http2FrameListener;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.Http2Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contractimpl.listener.HttpServerChannelInitializer;
import org.wso2.transport.http.netty.internal.HttpTransportContextHolder;
import org.wso2.transport.http.netty.message.Http2DataFrame;
import org.wso2.transport.http.netty.message.Http2HeadersFrame;

import static io.netty.handler.codec.http2.Http2CodecUtil.getEmbeddedHttp2Exception;
import static org.wso2.transport.http.netty.contractimpl.common.Util.safelyRemoveHandlers;

/**
 * {@code Http2SourceConnectionHandler} takes care of handling HTTP/2 source connections.
 */
public class Http2SourceConnectionHandler extends Http2ConnectionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(Http2SourceConnectionHandler.class);

    private Http2FrameListener http2FrameListener;
    private Http2ConnectionEncoder encoder;
    private String interfaceId;
    private ServerConnectorFuture serverConnectorFuture;
    private String serverName;
    private HttpServerChannelInitializer serverChannelInitializer;

    Http2SourceConnectionHandler(HttpServerChannelInitializer serverChannelInitializer,
                                 Http2ConnectionDecoder decoder, Http2ConnectionEncoder encoder,
                                 Http2Settings initialSettings,
                                 String interfaceId,
                                 ServerConnectorFuture serverConnectorFuture,
                                 String serverName) {
        super(decoder, encoder, initialSettings);
        this.serverChannelInitializer = serverChannelInitializer;
        this.encoder = encoder;
        this.interfaceId = interfaceId;
        this.serverConnectorFuture = serverConnectorFuture;
        this.serverName = serverName;
        http2FrameListener = new ServerFrameListener();
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);
        // Remove unwanted handlers after upgrade
        safelyRemoveHandlers(ctx.pipeline(), Constants.HTTP2_TO_HTTP_FALLBACK_HANDLER, Constants.HTTP_COMPRESSOR,
                Constants.HTTP_TRACE_LOG_HANDLER, Constants.HTTP_ACCESS_LOG_HANDLER);
        // Add HTTP2 Source handler
        Http2SourceHandler http2SourceHandler = new Http2SourceHandler(serverChannelInitializer, encoder, interfaceId,
                connection(), serverConnectorFuture, serverName);
        ctx.pipeline().addLast(Constants.HTTP2_SOURCE_HANDLER, http2SourceHandler);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (ctx != null && ctx.channel().isActive()) {
            ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void onError(ChannelHandlerContext ctx, Throwable cause) {
        Http2Exception embedded = getEmbeddedHttp2Exception(cause);
        if (embedded instanceof Http2Exception.ClosedStreamCreationException) {
            // We will end up here if we try to write to a already rejected stream
            LOG.warn("Stream creation failed, {}, {}", Constants.PROMISED_STREAM_REJECTED_ERROR, embedded.getMessage());
        } else {
            super.onError(ctx, cause);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        ctx.close();
        if (HttpTransportContextHolder.getInstance().getHandlerExecutor() != null) {
            HttpTransportContextHolder.getInstance().getHandlerExecutor()
                    .executeAtSourceConnectionTermination(Integer.toString(ctx.hashCode()));
        }
        ctx.fireChannelInactive();
    }

    /**
     * Gets the listener which listen to the HTTP/2 frames.
     *
     * @return the {@code Http2FrameListener} which listens for the HTTP/2 frames
     */
    Http2FrameListener getHttp2FrameListener() {
        return http2FrameListener;
    }

    /**
     * {@code ServerFrameListener} listens to HTTP/2 Events received from the frontend and constructs HTTP/2 frames.
     */
    private static class ServerFrameListener extends Http2EventAdapter {

        @Override
        public void onHeadersRead(ChannelHandlerContext ctx, int streamId,
                                  Http2Headers headers, int padding, boolean endOfStream) {
            Http2HeadersFrame http2HeadersFrame = new Http2HeadersFrame(streamId, headers, endOfStream);
            ctx.fireChannelRead(http2HeadersFrame);
        }

        @Override
        public void onHeadersRead(ChannelHandlerContext ctx, int streamId, Http2Headers headers, int streamDependency,
                                  short weight, boolean exclusive, int padding, boolean endOfStream) {
            onHeadersRead(ctx, streamId, headers, padding, endOfStream);
        }

        @Override
        public int onDataRead(ChannelHandlerContext ctx, int streamId, ByteBuf data, int padding, boolean endOfStream) {
            int readableBytes = data.readableBytes();
            ByteBuf forwardedData = data.copy();
            data.skipBytes(readableBytes);
            Http2DataFrame dataFrame = new Http2DataFrame(streamId, forwardedData, endOfStream);
            ctx.fireChannelRead(dataFrame);
            return readableBytes + padding;
        }
    }
}
