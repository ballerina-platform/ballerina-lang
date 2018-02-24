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
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.HttpConversionUtil;
import io.netty.util.ReferenceCountUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.transport.http.netty.common.Util;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import java.util.Locale;

/**
 * Sending requests and receiving responses from a backend server
 */
public class ClientOutboundHandler extends ChannelOutboundHandlerAdapter {

    private static final Log log = LogFactory.getLog(ClientOutboundHandler.class);
    private Http2Connection connection;
    private Http2ConnectionEncoder encoder;
    private TargetChannel targetChannel;

    public ClientOutboundHandler(Http2Connection connection, Http2ConnectionEncoder encoder) {
        this.connection = connection;
        this.encoder = encoder;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof OutboundMsgHolder) {
            OutboundMsgHolder outboundMsgHolder = (OutboundMsgHolder) msg;
            new Http2RequestWriter(outboundMsgHolder).writeContent(ctx);
        } else {
            ctx.write(msg, promise);
        }
    }

    public Http2Connection getConnection() {
        return connection;
    }

    public void setTargetChannel(TargetChannel targetChannel) {
        this.targetChannel = targetChannel;
    }

    private synchronized int getStreamId() {
        return connection.local().incrementAndGetNextStreamId();
    }

    private class Http2RequestWriter {

        boolean isHeadersWritten = false;
        HTTPCarbonMessage httpOutboundRequest;
        OutboundMsgHolder outboundMsgHolder;

        public Http2RequestWriter(OutboundMsgHolder outboundMsgHolder) {
            this.outboundMsgHolder = outboundMsgHolder;
            httpOutboundRequest = outboundMsgHolder.getRequest();
        }

        public void writeContent(ChannelHandlerContext ctx) {
            int streamId = getStreamId();
            targetChannel.putInFlightMessage(streamId, outboundMsgHolder);

            // Write Content
            httpOutboundRequest.getHttpContentAsync().
                    setMessageListener((httpContent ->
                                                targetChannel.getChannel().eventLoop().execute(() -> {
                                                    try {
                                                        writeOutboundRequest(ctx, httpContent, streamId, true);
                                                    } catch (Exception exception) {
                                                        String errorMsg = "Failed to send the request : " +
                                                                          exception.getMessage().
                                                                                  toLowerCase(Locale.ENGLISH);
                                                        log.error(errorMsg, exception);
                                                    }
                                                })));

        }

        private void writeOutboundRequest(ChannelHandlerContext ctx, HttpContent msg, int streamId,
                                          boolean validateHeaders) {

            boolean endStream = false;
            if (!isHeadersWritten) {
                HttpRequest httpRequest = Util.createHttpRequest(httpOutboundRequest);

                if (msg instanceof LastHttpContent && msg.content().capacity() == 0) {
                    endStream = true;
                }
                // Write Headers
                writeOutboundRequestHeaders(ctx, httpRequest, streamId, true, endStream);
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
                    http2Trailers = HttpConversionUtil.toHttp2Headers(trailers, validateHeaders);
                }

                // Write the data
                final ByteBuf content = msg.content();
                endStream = isLastContent && trailers.isEmpty();
                release = false;
                encoder.writeData(ctx, streamId, content, 0, endStream,
                                  ctx.newPromise());
                try {
                    encoder.flowController().writePendingBytes();
                } catch (Http2Exception e) {
                }
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
                                                 boolean validateHeaders, boolean endStream) {
            // Convert and write the headers.
            httpMsg.headers().add(HttpConversionUtil.ExtensionHeaderNames.SCHEME.text(), "HTTP");

            Http2Headers http2Headers = HttpConversionUtil.toHttp2Headers(httpMsg, validateHeaders);
            writeHttp2Headers(ctx, streamId, httpMsg.headers(), http2Headers, endStream);
        }

        private void writeHttp2Headers(ChannelHandlerContext ctx, int streamId, HttpHeaders headers,
                                       Http2Headers http2Headers, boolean endStream) {
            int dependencyId = headers.getInt(
                    HttpConversionUtil.ExtensionHeaderNames.STREAM_DEPENDENCY_ID.text(), 0);
            short weight = headers.getShort(
                    HttpConversionUtil.ExtensionHeaderNames.STREAM_WEIGHT.text(),
                    Http2CodecUtil.DEFAULT_PRIORITY_WEIGHT);
            encoder.writeHeaders(ctx, streamId, http2Headers, dependencyId, weight, false,
                                 0, endStream, ctx.newPromise());
            try {
                encoder.flowController().writePendingBytes();
                ctx.flush();
            } catch (Http2Exception e) {
            }
        }
    }

}
