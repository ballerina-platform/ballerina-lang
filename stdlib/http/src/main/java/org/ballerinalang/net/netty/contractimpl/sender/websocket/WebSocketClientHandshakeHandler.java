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

package org.ballerinalang.net.netty.contractimpl.sender.websocket;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.websocketx.WebSocket13FrameDecoder;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketFrameDecoder;
import org.ballerinalang.net.netty.contract.Constants;
import org.ballerinalang.net.netty.contract.websocket.WebSocketConnectorFuture;
import org.ballerinalang.net.netty.contractimpl.listener.WebSocketMessageQueueHandler;
import org.ballerinalang.net.netty.contractimpl.websocket.DefaultClientHandshakeFuture;
import org.ballerinalang.net.netty.contractimpl.websocket.DefaultWebSocketConnection;
import org.ballerinalang.net.netty.contractimpl.websocket.WebSocketInboundFrameHandler;
import org.ballerinalang.net.netty.message.DefaultListener;
import org.ballerinalang.net.netty.message.HttpCarbonResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WebSocket client handshake handler to handle incoming handshake response.
 */
public class WebSocketClientHandshakeHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(WebSocketClientHandshakeHandler.class);

    private final WebSocketClientHandshaker handshaker;
    private final org.ballerinalang.net.netty.contractimpl.listener.WebSocketMessageQueueHandler
            webSocketMessageQueueHandler;
    private final boolean secure;
    private final boolean autoRead;
    private final String requestedUri;
    private final org.ballerinalang.net.netty.contractimpl.websocket.DefaultClientHandshakeFuture handshakeFuture;
    private final org.ballerinalang.net.netty.contract.websocket.WebSocketConnectorFuture connectorFuture;
    private org.ballerinalang.net.netty.message.HttpCarbonResponse httpCarbonResponse;

    public WebSocketClientHandshakeHandler(WebSocketClientHandshaker handshaker,
                                           DefaultClientHandshakeFuture handshakeFuture, WebSocketMessageQueueHandler webSocketMessageQueueHandler,
                                           boolean secure, boolean autoRead, String requestedUri, WebSocketConnectorFuture connectorFuture) {
        this.handshaker = handshaker;
        this.webSocketMessageQueueHandler = webSocketMessageQueueHandler;
        this.secure = secure;
        this.autoRead = autoRead;
        this.requestedUri = requestedUri;
        this.connectorFuture = connectorFuture;
        this.handshakeFuture = handshakeFuture;
    }

    public org.ballerinalang.net.netty.message.HttpCarbonResponse getHttpCarbonResponse() {
        return httpCarbonResponse;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        handshaker.handshake(ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (!(msg instanceof FullHttpResponse)) {
            throw new IllegalArgumentException("HTTP response is expected");
        }

        FullHttpResponse handshakeResponse = (FullHttpResponse) msg;
        httpCarbonResponse = setUpCarbonMessage(ctx, handshakeResponse);
        try {
            ctx.channel().config().setAutoRead(false);
            handshaker.finishHandshake(ctx.channel(), handshakeResponse);
            Channel channel = ctx.channel();
            String extensionsHeader = handshakeResponse.headers().getAsString(HttpHeaderNames.SEC_WEBSOCKET_EXTENSIONS);
            if (extensionsHeader == null) {
                // This replaces the frame decoder to make sure the rsv bits are not allowed
                channel.pipeline().replace(WebSocketFrameDecoder.class, "ws-decoder",
                                           new WebSocket13FrameDecoder(false, false, handshaker.maxFramePayloadLength(),
                                                                       false));
            }
            org.ballerinalang.net.netty.contractimpl.websocket.WebSocketInboundFrameHandler
                    inboundFrameHandler = new WebSocketInboundFrameHandler(
                    false, secure, requestedUri, handshaker.actualSubprotocol(), connectorFuture,
                    webSocketMessageQueueHandler);
            channel.pipeline().addLast(org.ballerinalang.net.netty.contract.Constants.WEBSOCKET_FRAME_HANDLER, inboundFrameHandler);
            channel.pipeline().remove(this);
            DefaultWebSocketConnection webSocketConnection = inboundFrameHandler.getWebSocketConnection();
            if (autoRead) {
                webSocketConnection.startReadingFrames();
            } else {
                webSocketConnection.stopReadingFrames();
            }
            handshakeFuture.notifySuccess(webSocketConnection, httpCarbonResponse);
            ctx.fireChannelActive();
            LOG.debug("WebSocket Client connected");
        } finally {
            handshakeResponse.release();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOG.error("Caught exception", cause);
        handshakeFuture.notifyError(cause, httpCarbonResponse);
    }

    private org.ballerinalang.net.netty.message.HttpCarbonResponse setUpCarbonMessage(ChannelHandlerContext ctx, HttpResponse msg) {
        org.ballerinalang.net.netty.message.HttpCarbonResponse carbonResponse = new HttpCarbonResponse(msg, new DefaultListener(ctx));
        carbonResponse.setProperty(org.ballerinalang.net.netty.contract.Constants.DIRECTION, Constants.DIRECTION_RESPONSE);
        carbonResponse.setHttpStatusCode(msg.status().code());
        return carbonResponse;
    }

}
