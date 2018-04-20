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

package org.wso2.transport.http.netty.listener;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpRequestDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contractimpl.websocket.message.DefaultWebSocketInitMessage;
import org.wso2.transport.http.netty.internal.websocket.DefaultWebSocketSession;
import org.wso2.transport.http.netty.internal.websocket.WebSocketUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket handshake handler for carbon transports.
 */
public class WebSocketServerHandshakeHandler extends ChannelInboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(WebSocketServerHandshakeHandler.class);

    private final ServerConnectorFuture serverConnectorFuture;
    private final String interfaceId;

    public WebSocketServerHandshakeHandler(ServerConnectorFuture serverConnectorFuture, String interfaceId) {
        this.serverConnectorFuture = serverConnectorFuture;
        this.interfaceId = interfaceId;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        if (msg instanceof HttpRequest) {
            HttpRequest httpRequest = (HttpRequest) msg;
            HttpHeaders headers = httpRequest.headers();
            String httpMethod = httpRequest.method().name();
            if (httpMethod.equalsIgnoreCase("GET") && isConnectionUpgrade(headers) &&
                    Constants.WEBSOCKET_UPGRADE.equalsIgnoreCase(headers.get(HttpHeaderNames.UPGRADE))) {
                if (log.isDebugEnabled()) {
                    log.debug("Upgrading the connection from Http to WebSocket for " +
                                      "channel : " + ctx.channel());
                }
                ChannelPipeline pipeline = ctx.pipeline();
                ChannelHandlerContext decoderCtx = pipeline.context(HttpRequestDecoder.class);
                String aggregatorName = "aggregate";
                pipeline.addAfter(decoderCtx.name(), aggregatorName, new HttpObjectAggregator(8192));
                pipeline.addAfter(aggregatorName, "handshake", new SimpleChannelInboundHandler<FullHttpRequest>() {
                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
                        // Remove ourselves and do the actual handshake
                        ctx.pipeline().remove(this);
                        handleWebSocketHandshake(msg, ctx);
                    }

                    @Override
                    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                        // Remove ourselves and fail the handshake
                        ctx.pipeline().remove(this);
                        ctx.fireExceptionCaught(cause);
                    }

                    @Override
                    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                        ctx.fireChannelInactive();
                    }
                });
                decoderCtx.fireChannelRead(msg);
                return;
            }
        }
        ctx.fireChannelRead(msg);
    }

    /**
     * Some clients can send multiple parameters for "Connection" header. This checks whether the "Connection" header
     * contains "Upgrade" value.
     *
     * @param headers {@link HttpHeaders} of the request.
     * @return true if the "Connection" header contains value "Upgrade".
     */
    protected boolean isConnectionUpgrade(HttpHeaders headers) {
        if (!headers.contains(HttpHeaderNames.CONNECTION)) {
            return false;
        }

        String connectionHeaderValues = headers.get(HttpHeaderNames.CONNECTION);
        for (String connectionValue: connectionHeaderValues.split(",")) {
            if (HttpHeaderNames.UPGRADE.toString().equalsIgnoreCase(connectionValue.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Handle the WebSocket handshake.
     *
     * @param httpRequest {@link HttpRequest} of the request.
     */
    private void handleWebSocketHandshake(HttpRequest httpRequest, ChannelHandlerContext ctx) throws Exception {
        boolean isSecured = false;

        if (ctx.channel().pipeline().get(Constants.SSL_HANDLER) != null) {
            isSecured = true;
        }
        String uri = httpRequest.uri();
        DefaultWebSocketSession channelSession = WebSocketUtil.getSession(ctx, isSecured, uri);

        Map<String, String> headers = new HashMap<>();
        httpRequest.headers().forEach(
                header -> headers.put(header.getKey(), header.getValue())
        );
        WebSocketSourceHandler webSocketSourceHandler =
                new WebSocketSourceHandler(serverConnectorFuture, isSecured, channelSession, httpRequest,
                                           headers, ctx, interfaceId);
        DefaultWebSocketInitMessage initMessage = new DefaultWebSocketInitMessage(ctx, httpRequest,
                                                                                  webSocketSourceHandler, headers);

        // Setting common properties for init message
        initMessage.setChannelSession(channelSession);
        initMessage.setIsServerMessage(true);
        initMessage.setTarget(httpRequest.uri());
        initMessage.setListenerInterface(interfaceId);
        initMessage.setProperty(Constants.SRC_HANDLER, webSocketSourceHandler);
        initMessage.setIsConnectionSecured(isSecured);
        initMessage.setHttpRequest(httpRequest);

        serverConnectorFuture.notifyWSListener(initMessage);
    }
}
