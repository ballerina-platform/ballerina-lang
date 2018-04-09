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
 */

package org.ballerinalang.test.util.websocket.server;

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

import java.nio.ByteBuffer;

/**
 * Simple WebSocket frame handler for testing
 */
public class WebSocketRemoteServerFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private static final Logger log = LoggerFactory.getLogger(WebSocketRemoteServerFrameHandler.class);
    private boolean isOpen = true;
    private static final String PING = "ping";

    public boolean isOpen() {
        return isOpen;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debug("channel is active");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.debug("channel is inactive");
        isOpen = false;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        if (frame instanceof TextWebSocketFrame) {
            System.out.println("Text frame received");
            // Echos the same text
            String text = ((TextWebSocketFrame) frame).text();
            if (PING.equals(text)) {
                ctx.channel().writeAndFlush(new PingWebSocketFrame(
                        Unpooled.wrappedBuffer(ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5}))));
                return;
            }
            ctx.channel().writeAndFlush(new TextWebSocketFrame(text));
        } else if (frame instanceof BinaryWebSocketFrame) {
            System.out.println("Binary frame received");
            ByteBuffer originalBuffer = frame.content().nioBuffer();
            ByteBuffer bufferCopy = ByteBuffer.allocate(originalBuffer.capacity());
            originalBuffer.rewind();
            bufferCopy.put(originalBuffer);
            bufferCopy.flip();
            ctx.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(bufferCopy)));
        } else if (frame instanceof CloseWebSocketFrame) {
            ctx.close();
            isOpen = false;
        } else {
            String message = "unsupported frame type: " + frame.getClass().getName();
            throw new UnsupportedOperationException(message);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Exception Caught: " + cause.getMessage());
        cause.printStackTrace();
        ctx.close();
    }
}
