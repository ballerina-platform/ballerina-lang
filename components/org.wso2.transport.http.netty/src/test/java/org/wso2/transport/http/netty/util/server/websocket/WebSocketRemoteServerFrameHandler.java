/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

package org.wso2.transport.http.netty.util.server.websocket;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.Constants;

/**
 * Simple WebSocket frame handler for testing
 */
public class WebSocketRemoteServerFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private static final Logger log = LoggerFactory.getLogger(WebSocketRemoteServerFrameHandler.class);
    private static final String PING = "ping";
    private static final String CLOSE = "close";
    private static final String CLOSE_WITHOUT_FRAME = "close-without-frame";

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debug("channel is active");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.debug("channel is inactive");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        if (frame instanceof TextWebSocketFrame) {
            String text = ((TextWebSocketFrame) frame).text();
            switch (text) {
                case PING:
                    ctx.writeAndFlush(new PingWebSocketFrame(Unpooled.wrappedBuffer(new byte[]{1, 2, 3, 4})));
                    break;
                case CLOSE:
                    ctx.writeAndFlush(new CloseWebSocketFrame(Constants.WEBSOCKET_STATUS_CODE_NORMAL_CLOSURE,
                                                              "Close on request"));
                    break;
                case CLOSE_WITHOUT_FRAME:
                    ctx.close();
                    break;
                default:
                    ctx.channel().writeAndFlush(new TextWebSocketFrame(text));
            }
        } else if (frame instanceof BinaryWebSocketFrame) {
            ctx.channel().writeAndFlush(frame.retain());
        } else if (frame instanceof CloseWebSocketFrame) {
            ctx.close();
        } else {
            String message = "unsupported frame type: " + frame.getClass().getName();
            throw new UnsupportedOperationException(message);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Exception Caught: " + cause.getMessage());
        ctx.close();
    }
}
