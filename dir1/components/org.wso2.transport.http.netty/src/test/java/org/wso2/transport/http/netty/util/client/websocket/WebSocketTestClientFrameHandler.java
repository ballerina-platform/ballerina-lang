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

package org.wso2.transport.http.netty.util.client.websocket;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;

/**
 * WebSocket Client Handler for Testing.
 */
public class WebSocketTestClientFrameHandler extends SimpleChannelInboundHandler<Object> {

    private static final Logger LOG = LoggerFactory.getLogger(WebSocketTestClient.class);

    private final WebSocketClientHandshaker handshaker;
    private ChannelPromise handshakeFuture;
    private CountDownLatch latch;

    // Variables to store events received.
    private String textReceived;
    private ByteBuffer bufferReceived;
    private CloseWebSocketFrame receivedCloseFrame;
    private boolean isPingReceived;
    private boolean isPongReceived;
    private FullHttpResponse httpResponse;

    public WebSocketTestClientFrameHandler(WebSocketClientHandshaker handshaker) {
        this.handshaker = handshaker;
    }

    public ChannelFuture handshakeFuture() {
        return handshakeFuture;
    }

    public void setCountDownLatch(CountDownLatch latch) {
        this.latch = latch;
    }

    public HttpResponse getHttpResponse() {
        return httpResponse;
    }

    public WebSocketClientHandshaker getHandshaker() {
        return handshaker;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        handshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        handshaker.handshake(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws InterruptedException {
        LOG.debug("WebSocket Client disconnected!");
        countDownLatch();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel channel = ctx.channel();
        if (!handshaker.isHandshakeComplete()) {
            httpResponse = (FullHttpResponse) msg;
            handshaker.finishHandshake(channel, httpResponse);
            LOG.debug("WebSocket Client connected!");
            handshakeFuture.setSuccess();
            return;
        }

        if (msg instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) msg;
            throw new IllegalStateException(
                    "Unexpected FullHttpResponse (getStatus=" + response.status() +
                            ", content=" + response.content().toString(CharsetUtil.UTF_8) + ')');
        }

        WebSocketFrame frame = (WebSocketFrame) msg;
        if (frame instanceof TextWebSocketFrame) {
            TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
            LOG.debug("WebSocket Client received text message: " + textFrame.text());
            textReceived = textFrame.text();
        } else if (frame instanceof BinaryWebSocketFrame) {
            BinaryWebSocketFrame binaryFrame = (BinaryWebSocketFrame) frame;
            bufferReceived = binaryFrame.content().nioBuffer();
            LOG.debug("WebSocket Client received  binary message: " + bufferReceived.toString());
        } else if (frame instanceof PingWebSocketFrame) {
            LOG.debug("WebSocket Client received pong");
            PingWebSocketFrame pingFrame = (PingWebSocketFrame) frame;
            isPingReceived = true;
            bufferReceived = pingFrame.content().nioBuffer();
        } else if (frame instanceof PongWebSocketFrame) {
            LOG.debug("WebSocket Client received pong");
            PongWebSocketFrame pongFrame = (PongWebSocketFrame) frame;
            isPongReceived = true;
            bufferReceived = pongFrame.content().nioBuffer();
        } else if (frame instanceof CloseWebSocketFrame) {
            LOG.debug("WebSocket Client received closing");
            receivedCloseFrame = (CloseWebSocketFrame) frame.retain();
        }
        countDownLatch();
    }

    /**
     * @return the text received from the server.
     */
    public String getTextReceived() {
        String temp = textReceived;
        textReceived = null;
        return temp;
    }

    /**
     * @return the binary data received from the server.
     */
    public ByteBuffer getBufferReceived() {
        ByteBuffer tempBuffer = bufferReceived;
        bufferReceived = null;
        return tempBuffer;
    }

    /**
     * @return Received close frame.
     */
    public CloseWebSocketFrame getReceivedCloseFrame() {
         CloseWebSocketFrame tempFrame = receivedCloseFrame;
         receivedCloseFrame = null;
         return tempFrame;
    }

    /**
     * Check whether a ping is received.
     *
     * @return true if a ping is received.
     */
    public boolean isPingReceived() {
        boolean tmpBln = isPingReceived;
        isPingReceived = false;
        return tmpBln;
    }

    /**
     * Check whether a ping is received.
     *
     * @return true if a ping is received.
     */
    public boolean isPongReceived() {
        boolean tmpBln = isPongReceived;
        isPongReceived = false;
        return tmpBln;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (!handshakeFuture.isDone()) {
            handshakeFuture.setFailure(cause);
        }
        LOG.error("Exception caught: ", cause);
        ctx.close();
    }

    private void countDownLatch() {
        if (this.latch == null || latch.getCount() == 0) {
            return;
        }
        latch.countDown();
    }


}
