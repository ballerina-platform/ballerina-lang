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

package org.wso2.transport.http.netty.listener;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.Constants;

/**
 * Responsible for validating the request before sending it to the application
 */
public class UriLengthValidator extends ChannelInboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(HTTPServerChannelInitializer.class);

    private String serverName;

    UriLengthValidator(String serverName) {
        this.serverName = serverName;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            HttpRequest inboundRequest = (HttpRequest) msg;
            Throwable cause = inboundRequest.decoderResult().cause();
            if (cause instanceof TooLongFrameException) {
                if (cause.getMessage().contains("header")) {
                    HttpResponse outboundResponse = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
                            HttpResponseStatus.REQUEST_ENTITY_TOO_LARGE);
                    outboundResponse.headers().set(Constants.HTTP_SERVER_HEADER, serverName);
                    outboundResponse.headers().set(Constants.HTTP_CONTENT_LENGTH, 0);
                    outboundResponse.headers().set(Constants.HTTP_CONNECTION, Constants.CONNECTION_CLOSE);
                    ctx.channel().writeAndFlush(outboundResponse);
                    ctx.channel().close();
                    log.warn("Inbound request Entity exceeds the max entity size allowed for a request");
                } else {
                    HttpResponse outboundResponse = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
                            HttpResponseStatus.REQUEST_URI_TOO_LONG);
                    outboundResponse.headers().set(Constants.HTTP_SERVER_HEADER, serverName);
                    outboundResponse.headers().set(Constants.HTTP_CONTENT_LENGTH, 0);
                    ctx.channel().writeAndFlush(outboundResponse);
                    log.warn("Inbound request URI length exceeds the max uri length allowed for a request");
                }
            } else {
                super.channelRead(ctx, msg);
            }
        } else {
            super.channelRead(ctx, msg);
        }
    }
}
