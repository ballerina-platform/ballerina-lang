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
import java.io.OutputStream;
import java.io.Writer;
import java.nio.ByteBuffer;
import javax.websocket.RemoteEndpoint;

/**
 * This is {@link Basic} implementation for WebSocket Connection.
 */

public class WebSocketBasicRemoteEndpoint implements RemoteEndpoint.Basic {

    private final ChannelHandlerContext ctx;

    public WebSocketBasicRemoteEndpoint(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void sendText(String text) throws IOException {
        try {
            handleIOException(ctx.channel().writeAndFlush(new TextWebSocketFrame(text)).sync());
        } catch (InterruptedException err) {
            throw new IOException(err.getMessage(), err);
        }
    }

    @Override
    public void sendBinary(ByteBuffer data) throws IOException {
        try {
            ByteBuf byteBuf = Unpooled.wrappedBuffer(data);
            handleIOException(ctx.channel().writeAndFlush(new BinaryWebSocketFrame(byteBuf)).sync());
        } catch (InterruptedException err) {
            throw new IOException(err.getMessage(), err);
        }
    }

    @Override
    public void sendText(String text, boolean isFinal) throws IOException {
        try {
            handleIOException(ctx.channel().writeAndFlush(new TextWebSocketFrame(isFinal, 0, text)).sync());
        } catch (InterruptedException err) {
            throw new IOException(err.getMessage(), err);
        }
    }

    @Override
    public void sendBinary(ByteBuffer data, boolean isFinal) throws IOException {
        try {
            ByteBuf byteBuf = Unpooled.wrappedBuffer(data);
            handleIOException(ctx.channel().writeAndFlush(new BinaryWebSocketFrame(isFinal, 0, byteBuf)).sync());
        } catch (InterruptedException err) {
            throw new IOException(err.getMessage(), err);
        }
    }

    @Override
    public OutputStream getSendStream() throws IOException {
        throw new UnsupportedOperationException("Method is not supported");
    }

    @Override
    public Writer getSendWriter() throws IOException {
        throw new UnsupportedOperationException("Method is not supported");
    }

    @Override
    public void sendObject(Object data) throws IOException {
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
        try {
            handleIllegalArgumentException(applicationData);
            ByteBuf applicationDataBuf = Unpooled.wrappedBuffer(applicationData);
            handleIOException(ctx.channel().writeAndFlush(new PingWebSocketFrame(applicationDataBuf)).sync());
        } catch (InterruptedException err) {
            throw new IOException(err.getMessage(), err);
        }
    }

    @Override
    public void sendPong(ByteBuffer applicationData) throws IOException, IllegalArgumentException {
        try {
            handleIllegalArgumentException(applicationData);
            ByteBuf applicationDataBuf = Unpooled.wrappedBuffer(applicationData);
            handleIOException(ctx.channel().writeAndFlush(new PongWebSocketFrame(applicationDataBuf)).sync());
        } catch (InterruptedException err) {
            throw new IOException(err.getMessage(), err);
        }
    }

    private void handleIOException(ChannelFuture future) throws IOException {
        Throwable cause = future.cause();
        if (cause != null) {
            throw new IOException(cause.getMessage(), cause);
        }
    }

    private void handleIllegalArgumentException(ByteBuffer buffer) {
        if (buffer.capacity() > 125) {
            throw new IllegalArgumentException("Exceeds 125 bytes.");
        }
    }
}
