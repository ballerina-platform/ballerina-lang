/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.LastHttpContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * An initializer class for HTTP Server.
 */
public class SendChannelIDServerInitializer extends HttpServerInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(SendChannelIDServerInitializer.class);

    private int delay;
    private HttpRequest req;
    private AtomicInteger requestCount = new AtomicInteger(0);

    public SendChannelIDServerInitializer(int delay) {
        this.delay = delay;
    }

    protected void addBusinessLogicHandler(Channel channel) {
        channel.pipeline().addLast("handler", new MockServerHandler());
    }

    private class MockServerHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof HttpRequest) {
                req = (HttpRequest) msg;
            } else if (msg instanceof LastHttpContent) {
                boolean keepAlive = HttpUtil.isKeepAlive(req);
                int responseStatusCode = 200;

                FullHttpResponse response = createFullHttpResponse(ctx, responseStatusCode);

                if (!keepAlive) {
                    ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
                    LOG.debug("Writing response with data to client-connector");
                    LOG.debug("Closing the client-connector connection");
                } else {
                    response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
                    if (requestCount.get() < 1) {
                        // this need in order to simulate a delay
                        Thread.sleep(delay);
                    }
                    ctx.writeAndFlush(response);
                    requestCount.incrementAndGet();
                    LOG.debug("Writing response with data to client-connector");
                }
            }
        }

        public void channelInactive(ChannelHandlerContext ctx) {
            ctx.close();
            LOG.debug("Channel has become inactive hence closing the connection");
        }


        private FullHttpResponse createFullHttpResponse(ChannelHandlerContext ctx, int responseStatusCode) {
            HttpResponseStatus httpResponseStatus = new HttpResponseStatus(responseStatusCode,
                    HttpResponseStatus.valueOf(responseStatusCode).reasonPhrase());
            ByteBuf content =  Unpooled.wrappedBuffer(ctx.channel().id().asLongText().getBytes());
            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, httpResponseStatus, content);
            response.headers().set(CONTENT_TYPE, "plain/text");
            response.headers().set(CONTENT_LENGTH, content.readableBytes());
            return response;
        }
    }
}
