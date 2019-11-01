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
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http2.Http2ConnectionDecoder;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;
import io.netty.handler.codec.http2.Http2ConnectionHandler;
import io.netty.handler.codec.http2.Http2EventAdapter;
import io.netty.handler.codec.http2.Http2FrameListener;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.Http2Settings;
import io.netty.handler.codec.http2.Http2Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contractimpl.common.http2.Http2ExceptionHandler;
import org.wso2.transport.http.netty.contractimpl.listener.HttpServerChannelInitializer;
import org.wso2.transport.http.netty.internal.HttpTransportContextHolder;
import org.wso2.transport.http.netty.message.Http2DataFrame;
import org.wso2.transport.http.netty.message.Http2HeadersFrame;

import static org.wso2.transport.http.netty.contract.Constants.ZERO_READABLE_BYTES;
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
                Constants.HTTP_TRACE_LOG_HANDLER, Constants.HTTP_ACCESS_LOG_HANDLER, Constants.HTTP2_EXCEPTION_HANDLER);
        // Add HTTP2 Source handler
        Http2SourceHandler http2SourceHandler = new Http2SourceHandler(serverChannelInitializer, encoder, interfaceId,
                connection(), serverConnectorFuture, serverName);
        ctx.pipeline().addLast(Constants.HTTP2_SOURCE_HANDLER, http2SourceHandler);
        ctx.pipeline().addLast(Constants.HTTP2_EXCEPTION_HANDLER, new Http2ExceptionHandler(this));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Channel inactive event received in Http2SourceConnectionHandler");
        }
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
        private static final Logger LOG = LoggerFactory.getLogger(ServerFrameListener.class);

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
            return ZERO_READABLE_BYTES;
        }

        @Override
        public void onGoAwayReceived(int lastStreamId, long errorCode, ByteBuf debugData) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("GoAwayReceived event in server frame listener. Stream id : {} Error code : {}", lastStreamId,
                          errorCode);
            }
        }

        @Override
        public void onStreamClosed(Http2Stream stream) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("StreamClosed event in server frame listener. Stream id : {}", stream.id());
            }
        }

        @Override
        public void onRstStreamRead(ChannelHandlerContext ctx, int streamId, long errorCode) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("RstStreamRead event in server frame listener. Stream id : {} Error code : {}", streamId,
                          errorCode);
            }
        }
    }
}
