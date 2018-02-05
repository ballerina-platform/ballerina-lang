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
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.LastHttpContent;
import org.wso2.transport.http.netty.config.RequestSizeValidationConfig;

/**
 * Responsible for validating the request before sending it to the application
 */
public class UriSizeValidator extends ChannelInboundHandlerAdapter {

    private RequestSizeValidationConfig requestSizeValidationConfig;

    private boolean isUriTooLarge = false;
    private HttpRequest inboundRequest;

    public UriSizeValidator(RequestSizeValidationConfig requestSizeValidationConfig) {
        this.requestSizeValidationConfig = requestSizeValidationConfig;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            inboundRequest = (HttpRequest) msg;
            if (inboundRequest.uri().getBytes().length > requestSizeValidationConfig.getUriMaxSize()) {
                isUriTooLarge = true;
            } else {
                super.channelRead(ctx, msg);
            }
        } else if (msg instanceof HttpContent) {
            HttpContent inboundRequestContent = (HttpContent) msg;
            if (isUriTooLarge) {
                inboundRequestContent.release();
                if (inboundRequestContent instanceof LastHttpContent) {
                    HttpResponse outboundResponse = new DefaultHttpResponse(inboundRequest.protocolVersion(),
                            HttpResponseStatus.REQUEST_URI_TOO_LONG);
                    ctx.channel().writeAndFlush(outboundResponse);
                }
            } else {
                super.channelRead(ctx, msg);
            }
        } else {
            super.channelRead(ctx, msg);
        }
    }
}
