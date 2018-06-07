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

package org.wso2.transport.http.netty.sender.websocket;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnectorException;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnectorFuture;
import org.wso2.transport.http.netty.contractimpl.websocket.WebSocketInboundFrameHandler;
import org.wso2.transport.http.netty.internal.websocket.WebSocketUtil;
import org.wso2.transport.http.netty.listener.WebSocketFramesBlockingHandler;
import org.wso2.transport.http.netty.message.DefaultListener;
import org.wso2.transport.http.netty.message.HttpCarbonResponse;

/**
 * WebSocket Client Handler. This class responsible for handling the inbound messages for the WebSocket Client.
 * <i>Note: If the user uses both WebSocket Client and the server it is recommended to check the
 * <b>{@link Constants}.IS_WEBSOCKET_SERVER</b> property to identify whether the message is coming from the client
 * or the server in the application level.</i>
 */
public class WebSocketTargetHandler extends WebSocketInboundFrameHandler {

    private static final Logger log = LoggerFactory.getLogger(WebSocketClient.class);

    private final WebSocketClientHandshaker handshaker;
    private final WebSocketFramesBlockingHandler blockingHandler;
    private final boolean isSecure;
    private final boolean autoRead;
    private final String requestedUri;
    private ChannelPromise handshakeFuture;
    private HttpCarbonResponse httpCarbonResponse;

    public WebSocketTargetHandler(WebSocketClientHandshaker handshaker,
                                  WebSocketFramesBlockingHandler framesBlockingHandler, boolean isSecure,
                                  boolean autoRead, String requestedUri, WebSocketConnectorFuture connectorFuture) {
        super(connectorFuture, false, isSecure, null, null);
        this.handshaker = handshaker;
        this.blockingHandler = framesBlockingHandler;
        this.isSecure = isSecure;
        this.autoRead = autoRead;
        this.requestedUri = requestedUri;
        this.handshakeFuture = null;
    }

    public ChannelFuture handshakeFuture() {
        return handshakeFuture;
    }

    public HttpCarbonResponse getHttpCarbonResponse() {
        return httpCarbonResponse;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        super.handlerAdded(ctx);
        handshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        this.ctx = ctx;
        handshaker.handshake(ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!handshaker.isHandshakeComplete()) {
            Channel ch = ctx.channel();
            FullHttpResponse fullHttpResponse = (FullHttpResponse) msg;
            httpCarbonResponse = setUpCarbonMessage(ctx, fullHttpResponse);
            handshaker.finishHandshake(ch, fullHttpResponse);
            log.debug("WebSocket Client connected!");
            super.webSocketConnection =
                    WebSocketUtil.getWebSocketConnection(ctx, this, blockingHandler, isSecure, requestedUri);
            if (!autoRead) {
                ctx.channel().pipeline().addBefore(Constants.WEBSOCKET_FRAME_HANDLER,
                        Constants.WEBSOCKET_FRAME_BLOCKING_HANDLER, blockingHandler);
            }
            handshakeFuture.setSuccess();
            fullHttpResponse.release();
            return;
        }

        super.channelRead(ctx, msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws WebSocketConnectorException {
        caughtException = true;
        if (!handshakeFuture.isDone()) {
            handshakeFuture.setFailure(cause);
        } else {
            super.exceptionCaught(ctx, cause);
        }
    }

    private HttpCarbonResponse setUpCarbonMessage(ChannelHandlerContext ctx, HttpResponse msg) {
        HttpCarbonResponse carbonResponse = new HttpCarbonResponse(msg, new DefaultListener(ctx));
        carbonResponse.setProperty(Constants.DIRECTION, Constants.DIRECTION_RESPONSE);
        carbonResponse.setProperty(Constants.HTTP_STATUS_CODE, msg.status().code());
        return carbonResponse;
    }
}
