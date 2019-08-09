/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.LastHttpContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static org.wso2.transport.http.netty.contract.Constants.TEXT_PLAIN;

/**
 * An initializer class for HTTP Server
 */
public class HalfResponseServerInitializer extends HttpServerInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(HalfResponseServerInitializer.class);

    private HttpRequest req;

    public HalfResponseServerInitializer() {}

    protected void addBusinessLogicHandler(Channel channel) {
        channel.pipeline().addLast("handler", new HalfResponseServerHandler());
    }

    private class HalfResponseServerHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof HttpRequest) {
                req = (HttpRequest) msg;
            } else if (msg instanceof LastHttpContent) {
                if (HttpUtil.is100ContinueExpected(req)) {
                    ctx.writeAndFlush(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));
                }

                ByteBuf content = Unpooled.wrappedBuffer("Test-Value".getBytes("UTF-8"));
                respond(ctx, content);
            }
        }

        private void respond(ChannelHandlerContext ctx, ByteBuf content) {
            HttpResponse response = getFullHttpResponse(content);

            boolean keepAlive = HttpUtil.isKeepAlive(req);
            if (!keepAlive) {
                ctx.writeAndFlush(response);
                ctx.writeAndFlush(new DefaultHttpContent(content)).addListener(ChannelFutureListener.CLOSE);
                LOG.debug("Writing response with data to client-connector");
                LOG.debug("Closing the client-connector connection");
            } else {
                response.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
                ctx.writeAndFlush(response);
                ctx.writeAndFlush(new DefaultHttpContent(content));
                LOG.debug("Writing response with data to client-connector");
            }
        }

        private HttpResponse getFullHttpResponse(ByteBuf content) {
            HttpResponseStatus httpResponseStatus = new HttpResponseStatus(HttpResponseStatus.OK.code(),
                                                                           HttpResponseStatus.OK.reasonPhrase());
            HttpResponse response = new DefaultHttpResponse(HTTP_1_1, httpResponseStatus);
            response.headers().set(CONTENT_TYPE, TEXT_PLAIN);
            // Make the content length longer than the actual payload length
            response.headers().set(CONTENT_LENGTH, content.readableBytes() + 10);
            return response;
        }
    }
}
