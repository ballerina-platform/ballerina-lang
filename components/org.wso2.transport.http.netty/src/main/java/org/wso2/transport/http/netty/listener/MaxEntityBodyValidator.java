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
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.ReferenceCounted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.Util;

import java.util.LinkedList;

/**
 * Responsible for validating request entity body size before sending it to the application.
 */
public class MaxEntityBodyValidator extends ChannelInboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(MaxEntityBodyValidator.class);

    private String serverName;
    private long maxEntityBodySize;
    private long currentSize;
    private HttpRequest inboundRequest;
    private LinkedList<HttpContent> fullContent;

    MaxEntityBodyValidator(String serverName, long maxEntityBodySize) {
        this.serverName = serverName;
        this.maxEntityBodySize = maxEntityBodySize;
        this.fullContent = new LinkedList<>();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (ctx.channel().isActive()) {
            if (msg instanceof HttpRequest) {
                inboundRequest = (HttpRequest) msg;
                if (isContentLengthInvalid(inboundRequest, maxEntityBodySize)) {
                    sendEntityTooLargeResponse(ctx);
                }
                ctx.channel().read();
            } else {
                HttpContent inboundContent = (HttpContent) msg;
                this.currentSize += inboundContent.content().readableBytes();
                this.fullContent.add(inboundContent);
                if (this.currentSize > maxEntityBodySize) {
                    sendEntityTooLargeResponse(ctx);
                } else {
                    if (msg instanceof LastHttpContent) {
                        super.channelRead(ctx, this.inboundRequest);
                        while (!this.fullContent.isEmpty()) {
                            super.channelRead(ctx, this.fullContent.pop());
                        }
                    } else {
                        ctx.channel().read();
                    }
                }
            }
        }
    }

    private void sendEntityTooLargeResponse(ChannelHandlerContext ctx) {
        Util.sendAndCloseNoEntityBodyResp(ctx, HttpResponseStatus.REQUEST_ENTITY_TOO_LARGE,
                inboundRequest.protocolVersion(), this.serverName);

        this.fullContent.forEach(ReferenceCounted::release);
        this.fullContent.forEach(httpContent -> this.fullContent.remove(httpContent));

        log.warn("Inbound request URI length exceeds the max uri length allowed for a request");
    }

    private boolean isContentLengthInvalid(HttpMessage start, long maxContentLength) {
        try {
            return HttpUtil.getContentLength(start, -1L) > (long) maxContentLength;
        } catch (NumberFormatException var4) {
            return false;
        }
    }
}
