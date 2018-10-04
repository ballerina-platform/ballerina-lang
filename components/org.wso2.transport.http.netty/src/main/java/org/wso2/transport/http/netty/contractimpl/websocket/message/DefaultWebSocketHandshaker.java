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

package org.wso2.transport.http.netty.contractimpl.websocket.message;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.handler.timeout.IdleStateHandler;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contract.websocket.ServerHandshakeFuture;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;
import org.wso2.transport.http.netty.contract.websocket.WebSocketHandshaker;
import org.wso2.transport.http.netty.contractimpl.listener.MessageQueueHandler;
import org.wso2.transport.http.netty.contractimpl.websocket.DefaultServerHandshakeFuture;
import org.wso2.transport.http.netty.contractimpl.websocket.WebSocketInboundFrameHandler;
import org.wso2.transport.http.netty.contractimpl.websocket.WebSocketUtil;
import org.wso2.transport.http.netty.message.HttpCarbonRequest;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * Default implementation of {@link WebSocketHandshaker}.
 */
public class DefaultWebSocketHandshaker implements WebSocketHandshaker {

    private final ChannelHandlerContext ctx;
    private final FullHttpRequest httpRequest;
    private final ServerConnectorFuture connectorFuture;
    private final String target;
    private final boolean secureConnection;
    private final boolean serverMessage;
    private boolean cancelled = false;
    private boolean handshakeStarted = false;
    private HttpCarbonRequest request;

    public DefaultWebSocketHandshaker(ChannelHandlerContext ctx, ServerConnectorFuture connectorFuture,
                                      FullHttpRequest httpRequest, String target, boolean serverMessage) {
        this.ctx = ctx;
        this.connectorFuture = connectorFuture;
        this.secureConnection = ctx.channel().pipeline().get(Constants.SSL_HANDLER) != null;
        this.httpRequest = httpRequest;
        this.target = target;
        this.serverMessage = serverMessage;
    }

    @Override
    public String getTarget() {
        return this.target;
    }

    @Override
    public ServerHandshakeFuture handshake() {
        WebSocketServerHandshakerFactory wsFactory =
                new WebSocketServerHandshakerFactory(getWebSocketURL(httpRequest), null, true);
        WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(httpRequest);
        return handleHandshake(handshaker, 0, null);
    }

    @Override
    public ServerHandshakeFuture handshake(String[] subProtocols, boolean allowExtensions) {
        WebSocketServerHandshakerFactory wsFactory =
                new WebSocketServerHandshakerFactory(getWebSocketURL(httpRequest), getSubProtocolsCSV(subProtocols),
                                                     allowExtensions);
        WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(httpRequest);
        return handleHandshake(handshaker, 0, null);
    }

    @Override
    public ServerHandshakeFuture handshake(String[] subProtocols, boolean allowExtensions, int idleTimeout) {
        WebSocketServerHandshakerFactory wsFactory =
                new WebSocketServerHandshakerFactory(getWebSocketURL(httpRequest),
                                                     getSubProtocolsCSV(subProtocols), allowExtensions);
        WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(httpRequest);
        return handleHandshake(handshaker, idleTimeout, null);
    }

    @Override
    public ServerHandshakeFuture handshake(String[] subProtocols, boolean allowExtensions, int idleTimeout,
                                           HttpHeaders responseHeaders) {
        WebSocketServerHandshakerFactory wsFactory =
                new WebSocketServerHandshakerFactory(getWebSocketURL(httpRequest),
                                                     getSubProtocolsCSV(subProtocols), allowExtensions);
        WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(httpRequest);
        return handleHandshake(handshaker, idleTimeout, responseHeaders);
    }

    @Override
    public ServerHandshakeFuture handshake(String[] subProtocols, boolean allowExtensions, int idleTimeout,
                                           HttpHeaders responseHeaders, int maxFramePayloadLength) {
        WebSocketServerHandshakerFactory wsFactory =
                new WebSocketServerHandshakerFactory(getWebSocketURL(httpRequest), getSubProtocolsCSV(subProtocols),
                                                     allowExtensions, maxFramePayloadLength);
        WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(httpRequest);
        return handleHandshake(handshaker, idleTimeout, responseHeaders);
    }

    @Override
    public ChannelFuture cancelHandshake(int statusCode, String closeReason) {
        if (cancelled) {
            throw new IllegalStateException("Cannot cancel the handshake: handshake already cancelled");
        }

        if (handshakeStarted) {
            throw new IllegalStateException("Cannot cancel the handshake: handshake already started");
        }

        try {
            int responseStatusCode = statusCode >= 400 && statusCode < 600 ? statusCode : 400;
            ChannelFuture responseFuture;
            if (closeReason != null) {
                ByteBuf content = Unpooled.wrappedBuffer(closeReason.getBytes(StandardCharsets.UTF_8));
                responseFuture = ctx.writeAndFlush(new DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1, HttpResponseStatus.valueOf(responseStatusCode), content));
            } else {
                responseFuture = ctx.writeAndFlush(new DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1, HttpResponseStatus.valueOf(responseStatusCode)));
            }
            responseFuture.addListener((ChannelFutureListener) future -> ctx.close());
            return responseFuture;
        } finally {
            cancelled = true;
        }
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public boolean isHandshakeStarted() {
        return handshakeStarted;
    }

    @Override
    public boolean isSecure() {
        return secureConnection;
    }

    @Override
    public boolean isServerMessage() {
        return this.serverMessage;
    }

    @Override
    public WebSocketConnection getWebSocketConnection() {
        throw new IllegalStateException("Cannot get WebSocket connection without handshake completion");
    }

    @Override
    public String getChannelId() {
        return WebSocketUtil.getChannelId(ctx);
    }

    private ServerHandshakeFuture handleHandshake(WebSocketServerHandshaker handshaker, int idleTimeout,
                                                  HttpHeaders headers) {
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        }
        DefaultServerHandshakeFuture handshakeFuture = new DefaultServerHandshakeFuture();
        if (cancelled) {
            Throwable e = new IllegalAccessException("Handshake is already cancelled.");
            handshakeFuture.notifyError(e);
            return handshakeFuture;
        }
        ChannelFuture channelFuture =
                handshaker.handshake(ctx.channel(), httpRequest, headers, ctx.channel().newPromise());
        channelFuture.addListener(future -> {
            if (future.isSuccess() && future.cause() == null) {
                WebSocketInboundFrameHandler frameHandler = new WebSocketInboundFrameHandler(true, secureConnection,
                        target, handshaker.selectedSubprotocol(), connectorFuture, new MessageQueueHandler());
                configureFrameHandlingPipeline(idleTimeout, frameHandler);
                handshakeFuture.notifySuccess(frameHandler.getWebSocketConnection());
            } else {
                handshakeFuture.notifyError(future.cause());
            }
        });
        handshakeStarted = true;
        return handshakeFuture;
    }

    private void configureFrameHandlingPipeline(int idleTimeout, WebSocketInboundFrameHandler frameHandler) {
        ChannelPipeline pipeline = ctx.pipeline();
        pipeline.remove(Constants.WEBSOCKET_SERVER_HANDSHAKE_HANDLER);
        if (idleTimeout > 0) {
            pipeline.replace(Constants.IDLE_STATE_HANDLER, Constants.IDLE_STATE_HANDLER,
                             new IdleStateHandler(0, 0, idleTimeout, TimeUnit.MILLISECONDS));
        } else {
            pipeline.remove(Constants.IDLE_STATE_HANDLER);
        }
        pipeline.addLast(Constants.WEBSOCKET_FRAME_HANDLER, frameHandler);
        frameHandler.getWebSocketConnection().stopReadingFrames();
        pipeline.fireChannelActive();
    }

    /* Get the URL of the given connection */
    private String getWebSocketURL(HttpRequest req) {
        String protocol = Constants.WS_SCHEME;
        if (secureConnection) {
            protocol = Constants.WSS_SCHEME;
        }
        return protocol + "://" + req.headers().get("Host") + req.uri();
    }

    private String getSubProtocolsCSV(String[] subProtocols) {
        if (subProtocols == null || subProtocols.length == 0) {
            return null;
        }

        String subProtocolsStr = "";
        for (String subProtocol : subProtocols) {
            subProtocolsStr = subProtocolsStr.concat(subProtocol + ",");
        }
        subProtocolsStr = subProtocolsStr.substring(0, subProtocolsStr.length() - 1);
        return subProtocolsStr;
    }

    public HttpCarbonRequest getHttpCarbonRequest() {
        return request;
    }

    public void setHttpCarbonRequest(HttpCarbonRequest request) {
        this.request = request;
    }
}
