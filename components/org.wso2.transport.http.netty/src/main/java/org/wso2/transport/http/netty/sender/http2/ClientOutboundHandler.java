/*
 *   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.transport.http.netty.sender.http2;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.EmptyHttpHeaders;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http2.EmptyHttp2Headers;
import io.netty.handler.codec.http2.Http2CodecUtil;
import io.netty.handler.codec.http2.Http2Connection;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;
import io.netty.handler.codec.http2.Http2Error;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.HttpConversionUtil;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.Util;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.transport.http.netty.message.Http2Reset;

import java.util.Locale;

/**
 * {@code ClientOutboundHandler} is responsible for writing HTTP/2 Frames to the backend service
 */
public class ClientOutboundHandler extends ChannelOutboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(ClientOutboundHandler.class);
    private Http2Connection connection;
    // Encoder associated with the HTTP2ConnectionHandler
    private Http2ConnectionEncoder encoder;
    private Http2ClientChannel http2ClientChannel;

    public ClientOutboundHandler(Http2Connection connection, Http2ConnectionEncoder encoder) {
        this.connection = connection;
        this.encoder = encoder;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof OutboundMsgHolder) {
            OutboundMsgHolder outboundMsgHolder = (OutboundMsgHolder) msg;
            new Http2RequestWriter(outboundMsgHolder).writeContent(ctx);
        } else if (msg instanceof Http2Reset) {
            Http2Reset resetMsg = (Http2Reset) msg;
            resetStream(ctx, resetMsg.getStreamId(), resetMsg.getError());
        } else {
            ctx.write(msg, promise); // let other types of objects to pass this handler
        }
    }

    /**
     * Get the associated {@code Http2Connection}
     *
     * @return the associated Http2Connection
     */
    public Http2Connection getConnection() {
        return connection;
    }

    /**
     * Set the {@code TargetChannel}
     *
     * @param http2ClientChannel TargetChannel related to the handler
     */
    public void setHttp2ClientChannel(Http2ClientChannel http2ClientChannel) {
        this.http2ClientChannel = http2ClientChannel;
    }

    /**
     * Get the next available stream id
     *
     * @return next available stream id
     */
    private synchronized int getStreamId() {
        return connection.local().incrementAndGetNextStreamId();
    }

    /**
     * This is used to write Http2 content to the connection
     */
    private class Http2RequestWriter {

        // whether headers are written already
        boolean isHeadersWritten = false;
        HTTPCarbonMessage httpOutboundRequest;
        OutboundMsgHolder outboundMsgHolder;

        public Http2RequestWriter(OutboundMsgHolder outboundMsgHolder) {
            this.outboundMsgHolder = outboundMsgHolder;
            httpOutboundRequest = outboundMsgHolder.getRequest();
        }

        public void writeContent(ChannelHandlerContext ctx) {
            int streamId = getStreamId();
            http2ClientChannel.putInFlightMessage(streamId, outboundMsgHolder);

            // Write Content
            httpOutboundRequest.getHttpContentAsync().
                    setMessageListener((httpContent ->
                                                http2ClientChannel.getChannel().eventLoop().execute(() -> {
                                                    try {
                                                        writeOutboundRequest(
                                                                ctx, httpContent, streamId);
                                                    } catch (Exception exception) {
                                                        String errorMsg = "Failed to send the request : " +
                                                                          exception.getMessage().
                                                                                  toLowerCase(Locale.ENGLISH);
                                                        log.error(errorMsg, exception);
                                                    }
                                                })));
        }

        private void writeOutboundRequest(ChannelHandlerContext ctx, HttpContent msg, int streamId)
                throws Http2Exception {

            boolean endStream = false;
            if (!isHeadersWritten) {
                HttpRequest httpRequest = Util.createHttpRequest(httpOutboundRequest);

                if (msg instanceof LastHttpContent && msg.content().capacity() == 0) {
                    endStream = true;
                }
                // Write Headers
                writeOutboundRequestHeaders(ctx, httpRequest, streamId, endStream);
                isHeadersWritten = true;
                if (endStream) {
                    return;
                }
            }

            boolean release = true;
            try {
                boolean isLastContent = false;
                HttpHeaders trailers = EmptyHttpHeaders.INSTANCE;
                Http2Headers http2Trailers = EmptyHttp2Headers.INSTANCE;
                if (msg instanceof LastHttpContent) {
                    isLastContent = true;

                    // Convert any trailing headers.
                    final LastHttpContent lastContent = (LastHttpContent) msg;
                    trailers = lastContent.trailingHeaders();
                    http2Trailers = HttpConversionUtil.toHttp2Headers(trailers, true);
                }

                // Write the data
                final ByteBuf content = msg.content();
                endStream = isLastContent && trailers.isEmpty();
                release = false;
                encoder.writeData(ctx, streamId, content, 0, endStream, ctx.newPromise());
                encoder.flowController().writePendingBytes();
                ctx.flush();

                if (!trailers.isEmpty()) {
                    // Write trailing headers.
                    writeHttp2Headers(ctx, streamId, trailers, http2Trailers, true);
                }
            } finally {
                if (release) {
                    ReferenceCountUtil.release(msg);
                }
            }
        }

        private void writeOutboundRequestHeaders(ChannelHandlerContext ctx, HttpMessage httpMsg, int streamId,
                                                 boolean endStream) throws Http2Exception {
            // Convert and write the headers.
            httpMsg.headers().add(HttpConversionUtil.ExtensionHeaderNames.SCHEME.text(), "HTTP");
            Http2Headers http2Headers = HttpConversionUtil.toHttp2Headers(httpMsg, true);
            writeHttp2Headers(ctx, streamId, httpMsg.headers(), http2Headers, endStream);
        }

        private void writeHttp2Headers(ChannelHandlerContext ctx, int streamId, HttpHeaders headers,
                                       Http2Headers http2Headers, boolean endStream) throws Http2Exception {
            int dependencyId = headers.getInt(
                    HttpConversionUtil.ExtensionHeaderNames.STREAM_DEPENDENCY_ID.text(), 0);
            short weight = headers.getShort(HttpConversionUtil.ExtensionHeaderNames.STREAM_WEIGHT.text(),
                                            Http2CodecUtil.DEFAULT_PRIORITY_WEIGHT);
            encoder.writeHeaders(
                    ctx, streamId, http2Headers, dependencyId, weight, false, 0, endStream, ctx.newPromise());
            encoder.flowController().writePendingBytes();
            ctx.flush();
        }
    }

    /**
     * Terminate a stream
     *
     * @param streamId stream to be terminated
     * @param http2Error  Error code
     */
    private void resetStream(ChannelHandlerContext ctx, int streamId, Http2Error http2Error) {
        encoder.writeRstStream(ctx, streamId, http2Error.code(), ctx.newPromise());
        ctx.flush();
    }

}
