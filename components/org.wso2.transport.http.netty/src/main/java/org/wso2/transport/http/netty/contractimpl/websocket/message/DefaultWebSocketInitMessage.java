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
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contract.websocket.ServerHandshakeFuture;
import org.wso2.transport.http.netty.contract.websocket.WebSocketInitMessage;
import org.wso2.transport.http.netty.contractimpl.websocket.DefaultServerHandshakeFuture;
import org.wso2.transport.http.netty.contractimpl.websocket.DefaultWebSocketMessage;
import org.wso2.transport.http.netty.contractimpl.websocket.WebSocketInboundFrameHandler;
import org.wso2.transport.http.netty.contractimpl.websocket.WebSocketUtil;
import org.wso2.transport.http.netty.listener.MessageQueueHandler;
import org.wso2.transport.http.netty.message.HttpCarbonRequest;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of {@link WebSocketInitMessage}.
 */
public class DefaultWebSocketInitMessage extends DefaultWebSocketMessage implements WebSocketInitMessage {

    private final ChannelHandlerContext ctx;
    private final FullHttpRequest httpRequest;
    private final ServerConnectorFuture connectorFuture;
    private boolean cancelled = false;
    private boolean handshakeStarted = false;
    private HttpCarbonRequest request;

    public DefaultWebSocketInitMessage(ChannelHandlerContext ctx, ServerConnectorFuture connectorFuture,
                                       FullHttpRequest httpRequest) {
        this.ctx = ctx;
        this.connectorFuture = connectorFuture;
        this.secureConnection = ctx.channel().pipeline().get(Constants.SSL_HANDLER) != null;
        this.httpRequest = httpRequest;
        this.sessionID = WebSocketUtil.getChannelId(ctx);
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
            int responseStatusCode = statusCode >= 400 && statusCode < 500 ? statusCode : 400;
            ChannelFuture responseFuture;
            if (closeReason != null) {
                ByteBuf content = Unpooled.wrappedBuffer(closeReason.getBytes(StandardCharsets.UTF_8));
                responseFuture = ctx.writeAndFlush(new DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1, HttpResponseStatus.valueOf(responseStatusCode), content));
            } else {
                responseFuture = ctx.writeAndFlush(new DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1, HttpResponseStatus.valueOf(responseStatusCode)));
            }
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

    private ServerHandshakeFuture handleHandshake(WebSocketServerHandshaker handshaker, int idleTimeout,
                                                  HttpHeaders headers) {
        DefaultServerHandshakeFuture handshakeFuture = new DefaultServerHandshakeFuture();
        if (cancelled) {
            Throwable e = new IllegalAccessException("Handshake is already cancelled!");
            handshakeFuture.notifyError(e);
            return handshakeFuture;
        }
        ChannelFuture channelFuture =
                handshaker.handshake(ctx.channel(), httpRequest, headers, ctx.channel().newPromise());
        channelFuture.addListener(future -> {
            if (future.isSuccess() && future.cause() == null) {
                MessageQueueHandler messageQueueHandler = new MessageQueueHandler();
                WebSocketInboundFrameHandler frameHandler = new WebSocketInboundFrameHandler(true, secureConnection,
                        target, listenerInterface, handshaker.selectedSubprotocol(), connectorFuture,
                        messageQueueHandler);
                configureFrameHandlingPipeline(idleTimeout, messageQueueHandler, frameHandler);
                handshakeFuture.notifySuccess(frameHandler.getWebSocketConnection());
            } else {
                handshakeFuture.notifyError(future.cause());
            }
        });
        handshakeStarted = true;
        return handshakeFuture;
    }

    private void configureFrameHandlingPipeline(int idleTimeout, MessageQueueHandler messageQueueHandler,
                                                WebSocketInboundFrameHandler frameHandler) {
        ChannelPipeline pipeline = ctx.pipeline();
        if (idleTimeout > 0) {
            pipeline.replace(Constants.IDLE_STATE_HANDLER, Constants.IDLE_STATE_HANDLER,
                             new IdleStateHandler(idleTimeout, idleTimeout, idleTimeout,
                                                  TimeUnit.MILLISECONDS));
        } else {
            pipeline.remove(Constants.IDLE_STATE_HANDLER);
        }
        pipeline.addLast(Constants.MESSAGE_QUEUE_HANDLER, messageQueueHandler);
        pipeline.addLast(Constants.WEBSOCKET_FRAME_HANDLER, frameHandler);
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
