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

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.handler.timeout.IdleStateHandler;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.contract.websocket.HandshakeFuture;
import org.wso2.transport.http.netty.contract.websocket.WebSocketInitMessage;
import org.wso2.transport.http.netty.contractimpl.websocket.HandshakeFutureImpl;
import org.wso2.transport.http.netty.contractimpl.websocket.WebSocketMessageImpl;
import org.wso2.transport.http.netty.internal.websocket.DefaultWebSocketSession;
import org.wso2.transport.http.netty.internal.websocket.WebSocketUtil;
import org.wso2.transport.http.netty.listener.WebSocketSourceHandler;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of {@link WebSocketInitMessage}.
 */
public class DefaultWebSocketInitMessage extends WebSocketMessageImpl implements WebSocketInitMessage {

    private final ChannelHandlerContext ctx;
    private final HttpRequest httpRequest;
    private final WebSocketSourceHandler webSocketSourceHandler;
    private boolean isCancelled = false;
    private boolean handshakeStarted = false;
    private HttpRequest request;

    public DefaultWebSocketInitMessage(ChannelHandlerContext ctx, HttpRequest httpRequest,
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
    public void cancelHandShake(int closeCode, String closeReason) {
        try {
            WebSocketServerHandshakerFactory wsFactory =
                    new WebSocketServerHandshakerFactory(getWebSocketURL(httpRequest), getSubProtocol(), true);
            WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(httpRequest);
            ChannelFuture channelFuture =
                    handshaker.close(ctx.channel(), new CloseWebSocketFrame(closeCode, closeReason));
            channelFuture.channel().close();
        } finally {
            isCancelled = true;
        }
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public boolean isHandshakeStarted() {
        return handshakeStarted;
    }

    private HandshakeFuture handleHandshake(WebSocketServerHandshaker handshaker, int idleTimeout,
                                            HttpHeaders headers) {
        HandshakeFutureImpl handshakeFuture = new HandshakeFutureImpl();

        if (isCancelled) {
            Throwable e = new IllegalAccessException("Handshake is already cancelled!");
            handshakeFuture.notifyError(e);
            return handshakeFuture;
        }

        try {
            ChannelFuture future = handshaker.handshake(ctx.channel(), httpRequest, headers,
                                                        ctx.channel().newPromise());
            handshakeFuture.setChannelFuture(future);
            future.addListener(future1 -> {
                String selectedSubProtocol = handshaker.selectedSubprotocol();
                webSocketSourceHandler.setNegotiatedSubProtocol(selectedSubProtocol);
                setSubProtocol(selectedSubProtocol);
                DefaultWebSocketSession session = (DefaultWebSocketSession) getChannelSession();
                session.setIsOpen(true);
                session.setNegotiatedSubProtocol(selectedSubProtocol);

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
                handshakeFuture.notifySuccess(webSocketSourceHandler.getChannelSession());
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

    public HttpRequest getHttpRequest() {
        return request;
    }

    public void setHttpRequest(HttpRequest request) {
        this.request = request;
    }
}
