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

package org.wso2.transport.http.netty.sender.websocket;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnectorFuture;
import org.wso2.transport.http.netty.contractimpl.websocket.WebSocketInboundFrameHandler;
import org.wso2.transport.http.netty.listener.WebSocketFramesBlockingHandler;
import org.wso2.transport.http.netty.message.DefaultListener;
import org.wso2.transport.http.netty.message.HttpCarbonResponse;

/**
 * WebSocket client handshake handler to handle incoming handshake response.
 */
public class WebSocketClientHandshakeHandler extends ChannelInboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(WebSocketClientHandshakeHandler.class);

    private final WebSocketClientHandshaker handshaker;
    private final WebSocketFramesBlockingHandler blockingHandler;
    private final boolean isSecure;
    private final boolean autoRead;
    private final String requestedUri;
    private ChannelPromise handshakeFuture;
    private HttpCarbonResponse httpCarbonResponse;
    private final WebSocketConnectorFuture connectorFuture;
    private WebSocketInboundFrameHandler inboundFrameHandler;

    public WebSocketClientHandshakeHandler(WebSocketClientHandshaker handshaker,
                                  WebSocketFramesBlockingHandler framesBlockingHandler, boolean isSecure,
                                  boolean autoRead, String requestedUri, WebSocketConnectorFuture connectorFuture) {
        this.handshaker = handshaker;
        this.blockingHandler = framesBlockingHandler;
        this.isSecure = isSecure;
        this.autoRead = autoRead;
        this.requestedUri = requestedUri;
        this.handshakeFuture = null;
        this.connectorFuture = connectorFuture;
    }

    public ChannelFuture handshakeFuture() {
        return handshakeFuture;
    }

    public HttpCarbonResponse getHttpCarbonResponse() {
        return httpCarbonResponse;
    }

    public WebSocketInboundFrameHandler getInboundFrameHandler() {
        return this.inboundFrameHandler;
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
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof FullHttpResponse)) {
            throw new IllegalArgumentException("HTTP response is expected!");
        }

        FullHttpResponse fullHttpResponse = (FullHttpResponse) msg;
        httpCarbonResponse = setUpCarbonMessage(ctx, fullHttpResponse);
        log.debug("WebSocket Client connected!");
        if (!autoRead) {
            ctx.channel().pipeline().addLast(Constants.WEBSOCKET_FRAME_BLOCKING_HANDLER, blockingHandler);
        }
        inboundFrameHandler =
                new WebSocketInboundFrameHandler(connectorFuture, blockingHandler, false, isSecure, requestedUri, null);
        ctx.channel().pipeline().addLast(Constants.WEBSOCKET_FRAME_HANDLER, inboundFrameHandler);
        handshaker.finishHandshake(ctx.channel(), fullHttpResponse);
        ctx.channel().pipeline().remove(Constants.WEBSOCKET_CLIENT_HANDSHAKE_HANDLER);
        ctx.fireChannelActive();
        handshakeFuture.setSuccess();
        fullHttpResponse.release();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Caught exception", cause);
        handshakeFuture.setFailure(cause);
    }

    private HttpCarbonResponse setUpCarbonMessage(ChannelHandlerContext ctx, HttpResponse msg) {
        HttpCarbonResponse carbonResponse = new HttpCarbonResponse(msg, new DefaultListener(ctx));
        carbonResponse.setProperty(Constants.DIRECTION, Constants.DIRECTION_RESPONSE);
        carbonResponse.setProperty(Constants.HTTP_STATUS_CODE, msg.status().code());
        return carbonResponse;
    }

}
