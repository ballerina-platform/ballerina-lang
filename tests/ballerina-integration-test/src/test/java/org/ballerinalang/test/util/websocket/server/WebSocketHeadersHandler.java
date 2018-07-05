/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.test.util.websocket.server;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple Handler for testing the support for custom headers by WebSocket client and server.
 * The class is a {@link ChannelDuplexHandler} and returns the request headers for the request and sets a new header
 * to the response.
 */
public class WebSocketHeadersHandler extends ChannelDuplexHandler {

    private static final Logger log = LoggerFactory.getLogger(WebSocketHeadersHandler.class);
    private HttpHeaders requestHeaders;

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.debug("channel is active");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.debug("channel is inactive");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest request = (FullHttpRequest) msg;
            requestHeaders = request.headers();
        }
        super.channelRead(ctx, msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Exception Caught: " + cause.getMessage());
        ctx.pipeline().remove(this);
        ctx.fireExceptionCaught(cause);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof HttpResponse) {
            HttpResponse response = (HttpResponse) msg;
            response.headers().add("X-server-header", "server-header-value");
            promise.addListener(future -> ctx.pipeline().remove(ctx.name()));
        }
        super.write(ctx, msg, promise);
    }

    /**
     * Get http request requestHeaders.
     *
     * @return the http request requestHeaders
     */
    public HttpHeaders getRequestHeaders() {
        return requestHeaders;
    }
}
