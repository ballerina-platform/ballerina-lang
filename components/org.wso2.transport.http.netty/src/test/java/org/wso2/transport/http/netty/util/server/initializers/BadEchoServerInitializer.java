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

package org.wso2.transport.http.netty.util.server.initializers;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.LastHttpContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderNames.TRANSFER_ENCODING;
import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * An initializer class for HTTP Server.
 */
public class BadEchoServerInitializer extends HTTPServerInitializer {

    private static final Logger logger = LoggerFactory.getLogger(BadEchoServerInitializer.class);

    protected void addBusinessLogicHandler(Channel channel) {
        channel.pipeline().addLast("handler", new EchoServerHandler());
    }

    private class EchoServerHandler extends ChannelInboundHandlerAdapter {

        private HttpResponse httpResponse;
        private int responseStatusCode = 200;
        private boolean chunked = true;
        private long contentLength = 0;
        private boolean keepAlive = false;
        private BlockingQueue<HttpContent> content = new LinkedBlockingQueue<>();

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof HttpRequest) {
                HttpRequest req = (HttpRequest) msg;

                if (HttpUtil.is100ContinueExpected(req)) {
                    ctx.write(new DefaultHttpResponse(HTTP_1_1, CONTINUE));
                }

                createHttpResponse(req);
                setContentType(req);
                checkAndSetEncodingHeader(req);
                setConnectionKeepAliveHeader(req);
                keepAlive = HttpUtil.isKeepAlive(req);

                if (chunked) {
                    ctx.writeAndFlush(httpResponse);
                }
            } else if (msg instanceof HttpContent) {
                if (msg instanceof LastHttpContent) {
                    if (chunked) {
                        ctx.writeAndFlush(msg);
                    } else {
                        writeHeadersAndEntity(ctx);
                    }

                    if (!keepAlive) {
                        ctx.close();
                        logger.debug("Closing the client connection");
                    }
                    resetState();
                } else {
                    if (chunked) {
                        ctx.writeAndFlush(msg);
                        logger.debug("Writing content to client connection");
                    } else {
                        collectContent((HttpContent) msg);
                        logger.debug("Collecting content from client connection");
                    }
                }
            }
        }

        private void resetState() {
            contentLength = 0;
            content.clear();
            keepAlive = false;
            chunked = true;
        }

        private void writeHeadersAndEntity(ChannelHandlerContext ctx) {
            // Here we need to close connection after reading the request to
            // produce the bad behaviour
            ctx.channel().close();
        }

        private void collectContent(HttpContent msg) {
            HttpContent httpContent = msg;
            contentLength += httpContent.content().readableBytes();
            content.add(msg);
        }

        private void checkAndSetEncodingHeader(HttpRequest req) {
            if (req.headers().get(HttpHeaderNames.CONTENT_LENGTH) != null) {
                chunked = false;
            } else {
                httpResponse.headers().set(TRANSFER_ENCODING, "chunked");
            }
        }

        private void setContentType(HttpRequest req) {
            String contentType = req.headers().get(CONTENT_TYPE);
            if (contentType != null) {
                httpResponse.headers().set(CONTENT_TYPE, contentType);
            }
        }

        private void createHttpResponse(HttpRequest req) {
            HttpResponseStatus httpResponseStatus = new HttpResponseStatus(responseStatusCode,
                    HttpResponseStatus.valueOf(responseStatusCode).reasonPhrase());
            httpResponse = new DefaultHttpResponse(req.protocolVersion(), httpResponseStatus);
        }

        private void setConnectionKeepAliveHeader(HttpRequest req) {
            boolean keepAlive = HttpUtil.isKeepAlive(req);
            if (keepAlive) {
                httpResponse.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
                logger.debug("Setting connection keep-alive header");
            }
        }
    }
}
