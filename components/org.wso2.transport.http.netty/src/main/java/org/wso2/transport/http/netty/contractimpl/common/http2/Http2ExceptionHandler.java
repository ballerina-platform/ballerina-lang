/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.transport.http.netty.contractimpl.common.http2;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http2.Http2CodecUtil;
import io.netty.handler.codec.http2.Http2ConnectionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.wso2.transport.http.netty.contract.Constants.SECURITY;
import static org.wso2.transport.http.netty.contract.Constants.SSL;

/**
 * Handles inbound exceptions for both the streams and the connection.
 */
public class Http2ExceptionHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(Http2ExceptionHandler.class);
    private Http2ConnectionHandler http2ConnectionHandler;

    public Http2ExceptionHandler(Http2ConnectionHandler http2ConnectionHandler) {
        this.http2ConnectionHandler = http2ConnectionHandler;
    }

    public Http2ExceptionHandler() {}

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (http2ConnectionHandler != null && Http2CodecUtil.getEmbeddedHttp2Exception(cause) != null) {
            http2ConnectionHandler.onError(ctx, false, cause);
        } else {
            ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
        if (!cause.toString().contains(SSL) && !cause.toString().contains(SECURITY)) {
            LOG.error("Exception occurred in HTTP/2 inbound channel", cause);
        }
    }
}
