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
import io.netty.channel.ChannelPipeline;
import org.wso2.transport.http.netty.common.Constants;

/**
 * {@code Http2ToHttpFallbackHandler} is responsible for fallback from http2 to http when http2 upgrade fails.
 *
 */
public class Http2ToHttpFallbackHandler extends ChannelInboundHandlerAdapter {

    HttpServerChannelInitializer serverChannelInitializer;

    public Http2ToHttpFallbackHandler(
            HttpServerChannelInitializer serverChannelInitializer) {
        this.serverChannelInitializer = serverChannelInitializer;
    }

    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ChannelPipeline pipeline = ctx.pipeline();
        pipeline.remove(Constants.HTTP2_UPGRADE_HANDLER);
        if (serverChannelInitializer.isHttpAccessLogEnabled()) {
            pipeline.remove(Constants.HTTP_ACCESS_LOG_HANDLER);
        }
        if (serverChannelInitializer.isHttpTraceLogEnabled()) {
            pipeline.remove(Constants.HTTP_TRACE_LOG_HANDLER);
        }
        serverChannelInitializer.configureHttpPipeline(pipeline, Constants.HTTP2_CLEARTEXT_PROTOCOL);
        pipeline.remove(this);
        ctx.fireChannelRead(msg);
    }
}
