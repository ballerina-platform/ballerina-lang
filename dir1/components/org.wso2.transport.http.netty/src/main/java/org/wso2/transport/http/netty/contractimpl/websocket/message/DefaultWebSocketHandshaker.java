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
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
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
import org.wso2.transport.http.netty.contractimpl.listener.MaxEntityBodyValidator;
import org.wso2.transport.http.netty.contractimpl.listener.UriAndHeaderLengthValidator;
import org.wso2.transport.http.netty.contractimpl.listener.WebSocketMessageQueueHandler;
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
    private boolean cancelled = false;
    private boolean handshakeStarted = false;
    private HttpCarbonRequest request;

    public DefaultWebSocketHandshaker(ChannelHandlerContext ctx, ServerConnectorFuture connectorFuture,
                                      FullHttpRequest httpRequest) {
        this.ctx = ctx;
        this.connectorFuture = connectorFuture;
        this.secureConnection = ctx.channel().pipeline().get(Constants.SSL_HANDLER) != null;
        this.httpRequest = httpRequest;
        this.target = httpRequest.uri();
    }

    @Override
    public String getTarget() {
        return this.target;
    }

    @Override
    public ServerHandshakeFuture handshake() {
        return handshake(null);
    }

    @Override
    public ServerHandshakeFuture handshake(String[] subProtocols) {
        return handshake(subProtocols, 0);
    }

    @Override
    public ServerHandshakeFuture handshake(String[] subProtocols, int idleTimeout) {
        return handshake(subProtocols, idleTimeout, null);
    }

    @Override
    public ServerHandshakeFuture handshake(String[] subProtocols, int idleTimeout,
                                           HttpHeaders responseHeaders) {
        return handshake(subProtocols, idleTimeout, responseHeaders, Constants.WEBSOCKET_DEFAULT_FRAME_SIZE);
    }

    @Override
    public ServerHandshakeFuture handshake(String[] subProtocols, int idleTimeout,
                                           HttpHeaders responseHeaders, int maxFramePayloadLength) {
        boolean allowExtensions = httpRequest.headers().getAsString(HttpHeaderNames.SEC_WEBSOCKET_EXTENSIONS) != null;

        String subProtocolsStr = subProtocols != null ? String.join(",", subProtocols) : null;
        WebSocketServerHandshakerFactory wsFactory =
                new WebSocketServerHandshakerFactory(getWebSocketURL(), subProtocolsStr, allowExtensions,
                                                     maxFramePayloadLength);
        WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(httpRequest);
        return handleHandshake(handshaker, idleTimeout, responseHeaders);
    }

    /* Get the URL of the given connection */
    private String getWebSocketURL() {
        String scheme = Constants.WS_SCHEME;
        if (secureConnection) {
            scheme = Constants.WSS_SCHEME;
        }
        return scheme + "://" + httpRequest.headers().get("Host") + target;
    }

    @Override
    public ChannelFuture cancelHandshake(int statusCode, String closeReason) {
        handleIllegalStates();
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

    private void handleIllegalStates() {
        if (cancelled) {
            throw new IllegalStateException("Cannot cancel the handshake: handshake already cancelled");
        }

        if (handshakeStarted) {
            throw new IllegalStateException("Cannot cancel the handshake: handshake already started");
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
        return true;
    }

    @Override
    public WebSocketConnection getWebSocketConnection() {
        throw new IllegalStateException("Cannot get WebSocket connection before handshake is completed");
    }

    @Override
    public String getChannelId() {
        return WebSocketUtil.getChannelId(ctx);
    }

    private ServerHandshakeFuture handleHandshake(WebSocketServerHandshaker handshaker, int idleTimeout,
                                                  HttpHeaders headers) {
        DefaultServerHandshakeFuture handshakeFuture = new DefaultServerHandshakeFuture();
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
            handshakeFuture.notifyError(new UnsupportedOperationException("Unsupported WebSocket version"));
            return handshakeFuture;
        }
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
                        target, handshaker.selectedSubprotocol(), connectorFuture, new WebSocketMessageQueueHandler());
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
        pipeline.remove(Constants.HTTP_CHUNK_WRITER);
        pipeline.remove(Constants.HTTP_EXCEPTION_HANDLER);
        if (pipeline.get(UriAndHeaderLengthValidator.class) != null) {
            pipeline.remove(UriAndHeaderLengthValidator.class);
        }
        if (pipeline.get(MaxEntityBodyValidator.class) != null) {
            pipeline.remove(MaxEntityBodyValidator.class);
        }
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

    public HttpCarbonRequest getHttpCarbonRequest() {
        return request;
    }

    public void setHttpCarbonRequest(HttpCarbonRequest request) {
        this.request = request;
    }
}
