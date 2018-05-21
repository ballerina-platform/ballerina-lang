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
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.handler.timeout.IdleStateHandler;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.contract.websocket.HandshakeFuture;
import org.wso2.transport.http.netty.contract.websocket.WebSocketInitMessage;
import org.wso2.transport.http.netty.contractimpl.websocket.DefaultWebSocketConnection;
import org.wso2.transport.http.netty.contractimpl.websocket.HandshakeFutureImpl;
import org.wso2.transport.http.netty.contractimpl.websocket.WebSocketMessageImpl;
import org.wso2.transport.http.netty.internal.websocket.WebSocketUtil;
import org.wso2.transport.http.netty.listener.WebSocketSourceHandler;
import org.wso2.transport.http.netty.message.HttpCarbonRequest;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of {@link WebSocketInitMessage}.
 */
public class DefaultWebSocketInitMessage extends WebSocketMessageImpl implements WebSocketInitMessage {

    private final ChannelHandlerContext ctx;
    private final FullHttpRequest httpRequest;
    private final WebSocketSourceHandler webSocketSourceHandler;
    private boolean cancelled = false;
    private boolean handshakeStarted = false;
    private HttpCarbonRequest request;

    public DefaultWebSocketInitMessage(ChannelHandlerContext ctx, FullHttpRequest httpRequest,
                                       WebSocketSourceHandler webSocketSourceHandler, Map<String, String> headers) {
        this.ctx = ctx;
        this.httpRequest = httpRequest;
        this.webSocketSourceHandler = webSocketSourceHandler;
        this.headers = headers;
        this.sessionlID = WebSocketUtil.getSessionID(ctx);
    }

    @Override
    public HandshakeFuture handshake() {
        WebSocketServerHandshakerFactory wsFactory =
                new WebSocketServerHandshakerFactory(getWebSocketURL(httpRequest), null, true);
        WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(httpRequest);
        return handleHandshake(handshaker, 0, null);
    }

    @Override
    public HandshakeFuture handshake(String[] subProtocols, boolean allowExtensions) {
        WebSocketServerHandshakerFactory wsFactory =
                new WebSocketServerHandshakerFactory(getWebSocketURL(httpRequest), getSubProtocolsCSV(subProtocols),
                                                     allowExtensions);
        WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(httpRequest);
        return handleHandshake(handshaker, 0, null);
    }

    @Override
    public HandshakeFuture handshake(String[] subProtocols, boolean allowExtensions, int idleTimeout) {
        WebSocketServerHandshakerFactory wsFactory =
                new WebSocketServerHandshakerFactory(getWebSocketURL(httpRequest),
                                                     getSubProtocolsCSV(subProtocols), allowExtensions);
        WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(httpRequest);
        return handleHandshake(handshaker, idleTimeout, null);
    }

    @Override
    public HandshakeFuture handshake(String[] subProtocols, boolean allowExtensions, int idleTimeout,
                                     HttpHeaders responseHeaders) {
        WebSocketServerHandshakerFactory wsFactory =
                new WebSocketServerHandshakerFactory(getWebSocketURL(httpRequest),
                                                     getSubProtocolsCSV(subProtocols), allowExtensions);
        WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(httpRequest);
        return handleHandshake(handshaker, idleTimeout, responseHeaders);
    }

    @Override
    public HandshakeFuture handshake(String[] subProtocols, boolean allowExtensions, int idleTimeout,
                                     HttpHeaders responseHeaders, int maxFramePayloadLength) {
        WebSocketServerHandshakerFactory wsFactory =
                new WebSocketServerHandshakerFactory(getWebSocketURL(httpRequest), getSubProtocolsCSV(subProtocols),
                                                     allowExtensions, maxFramePayloadLength);
        WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(httpRequest);
        return handleHandshake(handshaker, idleTimeout, responseHeaders);
    }

    @Override
    public ChannelFuture cancelHandshake(int statusCode, String closeReason) {
        if (!cancelled && !handshakeStarted) {
            try {
                int responseStatusCode = statusCode >= 400 && statusCode < 500 ? statusCode : 400;
                ChannelFuture responseFuture;
                if (closeReason != null) {
                    ByteBuf content = Unpooled.wrappedBuffer(closeReason.getBytes(StandardCharsets.UTF_8));
                    responseFuture = ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                                                                                   HttpResponseStatus
                                                                                           .valueOf(responseStatusCode),
                                                                                   content));
                } else {
                    responseFuture = ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                                                                                   HttpResponseStatus.valueOf(
                                                                                           responseStatusCode)));
                }
                return responseFuture;
            } finally {
                cancelled = true;
            }
        } else {
            if (cancelled) {
                throw new IllegalStateException("Cannot cancel the handshake: handshake already cancelled");
            } else {
                throw new IllegalStateException("Cannot cancel the handshake: handshake already started");
            }
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

    private HandshakeFuture handleHandshake(WebSocketServerHandshaker handshaker, int idleTimeout,
                                            HttpHeaders headers) {
        HandshakeFutureImpl handshakeFuture = new HandshakeFutureImpl();

        if (cancelled) {
            Throwable e = new IllegalAccessException("Handshake is already cancelled!");
            handshakeFuture.notifyError(e);
            return handshakeFuture;
        }

        try {
            ChannelFuture channelFuture = handshaker.handshake(ctx.channel(), httpRequest, headers,
                                                               ctx.channel().newPromise());
            channelFuture.addListener(future -> {
                String selectedSubProtocol = handshaker.selectedSubprotocol();
                webSocketSourceHandler.setNegotiatedSubProtocol(selectedSubProtocol);
                setSubProtocol(selectedSubProtocol);
                DefaultWebSocketConnection webSocketConnection = (DefaultWebSocketConnection) getWebSocketConnection();
                webSocketConnection.getDefaultWebSocketSession().setNegotiatedSubProtocol(selectedSubProtocol);

                //Replace HTTP handlers  with  new Handlers for WebSocket in the pipeline
                ChannelPipeline pipeline = ctx.pipeline();

                if (idleTimeout > 0) {
                    pipeline.replace(Constants.IDLE_STATE_HANDLER, Constants.IDLE_STATE_HANDLER,
                                     new IdleStateHandler(idleTimeout, idleTimeout, idleTimeout,
                                                          TimeUnit.MILLISECONDS));
                } else {
                    pipeline.remove(Constants.IDLE_STATE_HANDLER);
                }
                pipeline.addLast(Constants.WEBSOCKET_SOURCE_HANDLER, webSocketSourceHandler);
                pipeline.remove(Constants.HTTP_SOURCE_HANDLER);
                setProperty(Constants.SRC_HANDLER, webSocketSourceHandler);
                pipeline.fireChannelActive();
                handshakeFuture.notifySuccess(webSocketConnection);
            });
            handshakeStarted = true;
            return handshakeFuture;
        } catch (Exception e) {
            /*
            Code 1002 : indicates that an endpoint is terminating the connection
            due to a protocol error.
             */
            handshaker.close(ctx.channel(),
                             new CloseWebSocketFrame(1002,
                                                     "Terminating the connection due to a protocol error."));
            handshakeFuture.notifyError(e);
            return handshakeFuture;
        }
    }

    /* Get the URL of the given connection */
    private String getWebSocketURL(HttpRequest req) {
        String protocol = Constants.WEBSOCKET_PROTOCOL;
        if (isConnectionSecured) {
            protocol = Constants.WEBSOCKET_PROTOCOL_SECURED;
        }
        String url = protocol + "://" + req.headers().get("Host") + req.uri();
        return url;
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
