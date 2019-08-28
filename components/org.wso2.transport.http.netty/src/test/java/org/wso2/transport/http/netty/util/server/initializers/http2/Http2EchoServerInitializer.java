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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http2.DefaultHttp2DataFrame;
import io.netty.handler.codec.http2.DefaultHttp2Headers;
import io.netty.handler.codec.http2.DefaultHttp2HeadersFrame;
import io.netty.handler.codec.http2.DefaultHttp2WindowUpdateFrame;
import io.netty.handler.codec.http2.Http2ConnectionHandler;
import io.netty.handler.codec.http2.Http2DataFrame;
import io.netty.handler.codec.http2.Http2FrameStream;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.Http2HeadersFrame;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static io.netty.buffer.Unpooled.unreleasableBuffer;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

/**
 * For requests with entity bodies this will send a echo response and for non entity body requests this
 * will simply send a "Hello" as the response.
 */
public class Http2EchoServerInitializer extends Http2ServerInitializer {

    @Override
    protected ChannelHandler getBusinessLogicHandler() {
        return new EchoHandler();
    }

    @Override
    protected Http2ConnectionHandler getBusinessLogicHandlerViaBuiler() {
        return null;
    }

    private class EchoHandler extends ChannelDuplexHandler {

        final ByteBuf responseBytes = unreleasableBuffer(copiedBuffer("Hello..!", CharsetUtil.UTF_8));
        private final Logger log = LoggerFactory.getLogger(EchoHandler.class);

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            super.exceptionCaught(ctx, cause);
            log.error(cause.getMessage());
            ctx.close();
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof Http2HeadersFrame) {
                onHeadersRead(ctx, (Http2HeadersFrame) msg);
            } else if (msg instanceof Http2DataFrame) {
                onDataRead(ctx, (Http2DataFrame) msg);
            } else {
                super.channelRead(ctx, msg);
            }
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.flush();
        }

        private void onDataRead(ChannelHandlerContext ctx, Http2DataFrame data) throws Exception {
            Http2FrameStream stream = data.stream();

            if (data.isEndStream()) {
                sendResponse(ctx, stream, data.content());
            } else {
                data.release();
            }
            ctx.write(new DefaultHttp2WindowUpdateFrame(data.initialFlowControlledBytes()).stream(stream));
        }

        private void onHeadersRead(ChannelHandlerContext ctx, Http2HeadersFrame headers)
                throws Exception {
            if (headers.isEndStream()) {
                ByteBuf content = ctx.alloc().buffer();
                content.writeBytes(responseBytes.duplicate());
                ByteBufUtil.writeAscii(content, " - via HTTP/2");
                sendResponse(ctx, headers.stream(), content);
            }
        }

        private void sendResponse(ChannelHandlerContext ctx, Http2FrameStream stream, ByteBuf payload) {
            // Send a frame for the response status
            Http2Headers headers = new DefaultHttp2Headers().status(OK.codeAsText());
            ctx.write(new DefaultHttp2HeadersFrame(headers).stream(stream));
            ctx.write(new DefaultHttp2DataFrame(payload, true).stream(stream));
        }
    }
}
