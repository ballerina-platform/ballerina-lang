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
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpRequest;
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

/**
 * An initializer class for HTTP Server.
 */
public class Send100ContinueAfterReceivingEntityBodyInitializer extends HttpServerInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(Send100ContinueAfterReceivingEntityBodyInitializer.class);

    private String stringContent;
    private String contentType;
    private int responseStatusCode;
    private HttpRequest req;
    private boolean continueSent;

    public Send100ContinueAfterReceivingEntityBodyInitializer() {
        this.stringContent = "inbound response entity body";
        this.contentType = "text/plain";
        this.responseStatusCode = HttpResponseStatus.OK.code();
        this.continueSent = false;
    }

    protected void addBusinessLogicHandler(Channel channel) {
        channel.pipeline().addLast("handler", new Send100ContinueAfterReceivingEntityBodyHandler());
    }

    private class Send100ContinueAfterReceivingEntityBodyHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

            if (stringContent != null) {
                if (msg instanceof HttpRequest) {
                    req = (HttpRequest) msg;
                } else if (msg instanceof HttpContent) {
                    if (msg instanceof LastHttpContent) {
                        ctx.writeAndFlush(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));
                        ByteBuf content = Unpooled.wrappedBuffer(stringContent.getBytes("UTF-8"));
                        respond(ctx, content);
                    }
                }
            }
        }

        private void respond(ChannelHandlerContext ctx, ByteBuf content) {
            FullHttpResponse response = getFullHttpResponse(content);

            boolean keepAlive = HttpUtil.isKeepAlive(req);
            if (!keepAlive) {
                ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
                LOG.debug("Writing response with data to client-connector");
                LOG.debug("Closing the client-connector connection");
            } else {
                response.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
                ctx.writeAndFlush(response);
                LOG.debug("Writing response with data to client-connector");
            }
        }

        private FullHttpResponse getFullHttpResponse(ByteBuf content) {
            HttpResponseStatus httpResponseStatus = new HttpResponseStatus(responseStatusCode,
                                                  HttpResponseStatus.valueOf(responseStatusCode).reasonPhrase());
            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, httpResponseStatus, content);
            response.headers().set(CONTENT_TYPE, contentType);
            response.headers().set(CONTENT_LENGTH, content.readableBytes());
            return response;
        }
    }
}
