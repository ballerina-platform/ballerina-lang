/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.transport.http.netty.util.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.LastHttpContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaders.Names.TRANSFER_ENCODING;
import static io.netty.handler.codec.http.HttpHeaders.is100ContinueExpected;
import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * HTTP Server Handler
 */
public class HTTPServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(HTTPServerHandler.class);

    private ByteBuf content;
    private String contentType;
    private int responseStatusCode = 200;
    private HttpRequest req;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        if (content != null) {
            if (msg instanceof HttpRequest) {
                req = (HttpRequest) msg;
            } else if (msg instanceof LastHttpContent) {
                if (is100ContinueExpected(req)) {
                    ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));
                }
                boolean keepAlive = isKeepAlive(req);
                HttpResponseStatus httpResponseStatus = new HttpResponseStatus(responseStatusCode,
                        HttpResponseStatus.valueOf(responseStatusCode).reasonPhrase());
                FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, httpResponseStatus, content);
                response.headers().set(CONTENT_TYPE, contentType);
                response.headers().set(CONTENT_LENGTH, content.readableBytes());

                if (!keepAlive) {
                    ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
                } else {
                    response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
                    ctx.writeAndFlush(response);
                }
            }

        } else {
            if (msg instanceof HttpRequest) {
                HttpRequest req = (HttpRequest) msg;

                if (is100ContinueExpected(req)) {
                    ctx.write(new DefaultHttpResponse(HTTP_1_1, CONTINUE));
                }
                boolean keepAlive = isKeepAlive(req);
                HttpResponseStatus httpResponseStatus = new HttpResponseStatus(responseStatusCode,
                        HttpResponseStatus.valueOf(responseStatusCode).reasonPhrase());

                HttpResponse response = new DefaultHttpResponse(req.getProtocolVersion(), httpResponseStatus);
                String contentType = req.headers().get(CONTENT_TYPE);
                if (contentType != null) {
                    response.headers().set(CONTENT_TYPE, contentType);
                }
                response.headers().set(TRANSFER_ENCODING, "chunked");

                if (!keepAlive) {
                    ctx.write(response).addListener(ChannelFutureListener.CLOSE);
                } else {
                    response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
                    ctx.write(response);
                }
            } else if (msg instanceof HttpContent) {
                if (msg instanceof LastHttpContent) {
                    ctx.writeAndFlush(msg);
                } else {
                    ctx.write(msg);
                }
            }
        }
    }

    public void setMessage(String message, String contentType) {
        if (message != null && contentType != null) {
            this.contentType = contentType;
            try {
                content = Unpooled.wrappedBuffer(message.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                logger.error("Encoding not supported", e);
            }
        } else {
            logger.debug("Please specify message and contentType ");
        }
    }

    public void setResponseStatusCode(int responseStatusCode) {
        this.responseStatusCode = responseStatusCode;
    }
}
