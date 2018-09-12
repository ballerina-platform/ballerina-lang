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
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.LastHttpContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderNames.LOCATION;
import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * An intializer for redirect server
 */
public class RedirectServerInitializer extends HttpServerInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(MockServerInitializer.class);

    private String stringContent;
    private String contentType;
    private int responseStatusCode;
    private HttpRequest req;
    private String location;
    private long delay;

    protected void addBusinessLogicHandler(Channel channel) {
        channel.pipeline().addLast("handler", new RedirectServerInitializer.RedirectServerHandler());
    }

    public RedirectServerInitializer(String stringContent, String contentType, int responseStatusCode, String location,
            long delay) {
        this.stringContent = stringContent;
        this.contentType = contentType;
        this.responseStatusCode = responseStatusCode;
        this.location = location;
        this.delay = delay;
    }

    private class RedirectServerHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

            if (stringContent != null) {
                ByteBuf content = Unpooled.wrappedBuffer(stringContent.getBytes("UTF-8"));
                if (msg instanceof HttpRequest) {
                    req = (HttpRequest) msg;
                } else if (msg instanceof LastHttpContent) {
                    if (HttpUtil.is100ContinueExpected(req)) {
                        ctx.writeAndFlush(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));
                    }
                    boolean keepAlive = HttpUtil.isKeepAlive(req);
                    HttpResponseStatus httpResponseStatus = new HttpResponseStatus(responseStatusCode,
                            HttpResponseStatus.valueOf(responseStatusCode).reasonPhrase());
                    FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, httpResponseStatus, content);
                    response.headers().set(CONTENT_TYPE, contentType);
                    response.headers().set(CONTENT_LENGTH, content.readableBytes());

                    if (location != null) {
                        response.headers().set(LOCATION, location);
                    }

                    if (!keepAlive) {
                        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
                        LOG.debug("Writing response with data to client-connector");
                        LOG.debug("Closing the client-connector connection");
                    } else {
                        response.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
                        ctx.executor().schedule(() -> ctx.writeAndFlush(response), delay, TimeUnit.MILLISECONDS);
                        LOG.debug("Writing response with data to client-connector");
                    }
                }
            }
        }
    }
}
