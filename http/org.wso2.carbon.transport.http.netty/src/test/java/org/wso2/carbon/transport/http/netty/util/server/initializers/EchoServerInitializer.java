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

package org.wso2.carbon.transport.http.netty.util.server.initializers;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.LastHttpContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaders.Names.TRANSFER_ENCODING;
import static io.netty.handler.codec.http.HttpHeaders.is100ContinueExpected;
import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * An initializer class for HTTP Server
 */
public class EchoServerInitializer extends HTTPServerInitializer {

    private static final Logger logger = LoggerFactory.getLogger(EchoServerInitializer.class);

    protected void addBusinessLogicHandler(Channel channel) {
        channel.pipeline().addLast("handler", new EchoServerHandler());
    }

    private class EchoServerHandler extends ChannelInboundHandlerAdapter {

        private int responseStatusCode = 200;

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
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
                    ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
                    logger.debug("Writing response to client-connector");
                } else {
                    response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
                    ctx.writeAndFlush(response);
                    logger.debug("Writing response to client-connector");
                }
            } else if (msg instanceof HttpContent) {
                if (msg instanceof LastHttpContent) {
                    ctx.writeAndFlush(msg);
                    ctx.close();
                    logger.debug("Closing the client-connector connection");
                } else {
                    ctx.writeAndFlush(msg);
                    logger.debug("Writing data to client-connector");
                }
            }
        }
    }
}
