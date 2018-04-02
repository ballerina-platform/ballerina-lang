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

package org.wso2.transport.http.netty.internal.websocket;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.io.IOException;
import java.nio.ByteBuffer;
import javax.websocket.RemoteEndpoint;
import javax.websocket.SendHandler;

/**
 * This is {@link Basic} implementation for WebSocket Connection.
 */

public class WebSocketAsyncRemoteEndpoint implements RemoteEndpoint.Async {

    private final ChannelHandlerContext ctx;

    public WebSocketAsyncRemoteEndpoint(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public long getSendTimeout() {
        return 0;
    }

    @Override
    public void setSendTimeout(long timeoutmillis) {

    }

    @Override
    public void sendText(String text, SendHandler handler) {
        throw new UnsupportedOperationException("Method is not supported");
    }

    @Override
    public ChannelFuture sendText(String text) {
        return ctx.channel().writeAndFlush(new TextWebSocketFrame(text));
    }

    @Override
    public ChannelFuture sendBinary(ByteBuffer data) {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(data);
        return ctx.channel().writeAndFlush(new BinaryWebSocketFrame(byteBuf));
    }

    @Override
    public void sendBinary(ByteBuffer data, SendHandler handler) {
        throw new UnsupportedOperationException("Method is not supported");
    }


    @Override
    public ChannelFuture sendObject(Object data) {
        throw new UnsupportedOperationException("Method is not supported");
    }

    @Override
    public void sendObject(Object data, SendHandler handler) {
        throw new UnsupportedOperationException("Method is not supported");
    }


    @Override
    public void setBatchingAllowed(boolean allowed) throws IOException {
        throw new UnsupportedOperationException("Method is not supported");
    }

    @Override
    public boolean getBatchingAllowed() {
        return false;
    }

    @Override
    public void flushBatch() throws IOException {
        throw new UnsupportedOperationException("Method is not supported");
    }

    @Override
    public void sendPing(ByteBuffer applicationData) throws IOException, IllegalArgumentException {
        ByteBuf applicationDataBuf = Unpooled.wrappedBuffer(applicationData);
        ctx.channel().writeAndFlush(new PingWebSocketFrame(applicationDataBuf));
    }

    @Override
    public void sendPong(ByteBuffer applicationData) throws IOException, IllegalArgumentException {
        ByteBuf applicationDataBuf = Unpooled.wrappedBuffer(applicationData);
        ctx.channel().writeAndFlush(new PongWebSocketFrame(applicationDataBuf));
    }
}
