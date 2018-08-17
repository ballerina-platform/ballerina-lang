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
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.ContinuationWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Simple WebSocket frame handler for testing.
 */
public class WebSocketRemoteServerFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private static final Logger log = LoggerFactory.getLogger(WebSocketRemoteServerFrameHandler.class);
    private boolean isOpen = true;
    private static final String PING = "ping";
    private static final String CUSTOM_HEADERS = "custom-headers";
    private WebSocketHeadersHandler headersHandler;

    public WebSocketRemoteServerFrameHandler(WebSocketHeadersHandler headersHandler) {
        this.headersHandler = headersHandler;
    }

    public boolean isOpen() {
        return isOpen;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.debug("channel is active");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.debug("channel is inactive");
        isOpen = false;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (frame instanceof TextWebSocketFrame) {
            // Echos the same text
            String text = ((TextWebSocketFrame) frame).text();
            if (PING.equals(text)) {
                ctx.channel().writeAndFlush(new PingWebSocketFrame(
                        Unpooled.wrappedBuffer(ByteBuffer.wrap("data".getBytes(StandardCharsets.UTF_8)))));
                return;
            }
            if (text.contains(CUSTOM_HEADERS)) {
                HttpHeaders headers = headersHandler.getRequestHeaders();
                ctx.writeAndFlush(new TextWebSocketFrame(headers.get(text.split(":")[1])));
                return;
            }
            ctx.channel().writeAndFlush(new TextWebSocketFrame(frame.isFinalFragment(), 0, text));
        } else if (frame instanceof BinaryWebSocketFrame) {
            ByteBuffer bufferCopy = cloneBuffer(frame.content().nioBuffer());
            ctx.writeAndFlush(new BinaryWebSocketFrame(frame.isFinalFragment(), 0,
                                                       Unpooled.wrappedBuffer(bufferCopy)));
        } else if (frame instanceof CloseWebSocketFrame) {
            ctx.close();
            isOpen = false;
        } else if (frame instanceof ContinuationWebSocketFrame) {
            ByteBuffer clonedBuffer = cloneBuffer(frame.content().nioBuffer());
            ctx.writeAndFlush(new ContinuationWebSocketFrame(frame.isFinalFragment(), 0,
                                                             Unpooled.wrappedBuffer(clonedBuffer)));
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

    private ByteBuffer cloneBuffer(ByteBuffer originalBuffer) {
        ByteBuffer bufferCopy = ByteBuffer.allocate(originalBuffer.capacity());
        originalBuffer.rewind();
        bufferCopy.put(originalBuffer);
        bufferCopy.flip();
        return bufferCopy;
    }
}
